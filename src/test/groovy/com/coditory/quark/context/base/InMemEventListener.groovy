package com.coditory.quark.context.base


import com.coditory.quark.eventbus.EventListener

class InMemEventListener<T> implements EventListener<T> {
    private final List<T> received = new ArrayList<>()
    private final String name

    InMemEventListener() {
        this("listener")
    }

    InMemEventListener(String name) {
        this.name = name
    }

    @Override
    void handle(T event) {
        received.add(event)
    }

    List<T> getEvents() {
        return List.copyOf(received)
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

    @Override
    String toString() {
        return "InMemEventListener{name='" + name + "', received=" + received + "}";
    }
}
