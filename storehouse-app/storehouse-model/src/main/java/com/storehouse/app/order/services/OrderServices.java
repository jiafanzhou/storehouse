package com.storehouse.app.order.services;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.List;

import javax.ejb.Local;

/**
 * Order Service layer API.
 *
 * @author ejiafzh
 *
 */
@Local
public interface OrderServices {

    /**
     * To add an order.
     *
     * @param order
     *            order to be added
     * @return an added order.
     */
    Order add(Order order);

    /**
     * To find an existing order by ID.
     *
     * @param id
     *            the order ID.
     * @return an existing Order, null if it could not find one.
     */
    Order findById(Long id);

    /**
     * Update the order status.
     *
     * @param id
     *            the ID of the order
     * @param newStatus
     *            the new Order Status.
     */
    void updateStatus(Long id, OrderStatus newStatus);

    /**
     * Find all the orders.
     *
     * @return a list of the found orders.
     */
    List<Order> findAll();

    /**
     * Find all the orders and sort based on the orderField.
     *
     * @param orderField
     *            the order field to sort
     * @return a sorted list of orders.
     */
    List<Order> findAll(String orderField);

    /**
     * Find paginated data of order by the order filter.
     * 
     * @param orderFilter
     *            the order filter.
     * @return a paginated data of orders.
     */
    PaginatedData<Order> findByFilter(OrderFilter orderFilter);

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
    Integer checkOrderPositionInQueueByCustomerId(Long customerId);

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
    Integer checkOrderWaitTimeInQueueByCustomerId(Long customerId);

    /**
     * Find all the orders who order status is RESERVED.
     * 
     * @return a list of reserved orders.
     */
    List<Order> findAllReservedOrders();
}
