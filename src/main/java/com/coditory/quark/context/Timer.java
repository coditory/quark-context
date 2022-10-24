package com.coditory.quark.context;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

final class Timer {
    public static Timer start() {
        return new Timer();
    }

    private long elapsedMs = 0;
    private long startMs = now();

    void pause() {
        elapsedMs += delta();
        startMs = 0;
    }

    void resume() {
        if (startMs == 0) {
            startMs = System.currentTimeMillis();
        }
    }

    Duration measure() {
        long millis = elapsedMs + delta();
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

    private long delta() {
        return startMs == 0 ? 0 : (now() - startMs);
    }

    private long now() {
        return System.currentTimeMillis();
    }
}
