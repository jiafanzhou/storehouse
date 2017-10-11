package com.storehouse.app.user.resource;

import static com.storehouse.app.common.model.StandardsOperationResults.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.UserAlreadyExistingException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.json.JsonUtils;
import com.storehouse.app.common.json.OperationResultJsonWriter;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.common.model.OperationResult;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.ResourceMessage;
import com.storehouse.app.common.model.filter.UserFilter;
import com.storehouse.app.user.model.Customer;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.model.User.Roles;
import com.storehouse.app.user.model.User.UserType;
import com.storehouse.app.user.services.UserServices;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User resource REST endpoint.
 *
 * @author ejiafzh
 *
 */
@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private static final ResourceMessage RM = new ResourceMessage("user");

    @Inject
    UserServices userServices;

    @Inject
    UserJsonConverter converter;

    // we need to extract a few parameters from the URL to create the user filter
    // object in order for pagination
    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    /**
     * REST endpoint to add an order.
     *
     * @param body
     *            json string of the body.
     * @return a response from the REST endpoint.
     */
    @POST
    public Response add(final String body) {
        logger.info("Adding a new user with body {}", body);

        // convert the json object from String to JsonObject, and then a User object
        User user = converter.convertFrom(body);

        // it is not allowed to add EMPLOYEE through this REST endpoint
        if (user.getUserType().equals(UserType.EMPLOYEE)) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;

        try {
            user = userServices.add(user);
            result = OperationResult.success(JsonUtils.getJsonElementWithId(user.getId()));
        } catch (final FieldNotValidException ex) {
            logger.error("Field is not valid", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultInvalidField(RM, ex);
        } catch (final UserAlreadyExistingException ex) {
            logger.error("There is already a user for the given email", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultExistent(RM, "email");
        }

        logger.info("Returning the operation result after adding user: {}", result);

        // convert the OperationResult object to Json
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    /**
     * REST endpoint to update an order.
     *
     * @param id
     *            the database id
     * @param body
     *            json string of the body.
     * @return a response from the REST endpoint.
     */
    @PUT
    @Path("/{id}")
    @PermitAll
    public Response update(@PathParam("id") final Long id, final String body) {
        logger.info("Updating the user id {} with body {}", id, body);

        // check the security, only admin can update others unless the logged user
        // trying to update himself
        if (!securityContext.isUserInRole(Roles.ADMIN.name())) {
            if (!isLoggedUser(id)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
        }

        // this means the security check is passed now.

        final User user = converter.convertFrom(body);
        user.setId(id);

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;

        try {
            userServices.update(user);
            result = OperationResult.success();
        } catch (final FieldNotValidException ex) {
            logger.error("Field is not valid", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultInvalidField(RM, ex);
        } catch (final UserNotFoundException ex) {
            logger.error("No user found for the given id", ex);
            httpCode = HttpCode.NOT_FOUND;
            result = getOperationResultNotFound(RM);
        } catch (final UserAlreadyExistingException ex) {
            logger.error("There is already an user for the given email", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultExistent(RM, "email");
        }

        logger.info("Returning the operation result after updating user: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    private boolean isLoggedUser(final Long id) {
        try {
            // principal name is the email (security identity of the user)
            final User loggedUser = userServices.findByEmail(securityContext.getUserPrincipal().getName());
            if (loggedUser.getId().equals(id)) {
                return true;
            }
        } catch (final UserNotFoundException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private String getPasswordFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return JsonReader.getStringOrNull(jsonObject, "password");
    }

    /**
     * REST endpoint to update the password for a given user based on the clientID
     *
     * @param id
     *            client ID for the customer.
     * @param body
     *            json string of the body.
     * @return a response from the REST endpoint.
     */
    @PUT
    @Path("/{id}/password")
    @PermitAll
    public Response updatePassword(@PathParam("id") final Long id, final String body) {
        logger.info("Updating the user password for user id {}", id);

        // check the security, only admin can update others unless the logged user
        // trying to update himself
        if (!securityContext.isUserInRole(Roles.ADMIN.name())) {
            if (!isLoggedUser(id)) {
                return Response.status(HttpCode.FORBIDDEN.getCode()).build();
            }
        }

        HttpCode httpCode = HttpCode.OK;
        OperationResult result;

        try {
            userServices.updatePassword(id, getPasswordFromJson(body));
            result = OperationResult.success();
        } catch (final UserNotFoundException ex) {
            logger.error("No user found for this given id", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultNotFound(RM);
        }

        logger.info("Returning the operation result after updating user password: {}", result);
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    /**
     * Find the user by id REST endpoint.
     * 
     * @param id
     *            the customer id.
     * @return a found user.
     */
    @GET
    @Path("/{id}")
    @RolesAllowed("ADMIN") // we just want ADMIN to perform this
    public Response findById(@PathParam("id") final Long id) {
        logger.info("Find user id: {}", id);

        ResponseBuilder rb;
        try {
            final User user = userServices.findById(id);
            final OperationResult result = OperationResult.success(converter.convertToJsonElement(user));
            rb = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.info("User found: {}", user);
        } catch (final UserNotFoundException ex) {
            logger.info("No User found for id: {}", id);
            rb = Response.status(HttpCode.NOT_FOUND.getCode())
                    .entity(OperationResultJsonWriter.toJson(getOperationResultNotFound(RM)));
        }
        return rb.build();
    }

    private User getUserWithEmailAndPasswordFromJson(final String body) {
        final User user = new Customer(); // implementation does not matter here
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        user.setEmail(JsonReader.getStringOrNull(jsonObject, "email"));
        user.setPassword(JsonReader.getStringOrNull(jsonObject, "password"));
        return user;
    }

    /**
     * Find the user by its email and password.
     * This REST endpoint is essentially used to authenticate a user.
     *
     * @param body
     *            includes email and password
     * @return found user otherwise User not found or user not valid.
     */
    @POST
    @Path("/authenticate")
    @PermitAll
    public Response findByEmailAndPassword(final String body) {
        logger.info("Find user by Email/Password: {}", body);

        ResponseBuilder rb;
        try {
            final User userWithEmailAndPassword = getUserWithEmailAndPasswordFromJson(body);
            final User user = userServices.findByEmailAndPassword(
                    userWithEmailAndPassword.getEmail(), userWithEmailAndPassword.getPassword());
            final OperationResult result = OperationResult.success(converter.convertToJsonElement(user));
            rb = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.info("User found by Email/Password: {}", user);
        } catch (final UserNotFoundException ex) {
            logger.info("No User found for Email/Password: {}", ex);
            rb = Response.status(HttpCode.NOT_FOUND.getCode())
                    .entity(OperationResultJsonWriter.toJson(getOperationResultNotFound(RM)));
        }
        return rb.build();
    }

    /**
     * FInd the user by email.
     *
     * @param email
     *            email of the user
     * @return a json object response with the user.
     */
    @GET
    @Path("/email/{email}")
    @RolesAllowed("ADMIN") // we just want ADMIN to perform this
    public Response findByEmail(@PathParam("email") final String email) {
        logger.info("Find user by email: {}", email);

        ResponseBuilder rb;
        try {
            final User user = userServices.findByEmail(email);
            final OperationResult result = OperationResult.success(converter.convertToJsonElement(user));
            rb = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.info("User found: {}", user);
        } catch (final UserNotFoundException ex) {
            logger.info("No User found for email: {}", email);
            rb = Response.status(HttpCode.NOT_FOUND.getCode())
                    .entity(OperationResultJsonWriter.toJson(getOperationResultNotFound(RM)));
        }
        return rb.build();
    }

    /**
     * Find all users.
     * An example:
     * http://localhost:8080/storehouse/api/users/all
     *
     * @return a Json response
     */
    @GET
    @Path("/all")
    @RolesAllowed("ADMIN") // we just want ADMIN to perform this
    public Response findAll() {
        logger.info("Find all users.");

        final List<User> users = userServices.findAll();

        logger.info("Found all users: {}", users);

        final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<>(users.size(), users), converter);
        return Response.status(HttpCode.OK.getCode())
                .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries))).build();
    }

    /**
     * Find all users based on the filter.
     * An example:
     * http://localhost:8080/storehouse/api/users?page=0&per_page=2&sort=-name
     *
     * @return a Json response
     */
    @GET
    @RolesAllowed("ADMIN") // we just want ADMIN to perform this
    public Response findByFilter() {
        final UserFilter userFilter = new UserFilterExtractorFromUrl(uriInfo).getFilter();
        logger.info("Finding users using filter: {}", userFilter);

        final PaginatedData<User> data = userServices.findByFilter(userFilter);

        logger.info("Finding the paginated users: {}", data);

        final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                data, converter);
        return Response.status(HttpCode.OK.getCode())
                .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries))).build();
    }

}
