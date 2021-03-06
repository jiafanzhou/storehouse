package com.storehouse.app.order.services.impl;

import com.storehouse.app.common.exception.InvalidClientIdInOrder;
import com.storehouse.app.common.exception.OrderAlreadyExistingException;
import com.storehouse.app.common.exception.OrderNotFoundException;
import com.storehouse.app.common.exception.OrderStatusCannotBeChangedException;
import com.storehouse.app.common.exception.UserNotAuthorizedException;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.utils.ValidationUtils;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.order.repository.OrderRepository;
import com.storehouse.app.order.services.OrderServices;
import com.storehouse.app.user.model.Customer;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.Roles;
import com.storehouse.app.user.services.UserServices;

import java.util.List;

import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConnectionFactory;
import javax.jms.JMSContext;
import javax.jms.JMSProducer;
import javax.jms.Queue;
import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order Service layer implementation.
 *
 * @author ejiafzh
 *
 */
@Stateless
public class OrderServicesImpl implements OrderServices {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    OrderRepository orderRepository;

    @Inject
    UserServices userServices;

    @Inject
    Validator validator;

    // uses @Resource, to inject sessionContext, we need to be @Resource, not @Inject
    // it is just the way how sessionContext is injected.
    @Resource
    SessionContext sessionContext;

    // configures our JMS queue
    @Resource(mappedName = "java:/jms/queue/Orders")
    private Queue ordersQueue;

    @Inject
    @JMSConnectionFactory("java:jboss/DefaultJMSConnectionFactory")
    private JMSContext jmsContext;

    /**
     * A customer must login first with his/her email as principle to place a
     * new order. We set the customer from the logged in user if and only if
     * the clientId in the order matches the logged in customer ID.
     *
     * @param order
     *            to be placed.
     */
    private void setCustomerOnOrder(final Order order) {
        final User user = userServices.findByEmail(sessionContext.getCallerPrincipal().getName());
        validateOrderClientId(order, user);
        validateOneOrderPerClient(order.getCustomer().getId());
        order.setCustomer((Customer) user);
    }

    // validate that a client can only place 1 order
    private void validateOneOrderPerClient(final Long clientId) {
        // if the clientId has been in the db which current_status is RESERVED
        if (orderRepository.checkIfOrderExistsAlready(clientId)) {
            final String strTemplate = "Order for clientId %s already exists";
            throw new OrderAlreadyExistingException(String.format(strTemplate, clientId));
        }
    }

    /**
     * We also validate that logged in credential has the right
     * clientId passed in from the order.
     *
     * @param order
     *            to be placed
     * @param user
     *            the customer to be logged in.
     */
    private void validateOrderClientId(final Order order, final User user) {
        final Long orderCustomerId = order.getCustomer().getId();
        if (orderCustomerId == null) {
            final String strTemplate = "ClientId in order must not be null.";
            throw new InvalidClientIdInOrder(strTemplate);
        }

        final Long userId = user.getId();
        if (!userId.equals(orderCustomerId)) {
            final String strTemplate = "ClientId %d in order does not match the logged in customer id %d.";
            throw new InvalidClientIdInOrder(String.format(strTemplate, orderCustomerId, userId));
        }
    }

    private void validateOrder(final Order order) {
        ValidationUtils.validateEntityFields(validator, order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Order add(final Order order) {
        setCustomerOnOrder(order);

        // when adding a new order, we need to set the initial status, and calculate the total
        order.setInitialStatus();
        order.calculateTotalPrice();

        validateOrder(order);

        // part of the XA transaction
        // sends order event to the queue
        final Order addedOrder = orderRepository.add(order);
        sendEvent(addedOrder);

        return addedOrder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Order findById(final Long id) {
        final Order order = orderRepository.findById(id);
        if (order == null) {
            throw new OrderNotFoundException();
        }
        return order;
    }

    private void addNewStatusToHistory(final OrderStatus newStatus, final Order order) {
        try {
            order.addHistoryEntry(newStatus);
        } catch (final IllegalArgumentException ex) {
            throw new OrderStatusCannotBeChangedException(ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateStatus(final Long id, final OrderStatus newStatus) {
        final Order order = findById(id);

        // only employee can make this order delivered
        if (newStatus == OrderStatus.DELIVERED) {
            if (!sessionContext.isCallerInRole(Roles.EMPLOYEE.name())) {
                throw new UserNotAuthorizedException();
            }
        }

        // only the customer who made this order can cancel this order
        if (newStatus == OrderStatus.CANCELLED) {
            if (sessionContext.isCallerInRole(Roles.CUSTOMER.name())) {
                if (!order.getCustomer().getEmail().equals(sessionContext.getCallerPrincipal().getName())) {
                    throw new UserNotAuthorizedException();
                }
            }
            // if it is an employee or admin, it is OK to cancel order
        }

        addNewStatusToHistory(newStatus, order);

        order.setCurrentStatus(newStatus);
        orderRepository.update(order);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Order> findAll(final String orderField) {
        return orderRepository.findAll(orderField);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PaginatedData<Order> findByFilter(final OrderFilter orderFilter) {
        return orderRepository.findByFilter(orderFilter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer checkOrderPositionInQueueByCustomerId(final Long customerId) {
        return orderRepository.checkOrderPositionInQueueByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer checkOrderWaitTimeInQueueByCustomerId(final Long customerId) {
        return orderRepository.checkOrderWaitTimeInQueueByCustomerId(customerId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Order> findAllReservedOrders() {
        if (sessionContext.isCallerInRole(Roles.CUSTOMER.name())) {
            throw new UserNotAuthorizedException();
        }
        return orderRepository.findAllReservedOrders();
    }

    /**
     * Send an event to the queue.
     *
     * @param order
     *            the order to be send
     */
    private void sendEvent(final Order order) {
        logger.info("Sending an event to the orders queue");
        if (jmsContext != null) {
            final JMSProducer producer = jmsContext.createProducer();
            final Customer customer = order.getCustomer();
            setQueuePriority(producer, customer);
            producer.send(ordersQueue, order);
        }
    }

    /**
     * Set the queue priority based on the customerID.
     *
     * @param producer
     *            JMS producer.
     * @param customer
     *            customer with the ID.
     */
    private void setQueuePriority(final JMSProducer producer, final Customer customer) {
        if (customer.isPremiumCustomer()) {
            logger.info("This is a priority queue for customerId {}", customer.getId());
            // this is a priority queue
            producer.setPriority(9); // set this to highest priority
        } else {
            logger.info("This is a non-priority queue for customerId {}", customer.getId());
            producer.setPriority(3); // default is 4, we decrease it by 1
        }
    }
}
