package com.storehouse.app.order.resource;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;
import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static com.storehouse.app.commontests.utils.JsonTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.json.JsonWriter;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.commontests.utils.ArquillianTestUtils;
import com.storehouse.app.commontests.utils.IntegrationTestUtils;
import com.storehouse.app.commontests.utils.ResourceClient;
import com.storehouse.app.commontests.utils.ResourceDefinitions;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.order.model.OrderHistoryEntry;
import com.storehouse.app.order.model.OrderItem;

import java.net.URL;

import javax.ws.rs.core.Response;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class OrderResourceIntTest {
    @ArquillianResource
    private URL url;

    private ResourceClient resourceClient;

    private static final String PATH_RESOURCE = ResourceDefinitions.ORDER.getResourceName();
    private static final String USER_PATH_RESOURCE = ResourceDefinitions.USER.getResourceName();

    /**
     * ShrinkWrap is used to create the WebArchive file.
     *
     * beans.xml file (an empty marker file) is required for CDI to work.
     *
     * @return
     */
    @Deployment
    public static WebArchive createDeployment() {
        return ArquillianTestUtils.createDeployment();
    }

    // clean database for Integration tests
    @Before
    public void initTestCase() {
        this.resourceClient = new ResourceClient(url);
        resourceClient.resourcePath("DB").delete();

        // add all the users into the db
        resourceClient.resourcePath("DB/" + USER_PATH_RESOURCE).postWithContent("");

        // set the default user to be admin user()
        resourceClient.user(admin());
    }

    private String getJsonForOrder(final Order order) {
        final JsonObject orderJsonObject = new JsonObject();

        orderJsonObject.addProperty("clientId", order.getCustomer().getId());
        final JsonArray itemsJson = new JsonArray();
        order.getItems().forEach(e -> {
            final JsonObject itemJsonObject = new JsonObject();
            itemJsonObject.addProperty("quantity", e.getQuantity());
            itemsJson.add(itemJsonObject);
        });
        orderJsonObject.add("items", itemsJson);
        return JsonWriter.writeToString(orderJsonObject);
    }

    private Long addOrderAndGetId(final Order order) {
        return IntegrationTestUtils.addElementWithContentAndGetId(resourceClient, PATH_RESOURCE,
                getJsonForOrder(order));
    }

    private void assertResponseWithOrder(final String bodyResponse, final Order expectedOrder) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(bodyResponse);
        assertThat(JsonReader.getIntegerOrNull(jsonObject, "id"), is(notNullValue()));
        assertThat(JsonReader.getStringOrNull(jsonObject, "createdAt"), is(notNullValue()));
        assertThat(JsonReader.getJsonObjectOrNull(jsonObject, "customer").get("name").getAsString(),
                is(equalTo(expectedOrder.getCustomer().getName())));

        final JsonArray itemsJson = JsonReader.getJsonArrayOrNull(jsonObject, "items");
        assertThat(itemsJson.size(), is(equalTo(expectedOrder.getItems().size())));
        int numberOfItemsChecked = 0;
        for (final OrderItem expectedOrderItem : expectedOrder.getItems()) {
            for (int i = 0; i < itemsJson.size(); i++) {
                final JsonObject actualItemJson = itemsJson.get(i).getAsJsonObject();
                numberOfItemsChecked++;
                assertThat(actualItemJson.get("price").getAsDouble(), is(equalTo(expectedOrderItem.getPrice())));
            }
        }
        assertThat(numberOfItemsChecked, is(equalTo(expectedOrder.getItems().size())));

        final JsonArray historyEntriesJson = JsonReader.getJsonArrayOrNull(jsonObject, "historyEntries");
        assertThat(historyEntriesJson.size(), is(equalTo(expectedOrder.getHistoryEntries().size())));

        for (int i = 0; i < historyEntriesJson.size(); i++) {
            final JsonObject actualHistoryEntryJson = historyEntriesJson.get(i).getAsJsonObject();
            final OrderStatus actualHistoryStatus = OrderStatus
                    .valueOf(actualHistoryEntryJson.get("status").getAsString());
            assertThat(expectedOrder.getHistoryEntries().contains(new OrderHistoryEntry(actualHistoryStatus)),
                    is(equalTo(true)));
        }

        assertThat(JsonReader.getDoubleOrNull(jsonObject, "total"), is(equalTo(expectedOrder.getTotalPrice())));
        assertThat(JsonReader.getStringOrNull(jsonObject, "currentStatus"),
                is(equalTo(expectedOrder.getCurrentStatus().name())));
    }

    private void findOrderAndAssertResposneWithOrder(final Long orderId, final Order expectedOrder) {
        final String bodyResponse = IntegrationTestUtils.findById(resourceClient, PATH_RESOURCE, orderId);
        assertResponseWithOrder(bodyResponse, expectedOrder);
    }

    @Test
    @RunAsClient // 2 ways of running tests (a. within container b. as a client)
    public void addValidOrderAndFindIt() {
        resourceClient.user(johnDoe());
        final Long orderId = addOrderAndGetId(orderReservedJohnDoe());
        findOrderAndAssertResposneWithOrder(orderId, orderReservedJohnDoe());
    }

    @Test
    @RunAsClient
    public void addOrderAsAdmin() {
        final Order order = orderReservedJohnDoe();
        final Response response = resourceClient.user(admin()).resourcePath(PATH_RESOURCE)
                .postWithContent(getJsonForOrder(order));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    @Test
    @RunAsClient
    public void addStatusDeliveredNotAsEmployee() {
        resourceClient.user(johnDoe());
        final Long orderId = addOrderAndGetId(orderReservedJohnDoe());
        final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/" + orderId + "/status")
                .postWithContent(getJsonWithOrderStatus(OrderStatus.DELIVERED));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    @Test
    @RunAsClient
    public void addStatusDeliveredAsEmployee() {
        resourceClient.user(johnDoe());
        final Long orderId = addOrderAndGetId(orderReservedJohnDoe());
        final Response response = resourceClient.user(admin()).resourcePath(PATH_RESOURCE + "/" + orderId + "/status")
                .postWithContent(getJsonWithOrderStatus(OrderStatus.DELIVERED));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));

        final Order expectedOrder = orderReservedJohnDoe();
        expectedOrder.addHistoryEntry(OrderStatus.DELIVERED);
        findOrderAndAssertResposneWithOrder(orderId, expectedOrder);
    }

    @Test
    @RunAsClient
    public void findByIdNotFound() {
        final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/999").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
    }

    private void assertResponseContainsTheOrdersStatus(final Response response, final int expectedTotalRecords,
            final OrderStatus... expectedOrdersStatus) {
        final JsonArray orderList = IntegrationTestUtils.assertJsonHasTheNumberofElementsAndReturnTheEntries(response,
                expectedTotalRecords, expectedOrdersStatus.length);

        for (int i = 0; i < expectedOrdersStatus.length; i++) {
            final OrderStatus expectedOrder = expectedOrdersStatus[i];
            assertThat(orderList.get(i).getAsJsonObject().get("currentStatus").getAsString(),
                    is(equalTo(expectedOrder.name())));
        }
    }

    @Test
    @RunAsClient
    public void findByFilterPaginationByDates() {
        resourceClient.user(johnDoe());
        resourceClient.resourcePath("DB/" + PATH_RESOURCE).postWithContent("");

        resourceClient.user(admin());
        final Response response = resourceClient.resourcePath(
                PATH_RESOURCE + "?page=0&per_page=3&startDate=2017-10-01T10:00:00Z&endDate=2017-11-07T10:00:00Z").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertResponseContainsTheOrdersStatus(response, 3, OrderStatus.RESERVED, OrderStatus.RESERVED,
                OrderStatus.RESERVED);
    }

    @Test
    @RunAsClient
    public void getAllOrderStatus() {
        resourceClient.user(johnDoe());
        resourceClient.resourcePath("DB/" + PATH_RESOURCE).postWithContent("");

        resourceClient.user(admin());

        final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/stats/all").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        final JsonObject result = JsonReader.readAsJsonObject(response.readEntity(String.class));
        assertThat(result, is(notNullValue()));
    }
}
