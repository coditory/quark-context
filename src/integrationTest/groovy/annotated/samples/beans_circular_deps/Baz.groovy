package annotated.samples.beans_circular_deps

import com.coditory.quark.context.annotations.Bean

import static java.util.Objects.requireNonNull

@Bean
class Baz {
    final Foo foo

    Baz(Foo foo) {
        this.foo = requireNonNull(foo)
    }
}
