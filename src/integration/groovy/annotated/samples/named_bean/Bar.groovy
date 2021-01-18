package annotated.samples.named_bean

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Dependency

@Bean
class Bar {
    final Foo foo
    final Baz baz

    Bar(
            @Dependency(name = "fooo") Foo foo,
            @Dependency(name = "baaz", required = false) Baz baz
    ) {
        this.foo = foo
        this.baz = baz
    }
}
