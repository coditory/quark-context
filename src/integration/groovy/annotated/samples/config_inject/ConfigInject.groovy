package annotated.samples.config_inject

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Inject
import com.coditory.quark.context.Configuration

@Configuration
class ConfigInject {
    private final Bar bar

    @Inject
    ConfigInject(Bar bar) {
        this.bar = bar
    }

    @Bean
    Baz baz() {
        return new Baz(bar);
    }
}
