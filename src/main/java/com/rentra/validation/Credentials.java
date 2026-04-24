package com.rentra.validation;

import java.util.Map;

import com.rentra.exception.InvalidCredentialsException;

public final class Credentials {
    private Credentials() {}

    public static String requiredNormalized(Map<String, Object> credentials, String... keys) {
        return requiredRaw(credentials, keys).trim().toLowerCase();
    }

    public static String requiredRaw(Map<String, Object> credentials, String... keys) {
        for (String key : keys) {
            Object value = credentials.get(key);
            if (value instanceof String text && !text.isBlank()) {
                return text;
            }
        }
        throw new InvalidCredentialsException("Missing credential: " + String.join(" or ", keys));
    }

    public static String optionalRaw(Map<String, Object> credentials, String key) {
        Object value = credentials.get(key);
        if (value instanceof String text && !text.isBlank()) {
            return text;
        }
        return null;
    }
}
