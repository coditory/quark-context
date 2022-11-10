package com.coditory.quark.context.events

import com.coditory.quark.context.Context
import com.coditory.quark.context.ResolutionPath
import com.coditory.quark.context.annotations.Bean
import com.coditory.quark.context.annotations.Configuration
import com.coditory.quark.context.base.InMemEventHandler
import com.coditory.quark.eventbus.EventHandler
import spock.lang.Specification

import static com.coditory.quark.context.BeanDescriptor.descriptor
import static com.coditory.quark.context.events.ContextEvent.BeanPostCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPostCreateEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCloseEvent
import static com.coditory.quark.context.events.ContextEvent.BeanPreCreateEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPostCloseEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPostCreateEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPreCloseEvent
import static com.coditory.quark.context.events.ContextEvent.ContextPreCreateEvent

class ConfigurationEventEmissionSpec extends Specification {
    SimpleHandler contextHandler = new SimpleHandler()
    Context context = Context.builder()
            .subscribe(contextHandler)
            .scanClass(SampleConfig)
            .build()

    def "should emit events when context is built"() {
        expect:
            contextHandler.events == [
                    new ContextPreCreateEvent(),
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

    def "should emit events for Configuration beans when context is closed"() {
        given:
            Context context = Context.builder()
                    .subscribe(contextHandler)
                    .scanClass(SampleConfig)
                    .registerConfigurationBeans()
                    .build()
            contextHandler.reset()
        when:
            context.get(A)
            context.get(C)
        then:
            contextHandler.events == [
                    new BeanPreCreateEvent(descriptor(A), ResolutionPath.of(A)),
                    new BeanPreCreateEvent(descriptor(SampleConfig), ResolutionPath.of(A, SampleConfig)),
                    new BeanPostCreateEvent(descriptor(SampleConfig), ResolutionPath.of(A, SampleConfig)),
                    new BeanPreCreateEvent(descriptor(B), ResolutionPath.of(A, B)),
                    new BeanPostCreateEvent(descriptor(B), ResolutionPath.of(A, B)),
                    new BeanPostCreateEvent(descriptor(A), ResolutionPath.of(A)),
                    new BeanPreCreateEvent(descriptor(C), ResolutionPath.of(C)),
                    new BeanPostCreateEvent(descriptor(C), ResolutionPath.of(C))
            ]
        when:
            contextHandler.reset()
            context.close()
        then:
            contextHandler.events == [
                    new ContextPreCloseEvent(),
                    new BeanPreCloseEvent(descriptor(SampleConfig)),
                    new BeanPostCloseEvent(descriptor(SampleConfig)),
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
