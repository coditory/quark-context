package com.coditory.quark.context;

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
        timers.put(event.bean(), Timer.start());
        totalTimers.put(event.bean(), Timer.start());
        BeanDescriptor<?> parent = event.path().getParent(event.bean());
        if (parent != null) {
            dependencies.computeIfAbsent(parent, (k) -> new HashSet<>())
                    .add(event.bean());
            parentDependencies.computeIfAbsent(event.bean(), (k) -> new HashSet<>())
                    .add(parent);
            timers.get(parent).pause();
        }
    }

    @EventHandler
    public void handle(BeanPostCreateEvent event) {
        for (BeanDescriptor<?> parent : parentDependencies.getOrDefault(event.bean(), Set.of())) {
            Set<BeanDescriptor<?>> children = dependencies.computeIfAbsent(parent, (k) -> new HashSet<>());
            children.remove(event.bean());
            if (children.isEmpty()) {
                timers.get(parent).resume();
            }
        }
        dependencies.remove(event.bean());
        parentDependencies.remove(event.bean());
        Timer timer = timers.get(event.bean());
        timer.pause();
        if (timerThreshold != null && timerThreshold.minus(timer.measure()).isNegative()) {
            logger.warn("Slow bean creation. Bean: " + event.bean().toShortString() + ". Duration: " + timer.measureAndFormat());
        }
        Timer totalTimer = totalTimers.get(event.bean());
        totalTimer.pause();
        if (totalTimerThreshold != null && totalTimerThreshold.minus(totalTimer.measure()).isNegative()) {
            logger.warn("Slow bean creation with dependencies. Bean: " + event.bean().toShortString() + ". Duration: " + timer.measureAndFormat());
        }
    }
}
