package com.storehouse.app.common.utils;

import com.storehouse.app.common.exception.FieldNotValidException;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

public final class ValidationUtils {

    public static <T> void validateEntityFields(final Validator validator, final T entity) {
        final Set<ConstraintViolation<T>> errors = validator.validate(entity);
        errors.forEach(e -> {
            throw new FieldNotValidException(e.getPropertyPath().toString(),
                    e.getInvalidValue(), e.getMessage());
        });
    }
}
