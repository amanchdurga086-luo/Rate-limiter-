
package test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.RateLimiterStrategy;
import Strategy.TokenBucket.TokenBucketStrategy;
import Strategy.LeakyBucket.LeakyBucketStrategy;
import Strategy.fixedwindow.FixedWindowStrategy;
import Strategy.slidingwindow.SlidingWindowStrategy;

class RateLimiterConcurrencyTest {

    private static final int THREAD_COUNT = 20;
    private static final int REQUESTS_PER_THREAD = 50;
    private static final int CAPACITY = 100;

    @Test
    void shouldHandleConcurrentTokenBucketRequests()
            throws InterruptedException {

        testConcurrentRequests(
                () -> new TokenBucketStrategy(
                        CAPACITY, 1.0, new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentLeakyBucketRequests()
            throws InterruptedException {

        testConcurrentRequests(
                () -> new LeakyBucketStrategy(
                        CAPACITY, 1.0, new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentFixedWindowRequests()
            throws InterruptedException {

        testConcurrentRequests(
                () -> new FixedWindowStrategy(
                        CAPACITY,
                        TimeUnit.MINUTES.toNanos(1),
                        new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentSlidingWindowRequests()
            throws InterruptedException {

        testConcurrentRequests(
                () -> new SlidingWindowStrategy(
                        CAPACITY,
                        TimeUnit.MINUTES.toNanos(1),
                        new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentRequestsFromDifferentUsers()
            throws InterruptedException {

        int userCount = 20;
        int requestsPerUser = 50;

        int capacityPerUser = 50;

        RateLimiterStrategy strategy = new TokenBucketStrategy(
                capacityPerUser,
                1.0,
                new FakeClock());

        ExecutorService executor = Executors.newFixedThreadPool(userCount);

        CountDownLatch ready = new CountDownLatch(userCount);

        CountDownLatch start = new CountDownLatch(1);

        CountDownLatch done = new CountDownLatch(userCount);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        try {

            for (int i = 0; i < userCount; i++) {

                final String userKey = "user-" + i;

                executor.submit(() -> {

                    ready.countDown();

                    try {
                        start.await();

                        for (int j = 0; j < requestsPerUser; j++) {

                            if (strategy.allow(userKey).isAllowed()) {
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

            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Workers did not become ready");

            start.countDown();

            assertTrue(
                    done.await(10, TimeUnit.SECONDS),
                    "Concurrent test timed out");

        } finally {

            start.countDown();
            executor.shutdownNow();
        }

        int expectedAllowed = userCount * capacityPerUser;

        assertEquals(expectedAllowed, allowedCount.get());

        assertEquals(0, rejectedCount.get());

        assertEquals(
                0,
                strategy.getRemainingCapacity("user-0"));
    }

    @Test
    void shouldHandleConcurrentLeakyBucketRequestsFromDifferentUsers()
            throws InterruptedException {

        testConcurrentDifferentUsers(
                () -> new LeakyBucketStrategy(
                        50, 1.0, new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentFixedWindowRequestsFromDifferentUsers()
            throws InterruptedException {

        testConcurrentDifferentUsers(
                () -> new FixedWindowStrategy(
                        50,
                        TimeUnit.MINUTES.toNanos(1),
                        new FakeClock()));
    }

    @Test
    void shouldHandleConcurrentSlidingWindowRequestsFromDifferentUsers()
            throws InterruptedException {

        testConcurrentDifferentUsers(
                () -> new SlidingWindowStrategy(
                        50,
                        TimeUnit.MINUTES.toNanos(1),
                        new FakeClock()));
    }

    private void testConcurrentDifferentUsers(
            Supplier<RateLimiterStrategy> strategySupplier)
            throws InterruptedException {

        int userCount = 20;
        int requestsPerUser = 50;
        // int capacityPerUser = 50;

        RateLimiterStrategy strategy = strategySupplier.get();

        ExecutorService executor = Executors.newFixedThreadPool(userCount);

        CountDownLatch ready = new CountDownLatch(userCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(userCount);

        AtomicInteger allowedCount = new AtomicInteger();
        AtomicInteger rejectedCount = new AtomicInteger();

        try {

            for (int i = 0; i < userCount; i++) {

                final String userKey = "user-" + i;

                executor.submit(() -> {

                    ready.countDown();

                    try {
                        start.await();

                        for (int j = 0; j < requestsPerUser; j++) {

                            if (strategy.allow(userKey).isAllowed()) {
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

            assertTrue(ready.await(5, TimeUnit.SECONDS));

            start.countDown();

            assertTrue(done.await(10, TimeUnit.SECONDS));

        } finally {
            start.countDown();
            executor.shutdownNow();
        }

        assertEquals(1000, allowedCount.get());
        assertEquals(0, rejectedCount.get());

        for (int i = 0; i < userCount; i++) {
            assertEquals(
                    0,
                    strategy.getRemainingCapacity("user-" + i),
                    "Incorrect remaining capacity for user-" + i);
        }
    }

    private void testConcurrentRequests(
            Supplier<RateLimiterStrategy> strategySupplier)
            throws InterruptedException {

        RateLimiterStrategy strategy = strategySupplier.get();

        // Creates a pool of 20 worker threads
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

        // Wait until all 20 threads are prepared
        CountDownLatch ready = new CountDownLatch(THREAD_COUNT);

        // Release all threads together to start sending requests
        CountDownLatch start = new CountDownLatch(1);

        // Wait until all threads finish
        CountDownLatch done = new CountDownLatch(THREAD_COUNT);

        AtomicInteger allowedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);

        String userKey = "concurrent-user";

        try {

            for (int i = 0; i < THREAD_COUNT; i++) {

                executor.submit(() -> {

                    ready.countDown();

                    try {
                        // Wait until all workers are ready.
                        start.await();

                        for (int j = 0; j < REQUESTS_PER_THREAD; j++) {

                            boolean allowed = strategy.allow(userKey).isAllowed();

                            if (allowed) {
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

            // Ensure all threads are ready.
            assertTrue(
                    ready.await(5, TimeUnit.SECONDS),
                    "Workers did not become ready in time");

            // Release all workers together.
            start.countDown();

            // Wait for all requests to finish.
            assertTrue(
                    done.await(10, TimeUnit.SECONDS),
                    "Concurrent test timed out");

        } finally {

            // Prevent workers from remaining blocked on failure.
            start.countDown();

            executor.shutdownNow();
        }

        int totalRequests = THREAD_COUNT * REQUESTS_PER_THREAD;

        assertEquals(
                totalRequests,
                allowedCount.get() + rejectedCount.get());

        assertEquals(
                CAPACITY,
                allowedCount.get(),
                "Allowed requests must not exceed capacity");

        assertEquals(
                totalRequests - CAPACITY,
                rejectedCount.get());
    }
}
