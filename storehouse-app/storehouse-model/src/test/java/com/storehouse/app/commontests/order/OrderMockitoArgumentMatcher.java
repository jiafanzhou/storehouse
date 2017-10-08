package com.storehouse.app.commontests.order;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;

import com.storehouse.app.order.model.Order;

import org.mockito.ArgumentMatcher;

public class OrderMockitoArgumentMatcher extends ArgumentMatcher<Order> {

    private final Order expectedOrder;

    public static Order orderEqual(final Order expectedOrder) {
        return argThat(new OrderMockitoArgumentMatcher(expectedOrder));
    }

    public OrderMockitoArgumentMatcher(final Order expectedOrder) {
        this.expectedOrder = expectedOrder;
    }

    @Override
    public boolean matches(final Object argument) {
        final Order actualOrder = (Order) argument;
        assertThat(actualOrder.getId(), is(equalTo(expectedOrder.getId())));
        // assertThat(actualOrder.getCustomer(), is(equalTo(expectedOrder.getCustomer())));
        assertThat(actualOrder.getItems(), is(equalTo(expectedOrder.getItems())));
        assertThat(actualOrder.getTotalPrice(), is(equalTo(expectedOrder.getTotalPrice())));
        assertThat(actualOrder.getHistoryEntries(), is(equalTo(expectedOrder.getHistoryEntries())));
        assertThat(actualOrder.getCurrentStatus(), is(equalTo(expectedOrder.getCurrentStatus())));
        return true;
    }
}
