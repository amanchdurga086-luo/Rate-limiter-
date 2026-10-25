package Strategy;

import Result.RateLimitResult;

public interface RateLimiterStrategy {
    // strategy pattern

    RateLimitResult allow(String key);
}