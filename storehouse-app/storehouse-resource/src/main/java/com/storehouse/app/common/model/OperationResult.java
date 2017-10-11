package com.storehouse.app.common.model;

/**
 * Operational result, which includes success or error and its entity if necessary.
 *
 * @author ejiafzh
 *
 */
public class OperationResult {
    private boolean success;
    private String errorIdentification;
    private String errorDescription;
    private Object entity;

    private OperationResult(final Object entity) {
        this.success = true;
        this.entity = entity;
    }

    private OperationResult(final String errorIdentification, final String errorDescription) {
        this.success = false;
        this.errorIdentification = errorIdentification;
        this.errorDescription = errorDescription;
    }

    /**
     * Constructs an entity for success.
     *
     * @param entity
     *            the entity to be constructed.
     * @return a constructed success operational result.
     */
    public static OperationResult success(final Object entity) {
        return new OperationResult(entity);
    }

    /**
     * Constructs a success without an entity
     *
     * @return a constructed success operational result.
     */
    public static OperationResult success() {
        return new OperationResult(null);
    }

    /**
     * Constructs an error result with error identification code and description.
     *
     * @param errorIdentification
     *            identification code of the error
     * @param errorDescription
     *            description of the error
     * @return a constructed error operational result.
     */
    public static OperationResult error(final String errorIdentification, final String errorDescription) {
        return new OperationResult(errorIdentification, errorDescription);
    }

    /**
     * If this operational result is success.
     *
     * @return true if it is success, false otherwise.
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Get the entity of this operational result.
     *
     * @return entity wrapped in this result.
     */
    public Object getEntity() {
        return entity;
    }

    /**
     * Get the error identification code.
     *
     * @return the error identification code.
     */
    public String getErrorIdentification() {
        return errorIdentification;
    }

    /**
     * Get the error description.
     *
     * @return the error description.
     */
    public String getErrorDescription() {
        return errorDescription;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "OperationResult [success=" + success + ", errorIdentification=" + errorIdentification
                + ", errorDescription=" + errorDescription + ", entity=" + entity + "]";
    }
}
