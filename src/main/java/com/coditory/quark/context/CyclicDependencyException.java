package com.coditory.quark.context;

public class CyclicDependencyException extends RuntimeException {
    public CyclicDependencyException(String message) {
        super(message);
    }

    public CyclicDependencyException(String message, Throwable cause) {
        super(message, cause);
    }
}
