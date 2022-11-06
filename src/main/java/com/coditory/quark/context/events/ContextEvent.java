package com.coditory.quark.context.events;

import com.coditory.quark.context.BeanDescriptor;
import com.coditory.quark.context.ResolutionPath;
import org.jetbrains.annotations.NotNull;

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

    record BeanPreCreateEvent(@NotNull BeanDescriptor<?> bean, @NotNull ResolutionPath path) implements ContextEvent {
    }

    record BeanPostCreateEvent(@NotNull BeanDescriptor<?> bean, @NotNull ResolutionPath path) implements ContextEvent {
    }

    record BeanPreCloseEvent(@NotNull BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPostCloseEvent(@NotNull BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPreIsActiveCheckEvent(@NotNull BeanDescriptor<?> bean) implements ContextEvent {
    }

    record BeanPostIsActiveCheckEvent(@NotNull BeanDescriptor<?> bean, boolean active) implements ContextEvent {
    }
}

