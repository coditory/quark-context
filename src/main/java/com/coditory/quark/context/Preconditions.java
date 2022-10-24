package com.coditory.quark.context;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class Preconditions {
    private Preconditions() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static void expect(boolean check, String message) {
        if (!check) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> T expectNonNull(@Nullable T value, String name) {
        if (value == null) {
            String message = message("Expected non-null value", name, null);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    public static String expectNonBlank(@Nullable String value, String name) {
        if (value == null || value.isBlank()) {
            String message = message("Expected non-null and non-blank value", name, null);
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static String message(String expectation, String fieldName, Object value) {
        String field = fieldName != null ? (": " + fieldName) : "";
        String stringValue = value instanceof String
                ? ("\"" + value + "\"")
                : Objects.toString(value);
        return expectation + field + ". Got: " + stringValue;
    }
}
