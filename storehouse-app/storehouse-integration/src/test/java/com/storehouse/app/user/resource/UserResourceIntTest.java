package com.storehouse.app.user.resource;

import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static com.storehouse.app.commontests.utils.FileTestNameUtils.*;
import static com.storehouse.app.commontests.utils.JsonTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.commontests.utils.ArquillianTestUtils;
import com.storehouse.app.commontests.utils.IntegrationTestUtils;
import com.storehouse.app.commontests.utils.ResourceClient;
import com.storehouse.app.commontests.utils.ResourceDefinitions;
import com.storehouse.app.user.model.User;

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
public class UserResourceIntTest {

    @ArquillianResource
    private URL url;

    private ResourceClient resourceClient;

    private static final String PATH_RESOURCE = ResourceDefinitions.USER.getResourceName();

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

        // we add an admin user for each test
        resourceClient.resourcePath("DB/" + PATH_RESOURCE + "/admin").postWithContent("");
    }

    private Long addUserAndGetId(final String fileName) {
        resourceClient.user(null); // don't need a user to add
        return IntegrationTestUtils.addElementWithFileAndGetId(resourceClient, PATH_RESOURCE,
                PATH_RESOURCE, fileName);
    }

    private void findUserAndAssertResposneWithUser(final Long userId, final User expectedUser) {
        resourceClient.user(admin());
        final String bodyResponse = IntegrationTestUtils.findById(resourceClient, PATH_RESOURCE, userId);
        assertResponseWithUser(bodyResponse, expectedUser);
    }

    private void assertResponseWithUser(final String bodyResponse, final User expectedUser) {
        final JsonObject userJsonObject = JsonReader.readAsJsonObject(bodyResponse);
        assertThat(JsonReader.getIntegerOrNull(userJsonObject, "id"), is(notNullValue()));
        assertThat(JsonReader.getStringOrNull(userJsonObject, "name"), is(equalTo(expectedUser.getName())));
        assertThat(JsonReader.getStringOrNull(userJsonObject, "email"), is(equalTo(expectedUser.getEmail())));
        assertThat(JsonReader.getStringOrNull(userJsonObject, "type"),
                is(equalTo(expectedUser.getUserType().toString())));
        assertThat(JsonReader.getStringOrNull(userJsonObject, "createdAt"), is(notNullValue()));

        final JsonArray roles = userJsonObject.getAsJsonArray("roles");
        assertThat(roles.size(), is(equalTo(expectedUser.getRoles().size())));
        for (int i = 0; i < roles.size(); i++) {
            final String actualRole = roles.get(i).getAsJsonPrimitive().getAsString();
            final String expectedRole = expectedUser.getRoles().get(i).toString();
            assertThat(actualRole, is(equalTo(expectedRole)));
        }
    }

    @Test
    @RunAsClient // 2 ways of running tests (a. within container b. as a client)
    public void addValidCustomerAndFindIt() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());
    }

    private void addUserWithValidationError(final String requestFileName, final String responseFileName) {
        final Response response = resourceClient.user(null).resourcePath(PATH_RESOURCE)
                .postWithFile(getPathFileRequest(PATH_RESOURCE, requestFileName));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, responseFileName);
    }

    private void assertJsonResponseWithFile(final Response response, final String fileName) {
        assertJsonMatchesFileContent(response.readEntity(String.class), getPathFileResponse(PATH_RESOURCE, fileName));
    }

    @Test
    @RunAsClient
    public void addUserWithNullName() {
        addUserWithValidationError("nullNameUser.json", "userErrorNullName.json");
    }

    @Test
    @RunAsClient
    public void addExistingUser() {
        addUserAndGetId("customerMarySimpson.json");
        addUserWithValidationError("customerMarySimpson.json", "userAlreadyExists.json");
    }

    @Test
    @RunAsClient
    public void updateValidCustomerAsAdmin() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpsonWithNewName.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));

        final User expectedUser = marySimpson();
        expectedUser.setName("New Name");
        findUserAndAssertResposneWithUser(id, expectedUser);
    }

    @Test
    @RunAsClient
    public void updateValidLoggedCustomerAsCustomer() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        final Response response = resourceClient.user(marySimpson())
                .resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpsonWithNewName.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));

        resourceClient.user(admin());
        final User expectedUser = marySimpson();
        expectedUser.setName("New Name");
        findUserAndAssertResposneWithUser(id, expectedUser);
    }

    @Test
    @RunAsClient
    public void updateCustomerAsNotLoggedCustomer() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());
        addUserAndGetId("customerJohnDoe.json");

        final Response response = resourceClient.user(johnDoe())
                .resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpsonWithNewName.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    // we do not allow to change uesrType from customer to employee
    @Test
    @RunAsClient
    public void updateCustomerTryingToChangeType() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        final Response response = resourceClient.resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpsonWithNewType.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.INTERNAL_ERR.getCode())));
    }

    private boolean authenticate(final String email, final String password) {
        final Response response = resourceClient.user(null)
                .resourcePath(PATH_RESOURCE + "/authenticate")
                .postWithContent(getJsonWithEmailAndPassword(email, password));
        return response.getStatus() == HttpCode.OK.getCode();
    }

    @Test
    @RunAsClient
    public void updateCustomerTryingToChangePasswordWithUpdate() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(true)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(false)));

        final Response response = resourceClient.user(marySimpson())
                .resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpsonWithNewPassword.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(true)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(false)));
    }

    @Test
    @RunAsClient
    public void updateUserWithEmailBelongingToAnotherUser() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        addUserAndGetId("customerJohnDoe.json");

        final Response response = resourceClient.user(admin())
                .resourcePath(PATH_RESOURCE + "/" + id)
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "customerJohnDoe.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, "userAlreadyExists.json");
    }

    @Test
    @RunAsClient
    public void updateUserNotFound() {
        final Response response = resourceClient.user(admin()).resourcePath(PATH_RESOURCE + "/999")
                .putWithFile(getPathFileRequest(PATH_RESOURCE, "customerJohnDoe.json"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
        assertJsonResponseWithFile(response, "userNotFound.json");
    }

    @Test
    @RunAsClient
    public void updatePasswordAsAdmin() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(true)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(false)));

        final Response response = resourceClient.user(admin())
                .resourcePath(PATH_RESOURCE + "/" + id + "/password")
                .putWithContent(getJsonWithPassword("111111"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));

        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(false)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(true)));
    }

    @Test
    @RunAsClient
    public void updatePasswordAsLoggedCustomer() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());

        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(true)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(false)));

        final Response response = resourceClient.user(marySimpson())
                .resourcePath(PATH_RESOURCE + "/" + id + "/password")
                .putWithContent(getJsonWithPassword("111111"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));

        assertThat(authenticate(marySimpson().getEmail(), marySimpson().getPassword()), is(equalTo(false)));
        assertThat(authenticate(marySimpson().getEmail(), "111111"), is(equalTo(true)));
    }

    @Test
    @RunAsClient
    public void updatePasswordAsNotLoggedCustomer() {
        final Long id = addUserAndGetId("customerMarySimpson.json");
        findUserAndAssertResposneWithUser(id, marySimpson());
        addUserAndGetId("customerJohnDoe.json");

        final Response response = resourceClient.user(johnDoe())
                .resourcePath(PATH_RESOURCE + "/" + id + "/password")
                .putWithContent(getJsonWithPassword("111111"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    @Test
    @RunAsClient
    public void findUserByIdNotFound() {
        final Response response = resourceClient.user(admin()).resourcePath(PATH_RESOURCE + "/999").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
        assertJsonResponseWithFile(response, "userNotFound.json");
    }

    private void assertResponseContainsTheUsers(final Response response, final int expectedTotalRecords,
            final User... expectedUsers) {
        final JsonArray usersList = IntegrationTestUtils.assertResponseContainsTheEntities(response,
                expectedTotalRecords, expectedUsers);

        for (int i = 0; i < expectedUsers.length; i++) {
            final User expectedUser = expectedUsers[i];
            assertThat(usersList.get(i).getAsJsonObject().get("name").getAsString(),
                    is(equalTo(expectedUser.getName())));
        }
    }

    @Test
    @RunAsClient
    public void findByFilterPaginationAndOrderingDesendingByName() {
        resourceClient.resourcePath("DB/").delete();
        resourceClient.resourcePath("DB/" + PATH_RESOURCE).postWithContent("");
        resourceClient.user(admin());

        // first page
        Response response = resourceClient.resourcePath(PATH_RESOURCE + "?page=0&per_page=2&sort=-name").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertResponseContainsTheUsers(response, 3, marySimpson(), johnDoe());

        // second page
        response = resourceClient.resourcePath(PATH_RESOURCE + "?page=1&per_page=2&sort=-name").get();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertResponseContainsTheUsers(response, 3, admin());
    }

}
