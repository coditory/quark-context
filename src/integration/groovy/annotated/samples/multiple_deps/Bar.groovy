package annotated.samples.multiple_deps

import com.coditory.quark.context.annotations.Bean

@Bean
class Bar {
    final List<Foo> foo

    Bar(List<Foo> foo) {
        this.foo = foo
    }
}
