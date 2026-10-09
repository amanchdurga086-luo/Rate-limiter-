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

                // --------------------------------------------------
                // Single-thread test
                // --------------------------------------------------

                System.out.println("----- Single Thread Test -----");

                for (int i = 1; i <= 7; i++) {

                        RateLimitResult result = rateLimiter.allow("user-1");

                        System.out.println("Request " + i + " -> " + result);
                }

                // --------------------------------------------------
                // Wait for tokens to refill
                // --------------------------------------------------

                System.out.println(
                                "\nWaiting for tokens to refill...");

                Thread.sleep(2000);

                RateLimitResult result = rateLimiter.allow("user-1");

                System.out.println(
                                "After 2 seconds -> " + result);

        }

}