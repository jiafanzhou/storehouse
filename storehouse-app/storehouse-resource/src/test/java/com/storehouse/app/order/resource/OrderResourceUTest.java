package com.storehouse.app.order.resource;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;
import static com.storehouse.app.commontests.order.OrderMockitoArgumentMatcher.*;
import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static com.storehouse.app.commontests.utils.FileTestNameUtils.*;
import static com.storehouse.app.commontests.utils.JsonTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.OrderNotFoundException;
import com.storehouse.app.common.exception.OrderStatusCannotBeChangedException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.commontests.utils.ResourceDefinitions;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.order.model.OrderHistoryEntry;
import com.storehouse.app.order.services.OrderServices;
import com.storehouse.app.user.model.Customer;
import com.storehouse.app.user.services.UserServices;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class OrderResourceUTest {
    private OrderResource orderResource;

    private static final String PATH_RESOURCE = ResourceDefinitions.ORDER.getResourceName();

    @Mock
    private OrderServices orderServices;

    @Mock
    private UserServices userServices;

    @Mock
    private UriInfo uriInfo;

    private OrderJsonConverter converter;

    @Mock
    private SecurityContext securityContext; // this can get the login user who makes the call

    @Before
    public void initTestCase() {
        MockitoAnnotations.initMocks(this);
        orderResource = new OrderResource();
        converter = new OrderJsonConverter();

        orderResource.orderServices = orderServices;
        orderResource.userServices = userServices;
        orderResource.converter = converter;
        orderResource.uriInfo = uriInfo;
        orderResource.securityContext = securityContext;
    }

    private void assertJsonResponseWithFile(final Response response, final String fileName) {
        assertJsonMatchesFileContent(response.getEntity().toString(), getPathFileResponse(PATH_RESOURCE, fileName));
    }

    @Test
    public void addValidOrder() {
        final Order expectedOrder = new Order();
        expectedOrder.setItems(orderReservedJohnDoe().getItems());
        expectedOrder.setCustomer((Customer) userWithIdAndDate(johnDoe(), 1L));
        when(orderServices.add(orderEqual(expectedOrder))).thenReturn(orderWithId(orderReservedJohnDoe(), 1L));

        final Response response = orderResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "orderForRubberDuck.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.CREATED.getCode())));
        assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
    }

    private void addOrderWithValidationError(final Exception exception,
            final String requestFileName, final String responseFileName) {
        when(orderServices.add((Order) anyObject())).thenThrow(exception);

        final Response response = orderResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, requestFileName)));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, responseFileName);
    }

    @Test
    public void addOrderWithNullItems() {
        addOrderWithValidationError(new FieldNotValidException("items", null,
                "may not be null"), "orderForRubberDuck.json", "orderErrorNullItems.json");
    }

    @Test
    public void addOrderWithNoCustomer() {
        addOrderWithValidationError(new UserNotFoundException(), "orderForRubberDuck.json",
                "orderErrorNoCustomer.json");
    }

    @Test
    public void addStatusOrderNotFound() {
        doThrow(new OrderNotFoundException()).when(orderServices).updateStatus(1L, OrderStatus.CANCELLED);
        final Response response = orderResource
                .addStatus(1L, getJsonWithOrderStatus(OrderStatus.CANCELLED));
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
    }

    @Test
    public void addStatusThatCannotBeChanged() {
        doThrow(new OrderStatusCannotBeChangedException("The new state must not be the same as the current state"))
                .when(orderServices).updateStatus(1L, OrderStatus.RESERVED);
        final Response response = orderResource
                .addStatus(1L, getJsonWithOrderStatus(OrderStatus.RESERVED));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
    }

    @Test
    public void findOrderByIdNotFound() {
        when(orderServices.findById(1L)).thenThrow(new OrderNotFoundException());

        final Response response = orderResource.findById(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
        assertJsonResponseWithFile(response, "orderNotFound.json");
    }

    /**
     * OrderItem is a HashSet, so need to prepare the order
     *
     * @param order
     */
    private void prepareOrderForJsonComparison(final Order order) {

        final Comparator<OrderHistoryEntry> sortOrderHistoryEntry = (final OrderHistoryEntry o1,
                final OrderHistoryEntry o2) -> (o1.getCreatedAt().compareTo(o2.getCreatedAt()));
        final SortedSet<OrderHistoryEntry> historyEntries = new TreeSet<>(sortOrderHistoryEntry);
        historyEntries.addAll(order.getHistoryEntries());
        order.setHistoryEntries(historyEntries);

        final Iterator<OrderHistoryEntry> it = order.getHistoryEntries().iterator();
        it.next().setCreatedAt(DateUtils.getAsDateTime("2017-10-10T10:00:00Z"));
        if (it.hasNext()) {
            it.next().setCreatedAt(DateUtils.getAsDateTime("2017-10-11T10:00:00Z"));
        }

    }

    @Test
    public void findOrderById() {
        final Order expectedOrder = orderWithId(orderDelivered(), 1L);
        expectedOrder.setCreatedAt(DateUtils.getAsDateTime("2017-10-10T10:00:00Z"));
        expectedOrder.getCustomer().setId(1L);
        prepareOrderForJsonComparison(expectedOrder);

        when(orderServices.findById(1L)).thenReturn(expectedOrder);

        final Response response = orderResource.findById(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "orderDeliveredFound.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findOrderByFilter() {
        final Order order1 = orderWithId(orderDelivered(), 1L);
        order1.getCustomer().setId(1L);
        order1.setCreatedAt(DateUtils.getAsDateTime("2017-10-10T10:00:00Z"));
        final Order order2 = orderWithId(orderReservedJohnDoe(), 2L);
        order2.getCustomer().setId(2L);
        order2.setCreatedAt(DateUtils.getAsDateTime("2017-10-11T10:00:00Z"));
        prepareOrderForJsonComparison(order1);
        prepareOrderForJsonComparison(order2);

        final PaginatedData<Order> orders = new PaginatedData<>(2, Arrays.asList(order1, order2));
        final MultivaluedMap<String, String> multiMap = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(multiMap);
        when(orderServices.findByFilter((OrderFilter) anyObject())).thenReturn(orders);

        final Response response = orderResource.findByFilter();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "ordersAllInOnePage.json");
    }

    @Test
    public void getOrderStatsInQueue() {
        when(userServices.findById(1L)).thenReturn(johnDoe());
        when(orderServices.checkOrderPositionInQueueByCustomerId(1L)).thenReturn(2);
        when(orderServices.checkOrderWaitTimeInQueueByCustomerId(1L)).thenReturn(10);
        final Response response = orderResource.getOrderStats(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "orderStatsResult.json");
    }

    @Test
    public void getAllOrdersStatsInQueue() {
        final List<Order> orders = Arrays.asList(orderReservedJohnDoe(), orderReservedEndaKenny(),
                orderReservedDonaldTrump());
        long index = 1L;
        for (final Order order : orders) {
            order.getCustomer().setId(index++);
        }
        when(orderServices.findAllReservedOrders()).thenReturn(orders);
        when(orderServices.checkOrderPositionInQueueByCustomerId(1L)).thenReturn(1);
        when(orderServices.checkOrderWaitTimeInQueueByCustomerId(1L)).thenReturn(2);
        when(orderServices.checkOrderPositionInQueueByCustomerId(2L)).thenReturn(2);
        when(orderServices.checkOrderWaitTimeInQueueByCustomerId(2L)).thenReturn(4);
        when(orderServices.checkOrderPositionInQueueByCustomerId(3L)).thenReturn(2);
        when(orderServices.checkOrderWaitTimeInQueueByCustomerId(3L)).thenReturn(10);

        final Response response = orderResource.getAllOrdersStats();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "allOrdersStatsResult.json");
    }

    @Test
    public void cancelOrder() {
        final Order order1 = orderWithId(orderReservedJohnDoe(), 1L);
        order1.getCustomer().setId(1L);
        final PaginatedData<Order> orders = new PaginatedData<>(1, Arrays.asList(order1));
        when(orderServices.findByFilter((OrderFilter) anyObject())).thenReturn(orders);

        final Response response = orderResource.cancelOrderByCustomerId(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
    }
}
