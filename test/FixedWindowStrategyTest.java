
package test;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.fixedwindow.FixedWindowStrategy;

class FixedWindowStrategyTest {

    private FixedWindowStrategy strategy;
    private FakeClock clock;

    private static final long LIMIT = 3;

    private static final long WINDOW_SIZE =
        TimeUnit.SECONDS.toNanos(10);

    @BeforeEach
    void setUp() {

        clock = new FakeClock();

        strategy = new FixedWindowStrategy(
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
    }

    // 3. Requests beyond the limit should be rejected

    @Test
    void shouldRejectRequestsWhenLimitIsReached() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertFalse(
            strategy.allow("user1").isAllowed()
        );
    }

    // 4. Counter should reset at the window boundary

    @Test
    void shouldResetCounterAfterWindowExpires() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        clock.advanceSeconds(10);

        assertEquals(
            LIMIT,
            strategy.getRemainingCapacity("user1")
        );

        assertTrue(
            strategy.allow("user1").isAllowed()
        );
    }

    // 5. Counter should not reset before the boundary

    @Test
    void shouldNotResetCounterBeforeWindowExpires() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        clock.advanceMillis(9999);

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        assertFalse(
            strategy.allow("user1").isAllowed()
        );
    }

    // 6. Multiple elapsed windows should reset the counter

    @Test
    void shouldResetAfterMultipleWindowsHavePassed() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        // Advance 25 seconds (more than two windows).
        clock.advanceSeconds(25);

        assertEquals(
            LIMIT,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 7. Different users should have independent counters

    @Test
    void shouldMaintainIndependentCountersForDifferentUsers() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

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

    // 8. Invalid user keys should be rejected

    @Test
    void shouldRejectNullOrEmptyUserKey() {

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
    }

    // 9. Invalid configurations should be rejected

    @Test
    void shouldRejectInvalidConfigurations() {

        assertThrows(
            IllegalArgumentException.class,
            () -> new FixedWindowStrategy(
                0, WINDOW_SIZE, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new FixedWindowStrategy(
                -1, WINDOW_SIZE, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new FixedWindowStrategy(
                LIMIT, 0, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new FixedWindowStrategy(
                LIMIT, -1, clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new FixedWindowStrategy(
                LIMIT, WINDOW_SIZE, null
            )
        );
    }
}
