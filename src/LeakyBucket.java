public class LeakyBucket {

    private final long capacity;
    private final double leakRatePerSecond;
    private final Clock clock;

    private double waterLevel;
    private long lastLeakNanos;

    public LeakyBucket(
            long capacity,
            double leakRatePerSecond,
            Clock clock) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        if (!Double.isFinite(leakRatePerSecond)
                || leakRatePerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Leak rate must be finite and greater than zero"
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "Clock cannot be null"
            );
        }

        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
        this.clock = clock;

        this.waterLevel = 0.0;
        this.lastLeakNanos = clock.nanoTime();
    }

    public synchronized RateLimitResult tryConsume() {

        leak();

        if (waterLevel + 1.0 <= capacity) {

            waterLevel += 1.0;

            long remainingCapacity =
                    (long) Math.floor(capacity - waterLevel);

            return RateLimitResult.allowed(
                    remainingCapacity
            );
        }

        long retryAfterMillis =
                calculateRetryAfterMillis();

        long remainingCapacity =
                (long) Math.floor(capacity - waterLevel);

        return RateLimitResult.rejected(
                remainingCapacity,
                retryAfterMillis
        );
    }

    private void leak() {

        long now = clock.nanoTime();

        long elapsedNanos = now - lastLeakNanos;

        if (elapsedNanos <= 0) {
            return;
        }

        double elapsedSeconds =
                elapsedNanos / 1_000_000_000.0;

        double leaked =
                elapsedSeconds * leakRatePerSecond;

        waterLevel = Math.max(
                0.0,
                waterLevel - leaked
        );

        lastLeakNanos = now;
    }

    private long calculateRetryAfterMillis() {

        double excessWork = waterLevel + 1.0 - capacity;

        double seconds =
                excessWork / leakRatePerSecond;

        return Math.max(
                1,
                (long) Math.ceil(seconds * 1000)
        );
    }
}