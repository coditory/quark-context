package com.coditory.quark.context.events;

import com.coditory.quark.context.BeanDescriptor;
import com.coditory.quark.context.ResolutionPath;

public sealed interface ContextEvent permits
        ContextEvent.ContextPreCreateEvent,
        ContextEvent.ContextPostCreateEvent,
        ContextEvent.ContextPreCloseEvent,
        ContextEvent.ContextPostCloseEvent,
        ContextEvent.BeanPreCreateEvent,
        ContextEvent.BeanPostCreateEvent,
        ContextEvent.BeanPreCloseEvent,
        ContextEvent.BeanPostCloseEvent,
        ContextEvent.BeanPreIsActiveCheckEvent,
        ContextEvent.BeanPostIsActiveCheckEvent {

    record ContextPreCreateEvent() implements ContextEvent {
    }

    record ContextPostCreateEvent() implements ContextEvent {
    }

    record ContextPreCloseEvent() implements ContextEvent {
    }

    record ContextPostCloseEvent() implements ContextEvent {
    }

    record BeanPreCreateEvent(BeanDescriptor<?> bean, ResolutionPath path) implements ContextEvent {
    }

    record BeanPostCreateEvent(BeanDescriptor<?> bean, ResolutionPath path) implements ContextEvent {
    }

    record BeanPreCloseEvent(BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPostCloseEvent(BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPreIsActiveCheckEvent(BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPostIsActiveCheckEvent(BeanDescriptor<?> bean, boolean active) implements ContextEvent {
    }
}

