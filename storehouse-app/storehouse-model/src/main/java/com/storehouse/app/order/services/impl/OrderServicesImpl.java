package com.storehouse.app.order.services.impl;

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
import javax.validation.Validator;

@Stateless
public class OrderServicesImpl implements OrderServices {

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

    private void setCustomerOnOrder(final Order order) {
        // we want to use the customer that is logged in
        final User user = userServices.findByEmail(sessionContext.getCallerPrincipal().getName());
        order.setCustomer((Customer) user);
    }

    private void validateOrder(final Order order) {
        ValidationUtils.validateEntityFields(validator, order);
    }

    @Override
    public Order add(final Order order) {
        // jiafanz: update this
        setCustomerOnOrder(order);


        // when adding a new order, we need to set the initial status, and calculate the total
        order.setInitialStatus();
        order.calculateTotalPrice();

        validateOrder(order);
        return orderRepository.add(order);
    }

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

    // jiafanz: document this
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

    @Override
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    @Override
    public List<Order> findAll(final String orderField) {
        return orderRepository.findAll(orderField);
    }

    @Override
    public PaginatedData<Order> findByFilter(final OrderFilter orderFilter) {
        return orderRepository.findByFilter(orderFilter);
    }

    @Override
    public Integer checkOrderPositionInQueueByCustomerId(final Long customerId) {
        return orderRepository.checkOrderPositionInQueueByCustomerId(customerId);
    }

    @Override
    public Integer checkOrderWaitTimeInQueueByCustomerId(final Long customerId) {
        return orderRepository.checkOrderWaitTimeInQueueByCustomerId(customerId);
    }

    @Override
    public List<Order> findAllReservedOrders() {
        if (sessionContext.isCallerInRole(Roles.CUSTOMER.name())) {
            throw new UserNotAuthorizedException();
        }
        return orderRepository.findAllReservedOrders();
    }

}
