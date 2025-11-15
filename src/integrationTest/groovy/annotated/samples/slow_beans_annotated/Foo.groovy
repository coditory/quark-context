package annotated.samples.slow_beans_annotated


import static java.util.Objects.requireNonNull

class Foo {
    final Baz baz

    Foo(Baz baz) {
        this.baz = requireNonNull(baz)
    }

    Foo(annotated.samples.slow_beans_annotated.Bar bar) {
        throw new IllegalStateException("This constructor should not be used")
    }
}
