import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import config.TokenBucketConfig;
import config.LeakyBucketConfig;
import config.FixedWindowConfig;

import time.Clock;
import time.SystemClock;

import util.RateLimiter;
import util.RateLimiterFactory;

public class Main {

    private static final Clock CLOCK = new SystemClock();

    public static void main(String[] args) throws InterruptedException {

        System.out.println("======================================");
        System.out.println("       RATE LIMITER TEST SUITE");
        System.out.println("======================================");

        testTokenBucket();
        testLeakyBucket();
        testFixedWindow();

        System.out.println("\n======================================");
        System.out.println("       ALL TESTS COMPLETED");
        System.out.println("======================================");
    }

    // ==========================================
    // TOKEN BUCKET TESTS
    // ==========================================

    private static void testTokenBucket() throws InterruptedException {

        System.out.println("\n\n========== TOKEN BUCKET ==========");

        // Single-thread test
        testSingleThread(
            "Token Bucket",
            () -> RateLimiterFactory.create(
                new TokenBucketConfig(3, 1.0), CLOCK),
            "user1",
            3
        );

        // Waiting / refill test
        testWaiting(
            "Token Bucket",
            () -> RateLimiterFactory.create(
                new TokenBucketConfig(2, 1.0), CLOCK),
            "user1",
            2,
            1100
        );

        // Different users test
        testDifferentUsers(
            "Token Bucket",
            () -> RateLimiterFactory.create(
                new TokenBucketConfig(2, 1.0), CLOCK),
            2
        );

        // Concurrent test
        testConcurrent(
            "Token Bucket",
            () -> RateLimiterFactory.create(
                new TokenBucketConfig(50, 0.000000001), CLOCK),
            50
        );
    }

    // ==========================================
    // LEAKY BUCKET TESTS
    // ==========================================

    private static void testLeakyBucket() throws InterruptedException {

        System.out.println("\n\n========== LEAKY BUCKET ==========");

        // Single-thread test
        testSingleThread(
            "Leaky Bucket",
            () -> RateLimiterFactory.create(
                new LeakyBucketConfig(3, 1.0), CLOCK),
            "user1",
            3
        );

        // Waiting / leaking test
        testWaiting(
            "Leaky Bucket",
            () -> RateLimiterFactory.create(
                new LeakyBucketConfig(2, 1.0), CLOCK),
            "user1",
            2,
            1100
        );

        // Different users test
        testDifferentUsers(
            "Leaky Bucket",
            () -> RateLimiterFactory.create(
                new LeakyBucketConfig(2, 1.0), CLOCK),
            2
        );

        // Concurrent test
        testConcurrent(
            "Leaky Bucket",
            () -> RateLimiterFactory.create(
                new LeakyBucketConfig(50, 0.000000001), CLOCK),
            50
        );
    }

    // ==========================================
    // FIXED WINDOW TESTS
    // ==========================================

    private static void testFixedWindow() throws InterruptedException {

        System.out.println("\n\n========== FIXED WINDOW ==========");

        // Single-thread test
        testSingleThread(
            "Fixed Window",
            () -> RateLimiterFactory.create(
                new FixedWindowConfig(3, 10, TimeUnit.SECONDS), CLOCK),
            "user1",
            3
        );

        // Waiting / window reset test
        testWaiting(
            "Fixed Window",
            () -> RateLimiterFactory.create(
                new FixedWindowConfig(2, 2, TimeUnit.SECONDS), CLOCK),
            "user1",
            2,
            2200
        );

        // Different users test
        testDifferentUsers(
            "Fixed Window",
            () -> RateLimiterFactory.create(
                new FixedWindowConfig(2, 10, TimeUnit.SECONDS), CLOCK),
            2
        );

        // Concurrent test
        testConcurrent(
            "Fixed Window",
            () -> RateLimiterFactory.create(
                new FixedWindowConfig(50, 1, TimeUnit.MINUTES), CLOCK),
            50
        );
    }

    // ==========================================
    // 1. SINGLE-THREAD TEST
    // ==========================================

    private static void testSingleThread(
        String name,
        Supplier<RateLimiter> limiterSupplier,
        String user,
        int expectedAllowed) {

        System.out.println("\n--- " + name + " : Single Thread Test ---");

        RateLimiter limiter = limiterSupplier.get();

        int allowed = 0;
        int rejected = 0;

        for (int i = 1; i <= expectedAllowed + 2; i++) {

            boolean result = limiter.allow(user).isAllowed();

            if (result) {
                allowed++;
                System.out.println("Request " + i + " : ALLOWED");
            } else {
                rejected++;
                System.out.println("Request " + i + " : REJECTED");
            }
        }

        System.out.println("Allowed: " + allowed);
        System.out.println("Rejected: " + rejected);

        printResult(
            allowed == expectedAllowed &&
            rejected == 2
        );
    }

    // ==========================================
    // 2. WAITING TEST
    // ==========================================

    private static void testWaiting(
        String name,
        Supplier<RateLimiter> limiterSupplier,
        String user,
        int capacity,
        long waitMillis) throws InterruptedException {

        System.out.println("\n--- " + name + " : Waiting Test ---");

        RateLimiter limiter = limiterSupplier.get();

        // Exhaust available capacity
        for (int i = 0; i < capacity; i++) {
            limiter.allow(user);
        }

        boolean rejected = !limiter.allow(user).isAllowed();

        System.out.println("Request after exhausting capacity: "
            + (rejected ? "REJECTED" : "UNEXPECTEDLY ALLOWED"));

        System.out.println("Remaining before waiting: "
            + limiter.getRemainingCapacity(user));

        System.out.println("Waiting for " + waitMillis + " ms...");

        Thread.sleep(waitMillis);

        System.out.println("Remaining after waiting: "
            + limiter.getRemainingCapacity(user));

        boolean allowedAfterWaiting =
            limiter.allow(user).isAllowed();

        System.out.println("Request after waiting: "
            + (allowedAfterWaiting ? "ALLOWED" : "REJECTED"));

        printResult(rejected && allowedAfterWaiting);
    }

    // ==========================================
    // 3. DIFFERENT USERS TEST
    // ==========================================

    private static void testDifferentUsers(
        String name,
        Supplier<RateLimiter> limiterSupplier,
        int capacity) {

        System.out.println("\n--- " + name + " : Different Users Test ---");

        RateLimiter limiter = limiterSupplier.get();

        String userA = "user-A";
        String userB = "user-B";

        int allowedA = 0;
        int allowedB = 0;

        // User A sends requests
        for (int i = 0; i < capacity + 1; i++) {
            if (limiter.allow(userA).isAllowed()) {
                allowedA++;
            }
        }

        // User B sends requests
        for (int i = 0; i < capacity + 1; i++) {
            if (limiter.allow(userB).isAllowed()) {
                allowedB++;
            }
        }

        System.out.println("User A allowed: " + allowedA);
        System.out.println("User B allowed: " + allowedB);

        printResult(
            allowedA == capacity &&
            allowedB == capacity
        );
    }

    // ==========================================
    // 4. CONCURRENT TEST
    // ==========================================

    private static void testConcurrent(
        String name,
        Supplier<RateLimiter> limiterSupplier,
        int expectedAllowed) throws InterruptedException {

        System.out.println("\n--- " + name + " : Concurrent Test ---");

        RateLimiter limiter = limiterSupplier.get();

        int numberOfThreads = 20;
        int requestsPerThread = 10;

        ExecutorService executor =
            Executors.newFixedThreadPool(numberOfThreads);

        CountDownLatch ready =
            new CountDownLatch(numberOfThreads);

        CountDownLatch start =
            new CountDownLatch(1);

        CountDownLatch done =
            new CountDownLatch(numberOfThreads);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        String user = "concurrent-user";

        for (int i = 0; i < numberOfThreads; i++) {

            executor.submit(() -> {

                ready.countDown();

                try {
                    // All threads start together
                    start.await();

                    for (int j = 0; j < requestsPerThread; j++) {

                        if (limiter.allow(user).isAllowed()) {
                            allowedCount.incrementAndGet();
                        } else {
                            rejectedCount.incrementAndGet();
                        }
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();

                } finally {
                    done.countDown();
                }
            });
        }

        // Wait until all threads are ready
        ready.await();

        long startTime = System.nanoTime();

        // Release all threads together
        start.countDown();

        boolean completed = done.await(10, TimeUnit.SECONDS);

        long endTime = System.nanoTime();

        executor.shutdownNow();

        System.out.println("Threads: " + numberOfThreads);
        System.out.println("requests Per Thread: " + requestsPerThread);
        System.out.println("Total requests: "
            + (numberOfThreads * requestsPerThread));

        System.out.println("Allowed: " + allowedCount.get());
        System.out.println("Rejected: " + rejectedCount.get());

        System.out.println("Execution time: "
            + TimeUnit.NANOSECONDS.toMillis(endTime - startTime)
            + " ms");

        printResult(
            completed &&
            allowedCount.get() == expectedAllowed &&
            rejectedCount.get() ==
                (numberOfThreads * requestsPerThread - expectedAllowed)
        );
    }

    // ==========================================
    // RESULT HELPER
    // ==========================================

    private static void printResult(boolean passed) {

        if (passed) {
            System.out.println("TEST RESULT: PASS");
        } else {
            System.out.println("TEST RESULT: FAIL");
        }
    }
}
