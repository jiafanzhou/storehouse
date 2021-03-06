package com.storehouse.app.common.json;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.storehouse.app.common.exception.InvalidJsonException;

/**
 * Utility class to be responsible for reading json.
 *
 * @author ejiafzh
 *
 */
public class JsonReader {
    /**
     * Read a json object.
     *
     * @param json
     *            json object to be read
     * @return a read json object
     * @throws InvalidJsonException
     *             if any exception occurs.
     */
    public static JsonObject readAsJsonObject(final String json) throws InvalidJsonException {
        return readJsonAs(json, JsonObject.class);
    }

    /**
     * Read a json array.
     *
     * @param json
     *            json array object to be read
     * @return a read json object
     * @throws InvalidJsonException
     *             if any exception occurs.
     */
    public static JsonArray readAsJsonArray(final String json) throws InvalidJsonException {
        return readJsonAs(json, JsonArray.class);
    }

    /**
     * Read json object based on its type.
     * 
     * @param json
     *            json object to be read
     * @param jsonClass
     *            type class
     * @return a read json object.
     * @throws InvalidJsonException
     */
    public static <T> T readJsonAs(final String json, final Class<T> jsonClass)
            throws InvalidJsonException {
        if (json == null || json.trim().isEmpty()) {
            throw new InvalidJsonException("Json String cannot be null");
        }
        try {
            return new Gson().fromJson(json, jsonClass);
        } catch (final JsonSyntaxException ex) {
            throw new InvalidJsonException(ex);
        }
    }

    public static Long getLongOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsLong();
    }

    public static Integer getIntegerOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsInt();
    }

    public static String getStringOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsString();
    }

    public static Double getDoubleOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsDouble();
    }

    public static JsonObject getJsonObjectOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsJsonObject();
    }

    public static JsonArray getJsonArrayOrNull(final JsonObject jsonObject, final String propertyName) {
        final JsonElement property = jsonObject.get(propertyName);
        if (isJsonElementNull(property)) {
            return null;
        }
        return property.getAsJsonArray();
    }

    private static boolean isJsonElementNull(final JsonElement element) {
        return element == null || element.isJsonNull();
    }

}
