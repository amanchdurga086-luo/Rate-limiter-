
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

    public synchronized boolean tryAcquire() {

        long now = clock.nanoTime();

        updateWindow(now);

        long elapsedNanos =
            Math.floorMod(now, windowSizeNanos);

        double previousWeight =
            (double) (windowSizeNanos - elapsedNanos)
                / windowSizeNanos;

        double estimatedCount =
            currentWindowCount +
            previousWindowCount * previousWeight;

        if (estimatedCount + 1 > limit) {
            return false;
        }

        currentWindowCount++;

        return true;
    }

    public synchronized long getRemainingCapacity() {

        long now = clock.nanoTime();

        updateWindow(now);

        long elapsedNanos =
            Math.floorMod(now, windowSizeNanos);

        double previousWeight =
            (double) (windowSizeNanos - elapsedNanos)
                / windowSizeNanos;

        double estimatedCount =
            currentWindowCount +
            previousWindowCount * previousWeight;

        return Math.max(
            0,
            (long) Math.floor(limit - estimatedCount)
        );
    }

    private void updateWindow(long now) {

        long newWindow =
            Math.floorDiv(now, windowSizeNanos);

        if (newWindow == currentWindow) {
            return;
        }

        long windowsPassed =
            newWindow - currentWindow;

        if (windowsPassed == 1) {

            previousWindowCount = currentWindowCount;

        } else {

            // More than one window has passed.
            // The previous window is no longer relevant.
            previousWindowCount = 0;
        }

        currentWindowCount = 0;
        currentWindow = newWindow;
    }
}
