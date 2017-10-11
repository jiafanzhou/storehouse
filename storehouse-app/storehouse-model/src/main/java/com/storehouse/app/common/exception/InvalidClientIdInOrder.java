package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * This exception is thrown is there is an invalid CLientId placed in order.
 *
 * This could mean either the ClientId is null (not set), or it does not match
 * the logged in user's clientId.
 *
 * @author ejiafzh
 *
 */
@ApplicationException
public class InvalidClientIdInOrder extends RuntimeException {
    private static final long serialVersionUID = 7996554213777423993L;

    /**
     * Constructs this exception based on the message.
     * 
     * @param message
     *            the message of this exception.
     */
    public InvalidClientIdInOrder(final String message) {
        super(message);
    }
}
