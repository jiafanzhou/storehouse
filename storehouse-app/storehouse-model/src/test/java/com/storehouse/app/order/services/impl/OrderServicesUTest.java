package com.storehouse.app.order.services.impl;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;
import static com.storehouse.app.commontests.order.OrderMockitoArgumentMatcher.*;
import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.OrderNotFoundException;
import com.storehouse.app.common.exception.OrderStatusCannotBeChangedException;
import com.storehouse.app.common.exception.UserNotAuthorizedException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.order.repository.OrderRepository;
import com.storehouse.app.order.services.OrderServices;
import com.storehouse.app.user.model.User.Roles;
import com.storehouse.app.user.services.UserServices;

import java.security.Principal;
import java.util.List;

import javax.ejb.SessionContext;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderServicesUTest {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private OrderServices orderServices;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserServices userServices; // to get the customer logged and associate with the order

    @Mock
    private SessionContext sessionContext; // to check the user logged in the system

    private static final String LOGGED_EMAIL = "anyemail@domain.com";

    private Validator validator;

    @Before
    public void initTestCase() {
        orderServices = new OrderServicesImpl();

        MockitoAnnotations.initMocks(this);

        validator = Validation.buildDefaultValidatorFactory().getValidator();
        ((OrderServicesImpl) orderServices).validator = validator;
        ((OrderServicesImpl) orderServices).orderRepository = orderRepository;
        ((OrderServicesImpl) orderServices).userServices = userServices;
        ((OrderServicesImpl) orderServices).sessionContext = sessionContext;

        setUpLoggedEmail(LOGGED_EMAIL, Roles.ADMIN);
    }

    private void setUpLoggedEmail(final String email, final Roles userRole) {
        reset(sessionContext); // responsible for telling sessionContent to be in initial state

        final Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(email);

        when(sessionContext.getCallerPrincipal()).thenReturn(principal);
        when(sessionContext.isCallerInRole(Roles.EMPLOYEE.name())).thenReturn(userRole == Roles.EMPLOYEE);
        when(sessionContext.isCallerInRole(Roles.CUSTOMER.name())).thenReturn(userRole == Roles.CUSTOMER);
    }

    @Test(expected = UserNotFoundException.class)
    public void addOrderWithNonCustomer() {
        when(userServices.findByEmail(LOGGED_EMAIL)).thenThrow(new UserNotFoundException());
        orderServices.add(orderReservedJohnDoe());
    }

    private void addOrderWithInvalidField(final Order order, final String invalidField) {
        try {
            orderServices.add(order);
            fail("An error should have been thrown here");
        } catch (final FieldNotValidException ex) {
            assertThat(ex.getFieldName(), is(equalTo(invalidField)));
        }
    }

    @Test
    public void addOrderWithNullQuantityItem() {
        when(userServices.findByEmail(LOGGED_EMAIL)).thenReturn(johnDoe());

        final Order order = orderReservedJohnDoe();
        order.getItems().iterator().next().setQuantity(null);

        addOrderWithInvalidField(order, "items[].quantity"); // items[] => collection
    }

    @Test
    public void addOrderWithoutItems() {
        when(userServices.findByEmail(LOGGED_EMAIL)).thenReturn(johnDoe());

        final Order order = orderReservedJohnDoe();
        order.setItems(null);

        addOrderWithInvalidField(order, "items"); // items[] => collection
    }

    @Test
    public void addValidOrder() {
        when(userServices.findByEmail(LOGGED_EMAIL)).thenReturn(johnDoe());
        when(orderRepository.add(orderEqual(orderReservedJohnDoe())))
                .thenReturn(orderWithId(orderReservedJohnDoe(), 1L));

        final Order addedOrder = orderServices.add(orderReservedJohnDoe());
        logger.info("jiafanz: {}", addedOrder);
        assertThat(addedOrder, is(notNullValue()));
        assertThat(addedOrder.getId(), is(equalTo(1L)));
    }

    @Test(expected = OrderNotFoundException.class)
    public void findOrderByIdNotFound() {
        when(orderRepository.findById(1L)).thenReturn(null);
        orderServices.findById(1L);
    }

    @Test
    public void findOrderById() {
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        final Order order = orderServices.findById(1L);
        assertThat(order, is(notNullValue()));
        assertThat(order.getId(), is(equalTo(1L)));
        assertThat(order.getItems(), is(equalTo(orderReservedJohnDoe().getItems())));
    }

    // we cannot update an order just like other items, but we can update the order status

    @Test(expected = OrderNotFoundException.class)
    public void updateOrderStatusOrderNotFound() {
        when(orderRepository.findById(1L)).thenReturn(null);
        orderServices.updateStatus(1L, OrderStatus.DELIVERED);
    }

    @Test(expected = OrderStatusCannotBeChangedException.class)
    public void updateOrderStatusForSameStatus() {
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        orderServices.updateStatus(1L, OrderStatus.RESERVED);
    }

    @Test(expected = UserNotAuthorizedException.class)
    public void updateStatusDeliveredAsNotEmployee() {
        setUpLoggedEmail(LOGGED_EMAIL, Roles.CUSTOMER);
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        orderServices.updateStatus(1L, OrderStatus.DELIVERED);
    }

    @Test
    public void updateStatusDeliveredAsEmployee() {
        setUpLoggedEmail(LOGGED_EMAIL, Roles.EMPLOYEE);
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        orderServices.updateStatus(1L, OrderStatus.DELIVERED);

        final Order expectedOrder = orderWithId(orderReservedJohnDoe(), 1L);
        expectedOrder.addHistoryEntry(OrderStatus.DELIVERED);
        verify(orderRepository).update(orderEqual(expectedOrder));
    }

    // only a customer who made this order can cancel his order

    @Test(expected = UserNotAuthorizedException.class)
    public void updateStatusCancelledAsCustomerNotTheOrderCustomer() {
        setUpLoggedEmail(LOGGED_EMAIL, Roles.CUSTOMER);
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        orderServices.updateStatus(1L, OrderStatus.CANCELLED);
    }

    @Test
    public void updateStatusCancelledAsCustomerSameAsTheOrderCustomer() {
        setUpLoggedEmail(orderReservedJohnDoe().getCustomer().getEmail(), Roles.CUSTOMER);
        when(orderRepository.findById(1L)).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));
        orderServices.updateStatus(1L, OrderStatus.CANCELLED);

        final Order expectedOrder = orderWithId(orderReservedJohnDoe(), 1L);
        expectedOrder.addHistoryEntry(OrderStatus.CANCELLED);
        verify(orderRepository).update(orderEqual(expectedOrder));
    }

    @Test
    public void findAllOrders() {
        when(orderRepository.findAll("createdAt")).thenReturn(allOrders());
        final List<Order> orders = orderServices.findAll("createdAt");
        logger.info(orders.toString());
        assertThat(orders.size(), is(equalTo(2)));
    }

    @Test
    public void findOrderByFilter() {
        final PaginatedData<Order> orders = new PaginatedData<>(2, allOrders());
        when(orderRepository.findByFilter((OrderFilter) anyObject())).thenReturn(orders);

        final PaginatedData<Order> ordersRetured = orderServices.findByFilter(new OrderFilter());
        assertThat(ordersRetured.getNumberOfRows(), is(equalTo(2)));
        assertThat(ordersRetured.getRows().size(), is(equalTo(2)));
    }

    @Test
    public void checkOrderPositionInQueueByCustomerId() {
        when(orderRepository.checkOrderPositionInQueueByCustomerId(1L)).thenReturn(2);
        final Integer position = orderServices.checkOrderPositionInQueueByCustomerId(1L);
        assertThat(position, is(equalTo(2)));
    }

    @Test
    public void checkOrderWaitTimeInQueueByCustomerId() {
        when(orderRepository.checkOrderWaitTimeInQueueByCustomerId(1L)).thenReturn(10);
        final Integer waitTime = orderServices.checkOrderWaitTimeInQueueByCustomerId(1L);
        assertThat(waitTime, is(equalTo(10)));
    }
}
