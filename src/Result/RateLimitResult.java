package Result;

public final class RateLimitResult {

    private final boolean allowed;
    private final long remainingCapacity;
    private final long retryAfterMillis;

    private RateLimitResult(
            boolean allowed,
            long remainingCapacity,
            long retryAfterMillis) {

        this.allowed = allowed;
        this.remainingCapacity = remainingCapacity;
        this.retryAfterMillis = retryAfterMillis;
    }

    public static RateLimitResult allowed(long remainingCapacity) {
        return new RateLimitResult(
                true,
                remainingCapacity,
                0
        );
    }

    public static RateLimitResult rejected(
            long remainingCapacity,
            long retryAfterMillis) {

        return new RateLimitResult(
                false,
                remainingCapacity,
                retryAfterMillis
        );
    }

    public boolean isAllowed() {
        return allowed;
    }

    public long getRemainingCapacity() {
        return remainingCapacity;
    }

    public long getRetryAfterMillis() {
        return retryAfterMillis;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
                "allowed=" + allowed +
                ", remainingCapacity=" + remainingCapacity +
                ", retryAfterMillis=" + retryAfterMillis +
                '}';
    }
}