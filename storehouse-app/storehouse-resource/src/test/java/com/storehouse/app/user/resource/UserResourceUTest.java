package com.storehouse.app.user.resource;

import static com.storehouse.app.commontests.user.UserForTestsRepository.*;
import static com.storehouse.app.commontests.user.UserMockitoArgumentMatcher.*;
import static com.storehouse.app.commontests.utils.FileTestNameUtils.*;
import static com.storehouse.app.commontests.utils.JsonTestUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.UserAlreadyExistingException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.commontests.utils.ResourceDefinitions;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.Roles;
import com.storehouse.app.user.resource.UserJsonConverter;
import com.storehouse.app.user.resource.UserResource;
import com.storehouse.app.user.services.UserServices;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserResourceUTest {
    private UserResource userResource;

    private static final String PATH_RESOURCE = ResourceDefinitions.USER.getResourceName();

    @Mock
    private UserServices userServices;

    @Mock
    private UriInfo uriInfo;

    private UserJsonConverter converter;

    @Mock
    private SecurityContext securityContext; // this can get the login user who makes the call

    @Before
    public void initTestCase() {
        MockitoAnnotations.initMocks(this);
        userResource = new UserResource();
        converter = new UserJsonConverter();

        userResource.userServices = userServices;
        userResource.converter = converter;
        userResource.uriInfo = uriInfo;
        userResource.securityContext = securityContext;
    }

    // everyone can create a customer
    @Test
    public void addValidCustomer() {
        when(userServices.add(userEqual(marySimpson()))).thenReturn(userWithIdAndDate(marySimpson(), 1L));

        final Response response = userResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "customerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.CREATED.getCode())));
        assertJsonMatchesExpectedJson(response.getEntity().toString(), "{\"id\": 1}");
    }

    // no one can add employee admin, (it is done in the db only)
    @Test
    public void addValidEmployee() {
        final Response response = userResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "employeeAdmin.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    private void assertJsonResponseWithFile(final Response response, final String fileName) {
        assertJsonMatchesFileContent(response.getEntity().toString(), getPathFileResponse(PATH_RESOURCE, fileName));
    }

    @Test
    public void addExistingUser() {
        when(userServices.add(marySimpson())).thenThrow(new UserAlreadyExistingException());

        final Response response = userResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "customerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, "userAlreadyExists.json");
    }

    @Test
    public void addUserWithNullName() {
        when(userServices.add((User) anyObject()))
                .thenThrow(new FieldNotValidException("name", null, "may not be null"));

        final Response response = userResource
                .add(readJsonFile(getPathFileRequest(PATH_RESOURCE, "nullNameUser.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, "userErrorNullName.json");
    }

    @Test
    public void updateValidCustomerAdmin() {
        // only administrator can update any customer in the system
        // a customer (not admin) can only update himself
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(true);

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(response.getEntity().toString(), is(equalTo("")));

        final User expectedUser = userWithIdAndDate(marySimpson(), 1L);
        verify(userServices).update(userEqual(expectedUser));
    }

    private void setUpPrincipalUser(final User user) {
        final Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn(user.getEmail());

        when(securityContext.getUserPrincipal()).thenReturn(principal);
        when(userServices.findByEmail(user.getEmail())).thenReturn(user);
    }

    // a customer (not admin) can only update himself
    @Test
    public void updateValidCustomerHimself() {
        setUpPrincipalUser(userWithIdAndDate(marySimpson(), 1L));
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(false);

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(response.getEntity().toString(), is(equalTo("")));

        final User expectedUser = userWithIdAndDate(marySimpson(), 1L);
        verify(userServices).update(userEqual(expectedUser));
    }

    // a customer (not admin) can only update himself
    @Test
    public void updateValidCustomerOtherCustomer() {
        setUpPrincipalUser(userWithIdAndDate(marySimpson(), 2L));
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(false);

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    @Test
    public void updateValidEmployeeAdmin() {
        // only administrator can update any customer in the system
        // a customer (not admin) can only update himself
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(true);

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "updateEmployeeAdmin.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(response.getEntity().toString(), is(equalTo("")));

        final User expectedUser = userWithIdAndDate(admin(), 1L);
        expectedUser.setPassword(null);
        verify(userServices).update(userEqual(expectedUser));
    }

    @Test
    public void updateUserEmailHoweverBelongToAnotherUser() {
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(true);

        when(userServices.update(userWithIdAndDate(marySimpson(), 1L)))
                .thenThrow(new UserAlreadyExistingException());

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "updateCustomerMarySimpson.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, "userAlreadyExists.json");
    }

    @Test
    public void updateUserWithNullName() {
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(true);
        final User nullNameCustomer = userWithIdAndDate(marySimpson(), 1L);
        nullNameCustomer.setName(null);
        when(userServices.update(nullNameCustomer))
                .thenThrow(new FieldNotValidException("name", null, "may not be null"));

        final Response response = userResource.update(1L,
                readJsonFile(getPathFileRequest(PATH_RESOURCE, "nullNameUser.json")));
        assertThat(response.getStatus(), is(equalTo(HttpCode.VALIDATION_ERROR.getCode())));
        assertJsonResponseWithFile(response, "userErrorNullName.json");
    }

    @Test
    public void updateUserPassword() {
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(true);

        final Response response = userResource.updatePassword(1L, getJsonWithPassword("new_pass"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(response.getEntity().toString(), is(equalTo("")));

        verify(userServices).updatePassword(1L, "new_pass");
    }

    @Test
    public void updateCustomerPasswordHimself() {
        setUpPrincipalUser(userWithIdAndDate(marySimpson(), 1L));
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(false);

        final Response response = userResource.updatePassword(1L, getJsonWithPassword("new_pass"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertThat(response.getEntity().toString(), is(equalTo("")));

        verify(userServices).updatePassword(1L, "new_pass");
    }

    @Test
    public void updateCustomerPasswordOtherCustomer() {
        setUpPrincipalUser(userWithIdAndDate(marySimpson(), 2L));
        when(securityContext.isUserInRole(Roles.ADMIN.name())).thenReturn(false);

        final Response response = userResource.updatePassword(1L, getJsonWithPassword("new_pass"));
        assertThat(response.getStatus(), is(equalTo(HttpCode.FORBIDDEN.getCode())));
    }

    @Test
    public void findCustomerById() {
        when(userServices.findById(1L)).thenReturn(userWithIdAndDate(marySimpson(), 1L));

        final Response response = userResource.findById(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "customerMarySimposonFound.json");
    }

    @Test
    public void findCustomerNotFound() {
        when(userServices.findById(1L)).thenThrow(new UserNotFoundException());

        final Response response = userResource.findById(1L);
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
        assertJsonResponseWithFile(response, "userNotFound.json");
    }

    @Test
    public void findCustomerByEmailAndPassword() {
        when(userServices.findByEmailAndPassword(marySimpson().getEmail(), marySimpson().getPassword()))
                .thenReturn(userWithIdAndDate(marySimpson(), 1L));

        final Response response = userResource.findByEmailAndPassword(
                getJsonWithEmailAndPassword(marySimpson().getEmail(), marySimpson().getPassword()));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "customerMarySimposonFound.json");
    }

    @Test
    public void findEmployeeByEmailAndPassword() {
        when(userServices.findByEmailAndPassword(admin().getEmail(), admin().getPassword()))
                .thenReturn(userWithIdAndDate(admin(), 1L));

        final Response response = userResource.findByEmailAndPassword(
                getJsonWithEmailAndPassword(admin().getEmail(), admin().getPassword()));
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "employeeAdminFound.json");
    }

    @Test
    public void findUserByEmailAndPasswordNotFound() {
        when(userServices.findByEmailAndPassword(admin().getEmail(), admin().getPassword()))
                .thenThrow(new UserNotFoundException());
        final Response response = userResource.findByEmailAndPassword(
                getJsonWithEmailAndPassword(admin().getEmail(), admin().getPassword()));
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
    }

    @Test
    public void findCustomerByEmail() {
        when(userServices.findByEmail(marySimpson().getEmail()))
                .thenReturn(userWithIdAndDate(marySimpson(), 1L));

        final Response response = userResource.findByEmail(marySimpson().getEmail());
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "customerMarySimposonFound.json");
    }

    @Test
    public void findCustomerByEmailNotFound() {
        when(userServices.findByEmail(marySimpson().getEmail()))
                .thenThrow(new UserNotFoundException());

        final Response response = userResource.findByEmail(marySimpson().getEmail());
        assertThat(response.getStatus(), is(equalTo(HttpCode.NOT_FOUND.getCode())));
        assertJsonResponseWithFile(response, "userNotFound.json");
    }

    @Test
    public void findAllUsersNotFound() {
        when(userServices.findAll()).thenReturn(new ArrayList<>());
        final Response response = userResource.findAll();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "emptyListOfUsers.json");
    }

    @Test
    public void findAllTwoUsers() {
        when(userServices.findAll())
                .thenReturn(Arrays.asList(userWithIdAndDate(marySimpson(), 1L),
                        userWithIdAndDate(admin(), 2L)));
        final Response response = userResource.findAll();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "twoUsers.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findByFilterNoFilter() {
        final List<User> users = allUsersWithId();
        final MultivaluedMap<String, String> multiMap = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(multiMap);
        when(userServices.findByFilter((UserFilter) anyObject())).thenReturn(
                new PaginatedData<>(users.size(), users));

        final Response response = userResource.findByFilter();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "usersAllInOnePage.json");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void findUsersByFilter() {
        final PaginatedData<User> users = new PaginatedData<>(2,
                Arrays.asList(userWithIdAndDate(marySimpson(), 1L), userWithIdAndDate(admin(), 2L)));
        final MultivaluedMap<String, String> multiMap = mock(MultivaluedMap.class);
        when(uriInfo.getQueryParameters()).thenReturn(multiMap);
        when(userServices.findByFilter((UserFilter) anyObject())).thenReturn(users);

        final Response response = userResource.findByFilter();
        assertThat(response.getStatus(), is(equalTo(HttpCode.OK.getCode())));
        assertJsonResponseWithFile(response, "twoUsers.json");
    }
}
