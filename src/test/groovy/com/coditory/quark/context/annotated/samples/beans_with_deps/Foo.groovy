package com.coditory.quark.context.annotated.samples.beans_with_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Foo {
    private final Baz baz

    Foo(Baz baz) {
        this.baz = requireNonNull(baz)
    }
}
