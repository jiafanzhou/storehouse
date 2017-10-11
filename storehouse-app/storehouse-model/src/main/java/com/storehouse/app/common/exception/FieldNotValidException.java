package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * Runtime exception is wrapped by EJBException.
 *
 * By using @ApplicationException, it will not be wrapped.
 *
 * This exception is thrown if any bean validation fails for the filed.
 *
 * @author ejiafzh
 *
 */
@ApplicationException
public class FieldNotValidException extends RuntimeException {
    private static final long serialVersionUID = 4525821332583716666L;

    private final String fieldName;
    private final Object fieldValue;

    /**
     * Constructs a FieldNotValidException based on the name, value and message.
     *
     * @param fieldName
     *            name of the field
     * @param fieldValue
     *            value of the field
     * @param message
     *            message to be thrown
     */
    public FieldNotValidException(final String fieldName,
            final Object fieldValue, final String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    /**
     * Get the name of the field.
     *
     * @return
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "FieldNotValidException [fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", errorMsg="
                + getMessage() + "]";
    }

}
