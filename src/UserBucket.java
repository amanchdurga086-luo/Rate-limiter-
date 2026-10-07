public class UserBucket {

    private final long capacity;
    private final double refillRatePerSecond;
    // The clock reference can be assigned only once
    private final Clock clock;

    private double tokens;
    private long lastRefillNanos;

    public UserBucket(
            long capacity,
            double refillRatePerSecond,
            Clock clock) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than 0"
            );
        }

        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Refill rate must be greater than 0"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null"
            );
        }

        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.clock = clock;

        // Bucket starts completely full.
        this.tokens = capacity;

        this.lastRefillNanos = clock.nanoTime();
    }

}