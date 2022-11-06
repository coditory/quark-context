package com.coditory.quark.context;

public final class ContextException extends RuntimeException {
    ContextException(String message) {
        super(message);
    }

    ContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
