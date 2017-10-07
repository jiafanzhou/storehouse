package com.storehouse.app.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.List;

public interface EntityJsonConverter<T> {
    T convertFrom(final String json);

    JsonElement convertToJsonElement(final T entity);

    /**
     * This is a similar API for all entity.
     *
     * A new JDK8 feature which allows default implementation in interface.
     *
     * @param entities
     * @return
     */
    default JsonElement convertToJsonElement(final List<T> entities) {
        final JsonArray jsonArray = new JsonArray();
        for (final T entity : entities) {
            jsonArray.add(convertToJsonElement(entity));
        }
        return jsonArray;
    }
}
