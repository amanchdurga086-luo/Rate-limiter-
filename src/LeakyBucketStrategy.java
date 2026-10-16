import java.util.concurrent.ConcurrentHashMap;

public class LeakyBucketStrategy
        implements RateLimiterStrategy {

    private final ConcurrentHashMap<String, LeakyBucket> buckets;

    private final long capacity;
    private final double leakRatePerSecond;
    private final Clock clock;

    public LeakyBucketStrategy(
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

        this.buckets = new ConcurrentHashMap<>();
    }

    
}