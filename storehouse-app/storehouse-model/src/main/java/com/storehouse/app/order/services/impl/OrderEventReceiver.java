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

/**
 * This is the Order event receiver.
 * Whenever orders are consumed, this receiver will be invoked to take orders from
 * the queue.
 *
 * @author ejiafzh
 *
 */
@Stateless
public class OrderEventReceiver {
    private Logger logger = LoggerFactory.getLogger(getClass());

    // configures our JMS queue
    @Resource(mappedName = "java:/jms/queue/Orders")
    private Queue ordersQueue;

    @Inject
    private JMSContext jmsContext;

    /**
     * Receive an order from the queue and process them.
     *
     * @return a list of the orders to be delivered.
     * @throws JMSException
     *             if any JMS exception occurs.
     */
    public List<Order> receiveOrder() throws JMSException {
        final List<Order> consumedOrders = new ArrayList<>();

        final Enumeration<ObjectMessage> messages = createQueueBrowser();
        final int orderCountToConsume = checkOrderCountToConsume(messages);

        logger.info("We will consume {} orders from the queue", orderCountToConsume);
        final JMSConsumer jmsConsumer = jmsContext.createConsumer(ordersQueue);
        consumeOrder(consumedOrders, orderCountToConsume, jmsConsumer);
        return consumedOrders;
    }

    // consume order from the queue
    private void consumeOrder(final List<Order> consumedOrders, final int orderCountToConsume,
            final JMSConsumer jmsConsumer) {
        for (int i = 0; i < orderCountToConsume; i++) {
            consumeAndAddToList(consumedOrders, jmsConsumer);
        }
    }

    // consume the item from the queue and add to list
    private void consumeAndAddToList(final List<Order> consumedOrders, final JMSConsumer jmsConsumer) {
        final Order order = jmsConsumer.receiveBody(Order.class, 0);
        logger.debug("Order received: {}", order);
        consumedOrders.add(order);
    }

    // check order count to be consumed
    private int checkOrderCountToConsume(final Enumeration<ObjectMessage> messages) throws JMSException {
        int currentCapacity = 0;
        int orderCountToConsume = 0;
        while (messages.hasMoreElements()) {
            final ObjectMessage message = messages.nextElement();
            final Order orderToCheck = (Order) message.getObject();
            if (currentCapacity == 0 && orderToCheck.calculateTotalQuantity() >= Order.MAX_LOAD) {
                // means next is a large order
                orderCountToConsume = 1;
                break;
            } else if (currentCapacity + orderToCheck.calculateTotalQuantity() <= Order.MAX_LOAD) {
                currentCapacity += orderToCheck.calculateTotalQuantity();
                orderCountToConsume++;
            } else {
                break;
            }
        }
        return orderCountToConsume;
    }

    // we need to browse the queue first to check how many items to retrieve
    @SuppressWarnings("unchecked")
    private Enumeration<ObjectMessage> createQueueBrowser() throws JMSException {
        final QueueBrowser queueBrowser = jmsContext.createBrowser(ordersQueue);
        final Enumeration<ObjectMessage> messages = queueBrowser.getEnumeration();
        return messages;
    }
}
