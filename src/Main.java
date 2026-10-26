import Result.RateLimitResult;
import time.Clock;
import time.SystemClock;
import util.RateLimiter;
import util.RateLimiterFactory;
import util.RateLimiterType;

public class Main {

    private static final long CAPACITY = 5;
    private static final double RATE_PER_SECOND = 2.0;

    public static void main(String[] args) {

        // Change this to switch algorithms.
        RateLimiterType type = RateLimiterType.TOKEN_BUCKET;

        Clock clock = new SystemClock();

        RateLimiter rateLimiter = RateLimiterFactory.create(
                type,
                CAPACITY,
                RATE_PER_SECOND,
                clock);

        System.out.println("Algorithm: " + type);
        System.out.println("Capacity: " + CAPACITY);
        System.out.println("Rate: " + RATE_PER_SECOND);
        System.out.println("--------------------------------");

        for (int i = 1; i <= 7; i++) {

            RateLimitResult result = rateLimiter.allow("user-1");

            System.out.println(
                    "Request " + i + " -> " + result);
        }

        System.out.println("\nWaiting for 2 seconds...");

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Main thread interrupted.");
            return;
        }

        System.out.println(
                "After 2 requests: " +
                        rateLimiter.getRemainingCapacity("user-1"));
    }
}