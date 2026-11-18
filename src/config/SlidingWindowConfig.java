
package config;

import java.util.concurrent.TimeUnit;

public final class SlidingWindowConfig {

    private final long limit;
    private final long windowSizeNanos;

    public SlidingWindowConfig(
            long limit,
            long windowSize,
            TimeUnit unit) {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                "Limit must be greater than zero");
        }

        if (windowSize <= 0) {
            throw new IllegalArgumentException(
                "Window size must be greater than zero");
        }

        if (unit == null) {
            throw new IllegalArgumentException(
                "TimeUnit cannot be null");
        }

        this.limit = limit;
        this.windowSizeNanos = unit.toNanos(windowSize);
    }

    public long getLimit() {
        return limit;
    }

    public long getWindowSizeNanos() {
        return windowSizeNanos;
    }
}
