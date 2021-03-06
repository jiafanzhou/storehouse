package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * If an user already exists in the system.
 * 
 * @author ejiafzh
 *
 */
@ApplicationException
public class UserAlreadyExistingException extends RuntimeException {
    private static final long serialVersionUID = -5380698745152600907L;
}
