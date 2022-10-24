package com.coditory.quark.context.base


import com.coditory.quark.eventbus.UnhandledEvent

class InMemUnhandledEventListener extends InMemEventListener<UnhandledEvent> {
    InMemUnhandledEventListener() {
        super()
    }

    InMemUnhandledEventListener(String name) {
        super(name)
    }

    List<Object> getUnwrappedEvents() {
        return events.collect { it.event() }
    }
}
