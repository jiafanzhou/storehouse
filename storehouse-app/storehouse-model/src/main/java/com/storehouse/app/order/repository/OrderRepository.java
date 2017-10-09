package com.storehouse.app.order.repository;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.repository.GenericRepository;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class OrderRepository extends GenericRepository<Order> {
    private Logger logger = LoggerFactory.getLogger(getClass());

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
        logger.info("checkOrderPostionInQueueByCustomerId invokeed, customerId {}", customerId);
        final OrderFilter dataFilter = initialiseOrderFilter(customerId);

        if (dataFilter == null) {
            return -1;
        }
        logger.info("dataFilter {}", dataFilter);
        return findByFilter(dataFilter).getNumberOfRows();
    }

    // jiafanz: document this
    public Integer checkOrderWaitTimeInQueueByCustomerId(final Long customerId) {
        final OrderFilter dataFilter = initialiseOrderFilter(customerId);
        if (dataFilter == null) {
            return -1;
        }

        int totalQuantity = 0;
        for (final Order order : findByFilter(dataFilter).getRows()) {
            totalQuantity += order.calculateTotalQuantity();
        }
        return totalQuantity;
    }

    private OrderFilter initialiseOrderFilter(final Long customerId) {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setCustomerId(customerId);
        orderFilter.setStatus(OrderStatus.RESERVED);
        final PaginatedData<Order> data = findByFilter(orderFilter);

        // if there is no data, it means there is no order in db for such customer
        // whose current status is reserved.
        if (data.getNumberOfRows() == 0) {
            return null;
        }
        final Date orderDate = data.getRow(0).getCreatedAt();

        final OrderFilter dateFilter = new OrderFilter();
        dateFilter.setStatus(OrderStatus.RESERVED);
        dateFilter.setEndDate(orderDate);
        return dateFilter;
    }

    public Boolean checkIfOrderExistsAlready(final Long clientId) {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setStatus(OrderStatus.RESERVED);
        orderFilter.setCustomerId(clientId);
        final PaginatedData<Order> data = findByFilter(orderFilter);
        return !data.getRows().isEmpty();
    }

    /**
     * Find all orders that are reserved in the queue.
     *
     * @return all orders that are reserved in the queue.
     */
    public List<Order> findAllReservedOrders() {
        final OrderFilter orderFilter = new OrderFilter();
        orderFilter.setStatus(OrderStatus.RESERVED);
        final PaginatedData<Order> data = findByFilter(orderFilter);
        return data.getRows();
    }
}
