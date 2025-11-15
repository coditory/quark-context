package com.coditory.quark.context.events;

import com.coditory.quark.context.BeanConfig;
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
        ContextEvent.BeanPostCloseEvent {

    record ContextPreCreateEvent() implements ContextEvent {
    }

    record ContextPostCreateEvent() implements ContextEvent {
    }

    record ContextPreCloseEvent() implements ContextEvent {
    }

    record ContextPostCloseEvent() implements ContextEvent {
    }

    record BeanPreCreateEvent(
            @NotNull BeanDescriptor<?> descriptor,
            @NotNull BeanConfig config,
            @NotNull ResolutionPath path) implements ContextEvent {
    }

    record BeanPostCreateEvent(
            @NotNull BeanDescriptor<?> descriptor,
            @NotNull BeanConfig config,
            @NotNull ResolutionPath path,
            @NotNull Object bean) implements ContextEvent {
    }

    record BeanPreCloseEvent(
            @NotNull BeanDescriptor<?> descriptor,
            @NotNull BeanConfig config,
            @NotNull Object bean) implements ContextEvent {
    }

    record BeanPostCloseEvent(
            @NotNull BeanDescriptor<?> descriptor,
            @NotNull BeanConfig config,
            @NotNull Object bean
    ) implements ContextEvent {
    }
}

