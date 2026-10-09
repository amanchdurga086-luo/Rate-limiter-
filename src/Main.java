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

                // --------------------------------------------------
                // Different users
                // --------------------------------------------------

                System.out.println(
                                "\n----- Multiple Users -----");

                System.out.println(
                                "User 1 -> " +
                                                rateLimiter.allow("user-1"));

                System.out.println(
                                "User 2 -> " +
                                                rateLimiter.allow("user-2"));

                System.out.println(
                                "User 3 -> " +
                                                rateLimiter.allow("user-3"));

                // --------------------------------------------------
                // Concurrent test
                // --------------------------------------------------

                System.out.println(
                                "\n----- Concurrent Test -----");

                runConcurrentTest(rateLimiter);
        }

        // same user on different threads simultaneously, to test thread safety of the rate limiter.
        private static void runConcurrentTest(
                        RateLimiter rateLimiter)
                        throws InterruptedException {

                int numberOfThreads = 10;

                Thread[] threads = new Thread[numberOfThreads];

                for (int i = 0; i < numberOfThreads; i++) {

                        int threadNumber = i + 1;

                        threads[i] = new Thread(() -> {

                                RateLimitResult result = rateLimiter.allow("concurrent-user");

                                System.out.println(
                                                "Thread " +
                                                        threadNumber +
                                                                " -> " +
                                                                (result.isAllowed()
                                                                                ? "ALLOWED"
                                                                                : "BLOCKED"));

                        });
                }

                // Start all threads.
                for (Thread thread : threads) {
                        thread.start();
                }

                // Wait for all threads.
                for (Thread thread : threads) {
                        thread.join();
                }
        }

}