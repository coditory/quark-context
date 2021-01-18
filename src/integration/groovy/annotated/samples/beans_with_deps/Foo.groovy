package annotated.samples.beans_with_deps

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Inject

import static java.util.Objects.requireNonNull

@Bean
class Foo {
    final Baz baz

    @Inject
    Foo(Baz baz) {
        this.baz = requireNonNull(baz)
    }

    Foo(Bar bar) {
        throw new IllegalStateException("This constructor should not be used")
    }
}
