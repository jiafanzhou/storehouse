package com.storehouse.app.common.model;

import com.storehouse.app.common.exception.FieldNotValidException;

public final class StandardsOperationResults {

    private StandardsOperationResults() {
    }

    public static OperationResult getOperationResultExistent(final ResourceMessage rm, final String fieldName) {
        return OperationResult.error(rm.getKeyResourceExistent(), rm.getMsgResourceExistent(fieldName));
    }

    public static OperationResult getOperationResultInvalidField(final ResourceMessage rm,
            final FieldNotValidException ex) {
        return OperationResult.error(rm.getKeyResourceInvalidField(ex.getFieldName()),
                rm.getMsgResourceInvalidField(ex.getFieldName(), ex.getMessage()));
    }

    public static OperationResult getOperationResultNotFound(final ResourceMessage rm) {
        return OperationResult.error(rm.getKeyResourceNotFound(), rm.getMsgResourceNotFound());
    }

    public static OperationResult getOperationResultDependencyNotFound(final ResourceMessage rm,
            final String invalidFieldName) {
        return OperationResult.error(rm.getKeyResourceInvalidField(invalidFieldName),
                rm.getMsgResourceDepNotFound(invalidFieldName));
    }
}
