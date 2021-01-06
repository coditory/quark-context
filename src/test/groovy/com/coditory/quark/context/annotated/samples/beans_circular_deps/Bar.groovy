package com.coditory.quark.context.annotated.samples.beans_circular_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Bar {
    private final Baz baz

    Bar(Baz baz) {
        this.baz = requireNonNull(baz)
    }
}
