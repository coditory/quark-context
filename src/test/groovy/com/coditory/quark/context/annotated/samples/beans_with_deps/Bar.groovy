package com.coditory.quark.context.annotated.samples.beans_with_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Bar {
    private final Baz baz
    private final Foo foo

    Bar(Baz baz, Foo foo) {
        this.baz = requireNonNull(baz)
        this.foo = requireNonNull(foo)
    }
}
