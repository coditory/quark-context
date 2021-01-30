package annotated.samples.config_init

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.annotations.Init

@Configuration
class ConfigInit {
    static boolean initialized = false
    static Bar bar

    @Init
    void initConfig(Bar bar) {
        ConfigInit.initialized = true
        ConfigInit.bar = bar
    }

    @Bean
    Baz baz() {
        return new Baz(bar)
    }
}
