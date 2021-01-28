package annotated.samples.named_multiple_deps

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Dependency

@Bean
class Bar {
    final List<Foo> foo

    Bar(@Dependency(name = "what") List<Foo> foo) {
        this.foo = foo
    }
}
