package com.coditory.quark.context

import com.coditory.quark.context.base.InMemEventHandler
import com.coditory.quark.context.events.ContextEventHandler
import com.coditory.quark.eventbus.EventBus
import com.coditory.quark.eventbus.EventHandler
import spock.lang.Specification

class EventBusRegistrationSpec extends Specification {
    def "should not register event bus by default"() {
        when:
            Context context = Context.builder()
                    .build()
        then:
            context.getOrNull(EventBus) == null
            context.getOrNull(EventBus, "ContextEventBus") == null
    }

    def "should register event bus when requested"() {
        when:
            Context context = Context.builder()
                    .registerContextEventBus()
                    .build()
        then:
            context.getOrNull(EventBus) != null
            context.getOrNull(EventBus, "ContextEventBus") != null
    }

    def "should subscribe handler annotated with @ContextEventHandler"() {
        given:
            NonContextHandler nonContextHandler = new NonContextHandler()
            ContextHandler contextHandler = new ContextHandler()
        and:
            Context context = Context.builder()
                    .registerContextEventBus()
                    .add(nonContextHandler)
                    .add(contextHandler)
                    .build()
        when:
            context.get(EventBus).emit("hello")
        then:
            nonContextHandler.events == []
            contextHandler.events == ["hello"]
    }

    def "should not subscribe any handler"() {
        given:
            NonContextHandler nonContextHandler = new NonContextHandler()
            ContextHandler contextHandler = new ContextHandler()
        and:
            Context context = Context.builder()
                    .registerContextEventBus()
                    .subscribeContextEventHandlers(false)
                    .add(nonContextHandler)
                    .add(contextHandler)
                    .build()
        when:
            context.get(EventBus).emit("hello")
        then:
            nonContextHandler.events == []
            contextHandler.events == []
    }

    class NonContextHandler extends InMemEventHandler {
        @EventHandler
        void handle(String event) {
            receive(event)
        }
    }

    @ContextEventHandler
    class ContextHandler extends InMemEventHandler {
        @EventHandler
        void handle(String event) {
            receive(event)
        }
    }
}
