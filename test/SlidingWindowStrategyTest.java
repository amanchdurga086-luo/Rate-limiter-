
package test;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.slidingwindow.SlidingWindowStrategy;

class SlidingWindowStrategyTest {

    private SlidingWindowStrategy strategy;
    private FakeClock clock;

    private static final long LIMIT = 4;

    private static final long WINDOW_SIZE =
        TimeUnit.SECONDS.toNanos(10);

    @BeforeEach
    void setUp() {

        clock = new FakeClock();

        strategy = new SlidingWindowStrategy(
            LIMIT,
            WINDOW_SIZE,
            clock
        );
    }

    // 1. New user should have full capacity

    @Test
    void shouldReturnFullCapacityForNewUser() {

        assertEquals(
            LIMIT,
            strategy.getRemainingCapacity("new-user")
        );
    }

    // 2. Requests within the limit should be allowed

    @Test
    void shouldAllowRequestsWithinLimit() {

        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
    }

    // 3. Requests beyond the limit should be rejected

    @Test
    void shouldRejectRequestsWhenLimitIsReached() {

        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        assertFalse(
            strategy.allow("user1").isAllowed()
        );
    }

    // 4. Previous window contribution should be weighted

    @Test
    void shouldApplyPreviousWindowWeight() {

        // Four requests in the previous window.
        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        // Move 15 seconds forward.
        // This is 5 seconds into the next window.
        clock.advanceSeconds(15);

        // Previous weight = (10 - 5) / 10 = 0.5
        // Estimated count = 4 * 0.5 = 2
        // Remaining capacity = 4 - 2 = 2

        assertEquals(
            2,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 5. Current window requests should be counted

    @Test
    void shouldCountRequestsInCurrentWindow() {

        // Fill the previous window.
        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        // Move to the next window, 5 seconds elapsed.
        clock.advanceSeconds(15);

        // Previous contribution = 2.
        // First current request: estimated count = 3.
        assertTrue(
            strategy.allow("user1").isAllowed()
        );

        // Second current request: estimated count = 4.
        assertTrue(
            strategy.allow("user1").isAllowed()
        );

        // Third current request must be rejected.
        assertFalse(
            strategy.allow("user1").isAllowed()
        );
    }

    // 6. Previous window contribution should disappear

    @Test
    void shouldRemovePreviousWindowContributionAfterTwoWindows() {

        // Four requests in the original window.
        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        // Move forward by two complete windows.
        clock.advanceSeconds(20);

        // Previous requests are no longer relevant.
        assertEquals(
            LIMIT,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 7. New window should retain previous window count initially

    @Test
    void shouldRetainPreviousWindowCountAtBoundary() {

        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        // Exactly one window has passed.
        clock.advanceSeconds(10);

        // At the start of the new window,
        // previous weight is still 100%.
        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 8. Different users should have independent counters

    @Test
    void shouldMaintainIndependentCountersForDifferentUsers() {

        for (int i = 0; i < LIMIT; i++) {
            strategy.allow("user1");
        }

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        assertEquals(
            LIMIT,
            strategy.getRemainingCapacity("user2")
        );

        assertTrue(
            strategy.allow("user2").isAllowed()
        );
    }

    // 9. Invalid keys and configurations should be rejected

    @Test
    void shouldRejectInvalidKeysAndConfigurations() {

        assertThrows(
            IllegalArgumentException.class,
            () -> strategy.allow(null)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> strategy.allow("")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> strategy.allow(" ")
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> strategy.getRemainingCapacity(null)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new SlidingWindowStrategy(
                0, WINDOW_SIZE, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new SlidingWindowStrategy(
                LIMIT, 0, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new SlidingWindowStrategy(
                LIMIT, WINDOW_SIZE, null
            )
        );
    }
}
