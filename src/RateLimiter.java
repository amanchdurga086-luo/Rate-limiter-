public class RateLimiter {

    private final RateLimiterStrategy strategy;

    public RateLimiter(RateLimiterStrategy strategy) {

        if (strategy == null) {
            throw new IllegalArgumentException(
                    "Strategy cannot be null"
            );
        }

        this.strategy = strategy;
    }

    
}