package com.coditory.quark.context;

public class ContextException extends RuntimeException {
    public ContextException(String message) {
        super(message);
    }

    public ContextException(String message, Throwable cause) {
        super(message, cause);
    }
}
