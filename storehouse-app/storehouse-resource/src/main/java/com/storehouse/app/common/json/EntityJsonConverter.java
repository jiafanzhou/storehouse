package com.storehouse.app.common.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.List;

/**
 * Utility class to convert Json element.
 *
 * @author ejiafzh
 *
 * @param <T>
 */
public interface EntityJsonConverter<T> {
    T convertFrom(final String json);

    /**
     * Entity needs to implement this method to perform the conversion.
     *
     * @param entity
     *            the entity to be converted.
     * @return a converted JsonElement
     */
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
