package annotated.samples.slow_beans

import static java.util.Objects.requireNonNull

class Foo {
    final Baz baz

    Foo(Baz baz) {
        this.baz = requireNonNull(baz)
    }

    Foo(Bar bar) {
        throw new IllegalStateException("This constructor should not be used")
    }
}
