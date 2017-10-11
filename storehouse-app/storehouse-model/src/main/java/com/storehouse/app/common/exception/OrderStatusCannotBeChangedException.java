package com.storehouse.app.common.exception;

/**
 * If an order status cannot be changed.
 *
 * Once the OrderStatus reaches DELIVERED or CANCELLED, it can no longer be changed.
 *
 * @author ejiafzh
 *
 */
public class OrderStatusCannotBeChangedException extends RuntimeException {
    private static final long serialVersionUID = -6917792777930014762L;

    public OrderStatusCannotBeChangedException(final String message) {
        super(message);
    }
}
