package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * This exception represents if an order already exists.
 *
 * We only allow one order to be RESERVED per client.
 *
 * @author ejiafzh
 *
 */
@ApplicationException
public class OrderAlreadyExistingException extends RuntimeException {
    private static final long serialVersionUID = -2625097221992980564L;

    /**
     * Constructs if an order already exists.
     *
     * @param message
     *            exception message
     */
    public OrderAlreadyExistingException(final String message) {
        super(message);
    }
}
