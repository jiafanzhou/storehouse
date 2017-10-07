package com.storehouse.app.commontests.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.core.MultivaluedMap;

public class HttpBasicAuthenticator implements ClientRequestFilter {

    private final String username;
    private final String password;

    public HttpBasicAuthenticator(final String username, final String password) {
        this.username = username; // use email for the username
        this.password = password;
    }

    /**
     * For every request, we add the header for authentication.
     */
    @Override
    public void filter(final ClientRequestContext requestContext) throws IOException {
        final MultivaluedMap<String, Object> headers = requestContext.getHeaders();
        headers.add("Authorization", getBasicAuthentication());
    }

    private Object getBasicAuthentication() {
        final String usernameAndPassword = username + ":" + password;
        try {
            return "Basic " + Base64.getMimeEncoder().encodeToString(
                    usernameAndPassword.getBytes("UTF-8"));
        } catch (final UnsupportedEncodingException ex) {
            throw new IllegalStateException("Error while converting using UTF-8", ex);
        }
    }

}
