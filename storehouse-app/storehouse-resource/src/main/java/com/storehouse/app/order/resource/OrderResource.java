package com.storehouse.app.order.resource;

import static com.storehouse.app.common.model.StandardsOperationResults.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.storehouse.app.common.exception.FieldNotValidException;
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
        }

        logger.info("Returning the operation result after adding order: {}", result);

        // convert the OperationResult object to Json
        return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
    }

    private OrderStatus getStatusFromJson(final String body) {
        final JsonObject jsonObject = JsonReader.readAsJsonObject(body);
        return OrderStatus.valueOf(JsonReader.getStringOrNull(jsonObject, "status"));
    }

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
        final Order order = data.getRows().get(0);
        return addStatus(order.getId(), String.format("{\"status\": \"%s\"}", OrderStatus.CANCELLED));
    }

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

    // jiafanz: document this
    @GET
    @Path("/stats/{id}")
    // http://localhost:8080/storehouse/api/orders/stats/{id}
    public Response getOrderStats(@PathParam("id") final Long customerId) {
        logger.info("Getting order statistics in the queue");

        // need to check if customer if exists
        // ensure customerId exists ...

        try {
            userServices.findById(customerId);
        } catch (final UserNotFoundException ex) {
            logger.error("Customer cannot be found for clientId", ex);
            final HttpCode httpCode = HttpCode.VALIDATION_ERROR;
            final OperationResult result = getOperationResultDependencyNotFound(RM, "customer");
            return Response.status(httpCode.getCode()).entity(OperationResultJsonWriter.toJson(result)).build();
        }

        ResponseBuilder rb;
        final Integer position = orderServices.checkOrderPositionInQueueByCustomerId(customerId);
        final Integer waitTime = orderServices.checkOrderWaitTimeInQueueByCustomerId(customerId);
        final OperationResult result = OperationResult
                .success(converter.convertQueueStatsToJsonElement(customerId, position, waitTime));
        final String tmp = OperationResultJsonWriter.toJson(result);
        rb = Response.status(HttpCode.OK.getCode()).entity(tmp);
        logger.info("Queue position: {}, wait time: {}, for customerId {}", position, waitTime, customerId);
        return rb.build();
    }

    // jiafanz: document this
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

                jsonArray.add(converter.convertQueueStatsToJsonElement(customerId, position, waitTime));
            }
            final JsonElement jsonWithPagingAndEntries = JsonUtils.getJsonElementWithJsonArray(jsonArray);
            return Response.status(HttpCode.OK.getCode())
                    .entity(OperationResultJsonWriter.toJson(OperationResult.success(jsonWithPagingAndEntries)))
                    .build();
        } catch (final UserNotAuthorizedException ex) {
            return Response.status(HttpCode.FORBIDDEN.getCode()).build();
        }

    }

    @GET
    @Path("/consume")
    @RolesAllowed({ "EMPLOYEE", "ADMIN" })
    // http://localhost:8080/storehouse/api/orders/consume
    // jiafanz: document all the REST endpoints.
    // jiafanz: optimize this method
    public Response consumeOrder() {
        logger.info("Consume orders from the queue");
        List<Order> orders;
        try {
            orders = orderEventReceiver.receiveOrder();
            logger.info("Consume orders: {}", orders);
            for (final Order order : orders) {
                // check the database if this order is not RESERVED
                final OrderStatus currentStatus = orderServices.findById(order.getId()).getCurrentStatus();
                if (currentStatus == OrderStatus.RESERVED) {
                    logger.info("Order {} is RESERVED, we will deliver it now", order);

                    // delivery

                    // make this order as PENDING state
                    try {
                        orderServices.updateStatus(order.getId(), OrderStatus.PENDING);
                    } catch (final Exception ex) {
                        logger.error("Failed to change the orderId {} to Pending state", order.getId());
                    }
                } else
                    logger.info("This order id {} could be cancelled or delivered, ignore", order.getId());
            }
        } catch (final JMSException ex) {
            ex.printStackTrace();
        }
        return Response.status(HttpCode.OK.getCode()).build();
    }
}
