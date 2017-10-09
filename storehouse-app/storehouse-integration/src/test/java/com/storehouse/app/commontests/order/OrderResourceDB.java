package com.storehouse.app.commontests.order;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;

import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.services.OrderServices;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Stateless
@Path("/DB/orders")
@Produces(MediaType.APPLICATION_JSON)
public class OrderResourceDB {
    @Inject
    private OrderServices orderServices;

    @PersistenceContext
    private EntityManager em;

    @POST
    @Path("/john")
    public void addJohnDoeOrder() {
        final Order order1 = normalizeDependencies(orderReservedJohnDoe(), em);
        order1.setCreatedAt(DateUtils.getAsDateTime("2017-10-10T10:00:00Z"));
        orderServices.add(order1);
    }

    @POST
    @Path("/enda")
    public void addEndaKennyOrder() {
        final Order order2 = normalizeDependencies(orderReservedEndaKenny(), em);
        order2.setCreatedAt(DateUtils.getAsDateTime("2017-10-11T10:00:00Z"));
        orderServices.add(order2);
    }

    @POST
    @Path("/donald")
    public void addDonaldTrumpOrder() {
        final Order order3 = normalizeDependencies(orderReservedDonaldTrump(), em);
        order3.setCreatedAt(DateUtils.getAsDateTime("2017-10-12T10:00:00Z"));
        orderServices.add(order3);
    }
}
