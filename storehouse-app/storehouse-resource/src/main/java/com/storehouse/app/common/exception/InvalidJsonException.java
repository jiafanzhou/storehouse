package com.storehouse.app.common.exception;

/**
 * If Json is invalid, this exception will be thrown.
 * 
 * @author ejiafzh
 *
 */
public class InvalidJsonException extends RuntimeException {
    private static final long serialVersionUID = 6087454351913028554L;

    /**
     * {@inheritDoc}
     */
    public InvalidJsonException(final String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public InvalidJsonException(final Throwable throwable) {
        super(throwable);
    }
}
