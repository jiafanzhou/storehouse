package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class UserNotFoundException extends RuntimeException {
    private static final long serialVersionUID = 8864816740973373324L;

}
