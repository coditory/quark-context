package annotated.samples.config_post_init


import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.annotations.PostInit

@Configuration
class ConfigPostInit {
    static boolean postInitialized = false
    static Baz baz
    static Bar bar

    @PostInit
    void postInit(Baz baz, Bar bar) {
        ConfigPostInit.postInitialized = true
        ConfigPostInit.baz = baz
        ConfigPostInit.bar = bar
    }

    @Bean
    Baz baz() {
        return new Baz()
    }

    @Bean
    Foo foo() {
        return new Foo()
    }
}
