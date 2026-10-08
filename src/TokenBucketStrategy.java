import java.util.concurrent.ConcurrentHashMap;

public class TokenBucketStrategy implements RateLimiterStrategy {

    private final ConcurrentHashMap<String, UserBucket> buckets;

    private final long capacity;
    private final double refillRatePerSecond;
    private final Clock clock;

    public TokenBucketStrategy(
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

        this.buckets = new ConcurrentHashMap<>();
    }

    @Override
    public RateLimitResult allow(String key) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Rate limit key cannot be null or empty");
        }

        /*
         * computeIfAbsent ensures that a bucket is created
         * safely when multiple threads request the same key.
         */
        UserBucket bucket = buckets.computeIfAbsent(
                key,
                k -> new UserBucket(
                        capacity,
                        refillRatePerSecond,
                        clock));

        return bucket.tryConsume();
    }
}