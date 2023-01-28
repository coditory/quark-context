package annotated.samples.beans_lifecycle

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Init

@Bean
class Bar {
    static boolean initialized = false
    static boolean finalized = false

    @Init
    void init() {
        Bar.initialized = true
    }

    @Close
    void close() {
        Bar.finalized = true
    }
}
