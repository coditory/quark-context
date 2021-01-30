package annotated.samples.beans_lifecycle

import com.coditory.quark.context.annotations.Close
import com.coditory.quark.context.annotations.Init

class Baz {
    static boolean initialized = false
    static boolean finalized = false
    final Bar bar

    Baz(Bar bar) {
        this.bar = bar
    }

    @Init
    void init() {
        Baz.initialized = true
    }

    @Close
    void close() {
        Baz.finalized = true
    }
}
