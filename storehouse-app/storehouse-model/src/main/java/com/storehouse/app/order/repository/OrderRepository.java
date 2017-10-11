package com.storehouse.app.order.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.repository.GenericRepository;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.user.model.Customer;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Order repository layer to interact with the database.
 *
 * @author ejiafzh
 *
 */
@Stateless
public class OrderRepository extends GenericRepository<Order> {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @PersistenceContext
    EntityManager em;

    /**
     * {@inheritDoc}
     */
    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Class<Order> getPersistentClass() {
        return Order.class;
    }

    /**
     * {@inheritDoc}
     * We might need to load the size in case we need to change to LAZY initialization.
     */
    @Override
    public Order findById(final Long id) {
        final Order order = super.findById(id);
        if (order != null) {
            order.getItems().size();
            order.getHistoryEntries().size();
        }
        return order;
    }

    /**
     * Find the paginated data by filter.
     *
     * @param filter
     *            the order filter to be used
     * @param defaultSort
     *            default sort the query
     * @return a paginated data of order
     */
    public PaginatedData<Order> findByFilterWithSort(final OrderFilter filter, final String defaultSort) {
        final StringBuilder clause = new StringBuilder("WHERE a.id is not null");
        final Map<String, Object> queryParameters = new HashMap<>();
        if (filter.getStartDate() != null) {
            clause.append(" And a.createdAt >= :startDate");
            queryParameters.put("startDate", filter.getStartDate());
        }
        if (filter.getEndDate() != null) {
            clause.append(" And a.createdAt <= :endDate");
            queryParameters.put("endDate", filter.getEndDate());
        }
        if (filter.getCustomerId() != null) {
            clause.append(" And a.customer.id = :customerId");
            queryParameters.put("customerId", filter.getCustomerId());
        }
        if (filter.getStatus() != null) {
            clause.append(" And a.currentStatus = :status");
            queryParameters.put("status", filter.getStatus());
        }

        return findByParameters(clause.toString(), filter, queryParameters, defaultSort);
    }

    /**
     * Find the paginated data by filter with the default sort of order createdAt
     * timestamp descending.
     *
     * @param filter
     *            the order filter to be used
     * @return a paginated data of order
     */
    public PaginatedData<Order> findByFilter(final OrderFilter filter) {
        return findByFilterWithSort(filter, "createdAt DESC");
    }

    /**
     * This is for find the premium customer by filter.
     *
     * @param filter
     *            filter to be used.
     * @param checkTime
     *            whether or not we want to filter by the createdAt.
     * @return a paginated data of order for premium customer.
     */
    private PaginatedData<Order> findByFilterWithPremiumCustomer(final OrderFilter filter, final boolean checkTime) {
        return findByFilterWithGeneric(filter, checkTime, true);
    }

    /**
     * This is for find the non-premium customer by filter.
     *
     * @param filter
     *            filter to be used.
     * @return a paginated data of order for premium customer.
     */
    private PaginatedData<Order> findByFilterWithNonPremiumCustomer(final OrderFilter filter) {
        return findByFilterWithGeneric(filter, true, false);
    }

    // find by filter with Generic customer
    private PaginatedData<Order> findByFilterWithGeneric(final OrderFilter filter,
            final boolean checkTime, final boolean isPremiumCustomer) {
        final StringBuilder clause = new StringBuilder("WHERE a.id is not null");
        final Map<String, Object> queryParameters = new HashMap<>();
        if (checkTime) {
            if (filter.getEndDate() != null) {
                clause.append(" And a.createdAt <= :endDate");
                queryParameters.put("endDate", filter.getEndDate());
            }
        }
        if (isPremiumCustomer) {
            clause.append(" And a.customer.id < " + Customer.PREMIUM_ID_MAX); // premium users
        } else {
            clause.append(" And a.customer.id >= " + Customer.PREMIUM_ID_MAX); // non-premium users
        }
        if (filter.getStatus() != null) {
            clause.append(" And a.currentStatus = :status");
            queryParameters.put("status", filter.getStatus());
        }

        return findByParameters(clause.toString(), filter, queryParameters, "createdAt DESC");
    }

    /**
     * Check the order from where it is in the queue for the position using the provided
     * customer ID. It is important to notice that the premium customers are always before
     * the non-premium customers.
     *
     * @param customerId
     *            the provided customer ID.
     * @return the position index from where the customer is in the queue.
     *         If there is no such order, it returns -1 for the given customerId.
     */
    public Integer checkOrderPositionInQueueByCustomerId(final Long customerId) {
        logger.info("checkOrderPostionInQueueByCustomerId invokeed, customerId {}", customerId);

        final OrderFilter dataFilter = initialiseOrderFilter(customerId);

        if (dataFilter == null) {
            return -1;
        }
        logger.debug("checkOrderPositionInQueueByCustomerId {}", dataFilter);
        int size = 0;
        if (customerId < Customer.PREMIUM_ID_MAX) {
            size = findByFilterWithPremiumCustomer(dataFilter, true).getRows().size();
        } else {
            size = findByFilterWithPremiumCustomer(dataFilter, false).getRows().size()
                    + findByFilterWithNonPremiumCustomer(dataFilter).getRows().size();
        }
        return size;
    }

    /**
     * Check the estimated time for the oder in the queue using the provided
     * customer ID. It is important to notice that the premium customers are always before
     * the non-premium customers.
     *
     *
     * Estimated time is calculated by the quantity of the order.
     * For example, if there are X customers before the given customerId and there each customer
     * has N items to order, the estimated time is calculated as X + N + customer's own item quantity.
     *
     * @param customerId
     *            the provided customer ID.
     * @return the estimated time for the order in the queue.
     *         If there is no such order, it returns -1 for the given customerId.
     */
    public Integer checkOrderWaitTimeInQueueByCustomerId(final Long customerId) {
        final OrderFilter dataFilter = initialiseOrderFilter(customerId);
        if (dataFilter == null) {
            return -1;
        }

        // if customerId < Customer.PREMIUM_ID_MAX, premium customer, we deal with them first
        int totalQuantity = 0;
        if (customerId < Customer.PREMIUM_ID_MAX) {
            for (final Order order : findByFilterWithPremiumCustomer(dataFilter, true).getRows()) {
                totalQuantity += order.calculateTotalQuantity();
            }
        } else {
            for (final Order order : findByFilterWithPremiumCustomer(dataFilter, false).getRows()) {
                totalQuantity += order.calculateTotalQuantity();
            }
            for (final Order order : findByFilterWithNonPremiumCustomer(dataFilter).getRows()) {
                totalQuantity += order.calculateTotalQuantity();
            }
        }
        return totalQuantity;
    }

    // initialise the order filter to be used by order position and estimated time.
    private OrderFilter initialiseOrderFilter(final Long customerId) {
        final OrderFilter orderFilter = createReservedOrderFilter();
        orderFilter.setCustomerId(customerId);
        final PaginatedData<Order> data = findByFilter(orderFilter);

        // if there is no data, it means there is no order in db for such customer
        // whose current status is reserved, so we just return null filter
        if (data.getNumberOfRows() == 0) {
            return null;
        } else {
            final Date orderDate = data.getRow(0).getCreatedAt(); // there should only be 1 row
            final OrderFilter dateFilter = createReservedOrderFilter();
            dateFilter.setEndDate(orderDate);
            return dateFilter;
        }
    }

    /**
     * Check whether or not the order already exists in the queue.
     *
     * We consider an order already exists if and only if its order status is RESERVED.
     *
     * @param clientId
     *            the clientID of the customer
     * @return whether or not the order already exists in the queue.
     */
    public Boolean checkIfOrderExistsAlready(final Long clientId) {
        final OrderFilter orderFilter = createReservedOrderFilter();
        orderFilter.setCustomerId(clientId);
        return !findByFilter(orderFilter).getRows().isEmpty();
    }

    /**
     * Find all orders that are reserved in the queue.
     *
     * @return all orders that are reserved in the queue.
     */
    public List<Order> findAllReservedOrders() {
        final OrderFilter orderFilter = createReservedOrderFilter();
        return findByFilter(orderFilter).getRows();
    }

    // And order is considered in the queue to be dealt with if its status is RESERVED.
    private OrderFilter createReservedOrderFilter() {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setStatus(OrderStatus.RESERVED);
        return orderFilter;
    }
}
