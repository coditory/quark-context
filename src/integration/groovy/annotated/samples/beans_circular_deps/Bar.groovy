package annotated.samples.beans_circular_deps

import com.coditory.quark.context.Bean

import static java.util.Objects.requireNonNull

@Bean
class Bar {
    final Baz baz

    Bar(Baz baz) {
        this.baz = requireNonNull(baz)
    }
}
