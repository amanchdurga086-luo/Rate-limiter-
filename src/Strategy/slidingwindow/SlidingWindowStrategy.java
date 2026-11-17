
package Strategy.slidingwindow;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import time.Clock;
import Strategy.RateLimiterStrategy;
import Result.RateLimitResult;

public class SlidingWindowStrategy implements RateLimiterStrategy {

    private final long limit;
    private final long windowSizeNanos;
    private final Clock clock;

    private final ConcurrentMap<String, SlidingWindowCounter> counters =
        new ConcurrentHashMap<>();

    public SlidingWindowStrategy(
            long limit,
            long windowSizeNanos,
            Clock clock) {

        if (limit <= 0) {
            throw new IllegalArgumentException(
                "Limit must be greater than zero");
        }

        if (windowSizeNanos <= 0) {
            throw new IllegalArgumentException(
                "Window size must be greater than zero");
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                "Clock cannot be null");
        }

        this.limit = limit;
        this.windowSizeNanos = windowSizeNanos;
        this.clock = clock;
    }

    @Override  
    public RateLimitResult allow(String key) {

        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Key cannot be null or empty");
        }

        SlidingWindowCounter counter =
            counters.computeIfAbsent(
                key,
                k -> new SlidingWindowCounter(
                    limit,
                    windowSizeNanos,
                    clock
                )
            );

        if (counter.tryAcquire()) {
            return RateLimitResult.allowed(
                counter.getRemainingCapacity()
            );
        }

        return RateLimitResult.rejected(
            counter.getRemainingCapacity(),
            calculateRetryAfter(counter)
        );
    }
}
