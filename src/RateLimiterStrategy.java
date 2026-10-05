public interface RateLimiterStrategy {

    RateLimitResult allow(String key);
}