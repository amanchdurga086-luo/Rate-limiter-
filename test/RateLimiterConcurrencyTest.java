
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
                CAPACITY, 1.0, new FakeClock()
            )
        );
    }

    @Test
    void shouldHandleConcurrentLeakyBucketRequests()
            throws InterruptedException {

        testConcurrentRequests(
            () -> new LeakyBucketStrategy(
                CAPACITY, 1.0, new FakeClock()
            )
        );
    }

    @Test
    void shouldHandleConcurrentFixedWindowRequests()
            throws InterruptedException {

        testConcurrentRequests(
            () -> new FixedWindowStrategy(
                CAPACITY,
                TimeUnit.MINUTES.toNanos(1),
                new FakeClock()
            )
        );
    }

    @Test
    void shouldHandleConcurrentSlidingWindowRequests()
            throws InterruptedException {

        testConcurrentRequests(
            () -> new SlidingWindowStrategy(
                CAPACITY,
                TimeUnit.MINUTES.toNanos(1),
                new FakeClock()
            )
        );
    }

    private void testConcurrentRequests(
            Supplier<RateLimiterStrategy> strategySupplier)
            throws InterruptedException {

        RateLimiterStrategy strategy = strategySupplier.get();

        ExecutorService executor =
            Executors.newFixedThreadPool(THREAD_COUNT);

            // Wait until all 20 threads are prepared
        CountDownLatch ready =
            new CountDownLatch(THREAD_COUNT);

            // Release all threads together to start sending requests
        CountDownLatch start =
            new CountDownLatch(1);

            // Wait until all threads finish
        CountDownLatch done =
            new CountDownLatch(THREAD_COUNT);

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

                        for (int j = 0;
                             j < REQUESTS_PER_THREAD;
                             j++) {

                            boolean allowed =
                                strategy.allow(userKey).isAllowed();

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
                "Workers did not become ready in time"
            );

            // Release all workers together.
            start.countDown();

            // Wait for all requests to finish.
            assertTrue(
                done.await(10, TimeUnit.SECONDS),
                "Concurrent test timed out"
            );

        } finally {

            // Prevent workers from remaining blocked on failure.
            start.countDown();

            executor.shutdownNow();
        }

        int totalRequests =
            THREAD_COUNT * REQUESTS_PER_THREAD;

        assertEquals(
            totalRequests,
            allowedCount.get() + rejectedCount.get()
        );

        assertEquals(
            CAPACITY,
            allowedCount.get(),
            "Allowed requests must not exceed capacity"
        );

        assertEquals(
            totalRequests - CAPACITY,
            rejectedCount.get()
        );
    }
}
