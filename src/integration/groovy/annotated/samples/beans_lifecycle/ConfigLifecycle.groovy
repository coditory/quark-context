package annotated.samples.beans_lifecycle

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Close
import com.coditory.quark.context.Configuration
import com.coditory.quark.context.Init
import com.coditory.quark.context.Inject

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
