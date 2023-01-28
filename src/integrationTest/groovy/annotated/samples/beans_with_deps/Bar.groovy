package annotated.samples.beans_with_deps

import com.coditory.quark.context.annotations.Bean

import static java.util.Objects.requireNonNull

@Bean
class Bar {
    final Baz baz
    final Foo foo

    Bar(Baz baz, Foo foo) {
        this.baz = requireNonNull(baz)
        this.foo = requireNonNull(foo)
    }
}
