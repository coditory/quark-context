package com.coditory.quark.context;

public class BeanFinalizationException extends RuntimeException {
    public BeanFinalizationException(String message) {
        super(message);
    }

    public BeanFinalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
