package com.coditory.quark.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface ConditionalOnProperty {
    String[] value() default {};

    String[] name() default {};

    String havingValue() default "true";

    boolean matchIfMissing() default false;
}
