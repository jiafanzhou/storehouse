package com.storehouse.app.common.model;

/**
 * HTTP code used by this storehouse application.
 * @author ejiafzh
 *
 */
public enum HttpCode {
    OK(200), 
    CREATED(201), 
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404), 
    VALIDATION_ERROR(422), 
    INTERNAL_ERR(500); 

    private int code;

    private HttpCode(final int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
