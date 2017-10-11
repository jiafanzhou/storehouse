package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * If user is not authorized to perform an action.
 * 
 * @author ejiafzh
 *
 */
@ApplicationException
public class UserNotAuthorizedException extends RuntimeException {

    private static final long serialVersionUID = -1449722059595947793L;

}
