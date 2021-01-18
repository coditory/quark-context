package annotated.samples.beans_lifecycle

import com.coditory.quark.context.Bean
import com.coditory.quark.context.Close
import com.coditory.quark.context.Init

@Bean
class Bar {
    boolean initialized = false
    boolean finalized = false

    @Init
    void init() {
        initialized = true
    }

    @Close
    void close() {
        finalized = true
    }
}
