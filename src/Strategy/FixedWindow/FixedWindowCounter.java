package Strategy.fixedwindow;

import Result.RateLimitResult;
import time.Clock;

public class FixedWindowCounter {

    private final long limit;
    private final long windowSizeNanos;
    private final Clock clock;

    private long currentWindow;
    private long requestCount;

    public FixedWindowCounter(
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

        this.currentWindow = getWindowId(clock.nanoTime());
        this.requestCount = 0;
    }

    public synchronized RateLimitResult tryAcquire() {

        long now = clock.nanoTime();
        long windowId = getWindowId(now);

        resetIfWindowChanged(windowId);

        if (requestCount < limit) {

            requestCount++;

            return RateLimitResult.allowed(
                    limit - requestCount
            );
        }

        return RateLimitResult.rejected(
                0,
                calculateRetryAfterMillis(now)
        );
    }

    public synchronized long getRemainingCapacity() {

        long now = clock.nanoTime();
        long windowId = getWindowId(now);

        resetIfWindowChanged(windowId);

        return limit - requestCount;
    }

    
}