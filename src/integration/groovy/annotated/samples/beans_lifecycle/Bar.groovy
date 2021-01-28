package annotated.samples.beans_lifecycle

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Init

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
