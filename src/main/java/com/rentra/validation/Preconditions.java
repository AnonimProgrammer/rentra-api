package com.rentra.validation;

public final class Preconditions {
    private Preconditions() {}

    public static String required(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
