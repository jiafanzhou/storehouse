package com.storehouse.app.commontests.utils;

import static com.storehouse.app.commontests.utils.FileTestNameUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.model.HttpCode;

import javax.ws.rs.core.Response;

import org.junit.Ignore;

@Ignore
public class IntegrationTestUtils {
    public static Long addElementWithFileAndGetId(final ResourceClient resourceClient, final String pathResource,
            final String mainFolder, final String fileName) {
        final Response response = resourceClient.resourcePath(pathResource).postWithFile(
                getPathFileRequest(mainFolder, fileName));
        return assertResponseIsCreatedAndGetId(response);
    }

    public static Long addElementWithContentAndGetId(final ResourceClient resourceClient, final String pathResource,
            final String content) {
        final Response response = resourceClient.resourcePath(pathResource).postWithContent(content);
        return assertResponseIsCreatedAndGetId(response);
    }

    public static String findById(final ResourceClient resourceClient, final String pathResource,
            final Long id) {
        final Response response = resourceClient.resourcePath(pathResource + "/" + id).get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        return response.readEntity(String.class);
    }

    private static Long assertResponseIsCreatedAndGetId(final Response response) {
        assertThat(response.getStatus(), is(equalTo(HttpCode.CREATED.getCode())));
        final Long id = JsonTestUtils.getIdFromJson(response.readEntity(String.class));
        assertThat(id, is(notNullValue()));
        return id;
    }

    public static <T> JsonArray assertResponseContainsTheEntities(final Response response, final int expectedSize,
            final T[] entities) {
        final JsonObject result = JsonReader.readAsJsonObject(response.readEntity(String.class));

        final int totalRecords = result.getAsJsonObject("paging").get("totalRecords").getAsInt();
        assertThat(totalRecords, is(equalTo(expectedSize)));

        final JsonArray authorsList = result.getAsJsonArray("entries");
        assertThat(authorsList.size(), is(equalTo(entities.length)));
        return authorsList;
    }

    public static <T> JsonArray assertJsonHasTheNumberofElementsAndReturnTheEntries(final Response response,
            final int expectedSize, final int entryLength) {
        final JsonObject result = JsonReader.readAsJsonObject(response.readEntity(String.class));

        final int totalRecords = result.getAsJsonObject("paging").get("totalRecords").getAsInt();
        assertThat(totalRecords, is(equalTo(expectedSize)));

        final JsonArray authorsList = result.getAsJsonArray("entries");
        assertThat(authorsList.size(), is(equalTo(entryLength)));
        return authorsList;
    }
}
