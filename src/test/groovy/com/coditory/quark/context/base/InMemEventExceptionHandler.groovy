package com.coditory.quark.context.base

import com.coditory.quark.eventbus.DispatchExceptionContext
import com.coditory.quark.eventbus.DispatchExceptionHandler

class InMemEventExceptionHandler implements DispatchExceptionHandler {
    private final List<DispatchExceptionContext> received = new ArrayList<>()

    @Override
    void handle(DispatchExceptionContext context) {
        received.add(context)
    }

    List<DispatchExceptionContext> getContexts() {
        return List.copyOf(received)
    }

    Throwable getLastException() {
        return received.isEmpty() ? null : received.get(0).exception()
    }

    boolean wasExecuted() {
        return !received.isEmpty()
    }

    boolean wasNotExecuted() {
        return !wasExecuted()
    }

    void reset() {
        received.clear()
    }
}
