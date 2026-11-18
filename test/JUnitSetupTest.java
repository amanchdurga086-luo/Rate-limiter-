package test;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JUnitSetupTest {

    @Test
    void junitShouldWork() {

        int actual = 10;

        assertEquals(10, actual);

        assertTrue(actual > 0);
    }
}
