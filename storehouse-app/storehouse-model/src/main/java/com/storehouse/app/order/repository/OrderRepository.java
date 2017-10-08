package com.storehouse.app.order.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.repository.GenericRepository;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class OrderRepository extends GenericRepository<Order> {
    @PersistenceContext
    EntityManager em;

    @Override
    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    protected Class<Order> getPersistentClass() {
        return Order.class;
    }

    @Override
    public Order findById(final Long id) {
        final Order order = super.findById(id);
        if (order != null) {
            order.getItems().size();
            order.getHistoryEntries().size();
        }
        return order;
    }

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

    public PaginatedData<Order> findByFilter(final OrderFilter filter) {
        return findByFilterWithSort(filter, "createdAt DESC");
    }

    // jiafanz: document this
    public Integer checkOrderPositionInQueueByCustomerId(final Long customerId) {
        final OrderFilter dateFilter = initialiseOrderFilter(customerId);
        return findByFilter(dateFilter).getNumberOfRows();
    }

    // jiafanz: document this
    public Integer checkOrderWaitTimeInQueueByCustomerId(final Long customerId) {
        final OrderFilter dateFilter = initialiseOrderFilter(customerId);

        int totalQuantity = 0;
        for (final Order order : findByFilter(dateFilter).getRows()) {
            totalQuantity += order.calculateTotalQuantity();
        }
        return totalQuantity;
    }

    private OrderFilter initialiseOrderFilter(final Long customerId) {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setCustomerId(customerId);
        orderFilter.setStatus(OrderStatus.RESERVED);
        final PaginatedData<Order> data = findByFilter(orderFilter);
        final Date orderDate = data.getRow(0).getCreatedAt();

        final OrderFilter dateFilter = new OrderFilter();
        dateFilter.setEndDate(orderDate);
        return dateFilter;
    }
}
