package config;

public final class TokenBucketConfig {

    private final long capacity;
    private final double refillRatePerSecond;

    public TokenBucketConfig(
            long capacity,
            double refillRatePerSecond) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        if (!Double.isFinite(refillRatePerSecond)
                || refillRatePerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Refill rate must be finite and positive"
            );
        }

        this.capacity = capacity;
        this.refillRatePerSecond = refillRatePerSecond;
    }

    public long getCapacity() {
        return capacity;
    }

    public double getRefillRatePerSecond() {
        return refillRatePerSecond;
    }
}