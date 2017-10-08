package com.storehouse.app.commontests.utils;

import org.junit.Ignore;

@Ignore
public enum ResourceDefinitions {
    USER("users"), ORDER("orders");

    private String resouceName;

    private ResourceDefinitions(final String resourceName) {
        this.resouceName = resourceName;
    }

    public String getResourceName() {
        return resouceName;
    }
}
