package com.storehouse.app.order.resource;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.json.EntityJsonConverter;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.OrderHistoryEntry;
import com.storehouse.app.order.model.OrderItem;
import com.storehouse.app.user.model.Customer;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderJsonConverter implements EntityJsonConverter<Order> {

    @Override
    public Order convertFrom(final String json) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        final Long clientId = JsonReader.getLongOrNull(jsonObject, "clientId");
        final Customer customer = new Customer();
        customer.setCreatedAt(null);
        customer.setId(clientId);

        final Order order = new Order();
        order.setCustomer(customer);
        final JsonArray itemsJsonArray = jsonObject.getAsJsonArray("items");
        if (itemsJsonArray != null) {
            itemsJsonArray.forEach(e -> {
                final Integer quantity = JsonReader.getIntegerOrNull(e.getAsJsonObject(), "quantity");
                order.addItem(quantity);
            });
        }
        return order;
    }

    private JsonElement getHistoryEntriesAsJsonElement(final OrderHistoryEntry historyEntry) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("createdAt", DateUtils.formatDateTime(historyEntry.getCreatedAt()));
        jsonObject.addProperty("status", historyEntry.getStatus().name());
        return jsonObject;
    }

    private JsonElement getOrderItemAsJsonElement(final OrderItem orderItem) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("quantity", orderItem.getQuantity());
        jsonObject.addProperty("price", orderItem.getPrice());
        return jsonObject;
    }

    private JsonElement getCustomerAsJsonElement(final Customer customer) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", customer.getId());
        jsonObject.addProperty("name", customer.getName());
        return jsonObject;
    }

    /**
     * For one order, we display the items and historyEntries.
     *
     * (orderDeliveredFound.json)
     *
     *
     * For all orders, we will not display the items and historyEntries.
     *
     * (ordersAllInOnePage.json)
     */
    private JsonElement getOrderAsJsonElement(final Order order, final boolean addItemsAndHistory) {
        final JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("id", order.getId());
        jsonObject.addProperty("createdAt", DateUtils.formatDateTime(order.getCreatedAt()));
        jsonObject.add("customer", getCustomerAsJsonElement(order.getCustomer()));

        if (addItemsAndHistory) {
            final JsonArray jsonArrayItems = new JsonArray();
            order.getItems().forEach(e -> {
                jsonArrayItems.add(getOrderItemAsJsonElement(e));
            });
            jsonObject.add("items", jsonArrayItems);

            final JsonArray jsonArrayHistoryEntries = new JsonArray();
            order.getHistoryEntries().forEach(e -> {
                jsonArrayHistoryEntries.add(getHistoryEntriesAsJsonElement(e));
            });
            jsonObject.add("historyEntries", jsonArrayHistoryEntries);
        }
        jsonObject.addProperty("total", order.getTotalPrice());
        jsonObject.addProperty("currentStatus", order.getCurrentStatus().name());

        return jsonObject;
    }

    @Override
    public JsonElement convertToJsonElement(final Order order) {
        return getOrderAsJsonElement(order, true);
    }

    @Override
    public JsonElement convertToJsonElement(final List<Order> orders) {
        final JsonArray jsonArray = new JsonArray();
        for (final Order order : orders) {
            jsonArray.add(getOrderAsJsonElement(order, false));
        }
        return jsonArray;
    }

}
