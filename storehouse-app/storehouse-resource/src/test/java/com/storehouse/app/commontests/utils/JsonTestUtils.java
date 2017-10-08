package com.storehouse.app.commontests.utils;

import com.google.gson.JsonObject;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.order.model.Order.OrderStatus;

import java.io.InputStream;
import java.util.Scanner;

import org.json.JSONException;
import org.junit.Ignore;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

@Ignore
public class JsonTestUtils {
    public static final String BASE_JSON_DIR = "json/";

    private JsonTestUtils() {
    }

    public static String readJsonFile(final String relativePath) {
        final InputStream is = JsonTestUtils.class.getClassLoader().getResourceAsStream(BASE_JSON_DIR + relativePath);
        try (Scanner s = new Scanner(is)) {
            // \A matches the beginning of the input, so it fetches the whole input as one move
            return s.useDelimiter("\\A").hasNext() ? s.next() : "";
        }
    }

    public static void assertJsonMatchesFileContent(final String actualJson, final String fileNameWithExpectedJson) {
        assertJsonMatchesExpectedJson(actualJson, readJsonFile(fileNameWithExpectedJson));
    }

    public static void assertJsonMatchesExpectedJson(final String actualJson, final String expectedJson) {
        try {
            // NON_EXTENSIBLE means it is flexible in relation to order
            JSONAssert.assertEquals(expectedJson, actualJson, JSONCompareMode.NON_EXTENSIBLE);
        } catch (final JSONException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    public static Long getIdFromJson(final String json) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(json);
        return JsonReader.getLongOrNull(jsonObject, "id");
    }

    public static String getJsonWithPassword(final String password) {
        return String.format("{\"password\": \"%s\"}", password);
    }

    public static String getJsonWithOrderStatus(final OrderStatus status) {
        return String.format("{\"status\": \"%s\"}", status);
    }

    public static String getJsonWithEmailAndPassword(final String email, final String password) {
        return String.format("{\"email\": \"%s\",\"password\": \"%s\"}", email, password);
    }
}
