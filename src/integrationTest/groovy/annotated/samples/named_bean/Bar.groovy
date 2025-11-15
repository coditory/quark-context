package annotated.samples.named_bean

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Dependency

@Bean
class Bar {
    final Foo foo
    final Baz baz

    Bar(
            @Dependency(name = "fooo") Foo foo,
            @Dependency(name = "baaz", optional = true) Baz baz
    ) {
        this.foo = foo
        this.baz = baz
    }
}
