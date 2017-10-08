package com.storehouse.app.order.repository;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;
import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.model.filter.PaginationData;
import com.storehouse.app.common.model.filter.PaginationData.OrderMode;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.commontests.utils.TestBaseRepository;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderRepositoryUTest extends TestBaseRepository {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OrderRepository orderRepository;

    @Override
    @Before
    public void initTestCase() {
        super.initTestCase();
        orderRepository = new OrderRepository();
        orderRepository.em = em;

        // we will load all the users into the db
        loadUsersIntoDb();
    }

    private void loadUsersIntoDb() {
        dbTxExecutor.executeCommand(() -> {
            allUsers().forEach(em::persist);
            return null;
        });
    }

    @Override
    @After
    public void closeEntityManager() {
        super.closeEntityManager();
    }

    private void assertOrder(final Order order, final Order expectedOrder) {
        assertThat(order.getCreatedAt(), is(notNullValue()));
        assertThat(order.getCustomer(), is(equalTo(expectedOrder.getCustomer())));
        assertThat(order.getItems().size(), is(equalTo(expectedOrder.getItems().size())));
        order.getItems().forEach(orderItem -> {
            assertThat(orderItem.getQuantity(), is(equalTo(
                    expectedOrder.getItems().iterator().next().getQuantity())));
        });
        assertThat(order.getTotalPrice(), is(equalTo(expectedOrder.getTotalPrice())));
        assertThat(order.getCurrentStatus(), is(equalTo(expectedOrder.getCurrentStatus())));
        assertThat(order.getHistoryEntries().size(), is(equalTo(expectedOrder.getHistoryEntries().size())));
        assertThat(order.getHistoryEntries(), is(equalTo(expectedOrder.getHistoryEntries())));
    }

    @Test
    public void addOrderAndFindIt() {
        final Order orderDelivered = normalizeDependencies(orderDelivered(), em);
        final Long orderAddedId = dbTxExecutor.executeCommand(() -> {
            final Order order = orderRepository.add(orderDelivered);
            return order.getId();
        });
        assertThat(orderAddedId, is(notNullValue()));

        final Order order = orderRepository.findById(orderAddedId);
        logger.info("jiafanz: {}", order);
        assertOrder(order, orderDelivered);
    }

    @Test
    public void findOrderByIdNotFound() {
        final Order order = orderRepository.findById(999L);
        assertThat(order, is(nullValue()));
    }

    @Test
    public void updateOrderWhenIsDelivered() {
        final Order orderReserved = normalizeDependencies(orderReservedJohnDoe(), em);
        final Long orderAddedId = dbTxExecutor.executeCommand(() -> {
            final Order order = orderRepository.add(orderReserved);
            return order.getId();
        });
        assertThat(orderAddedId, is(notNullValue()));
        final Order orderAdded = orderRepository.findById(orderAddedId);
        orderAdded.addHistoryEntry(OrderStatus.DELIVERED);

        dbTxExecutor.executeCommand(() -> {
            final Order order = orderRepository.update(orderAdded);
            return order.getId();
        });

        final Order orderAfterUpdate = orderRepository.findById(orderAddedId);
        logger.info("jiafanz: {}", orderAfterUpdate);
        assertThat(orderAfterUpdate.getHistoryEntries().size(), is(equalTo(2)));
        assertThat(orderAfterUpdate.getCurrentStatus(), is(equalTo(OrderStatus.DELIVERED)));
    }

    @Test
    public void existsById() {
        final Order orderReserved = normalizeDependencies(orderReservedJohnDoe(), em);
        final Long orderAddedId = dbTxExecutor.executeCommand(() -> {
            final Order order = orderRepository.add(orderReserved);
            return order.getId();
        });
        assertThat(orderAddedId, is(notNullValue()));

        assertThat(orderRepository.existsById(orderAddedId), is(equalTo(true)));
        assertThat(orderRepository.existsById(999L), is(equalTo(false)));
    }

    private void loadForFindByFilter() {
        final Order order1 = normalizeDependencies(orderReservedJohnDoe(), em);
        orderCreatedAt(order1, "2017-10-16T10:00:00Z");

        final Order order2 = normalizeDependencies(orderDelivered(), em);
        orderCreatedAt(order2, "2017-10-17T10:00:00Z");

        final Order order3 = normalizeDependencies(orderDelivered(), em);
        orderCreatedAt(order3, "2017-10-18T10:00:00Z");

        dbTxExecutor.executeCommand(() -> {
            orderRepository.add(order1);
            orderRepository.add(order2);
            orderRepository.add(order3);
            return null;
        });
    }

    @Test
    public void findByFilterByStatusReserved() {
        loadForFindByFilter();

        final OrderFilter filter = new OrderFilter();
        filter.setStatus(OrderStatus.RESERVED);

        final PaginatedData<Order> orders = orderRepository.findByFilter(filter);
        assertThat(orders.getNumberOfRows(), is(equalTo(1)));
        assertThat(DateUtils.formatDateTime(orders.getRow(0).getCreatedAt()), is(equalTo("2017-10-16T10:00:00Z")));
    }

    @Test
    public void findByFilterByStatusDelivered() {
        loadForFindByFilter();

        final OrderFilter filter = new OrderFilter();
        filter.setStatus(OrderStatus.DELIVERED);

        final PaginatedData<Order> orders = orderRepository.findByFilter(filter);
        assertThat(orders.getNumberOfRows(), is(equalTo(2)));
        assertThat(DateUtils.formatDateTime(orders.getRow(0).getCreatedAt()), is(equalTo("2017-10-18T10:00:00Z")));
        assertThat(DateUtils.formatDateTime(orders.getRow(1).getCreatedAt()), is(equalTo("2017-10-17T10:00:00Z")));
    }

    @Test
    public void findByFilterNoFilter() {
        loadForFindByFilter();
        final PaginatedData<Order> orders = orderRepository.findByFilter(new OrderFilter());
        assertThat(orders.getNumberOfRows(), is(equalTo(3)));
        assertThat(DateUtils.formatDateTime(orders.getRow(0).getCreatedAt()), is(equalTo("2017-10-18T10:00:00Z")));
        assertThat(DateUtils.formatDateTime(orders.getRow(1).getCreatedAt()), is(equalTo("2017-10-17T10:00:00Z")));
        assertThat(DateUtils.formatDateTime(orders.getRow(2).getCreatedAt()), is(equalTo("2017-10-16T10:00:00Z")));
    }

    @Test
    public void findByFilterByCustomerOrderingByCreationAsAscending() {
        loadForFindByFilter();

        final OrderFilter filter = new OrderFilter();
        filter.setCustomerId(normalizeDependencies(orderDelivered(), em).getCustomer().getId());
        filter.setPaginationData(new PaginationData(0, 10, "createdAt", OrderMode.ASCENDING));

        final PaginatedData<Order> orders = orderRepository.findByFilter(filter);
        assertThat(orders.getNumberOfRows(), is(equalTo(2)));
        assertThat(DateUtils.formatDateTime(orders.getRow(0).getCreatedAt()), is(equalTo("2017-10-17T10:00:00Z")));
        assertThat(DateUtils.formatDateTime(orders.getRow(1).getCreatedAt()), is(equalTo("2017-10-18T10:00:00Z")));
    }

    @Test
    public void findByFilterByDate() {
        loadForFindByFilter();

        final OrderFilter filter = new OrderFilter();
        filter.setStartDate(DateUtils.getAsDateTime("2017-10-16T10:00:00Z"));
        filter.setEndDate(DateUtils.getAsDateTime("2017-10-17T10:00:00Z"));

        final PaginatedData<Order> orders = orderRepository.findByFilter(filter);
        assertThat(orders.getNumberOfRows(), is(equalTo(2)));
        assertThat(DateUtils.formatDateTime(orders.getRow(0).getCreatedAt()), is(equalTo("2017-10-17T10:00:00Z")));
        assertThat(DateUtils.formatDateTime(orders.getRow(1).getCreatedAt()), is(equalTo("2017-10-16T10:00:00Z")));
    }

    private void loadForAllReservedOrders() {
        final Order order1 = normalizeDependencies(orderReservedJohnDoe(), em);
        orderCreatedAt(order1, "2017-10-16T10:00:00Z");

        final Order order2 = normalizeDependencies(orderReservedEndaKenny(), em);
        orderCreatedAt(order2, "2017-10-17T10:00:00Z");

        final Order order3 = normalizeDependencies(orderReservedDonaldTrump(), em);
        orderCreatedAt(order3, "2017-10-18T10:00:00Z");

        dbTxExecutor.executeCommand(() -> {
            orderRepository.add(order1);
            orderRepository.add(order2);
            orderRepository.add(order3);
            return null;
        });
    }

    @Test
    public void testOrderPostionInQueue() {
        loadForAllReservedOrders();
        final Integer position = orderRepository
                .checkOrderPositionInQueueByCustomerId(orderReservedDonaldTrump().getId());
        logger.info("jiafanz: {}", position);
        assertThat(position, is(equalTo(3)));
    }

    @Test
    public void testOrderEstimatedWaitTimeInQueue() {
        loadForAllReservedOrders();
        final Integer waitTime = orderRepository
                .checkOrderWaitTimeInQueueByCustomerId(orderReservedDonaldTrump().getId());
        logger.info("jiafanz: {}", waitTime);
        assertThat(waitTime, is(equalTo(17)));
    }
}