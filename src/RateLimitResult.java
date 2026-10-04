public final class RateLimitResult {

    private final boolean allowed;
    private final long remainingTokens;
    private final long retryAfterMillis;

    private RateLimitResult(
            boolean allowed,
            long remainingTokens,
            long retryAfterMillis) {

        this.allowed = allowed;
        this.remainingTokens = remainingTokens;
        this.retryAfterMillis = retryAfterMillis;
    }

    public static RateLimitResult allowed(long remainingTokens) {
        return new RateLimitResult(
                true,
                remainingTokens,
                0
        );
    }

    
}