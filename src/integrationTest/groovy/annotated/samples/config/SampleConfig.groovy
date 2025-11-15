package annotated.samples.config

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.annotations.Dependency

@Configuration
class SampleConfig {
    @Bean
    Bar bar() {
        return new Bar()
    }

    @Bean
    Foo foo(Bar bar) {
        return new Foo("no-name", bar)
    }

    @Bean("foooo")
    Foo foooo(Bar bar) {
        return new Foo("foooo", bar)
    }

    @Bean("foo2")
    Foo foo2(
            Bar bar,
            @Dependency("foooo") Foo foooo,
            @Dependency(optional = true) Baz baz
    ) {
        return new Foo("foo2", bar)
    }
}
