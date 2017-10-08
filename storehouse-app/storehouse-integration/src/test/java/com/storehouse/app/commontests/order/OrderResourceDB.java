package com.storehouse.app.commontests.order;

import static com.storehouse.app.commontests.order.OrderForTestsRepository.*;

import com.storehouse.app.common.utils.DateUtils;
import com.storehouse.app.order.model.Order;
import com.storehouse.app.order.model.Order.OrderStatus;
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
    public void addAll() {
        final Order order1 = normalizeDependencies(orderReservedJohnDoe(), em);
        order1.setCreatedAt(DateUtils.getAsDateTime("2017-10-10T10:00:00Z"));
        orderServices.add(order1);
        final Order order2 = normalizeDependencies(orderReservedEndaKenny(), em);
        order2.setCreatedAt(DateUtils.getAsDateTime("2017-10-11T10:00:00Z"));
        orderServices.add(order2);
        orderServices.updateStatus(order2.getId(), OrderStatus.CANCELLED);
    }
}
