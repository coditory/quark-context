package com.coditory.quark.context.events

import com.coditory.quark.context.Context
import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Inject
import com.coditory.quark.context.base.InMemEventHandler
import com.coditory.quark.eventbus.EventHandler
import spock.lang.Specification

import static com.coditory.quark.context.events.ConfigurationEventEmissionSpec.stringifyEvents

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
            stringifyEvents(contextHandler.events) == [
                    "ContextPreCreateEvent",
                    "ContextPostCreateEvent"
            ]
    }

    def "should emit events when beans are resolved"() {
        given:
            contextHandler.reset()
        when:
            context.get(A)
            context.get(C)
        then:
            stringifyEvents(contextHandler.events) == [
                    "BeanPreCreateEvent:A",
                    "BeanPreCreateEvent:B",
                    "BeanPostCreateEvent:B",
                    "BeanPostCreateEvent:A",
                    "BeanPreCreateEvent:C",
                    "BeanPostCreateEvent:C"
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
            stringifyEvents(contextHandler.events) == [
                    "ContextPreCloseEvent",
                    "BeanPreCloseEvent:A",
                    "BeanPostCloseEvent:A",
                    "BeanPreCloseEvent:B",
                    "BeanPostCloseEvent:B",
                    "BeanPreCloseEvent:C",
                    "BeanPostCloseEvent:C",
                    "ContextPostCloseEvent"
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
