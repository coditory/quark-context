package annotated.samples.beans_lifecycle

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.annotations.Init
import com.coditory.quark.context.annotations.Inject

import static java.util.Objects.requireNonNull

@Configuration
class ConfigLifecycle {
    private final Bar bar
    boolean initialized = false
    boolean initialized2 = false
    boolean finalized = false
    boolean finalized2 = false

    @Inject
    ConfigLifecycle(Bar bar) {
        this.bar = bar
    }

    @Bean
    Baz baz() {
        return new Baz(bar);
    }

    @Init
    void init(Foo foo) {
        requireNonNull(foo)
        initialized = true
    }

    @Init
    void init2() {
        initialized2 = true
    }

    @Close
    void close(Foo2 foo) {
        requireNonNull(foo)
        finalized = true
    }

    @Close
    void close2() {
        finalized2 = true
    }
}
