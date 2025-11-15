package com.coditory.quark.context.events

import com.coditory.quark.context.Context
import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.base.InMemEventHandler
import com.coditory.quark.eventbus.EventHandler
import spock.lang.Specification

import static com.coditory.quark.context.events.ContextEvent.BeanPostCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPostCreateEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCreateEvent

class ConfigurationEventEmissionSpec extends Specification {
    SimpleHandler contextHandler = new SimpleHandler()
    Context context = Context.builder()
            .subscribe(contextHandler)
            .scanClass(SampleConfig)
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
                    "BeanPreCreateEvent:SampleConfig",
                    "BeanPostCreateEvent:SampleConfig",
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
                    "BeanPreCloseEvent:SampleConfig",
                    "BeanPostCloseEvent:SampleConfig",
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

    static List<Object> stringifyEvents(List<Object> events) {
        return events.collect {
            if (it instanceof BeanPreCreateEvent) {
                return it.class.simpleName + ":" + it.descriptor().type().simpleName
            }
            if (it instanceof BeanPostCreateEvent) {
                return it.class.simpleName + ":" + it.descriptor().type().simpleName
            }
            if (it instanceof BeanPreCloseEvent) {
                return it.class.simpleName + ":" + it.descriptor().type().simpleName
            }
            if (it instanceof BeanPostCloseEvent) {
                return it.class.simpleName + ":" + it.descriptor().type().simpleName
            }
            return it.class.simpleName
        }
    }

    @Configuration
    static class SampleConfig {
        @Bean
        A beanA(B b) {
            return new A(b)
        }

        @Bean
        B beanB() {
            return new B()
        }

        @Bean
        C beanC() {
            return new C()
        }
    }

    static class A {
        A(B b) {}
    }

    static class B {}

    static class C {}

    class SimpleHandler extends InMemEventHandler {
        @EventHandler
        void handle(Object event) {
            receive(event)
        }
    }
}
