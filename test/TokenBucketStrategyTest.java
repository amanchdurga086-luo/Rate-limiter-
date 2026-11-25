
package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.TokenBucket.TokenBucketStrategy;

class TokenBucketStrategyTest {

    private TokenBucketStrategy strategy;
    private FakeClock clock;

    @BeforeEach
    void setUp() {

        clock = new FakeClock();

        strategy = new TokenBucketStrategy(
            3,
            1.0,
            clock
        );
    }

    @Test
    void shouldReturnFullCapacityForNewUser() {

        assertEquals(
            3,
            strategy.getRemainingCapacity("new-user")
        );
    }

    @Test
    void shouldAllowRequestsWithinCapacity() {

        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
        assertTrue(strategy.allow("user1").isAllowed());
    }

    @Test
    void shouldRejectRequestsWhenBucketIsEmpty() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertFalse(strategy.allow("user1").isAllowed());
    }

    @Test
    void shouldMaintainIndependentBucketsForDifferentUsers() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertTrue(strategy.allow("user2").isAllowed());
    }

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
    }

    // NEW TESTS USING FAKE CLOCK

    @Test
    void shouldRefillTokensAfterOneSecond() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        // Advance simulated time by one second.
        clock.advanceSeconds(1);

        assertEquals(
            1,
            strategy.getRemainingCapacity("user1")
        );

        assertTrue(strategy.allow("user1").isAllowed());
    }

    @Test
    void shouldRefillTokensAfterTwoSeconds() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        clock.advanceSeconds(2);

        assertEquals(
            2,
            strategy.getRemainingCapacity("user1")
        );
    }

    @Test
    void shouldNotExceedMaximumCapacityAfterRefill() {

        // Bucket starts full.
        assertEquals(
            3,
            strategy.getRemainingCapacity("user1")
        );

        // Advancing time cannot increase capacity beyond 3.
        clock.advanceSeconds(10);

        assertEquals(
            3,
            strategy.getRemainingCapacity("user1")
        );
    }

    @Test
    void shouldSupportPartialTokenRefill() {

        strategy.allow("user1");
        strategy.allow("user1");
        strategy.allow("user1");

        // Half a token should not count as a full token.
        clock.advanceMillis(500);

        assertEquals(
            0,
            strategy.getRemainingCapacity("user1")
        );

        // Total elapsed time is now one second.
        clock.advanceMillis(500);

        assertEquals(
            1,
            strategy.getRemainingCapacity("user1")
        );
    }
}
