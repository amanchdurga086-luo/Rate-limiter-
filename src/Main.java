public class Main {

        public static void main(String[] args)
                        throws InterruptedException {

                Clock clock = new SystemClock();

                RateLimiterStrategy strategy = new LeakyBucketStrategy(
                                5, // Maximum outstanding requests
                                2.0, // Leak 2 requests per second
                                clock);

                RateLimiter rateLimiter = new RateLimiter(strategy);

                System.out.println("----- Initial Requests -----");

                for (int i = 1; i <= 7; i++) {

                        RateLimitResult result = rateLimiter.allow("user-1");

                        System.out.println(
                                        "Request " + i + " -> " + result);
                }

                System.out.println(
                                "\nWaiting for capacity to become available...");

                Thread.sleep(2000);

                // RateLimitResult result =
                // rateLimiter.allow("user-1");

                System.out.println(
                                "After 2 seconds -> ");

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