package config;

public final class LeakyBucketConfig {

    private final long capacity;
    private final double leakRatePerSecond;

    public LeakyBucketConfig(
            long capacity,
            double leakRatePerSecond) {

        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }

        if (!Double.isFinite(leakRatePerSecond)
                || leakRatePerSecond <= 0) {
            throw new IllegalArgumentException(
                    "Leak rate must be finite and positive"
            );
        }

        this.capacity = capacity;
        this.leakRatePerSecond = leakRatePerSecond;
    }

    public long getCapacity() {
        return capacity;
    }

    public double getLeakRatePerSecond() {
        return leakRatePerSecond;
    }
}