package com.storehouse.app.common.exception;

public class OrderStatusCannotBeChangedException extends RuntimeException {
    private static final long serialVersionUID = -6917792777930014762L;

    public OrderStatusCannotBeChangedException(final String message) {
        super(message);
    }
}
