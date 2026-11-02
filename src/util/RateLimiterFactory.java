package util;

import config.TokenBucketConfig;
import config.LeakyBucketConfig;
import config.FixedWindowConfig;

import time.Clock;

import Strategy.RateLimiterStrategy;
import Strategy.TokenBucket.TokenBucketStrategy;
import Strategy.LeakyBucket.LeakyBucketStrategy;
import Strategy.fixedwindow.FixedWindowStrategy;

public final class RateLimiterFactory {

    private RateLimiterFactory() {
        // Prevent instantiation.
    }

    public static RateLimiter create(
            TokenBucketConfig config,
            Clock clock) {

        RateLimiterStrategy strategy =
                new TokenBucketStrategy(
                        config.getCapacity(),
                        config.getRefillRatePerSecond(),
                        clock
                );

        return new RateLimiter(strategy);
    }

    public static RateLimiter create(
            LeakyBucketConfig config,
            Clock clock) {

        RateLimiterStrategy strategy =
                new LeakyBucketStrategy(
                        config.getCapacity(),
                        config.getLeakRatePerSecond(),
                        clock
                );

        return new RateLimiter(strategy);
    }

    public static RateLimiter create(
            FixedWindowConfig config,
            Clock clock) {

        RateLimiterStrategy strategy =
                new FixedWindowStrategy(
                        config.getLimit(),
                        config.getWindowSizeNanos(),
                        clock
                );

        return new RateLimiter(strategy);
    }
}