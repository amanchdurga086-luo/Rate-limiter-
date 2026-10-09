public class Main {

        public static void main(String[] args)
                        throws InterruptedException {

                long bucketCapacity = 5;

                // 2 tokens will be generated every second.
                double refillRatePerSecond = 2.0;

                // --------------------------------------------------
                // Dependencies
                // --------------------------------------------------

                Clock clock = new SystemClock();

                RateLimiterStrategy strategy = new TokenBucketStrategy(
                                bucketCapacity,
                                refillRatePerSecond,
                                clock);

                RateLimiter rateLimiter = new RateLimiter(strategy);

        }

}