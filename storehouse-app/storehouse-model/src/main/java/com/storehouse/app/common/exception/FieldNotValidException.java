package com.storehouse.app.common.exception;

import javax.ejb.ApplicationException;

/**
 * Runtime exception is wrapped by EJBException. 
 * 
 * By using @ApplicationException, it will not be wrapped.
 *
 * @author ejiafzh
 *
 */
@ApplicationException
public class FieldNotValidException extends RuntimeException {
    private static final long serialVersionUID = 4525821332583716666L;

    private final String fieldName;
    private final Object fieldValue;

    public FieldNotValidException(final String fieldName,
            final Object fieldValue, final String message) {
        super(message);
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getFieldName() {
        return fieldName;
    }

    @Override
    public String toString() {
        return "FieldNotValidException [fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", errorMsg="
                + getMessage() + "]";
    }

}
