package com.coditory.quark.context;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

final class Timer {
    private static final Duration DEFAULT_THRESHOLD = Duration.ofMillis(50);

    public static Timer start() {
        return new Timer();
    }

    private final long startMs = System.currentTimeMillis();

    boolean isOverThreshold() {
        return DEFAULT_THRESHOLD.minus(measure()).isNegative();
    }

    Duration measure() {
        long millis = now() - startMs;
        return Duration.ofMillis(millis);
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

    private long now() {
        return System.currentTimeMillis();
    }
}
