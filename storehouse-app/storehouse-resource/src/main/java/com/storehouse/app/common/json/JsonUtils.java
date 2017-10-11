package com.storehouse.app.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.model.PaginatedData;

/**
 * Json Utility class to formalize the final Json result.
 *
 * @author ejiafzh
 *
 */
final public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Get json element with its ID.
     *
     * @param id
     *            the id of the object.
     * @return a JsonElement.
     */
    public static JsonElement getJsonElementWithId(final Long id) {
        final JsonObject idJson = new JsonObject();
        idJson.addProperty("id", id);
        return idJson;
    }

    /**
     * Get a json result for paginated data.
     *
     * @param data
     *            a paginated data.
     * @param converter
     *            the json converter
     * @return a returned json element for collection.
     */
    public static <T> JsonElement getJsonElementWithPagingAndEntries(final PaginatedData<T> data,
            final EntityJsonConverter<T> converter) {
        final JsonObject jsonObject = new JsonObject();

        final JsonObject jsonPaging = new JsonObject();
        jsonPaging.addProperty("totalRecords", data.getNumberOfRows());

        jsonObject.add("paging", jsonPaging);
        jsonObject.add("entries", converter.convertToJsonElement(data.getRows()));
        return jsonObject;
    }

    /**
     * Get a json result for a JsonArray.
     *
     * @param jsonArray
     *            jsonArray
     * @return a returned json element for collection.
     */
    public static JsonElement getJsonElementWithJsonArray(final JsonArray jsonArray) {
        final JsonObject jsonObject = new JsonObject();

        final JsonObject jsonPaging = new JsonObject();
        jsonPaging.addProperty("totalRecords", jsonArray.size());

        jsonObject.add("paging", jsonPaging);
        jsonObject.add("entries", jsonArray);
        return jsonObject;
    }
}
