package com.storehouse.app.common.utils;

import com.storehouse.app.common.exception.FieldNotValidException;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

/**
 * Utility class to perform bean validation.
 *
 * @author ejiafzh
 *
 */
public final class ValidationUtils {

    /**
     * Bean validation method for the given entity.
     * 
     * @param validator
     *            the bean validator
     * @param entity
     *            to be validated against.
     */
    public static <T> void validateEntityFields(final Validator validator, final T entity) {
        final Set<ConstraintViolation<T>> errors = validator.validate(entity);
        errors.forEach(e -> {
            throw new FieldNotValidException(e.getPropertyPath().toString(),
                    e.getInvalidValue(), e.getMessage());
        });
    }
}
