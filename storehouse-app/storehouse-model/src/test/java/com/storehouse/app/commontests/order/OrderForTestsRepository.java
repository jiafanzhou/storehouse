package com.storehouse.app.commontests.order;

import static com.storehouse.app.commontests.TestsRepositoryUtils.*;
import static com.storehouse.app.commontests.user.UserForTestsRepository.*;

import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.user.model.Customer;

import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;

import org.junit.Ignore;

@Ignore
public class OrderForTestsRepository {
    private OrderForTestsRepository() {
    }

    public static Order orderDelivered() {
        final Order order = new Order();
        order.setCustomer((Customer) marySimpson());
        order.addItem(2);
        order.setInitialStatus();
        order.calculateTotalPrice();
        order.addHistoryEntry(OrderStatus.DELIVERED);
        return order;
    }

    public static Order orderReservedJohnDoe() {
        final Order order = new Order();
        order.setCustomer((Customer) johnDoe());
        order.addItem(2);
        order.setInitialStatus();
        order.calculateTotalPrice();
        return order;
    }

    public static Order orderReservedEndaKenny() {
        final Order order = new Order();
        order.setCustomer((Customer) endaKenny());
        order.addItem(10);
        order.setInitialStatus();
        order.calculateTotalPrice();
        return order;
    }

    public static Order orderReservedDonaldTrump() {
        final Order order = new Order();
        order.setCustomer((Customer) donaldTrump());
        order.addItem(5);
        order.setInitialStatus();
        order.calculateTotalPrice();
        return order;
    }

    public static Order orderWithId(final Order order, final Long id) {
        order.setId(id);
        return order;
    }

    public static Order orderCreatedAt(final Order order, final String dateTime) {
        order.setCreatedAt(DateUtils.getAsDateTime(dateTime));
        return order;
    }

    public static Order normalizeDependencies(final Order order, final EntityManager em) {
        order.setCustomer(findByPropertyNameAndValue(em, Customer.class, "name",
                order.getCustomer().getName()));
        return order;
    }

    public static List<Order> allOrders() {
        return Arrays.asList(orderDelivered(), orderReservedJohnDoe());
    }
}
