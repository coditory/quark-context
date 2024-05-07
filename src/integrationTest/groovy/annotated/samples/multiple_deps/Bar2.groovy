package annotated.samples.multiple_deps

import com.coditory.quark.context.annotations.Bean

@Bean
class Bar2 {
    final List<Foo> foo

    Bar2(List<? extends Foo> foo) {
        this.foo = foo
    }
}
