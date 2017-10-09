package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class OrderAlreadyExistingException extends RuntimeException {
    private static final long serialVersionUID = -2625097221992980564L;

    public OrderAlreadyExistingException(final String message) {
        super(message);
    }
}
