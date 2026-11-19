
package test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import time.Clock;

public class FakeClock implements Clock {

    private final AtomicLong currentTimeNanos;

    public FakeClock() {
        this(0);
    }

    public FakeClock(long initialTimeNanos) {
        this.currentTimeNanos = new AtomicLong(initialTimeNanos);
    }

    @Override
    public long nanoTime() {
        return currentTimeNanos.get();
    }

    
}
