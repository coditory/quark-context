package com.coditory.quark.context;

import com.coditory.quark.eventbus.EventEmitter;
import org.jetbrains.annotations.NotNull;

class DummyEventEmitter implements EventEmitter {
    @Override
    public void emit(@NotNull Object event) {
        // deliberately empty
    }
}
