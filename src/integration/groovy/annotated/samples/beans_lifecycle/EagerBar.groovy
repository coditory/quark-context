package annotated.samples.beans_lifecycle

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Init;

@Bean(eager = true)
class EagerBar {
    static boolean initialized = false
    static boolean finalized = false

    @Init
    void init() {
        EagerBar.initialized = true
    }

    @Close
    void close() {
        EagerBar.finalized = true
    }
}
