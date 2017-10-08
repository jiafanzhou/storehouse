package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class OrderNotFoundException extends RuntimeException {
    private static final long serialVersionUID = -8525267218589190691L;

}
