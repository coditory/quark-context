package annotated.samples.beans_lifecycle

import com.coditory.quark.context.Close
import com.coditory.quark.context.Init

class Baz {
    boolean initialized = false
    boolean finalized = false
    final Bar bar

    Baz(Bar bar) {
        this.bar = bar
    }

    @Init
    void init() {
        initialized = true
    }

    @Close
    void close() {
        finalized = true
    }
}
