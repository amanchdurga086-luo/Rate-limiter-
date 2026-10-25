package util;

import time.Clock;
import Strategy.LeakyBucket.LeakyBucketStrategy;
import Strategy.TokenBucket.TokenBucketStrategy;
import Strategy.RateLimiterStrategy;



public final class RateLimiterFactory {

    private RateLimiterFactory() {
        // Prevent instantiation.
    }

    public static RateLimiter create(
            RateLimiterType type,
            long capacity,
            double ratePerSecond,
            Clock clock) {

        if (type == null) {
            throw new IllegalArgumentException(
                    "Rate limiter type cannot be null"
            );
        }

        RateLimiterStrategy strategy;

        switch (type) {

            case TOKEN_BUCKET:
                strategy = new TokenBucketStrategy(
                        capacity,
                        ratePerSecond,
                        clock
                );
                break;

            case LEAKY_BUCKET:
                strategy = new LeakyBucketStrategy(
                        capacity,
                        ratePerSecond,
                        clock
                );
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported rate limiter type: " + type
                );
        }

        return new RateLimiter(strategy);
    }
}