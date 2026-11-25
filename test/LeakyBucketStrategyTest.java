
package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.LeakyBucket.LeakyBucketStrategy;

class LeakyBucketStrategyTest {

    private LeakyBucketStrategy strategy;
    private FakeClock clock;

    @BeforeEach
    void setUp() {

        clock = new FakeClock();

        strategy = new LeakyBucketStrategy(
            3,
            1.0,
            clock
        );
    }

    // 1. New user should have full capacity

    @Test
    void shouldReturnFullCapacityForNewUser() {

        assertEquals(
            3,
            strategy.getRemainingCapacity("new-user")
        );
    }

    // 2. Requests within capacity should be allowed

    @Test
    void shouldAllowRequestsWithinCapacity() {

        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
    }

    // 3. Requests should be rejected when bucket is full

    @Test
    void shouldRejectRequestsWhenBucketIsFull() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertFalse(
            strategy.allow("user1").isAllowed()
        );
    }

    // 4. Water level should decrease after one second

    @Test
    void shouldLeakWaterAfterOneSecond() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        clock.advanceSeconds(1);

        assertEquals(
            1,
            strategy.getRemainingCapacity("user1")
        );

        assertTrue(
            strategy.allow("user1").isAllowed()
        );
    }

    // 5. Partial leakage should be handled correctly

    @Test
    void shouldSupportPartialWaterLeakage() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        // 0.5 units of water should leak.
        clock.advanceMillis(500);

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        // Another 0.5 units should leak.
        clock.advanceMillis(500);

        assertEquals(
            1,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 6. Water level should never become negative

    @Test
    void shouldNotLeakBelowZero() {

        // Bucket starts empty.
        clock.advanceSeconds(10);

        assertEquals(
            3,
            strategy.getRemainingCapacity("user1")
        );
    }

    // 7. Different users should have independent buckets

    @Test
    void shouldMaintainIndependentBucketsForDifferentUsers() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        assertEquals(
            3,
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
            () -> strategy.allow("   ")
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
            () -> new LeakyBucketStrategy(0, 1.0, clock)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new LeakyBucketStrategy(3, 0.0, clock)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new LeakyBucketStrategy(3, -1.0, clock)
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new LeakyBucketStrategy(
                3,
                Double.POSITIVE_INFINITY,
                clock
            )
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> new LeakyBucketStrategy(3, 1.0, null)
        );
    }
}
