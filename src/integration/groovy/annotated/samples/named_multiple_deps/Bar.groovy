package annotated.samples.named_multiple_deps

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Dependency

@Bean
class Bar {
    final List<Foo> foo

    Bar(@Dependency(name = "what") List<Foo> foo) {
        this.foo = foo
    }
}
