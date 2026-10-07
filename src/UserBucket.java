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
                    "Capacity must be greater than 0");
        }

        if (refillRatePerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Refill rate must be greater than 0");
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null");
        }

        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
        this.clock = clock;

        // Bucket starts completely full.
        this.tokens = capacity;

        this.lastRefillNanos = clock.nanoTime();
    }

    /**
     * Attempts to consume one token.
     *
     * synchronized is important because:
     *
     * refill()
     * +
     * check token
     * +
     * consume token
     *
     * must happen atomically.
     */

    public synchronized RateLimitResult tryConsume() {

        refill();

        if (tokens >= 1.0) {

            tokens--;

            return RateLimitResult.allowed(
                    (long) tokens);
        }

        long retryAfterMillis = calculateRetryAfterMillis();

        return RateLimitResult.rejected(
                0,
                retryAfterMillis);

    }

    // Adds tokens based on elapsed time.
    private void refill() {

        long now = clock.nanoTime();

        long elapsedNanos = now - lastRefillNanos;

        if (elapsedNanos <= 0) {
            return;
        }

        double elapsedSeconds = elapsedNanos / 1_000_000_000.0;

        double tokensToAdd = elapsedSeconds * refillRatePerSecond;

        tokens = Math.min(
                capacity,
                tokens + tokensToAdd);

        lastRefillNanos = now;
    }
    
}