package com.storehouse.app.order.services.impl;

import com.storehouse.app.order.model.Order;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class OrderEventReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // configures our JMS queue
    @Resource(mappedName = "java:/jms/queue/Orders")
    private Queue ordersQueue;

    @Inject
    private JMSContext jmsContext;

    public List<Order> receiveOrder() throws JMSException {
        final List<Order> consumedOrders = new ArrayList<>();

        final QueueBrowser queueBrowser = jmsContext.createBrowser(ordersQueue);
        int currentCapacity = 0;
        int orderCountToConsume = 0;
        final Enumeration<ObjectMessage> messages = queueBrowser.getEnumeration();
        final JMSConsumer jmsConsumer = jmsContext.createConsumer(ordersQueue);
        while (messages.hasMoreElements()) {
            final ObjectMessage message = messages.nextElement();
            final Order orderToCheck = (Order) message.getObject();
            if (currentCapacity == 0 && orderToCheck.calculateTotalQuantity() >= 25) {
                // means next is a large order
                orderCountToConsume = 1;
                break;
            } else if (currentCapacity + orderToCheck.calculateTotalQuantity() <= 25) {
                currentCapacity += orderToCheck.calculateTotalQuantity();
                orderCountToConsume++;
            } else {
                break;
            }
        }

        logger.info("We will consume {} orders from the queue", orderCountToConsume);

        for (int i = 0; i < orderCountToConsume; i++) {
            final Order order = jmsConsumer.receiveBody(Order.class, 0);
            logger.info("Order received: {}", order);
            consumedOrders.add(order);
        }

        return consumedOrders;
    }
}
