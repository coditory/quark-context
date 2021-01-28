package annotated.samples.config_inject

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Inject
import com.coditory.quark.context.annotations.Configuration

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
