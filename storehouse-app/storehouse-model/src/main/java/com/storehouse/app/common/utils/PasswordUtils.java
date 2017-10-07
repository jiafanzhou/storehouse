package com.storehouse.app.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Before adding a password to the database, we want to encrypt it first.
 *
 * @author ejiafzh
 *
 */
public final class PasswordUtils {

    private PasswordUtils() {
    }

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
