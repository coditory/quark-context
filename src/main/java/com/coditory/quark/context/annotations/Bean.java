package com.coditory.quark.context.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface Bean {
    /**
     * Bean name
     */
    String value() default "";

    /**
     * Bean name
     */
    String name() default "";

    /**
     * Create bean eagerly, without waiting for bean to be queries from the context.
     */
    boolean eager() default false;

    /**
     * Report bean creation time as slow after creationTimeMs.
     * Special values:
     * - 0 - always report
     * - -1 - use context wide threshold
     * - -2 - never report
     */
    int creationTimeMs() default -1;

    /**
     * Report bean creation time with all dependencies as slow after creationTotalTimeMs.
     * Special values:
     * - 0 - always report
     * - -1 - use context wide threshold
     * - -2 - never report
     */
    int creationTotalTimeMs() default -1;
}
