package com.storehouse.app.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Before adding a password to the database, we need to encrypt it first.
 *
 * @author ejiafzh
 *
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

    /**
     * Utility function to encrypt the password before storing into database.
     *
     * We use the SHA-256 strength algorithm to encrypt the password.
     *
     * @param password
     *            the password to be encrypted.
     * @return encrypted version of the password.
     */
    public static String encryptPassword(final String password) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex);
        }
        md.update(password.getBytes());
        return Base64.getMimeEncoder().encodeToString(md.digest());
    }

    // public static void main(final String[] args) {
    // System.out.println(PasswordUtils.encryptPassword("12345678"));
    // }
}
