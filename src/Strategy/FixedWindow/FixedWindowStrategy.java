package Strategy.fixedwindow;

import java.util.concurrent.ConcurrentHashMap;

import Result.RateLimitResult;
import Strategy.RateLimiterStrategy;
import time.Clock;

public class FixedWindowStrategy
        implements RateLimiterStrategy {

    private final ConcurrentHashMap<String, FixedWindowCounter>
            counters;

    private final long limit;
    private final long windowSizeNanos;
    private final Clock clock;

    public FixedWindowStrategy(
            long limit,
            long windowSizeNanos,
            Clock clock) {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                    "Limit must be greater than zero"
            );
        }

        if (windowSizeNanos <= 0) {
            throw new IllegalArgumentException(
                    "Window duration must be greater than zero"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null"
            );
        }

        this.limit = limit;
        this.windowSizeNanos = windowSizeNanos;
        this.clock = clock;

        this.counters = new ConcurrentHashMap<>();
    }

    
}