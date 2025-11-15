package annotated.samples.slow_beans_annotated

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration

@Configuration
class ConfigInit {
    @Bean(creationTimeMs = 100)
    Bar bar() {
        Thread.sleep(800)
        return new Bar()
    }

    @Bean
    Baz baz(Bar bar) {
        return new Baz(bar)
    }

    @Bean(creationTimeMs = 1100)
    Foo foo(Baz baz) {
        Thread.sleep(1_000)
        return new Foo(baz)
    }
}
