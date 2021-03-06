package com.storehouse.app.order.resource;

import static com.storehouse.app.common.model.StandardsOperationResults.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.exception.FieldNotValidException;
import com.storehouse.app.common.exception.InvalidClientIdInOrder;
import com.storehouse.app.common.exception.OrderAlreadyExistingException;
import com.storehouse.app.common.exception.OrderNotFoundException;
import com.storehouse.app.common.exception.OrderStatusCannotBeChangedException;
import com.storehouse.app.common.exception.UserNotAuthorizedException;
import com.storehouse.app.common.exception.UserNotFoundException;
import com.storehouse.app.common.json.JsonReader;
import com.storehouse.app.common.json.JsonUtils;
import com.storehouse.app.common.json.OperationResultJsonWriter;
import com.storehouse.app.common.model.HttpCode;
import com.storehouse.app.common.model.OperationResult;
import com.storehouse.app.common.model.PaginatedData;
import com.storehouse.app.common.model.ResourceMessage;
import com.storehouse.app.common.model.filter.OrderFilter;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
import com.storehouse.app.order.services.OrderServices;
import com.storehouse.app.order.services.impl.OrderEventReceiver;
import com.storehouse.app.user.model.User;
import com.storehouse.app.user.services.UserServices;

import java.util.List;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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
 * REST endpoint for the order.
 *
 * @author ejiafzh
 *
 */
@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final ResourceMessage RM = new ResourceMessage("order");

    @Inject
    OrderServices orderServices;

    @Inject
    UserServices userServices;

    @Inject
    OrderJsonConverter converter;

    // we need to extract a few parameters from the URL to create the user filter
    // object in order for pagination
    @Context
    UriInfo uriInfo;

    @Context
    SecurityContext securityContext;

    @Inject
    OrderEventReceiver orderEventReceiver;

    /**
     * REST endpoint to add an order into the system.
     *
     * @param body
     *            the body contains ClientId and quantity.
     * @return whether or not the order is added.
     */
    @POST
    @RolesAllowed("CUSTOMER")
    public Response add(final String body) {
        logger.info("Adding a new order with body {}", body);

        // convert the json object from String to JsonObject, and then an Order object
        Order order = converter.convertFrom(body);

        HttpCode httpCode = HttpCode.CREATED;
        OperationResult result;

        try {
            order = orderServices.add(order);
            result = OperationResult.success(JsonUtils.getJsonElementWithId(order.getId()));
        } catch (final FieldNotValidException ex) {
            logger.error("Field is not valid", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultInvalidField(RM, ex);
        } catch (final UserNotFoundException ex) {
            logger.error("Customer cannot be found for this order", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultDependencyNotFound(RM, "customer");
        } catch (final InvalidClientIdInOrder ex) {
            logger.error("Invalid customer clientId", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultDependencyNotFound(RM, "customer");
        } catch (final OrderAlreadyExistingException ex) {
            logger.error("Invalid customer clientId", ex);
            httpCode = HttpCode.VALIDATION_ERROR;
            result = getOperationResultClientOrderAlreadyExists(RM, "customer",
                    order.getCustomer().getId());
        }

        logger.info("Returning the operation result after adding order: {}", result);

        // convert the OperationResult object to Json
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    private OrderStatus getStatusFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return OrderStatus.valueOf(JsonReader.getStringOrNull(jsonObject, "status"));
    }

    /**
     * Cancel order REST endpoint.
     *
     * @param customerId
     *            the customer ID to cancel an order
     * @return whether or not the order cancellation is successful.
     */
    @GET
    @Path("/{id}/cancel")
    @PermitAll
    public Response cancelOrderByCustomerId(@PathParam("id") final Long customerId) {
        logger.info("Cancel an order for customerId {}", customerId);

        final OrderFilter filter = new OrderFilter();
        filter.setCustomerId(customerId);
        filter.setStatus(OrderStatus.RESERVED);
        final PaginatedData<Order> data = orderServices.findByFilter(filter);

        // there should be only 1 Order with that clientId
        if (data.getNumberOfRows() == 0) {
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        } else {
            final Order order = data.getRows().get(0);
            return addStatus(order.getId(), String.format("{\"status\": \"%s\"}", OrderStatus.CANCELLED));
        }
    }

    /**
     * Update the order status.
     *
     * @param id
     *            the order id
     * @param body
     *            includes new status of the order
     * @return whether or not the order status update is successful.
     */
    @POST
    @Path("/{id}/status")
    @PermitAll
    public Response addStatus(@PathParam("id") final Long id, final String body) {
        logger.info("Adding a new status{} for order {}", body, id);

        final OrderStatus newStatus = getStatusFromJson(body);

        try {
            orderServices.updateStatus(id, newStatus);
        } catch (final OrderNotFoundException ex) {
            logger.error("Order {} not found to add a new status", id);
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        } catch (final OrderStatusCannotBeChangedException ex) {
            logger.error("Error while changing order status {}", ex.getMessage());
            return Response.status(HttpCode.VALIDATION_ERROR.getCode()).build();
        } catch (final UserNotAuthorizedException ex) {
            logger.error("User is not authorized to perform this action {}", ex.getMessage());
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

        return Response.status(HttpCode.OK.getCode()).build();
    }

    /**
     * To find an existing order by ID.
     *
     * @param id
     *            the order ID.
     * @return an existing Order, null if it could not find one.
     */
    @GET
    @Path("/{id}")
    @PermitAll
    public Response findById(@PathParam("id") final Long id) {
        logger.info("Find order id: {}", id);

        ResponseBuilder rb;
        try {
            final Order order = orderServices.findById(id);
            final OperationResult result = OperationResult.success(converter.convertToJsonElement(order));
            rb = Response.status(HttpCode.OK.getCode()).entity(OperationResultJsonWriter.toJson(result));
            logger.info("Order found: {}", order);
        } catch (final OrderNotFoundException ex) {
            logger.info("No Order found for id: {}", id);
            rb = Response.status(HttpCode.NOT_FOUND.getCode())
                    .entity(OperationResultJsonWriter.toJson(getOperationResultNotFound(RM)));
        }
        return rb.build();
    }

    /**
     * Find all the orders.
     *
     * @return a list of the found orders.
     */
    @GET
    @Path("/all")
    @RolesAllowed("EMPLOYEE")
    public Response findAll() {
        logger.info("Find all orders.");

        final List<Order> orders = orderServices.findAll();

        logger.info("Found all orders: {}", orders);

        final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                new PaginatedData<>(orders.size(), orders), converter);
        return Response.status(HttpCode.OK.getCode())
                .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries))).build();
    }

    /**
     * Find paginated data of order by the order filter.
     *
     * @return a json of orders.
     */
    @GET
    @RolesAllowed("EMPLOYEE")
    // http://localhost:8080/storehouse/api/orders?page=0&per_page=2&sort=-startDate
    public Response findByFilter() {
        final OrderFilter orderFilter = new OrderFilterExtractorFromUrl(uriInfo).getFilter();
        logger.info("Finding orders using filter: {}", orderFilter);

        final PaginatedData<Order> data = orderServices.findByFilter(orderFilter);

        logger.info("Finding the paginated orders: {}", data);

        final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithPagingAndEntries(
                data, converter);
        return Response.status(HttpCode.OK.getCode())
                .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries))).build();
    }

    /**
     * Get the order statistics in the queue.
     *
     * @param customerId
     *            the client ID.
     * @return a statistics report of the order.
     */
    @GET
    @Path("/stats/{id}")
    // http://localhost:8080/storehouse/api/orders/stats/{id}
    public Response getOrderStats(@PathParam("id") final Long customerId) {
        logger.info("Getting order statistics in the queue");

        // need to check if customer if exists
        // ensure customerId exists ...

        User customer = null;

        try {
            customer = userServices.findById(customerId);
        } catch (final UserNotFoundException ex) {
            logger.error("Customer cannot be found for clientId", ex);
            final HttpCode httpCode = HttpCode.VALIDATION_ERROR;
            final OperationResult result = getOperationResultDependencyNotFound(RM, "customer");
            return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
        }

        ResponseBuilder rb;
        final Integer position = orderServices.checkOrderPositionInQueueByCustomerId(customerId);
        final Integer waitTime = orderServices.checkOrderWaitTimeInQueueByCustomerId(customerId);

        if (position == -1 || waitTime == -1) {
            logger.error("Customer {} does not have an order yet", customerId);
            return Response.status(HttpCode.NOT_FOUND.getCode()).build();
        }

        final OperationResult result = OperationResult
                .success(converter.convertQueueStatsToJsonElement(customerId, customer.getName(),
                        position, waitTime));
        final String tmp = OperationResultJsonWriter.toJson(result);
        rb = Response.status(HttpCode.OK.getCode()).entity(tmp);
        logger.info("Queue position: {}, wait time: {}, for customerId {}", position, waitTime, customerId);
        return rb.build();
    }

    /**
     * Get the all order statistics in the queue.
     *
     * @return a statistics report of all orders.
     */
    @GET
    @Path("/stats/all")
    @RolesAllowed({ "EMPLOYEE", "ADMIN" })
    // http://localhost:8080/storehouse/api/orders/stats/all
    public Response getAllOrdersStats() {
        logger.info("Getting all orders statistics in the queue");

        try {
            final JsonArray jsonArray = new JsonArray();
            // jiafanz: document this
            // jiafanz: this can be greatly improved by performance...
            for (final Order order : orderServices.findAllReservedOrders()) {
                final Long customerId = order.getCustomer().getId();
                final Integer position = orderServices.checkOrderPositionInQueueByCustomerId(customerId);
                final Integer waitTime = orderServices.checkOrderWaitTimeInQueueByCustomerId(customerId);

                if (position == -1 || waitTime == -1) {
                    logger.error("Customer {} does not have an order yet", customerId);
                    return Response.status(HttpCode.NOT_FOUND.getCode()).build();
                }

                jsonArray.add(converter.convertQueueStatsToJsonElement(customerId,
                        order.getCustomer().getName(), position, waitTime));
            }
            final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithJsonArray(jsonArray);
            return Response.status(HttpCode.OK.getCode())
                    .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries)))
                    .build();
        } catch (final UserNotAuthorizedException ex) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

    }

    /**
     * REST endpoint to consume orders.
     *
     * @return whether or not this consumption is successful.
     */
    @GET
    @Path("/consume")
    @RolesAllowed({ "EMPLOYEE", "ADMIN" })
    // http://localhost:8080/storehouse/api/orders/consume
    public Response consumeOrder() {
        logger.info("Consume orders from the queue");

        final JsonArray jsonArray = new JsonArray();
        List<Order> orders;
        try {
            orders = orderEventReceiver.receiveOrder();
            logger.info("Consume orders: {}", orders);
            for (final Order order : orders) {
                // check the database if this order is not RESERVED
                try {
                    final OrderStatus currentStatus = orderServices.findById(order.getId()).getCurrentStatus();
                    if (currentStatus == OrderStatus.RESERVED) {
                        logger.info("Order {} is RESERVED, we will deliver it now", order);

                        // delivery

                        // make this order as PENDING state
                        try {
                            orderServices.updateStatus(order.getId(), OrderStatus.PENDING);
                            logger.info("order id {} status changed to Pending", order.getId());
                        } catch (final Exception ex) {
                            logger.error("Failed to change the orderId {} to Pending state", order.getId());
                            throw ex;
                        }

                        final Long customerId = order.getCustomer().getId();
                        final String customerName = order.getCustomer().getName();
                        final String customerEmail = order.getCustomer().getEmail();
                        final Integer quantity = order.calculateTotalQuantity();
                        jsonArray.add(converter.convertDeliveryToJsonElement(customerId,
                                customerName, customerEmail, quantity));
                    } else {
                        logger.info("This order id {} could be cancelled or delivered, ignore", order.getId());
                    }
                } catch (final OrderNotFoundException ex) {
                    logger.info("No Order found for id: {}", order.getId());
                    return Response.status(HttpCode.NOT_FOUND.getCode())
                            .entity(OperationResultJsonWriter.toJson(getOperationResultNotFound(RM))).build();
                }

            }
            // construct the JSON response
            final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithJsonArray(jsonArray);
            return Response.status(HttpCode.OK.getCode())
                    .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries)))
                    .build();
        } catch (final JMSException ex) {
            ex.printStackTrace();
        }
        return Response.status(HttpCode.OK.getCode()).build();
    }
}
