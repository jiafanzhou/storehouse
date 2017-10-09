package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class InvalidClientIdInOrder extends RuntimeException {
    private static final long serialVersionUID = 7996554213777423993L;

    public InvalidClientIdInOrder(final String message) {
        super(message);
    }
}
