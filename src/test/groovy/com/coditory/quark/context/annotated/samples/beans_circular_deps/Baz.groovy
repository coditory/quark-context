package com.coditory.quark.context.annotated.samples.beans_circular_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Baz {
    private final Foo foo

    Baz(Foo foo) {
        this.foo = requireNonNull(foo)
    }
}
