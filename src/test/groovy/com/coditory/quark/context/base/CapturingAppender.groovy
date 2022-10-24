package com.coditory.quark.context.base

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.Marker
import spock.util.concurrent.BlockingVariable

import java.util.function.Predicate

class CapturingAppender extends AppenderBase<LoggingEvent> {
    private int loggerCalls = 0
    private final List<LoggingEvent> capturedEvents = new ArrayList<>()
    private final BlockingVariable<Boolean> called = new BlockingVariable<Boolean>(0.2) // 200ms

    CapturingAppender() {
        start()
    }

    @Override
    protected void append(LoggingEvent event) {
        capturedEvents.add(event)
        called.set(true)
    }

    void waitForFirstLog() {
        called.get()
    }

    synchronized int getLoggerCalls() {
        return loggerCalls
    }

    synchronized void reset() {
        capturedEvents.clear()
    }

    synchronized List<String> getLogsByMessagePrefix(String prefix) {
        return capturedEvents
                .findAll { it.message.startsWith(prefix) }
                .collect { it.toString() }
    }

    synchronized int capturedEvents() {
        return capturedEvents.size()
    }

    synchronized int countCapturedEvents(Predicate<LoggingEvent> predicate) {
        return capturedEvents.count { predicate.test(it) }
    }

    synchronized int countCapturedEvents(String message) {
        return capturedEvents.count { it.message == message }
    }

    synchronized int countCapturedEvents(String message, List<Object> params) {
        return capturedEvents.count {
            it.message == message &&
                    it.argumentArray == params.toArray()
        }
    }

    synchronized int countCapturedEvents(Level level) {
        return capturedEvents.count { it.level == level }
    }

    synchronized int countCapturedEvents(String message, List<Object> params, String exceptionMessage) {
        return capturedEvents.count {
            it.message == message &&
                    it.argumentArray == params.toArray() &&
                    it.throwableProxy.message == exceptionMessage
        }
    }

    synchronized int countCapturedEvents(Marker marker) {
        return capturedEvents.count { it.marker == marker }
    }

    synchronized int countCapturedEvents(Level level, String message, List<Object> params, String exceptionMessage) {
        return capturedEvents.count {
            it.level == level &&
                    it.message == message &&
                    it.argumentArray == params?.toArray() &&
                    it.throwableProxy.message == exceptionMessage
        }
    }

    synchronized int countCapturedEvents(Level level, String message, List<Object> params) {
        return capturedEvents.count {
            it.level == level &&
                    it.message == message &&
                    it.argumentArray == params.toArray()
        }
    }

    synchronized int countCapturedEvents(Level level, String message) {
        return capturedEvents.count {
            it.level == level &&
                    it.message == message
        }
    }
}
