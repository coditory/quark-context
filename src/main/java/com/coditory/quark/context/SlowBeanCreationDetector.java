package com.coditory.quark.context;

import com.coditory.quark.context.annotations.Bean;
import com.coditory.quark.context.events.ContextEvent.BeanPostCreateEvent;
import com.coditory.quark.context.events.ContextEvent.BeanPreCreateEvent;
import com.coditory.quark.eventbus.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

final class SlowBeanCreationDetector {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Map<BeanDescriptor<?>, Set<BeanDescriptor<?>>> dependencies = new HashMap<>();
    private final Map<BeanDescriptor<?>, Set<BeanDescriptor<?>>> parentDependencies = new HashMap<>();
    private final Map<BeanDescriptor<?>, Timer> timers = new HashMap<>();
    private final Map<BeanDescriptor<?>, Timer> totalTimers = new HashMap<>();
    private final Duration timerThreshold;
    private final Duration totalTimerThreshold;

    SlowBeanCreationDetector(Duration timerThreshold, Duration totalTimerThreshold) {
        this.timerThreshold = timerThreshold;
        this.totalTimerThreshold = totalTimerThreshold;
    }

    @EventHandler
    public void handle(BeanPreCreateEvent event) {
        timers.put(event.descriptor(), Timer.start());
        totalTimers.put(event.descriptor(), Timer.start());
        BeanDescriptor<?> parent = event.path().getParent(event.descriptor());
        if (parent != null) {
            dependencies.computeIfAbsent(parent, (k) -> new HashSet<>())
                    .add(event.descriptor());
            parentDependencies.computeIfAbsent(event.descriptor(), (k) -> new HashSet<>())
                    .add(parent);
            Timer parentTimer = timers.get(parent);
            if (parentTimer != null) {
                parentTimer.pause();
            }
        }
    }

    @EventHandler
    public void handle(BeanPostCreateEvent event) {
        for (BeanDescriptor<?> parent : parentDependencies.getOrDefault(event.descriptor(), Set.of())) {
            Set<BeanDescriptor<?>> children = dependencies.computeIfAbsent(parent, (k) -> new HashSet<>());
            children.remove(event.descriptor());
            Timer parentTimer = timers.get(parent);
            if (children.isEmpty() && parentTimer != null) {
                parentTimer.resume();
            }
        }
        dependencies.remove(event.descriptor());
        parentDependencies.remove(event.descriptor());
        reportTime(event);
        reportTotalTime(event);
    }

    private void reportTime(BeanPostCreateEvent event) {
        Timer timer = timers.get(event.descriptor());
        timer.pause();
        BeanConfig config = event.config();
        if (config.creationTimeMs() == -2) return;
        Duration threshold = (config.creationTimeMs() == -1)
                ? timerThreshold
                : Duration.ofMillis(config.creationTimeMs());
        if (threshold != null && threshold.minus(timer.measure()).isNegative()) {
            logger.warn("Slow bean creation. Bean: {}. Duration: {}", event.descriptor().toShortString(), timer.measureAndFormat());
        }
    }

    private void reportTotalTime(BeanPostCreateEvent event) {
        Timer timer = totalTimers.get(event.descriptor());
        timer.pause();
        BeanConfig config = event.config();
        if (config.creationTotalTimeMs() == -2) return;
        Duration threshold = (config.creationTotalTimeMs() == -1)
                ? totalTimerThreshold
                : Duration.ofMillis(config.creationTotalTimeMs());
        if (threshold != null && threshold.minus(timer.measure()).isNegative()) {
            logger.warn("Slow bean creation with dependencies. Bean: {}. Duration: {}", event.descriptor().toShortString(), timer.measureAndFormat());
        }
    }
}
