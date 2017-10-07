package com.storehouse.app.common.model;

public class ResourceMessage {
    private final String resource;

    private static final String KEY_EXISTENT = "%s.existent";
    private static final String MSG_EXISTENT = "There is already a %s for the given %s";
    private static final String KEY_INVALID_FIELD = "%s.invalidField.%s";
    private static final String MSG_INVALID_FIELD = "%s is not valid: %s";
    private static final String KEY_NOT_FOUND = "%s.notfound";
    private static final String MSG_NOT_FOUND = "%s not found";
    private static final String MSG_DEP_NOT_FOUND = "%s not found for the %s";

    public ResourceMessage(final String resource) {
        this.resource = resource;
    }

    public String getKeyResourceExistent() {
        return String.format(KEY_EXISTENT, resource);
    }

    public String getMsgResourceExistent(final String fieldName) {
        return String.format(MSG_EXISTENT, resource, fieldName);
    }

    public String getKeyResourceInvalidField(final String invalidField) {
        return String.format(KEY_INVALID_FIELD, resource, invalidField);
    }

    public String getMsgResourceInvalidField(final String invalidField, final String errorMsg) {
        return String.format(MSG_INVALID_FIELD, invalidField, errorMsg);
    }

    public String getKeyResourceNotFound() {
        return String.format(KEY_NOT_FOUND, resource);
    }

    public String getMsgResourceNotFound() {
        return String.format(MSG_NOT_FOUND, resource);
    }

    public String getMsgResourceDepNotFound(final String invalidField) {
        return String.format(MSG_DEP_NOT_FOUND, invalidField, resource);
    }
}
