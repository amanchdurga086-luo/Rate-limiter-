
package Strategy.slidingwindow;

import time.Clock;

public class SlidingWindowCounter {

    private final long limit;
    private final long windowSizeNanos;
    private final Clock clock;

    private long currentWindow;
    private long currentWindowCount;
    private long previousWindowCount;

    public SlidingWindowCounter(
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

        this.currentWindow =
            Math.floorDiv(clock.nanoTime(), windowSizeNanos);

        this.currentWindowCount = 0;
        this.previousWindowCount = 0;
    }

    
}
