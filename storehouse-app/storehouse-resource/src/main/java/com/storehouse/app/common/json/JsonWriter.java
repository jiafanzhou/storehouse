package com.storehouse.app.common.json;

import com.google.gson.Gson;

/**
 * Json utility from object to json string.
 *
 * @author ejiafzh
 *
 */
public final class JsonWriter {
    private JsonWriter() {
    }

    /**
     * Writes from object to Json string.
     * 
     * @param object
     *            object to write
     * @return a json string.
     */
    public static String writeToString(final Object object) {
        if (object == null) {
            return "";
        }
        return new Gson().toJson(object);
    }
}
