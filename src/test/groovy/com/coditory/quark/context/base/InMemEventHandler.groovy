package com.coditory.quark.context.base

abstract class InMemEventHandler {
    private final List<Object> events = new ArrayList<>();

    void receive(Object event) {
        events.add(event)
    }

    void reset() {
        events.clear()
    }

    List<Object> getEvents() {
        return List.copyOf(events)
    }
}