package com.coditory.quark.context

import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Inject
import com.coditory.quark.context.base.InMemEventHandler
import com.coditory.quark.context.events.ContextEventHandler
import com.coditory.quark.eventbus.EventHandler
import spock.lang.Specification

import static com.coditory.quark.context.BeanDescriptor.descriptor
import static com.coditory.quark.context.events.ContextEvent.BeanPostCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPostCreateEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPostIsActiveCheckEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCreateEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreIsActiveCheckEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPostCloseEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPostCreateEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPreCloseEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPreCreateEvent

class ContextEventEmissionSpec extends Specification {
    SimpleHandler contextHandler = new SimpleHandler()
    Context context = Context.builder()
            .subscribe(contextHandler)
            .scanClass(A)
            .scanClass(B)
            .scanClass(C)
            .build()

    def "should emit events when context is built"() {
        expect:
            contextHandler.events == [
                    new ContextPreCreateEvent(),
                    // A - isActive
                    new BeanPreIsActiveCheckEvent(descriptor(A)),
                    new BeanPostIsActiveCheckEvent(descriptor(A), true),
                    new BeanPreIsActiveCheckEvent(descriptor(A)),
                    new BeanPostIsActiveCheckEvent(descriptor(A), true),
                    // B - isActive
                    new BeanPreIsActiveCheckEvent(descriptor(B)),
                    new BeanPostIsActiveCheckEvent(descriptor(B), true),
                    new BeanPreIsActiveCheckEvent(descriptor(B)),
                    new BeanPostIsActiveCheckEvent(descriptor(B), true),
                    // C - isActive
                    new BeanPreIsActiveCheckEvent(descriptor(C)),
                    new BeanPostIsActiveCheckEvent(descriptor(C), true),
                    new BeanPreIsActiveCheckEvent(descriptor(C)),
                    new BeanPostIsActiveCheckEvent(descriptor(C), true),
                    // Context
                    new ContextPostCreateEvent()
            ]
    }

    def "should emit events when beans are resolved"() {
        given:
            contextHandler.reset()
        when:
            context.get(A)
            context.get(C)
        then:
            contextHandler.events == [
                    new BeanPreCreateEvent(descriptor(A), ResolutionPath.of(A)),
                    new BeanPreCreateEvent(descriptor(B), ResolutionPath.of(A, B)),
                    new BeanPostCreateEvent(descriptor(B), ResolutionPath.of(A, B)),
                    new BeanPostCreateEvent(descriptor(A), ResolutionPath.of(A)),
                    new BeanPreCreateEvent(descriptor(C), ResolutionPath.of(C)),
                    new BeanPostCreateEvent(descriptor(C), ResolutionPath.of(C))
            ]
    }

    def "should emit events when context is closed"() {
        given:
            context.get(A)
            context.get(C)
            contextHandler.reset()
        when:
            context.close()
        then:
            contextHandler.events == [
                    new ContextPreCloseEvent(),
                    new BeanPreCloseEvent(descriptor(A)),
                    new BeanPostCloseEvent(descriptor(A)),
                    new BeanPreCloseEvent(descriptor(B)),
                    new BeanPostCloseEvent(descriptor(B)),
                    new BeanPreCloseEvent(descriptor(C)),
                    new BeanPostCloseEvent(descriptor(C)),
                    new ContextPostCloseEvent(),
            ]
            contextHandler.reset()
    }

    def "should not emit context creation events to eventHandler registered as a bean"() {
        given:
            ContextHandler contextHandler = new ContextHandler()
        when:
            Context.builder()
                    .add(contextHandler)
                    .build()
        then:
            contextHandler.events == []
    }

    @Bean
    static class A {
        @Inject
        A(B b) {}
    }

    @Bean
    static class B {

    }

    @Bean
    static class C {

    }

    class SimpleHandler extends InMemEventHandler {
        @EventHandler
        void handle(Object event) {
            receive(event)
        }
    }

    @ContextEventHandler
    class ContextHandler extends InMemEventHandler {
        @EventHandler
        void handle(Object event) {
            receive(event)
        }
    }
}
