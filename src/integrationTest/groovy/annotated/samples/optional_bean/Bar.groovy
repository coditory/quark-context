package annotated.samples.optional_bean

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Dependency

@Bean
class Bar {
    final Foo foo;
    final Baz baz;

    Bar(@Dependency(required = false) Foo foo, @Dependency(required = false) Baz baz) {
        this.foo = foo
        this.baz = baz
    }
}
