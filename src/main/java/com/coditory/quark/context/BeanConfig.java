package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Bean;
import com.coditory.quark.context.annotations.Configuration;

public record BeanConfig(boolean eager, int creationTimeMs, int creationTotalTimeMs) {
    static final BeanConfig DEFAULT = new BeanConfig(false, -1, -1);

    static BeanConfig fromAnnotationOrDefault(Bean annotation) {
        if (annotation == null) return DEFAULT;
        return new BeanConfig(annotation.eager(), annotation.creationTimeMs(), annotation.creationTotalTimeMs());
    }

    static BeanConfig fromAnnotationOrDefault(Configuration annotation) {
        if (annotation == null) return DEFAULT;
        return new BeanConfig(annotation.eager(), annotation.creationTimeMs(), annotation.creationTotalTimeMs());
    }

    BeanConfig withEager(boolean eager) {
        return new BeanConfig(eager, creationTimeMs, creationTotalTimeMs);
    }
}