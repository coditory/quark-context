package annotated.samples.optional_multiple_deps

import com.coditory.quark.context.annotations.Bean

@Bean
class Baz {
    final List<Foo> foo

    Baz(List<Foo> foo) {
        this.foo = foo
    }
}
