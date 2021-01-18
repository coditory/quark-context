package com.coditory.quark.context;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

final class Args {
    private Args() {
        throw new UnsupportedOperationException("Do not instantiate utility class");
    }

    public static <T> T checkNonNull(@Nullable T value, String name) {
        if (value == null) {
            String message = message("Expected non-null value", name, null);
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
