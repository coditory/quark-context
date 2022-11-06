package com.coditory.quark.context;

public final class CyclicDependencyException extends RuntimeException {
    CyclicDependencyException(String message) {
        super(message);
    }
}
