package util;

import config.TokenBucketConfig;
import config.LeakyBucketConfig;
import config.FixedWindowConfig;
import config.SlidingWindowConfig;

import time.Clock;

import Strategy.RateLimiterStrategy;
import Strategy.TokenBucket.TokenBucketStrategy;
import Strategy.LeakyBucket.LeakyBucketStrategy;
import Strategy.fixedwindow.FixedWindowStrategy;
import Strategy.slidingwindow.SlidingWindowStrategy;

public final class RateLimiterFactory {

        private RateLimiterFactory() {
                // Prevent instantiation.
        }

        public static RateLimiter create(
                        TokenBucketConfig config,
                        Clock clock) {

                RateLimiterStrategy strategy = new TokenBucketStrategy(
                                config.getCapacity(),
                                config.getRefillRatePerSecond(),
                                clock);

                return new RateLimiter(strategy);
        }

        public static RateLimiter create(
                        LeakyBucketConfig config,
                        Clock clock) {

                RateLimiterStrategy strategy = new LeakyBucketStrategy(
                                config.getCapacity(),
                                config.getLeakRatePerSecond(),
                                clock);

                return new RateLimiter(strategy);
        }

        public static RateLimiter create(
                        FixedWindowConfig config,
                        Clock clock) {

                RateLimiterStrategy strategy = new FixedWindowStrategy(
                                config.getLimit(),
                                config.getWindowSizeNanos(),
                                clock);

                return new RateLimiter(strategy);
        }

        public static RateLimiter create(
                        SlidingWindowConfig config,
                        Clock clock) {

                if (config == null || clock == null) {
                        throw new IllegalArgumentException(
                                        "Config and clock cannot be null");
                }

                return new RateLimiter(
                                new SlidingWindowStrategy(
                                                config.getLimit(),
                                                config.getWindowSizeNanos(),
                                                clock));
        }
}