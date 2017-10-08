package com.storehouse.app.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.model.PaginatedData;

final public class JsonUtils {

    private JsonUtils() {
    }

    public static JsonElement getJsonElementWithId(final Long id) {
        final JsonObject idJson = new JsonObject();
        idJson.addProperty("id", id);
        return idJson;
    }

    public static <T> JsonElement getJsonElementWithPagingAndEntries(final PaginatedData<T> data,
            final EntityJsonConverter<T> converter) {
        final JsonObject jsonObject = new JsonObject();

        final JsonObject jsonPaging = new JsonObject();
        jsonPaging.addProperty("totalRecords", data.getNumberOfRows());

        jsonObject.add("paging", jsonPaging);
        jsonObject.add("entries", converter.convertToJsonElement(data.getRows()));
        return jsonObject;
    }

    public static JsonElement getJsonElementWithJsonArray(final JsonArray jsonArray) {
        final JsonObject jsonObject = new JsonObject();

        final JsonObject jsonPaging = new JsonObject();
        jsonPaging.addProperty("totalRecords", jsonArray.size());

        jsonObject.add("paging", jsonPaging);
        jsonObject.add("entries", jsonArray);
        return jsonObject;
    }
}
