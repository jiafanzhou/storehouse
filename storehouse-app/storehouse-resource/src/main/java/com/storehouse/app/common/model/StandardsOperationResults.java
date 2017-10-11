package com.storehouse.app.common.model;

import com.storehouse.app.common.exception.FieldNotValidException;

/**
 * Standard operational results, a utility class to easily construct an operation result.
 *
 * @author ejiafzh
 *
 */
public final class StandardsOperationResults {

    private StandardsOperationResults() {
    }

    /**
     * {@inheritDoc}
     */
    public static OperationResult getOperationResultExistent(final ResourceMessage rm, final String fieldName) {
        return OperationResult.error(rm.getKeyResourceExistent(), rm.getMsgResourceExistent(fieldName));
    }

    /**
     * {@inheritDoc}
     */
    public static OperationResult getOperationResultInvalidField(final ResourceMessage rm,
            final FieldNotValidException ex) {
        return OperationResult.error(rm.getKeyResourceInvalidField(ex.getFieldName()),
                rm.getMsgResourceInvalidField(ex.getFieldName(), ex.getMessage()));
    }

    /**
     * {@inheritDoc}
     */
    public static OperationResult getOperationResultNotFound(final ResourceMessage rm) {
        return OperationResult.error(rm.getKeyResourceNotFound(), rm.getMsgResourceNotFound());
    }

    /**
     * {@inheritDoc}
     */
    public static OperationResult getOperationResultDependencyNotFound(final ResourceMessage rm,
            final String invalidFieldName) {
        return OperationResult.error(rm.getKeyResourceInvalidField(invalidFieldName),
                rm.getMsgResourceDepNotFound(invalidFieldName));
    }

    /**
     * {@inheritDoc}
     */
    public static OperationResult getOperationResultClientOrderAlreadyExists(final ResourceMessage rm,
            final String invalidFieldName, final Long clientId) {
        return OperationResult.error(rm.getKeyResourceInvalidField(invalidFieldName),
                "There is already an order in the queue for this ClientID " + clientId);
    }
}
