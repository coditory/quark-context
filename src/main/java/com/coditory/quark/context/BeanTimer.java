package com.coditory.quark.context;

import java.time.Duration;
import java.util.Stack;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

final class BeanTimer {
    private final Stack<Timer> stack = new Stack<>();
    private Duration createThreshold = Duration.ofMillis(50);
    private Duration destroyThreshold = Duration.ofMillis(50);
    private Duration isActiveThreshold = Duration.ofMillis(50);

    public void measure(Runnable runnable) {
        try {
            start();
            runnable.run();
        } finally {
            stop();
        }
    }

    public <T> T measure(Supplier<T> suppier) {
        try {
            start();
            return suppier.get();
        } finally {
            stop();
        }
    }

    public void start() {
        if (!stack.isEmpty()) {
            stack.peek().pause();
        }
        stack.push(Timer.start());
    }

    public void stop() {
        if (!stack.isEmpty()) {
            Timer timer = stack.pop();
            timer.pause();
        }
        if (!stack.isEmpty()) {
            stack.peek().resume();
        }
    }

    Duration measure() {
        if (stack.isEmpty()) {
            return Duration.ZERO;
        }
        return stack.peek().measure();
    }

    String measureAndFormat() {
        Duration duration = measure();
        long totalMs = duration.toMillis();
        long ms = totalMs % 1000;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(totalMs) % 60;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(totalMs) % 60;
        long hours = TimeUnit.MILLISECONDS.toHours(totalMs) % 24;
        long days = TimeUnit.MILLISECONDS.toDays(totalMs);
        String result;
        if (days > 0) {
            result = String.format("%d:%02dd", days, hours);
        } else if (hours > 0) {
            result = String.format("%d:%02dh", hours, minutes);
        } else if (minutes > 0) {
            result = String.format("%d:%02dm", minutes, seconds);
        } else if (seconds > 0) {
            result = String.format("%d.%03ds", seconds, ms);
        } else {
            result = String.format("%dms", ms);
        }
        return result;
    }

    boolean isOverCreateThreshold() {
        return createThreshold.minus(measure()).isNegative();
    }

    boolean isOverDestroyThreshold() {
        return destroyThreshold.minus(measure()).isNegative();
    }

    boolean isOverIsActiveThreshold() {
        return isActiveThreshold.minus(measure()).isNegative();
    }

    public void setCreateThreshold(Duration createThreshold) {
        this.createThreshold = createThreshold;
    }

    public void setDestroyThreshold(Duration destroyThreshold) {
        this.destroyThreshold = destroyThreshold;
    }

    public void setIsActiveThreshold(Duration isActiveThreshold) {
        this.isActiveThreshold = isActiveThreshold;
    }
}
