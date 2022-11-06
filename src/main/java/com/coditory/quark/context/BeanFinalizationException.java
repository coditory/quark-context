package com.coditory.quark.context;

public final class BeanFinalizationException extends RuntimeException {
    BeanFinalizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
