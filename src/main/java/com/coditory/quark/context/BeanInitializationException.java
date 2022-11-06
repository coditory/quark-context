package com.coditory.quark.context;

public class BeanInitializationException extends RuntimeException {
    BeanInitializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
