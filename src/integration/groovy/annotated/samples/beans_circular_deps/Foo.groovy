package annotated.samples.beans_circular_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Foo {
    final Bar bar

    Foo(Bar bar) {
        this.bar = requireNonNull(bar)
    }
}
