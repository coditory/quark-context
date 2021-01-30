package annotated.samples.config_init

import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.annotations.Init

@Configuration(eager = true)
class ConfigEagerInit {
    public static boolean initialized = false
    public static Bar bar

    @Init
    void initConfig(Bar bar) {
        ConfigEagerInit.initialized = true
        ConfigEagerInit.bar = bar
    }
}
