package annotated.samples.slow_beans

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration

@Configuration
class ConfigInit {
    @Bean
    annotated.samples.slow_beans.Bar bar() {
        Thread.sleep(1_000)
        return new annotated.samples.slow_beans.Bar()
    }

    @Bean
    annotated.samples.slow_beans.Baz baz(annotated.samples.slow_beans.Bar bar) {
        return new annotated.samples.slow_beans.Baz(bar)
    }

    @Bean
    annotated.samples.slow_beans.Foo foo(annotated.samples.slow_beans.Baz baz) {
        Thread.sleep(1_000)
        return new annotated.samples.slow_beans.Foo(baz)
    }
}
