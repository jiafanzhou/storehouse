package com.storehouse.app.order.services;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.List;

import javax.ejb.Local;

@Local
public interface OrderServices {

    Order add(Order order);

    Order findById(Long id);

    void updateStatus(Long id, OrderStatus newStatus);

    List<Order> findAll();

    List<Order> findAll(String orderField);

    PaginatedData<Order> findByFilter(OrderFilter orderFilter);

    Integer checkOrderPositionInQueueByCustomerId(Long customerId);

    Integer checkOrderWaitTimeInQueueByCustomerId(Long customerId);

    List<Order> findAllReservedOrders();
}
