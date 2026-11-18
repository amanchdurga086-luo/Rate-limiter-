
package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import Strategy.TokenBucket.TokenBucketStrategy;
import time.Clock;
import time.SystemClock;

class TokenBucketStrategyTest {

    private TokenBucketStrategy strategy;

    // test isolation
    @BeforeEach
    void setUp() {
        Clock clock = new SystemClock();

        strategy = new TokenBucketStrategy(
            3,
            1.0,
            clock
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

    
}
