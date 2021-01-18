package annotated.samples.config

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Dependency
import com.coditory.quark.context.Configuration

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
            @Dependency(required = false) Baz baz
    ) {
        return new Foo("foo2", bar)
    }
}
