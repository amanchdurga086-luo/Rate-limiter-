# Rate Limiter \| Java

A thread-safe, extensible Rate Limiter implementation in Java supporting
multiple rate-limiting algorithms. This project explores Low-Level
Design (LLD), concurrency, object-oriented design principles, and
time-based request control.

**Project Status:** In Progress 🚧

## 1. Overview

A Rate Limiter controls how many requests a user or client can make
within a specified time period.

This project implements four rate-limiting algorithms using a common
strategy interface. It supports independent rate limits for different
users and protects shared state during concurrent requests.

The project is being developed with an emphasis on clean architecture,
extensibility, testability, and practical backend engineering concepts.

## 2. Implemented Algorithms

### 1. Token Bucket

-   Maintains a bucket of available tokens.
-   Each accepted request consumes one token.
-   Tokens are replenished at a configurable rate.
-   Supports burst traffic up to the configured capacity.

### 2. Leaky Bucket

-   Maintains a water level representing outstanding work.
-   Each accepted request increases the water level.
-   Water leaks continuously at a configurable rate.
-   Rejects requests when the bucket reaches capacity.

*Note: This implementation uses an admission-control model rather than a
request queue.*

### 3. Fixed Window Counter

-   Divides time into fixed-duration windows.
-   Maintains a request counter for each user.
-   Resets the counter when a new window begins.
-   Simple and memory-efficient implementation.

### 4. Sliding Window Counter

-   Maintains previous-window and current-window counters.
-   Uses weighted previous-window requests.
-   Reduces the boundary-burst problem of Fixed Window.
-   Provides an approximate rolling-window request count.

## 3. Technology Stack

  Technology          Purpose
  ------------------- -----------------------------------
  Java                Core implementation
  OOP                 Object-oriented design
  JUnit 5             Automated testing
  ConcurrentHashMap   Thread-safe user-state management
  AtomicLong          Thread-safe simulated clock
  ExecutorService     Concurrent request testing
  CountDownLatch      Coordinating concurrent tests
  FakeClock           Deterministic time-based testing
  VS Code             Development environment

## 4. Architecture and Design Patterns

The project follows a Strategy-based architecture.

### Strategy Pattern

A common `RateLimiterStrategy` interface allows different algorithms to
implement the same rate-limiting contract.

Each algorithm maintains its own implementation and internal state.

### Factory Pattern

`RateLimiterFactory` centralizes the creation of rate limiter instances
using their respective configuration classes.

### Facade

`RateLimiter` provides a common entry point for interacting with the
selected rate-limiting strategy.

### Thread Safety

-   `ConcurrentHashMap` maintains independent state for each user.
-   Synchronized methods protect mutable per-user bucket and counter
    state.
-   `AtomicLong` supports thread-safe simulated time advancement.

### Clock Abstraction

A common `Clock` interface separates time management from algorithm
implementation.

Two implementations are used:

-   `SystemClock` -- uses actual system time.
-   `FakeClock` -- allows tests to advance simulated time without
    waiting.

## 5. Project Structure

```text
RateLimiter/
├── lib/
│   └── junit-platform-console-standalone-1.14.0.jar
│
├── src/
│   ├── config/
│   │   ├── FixedWindowConfig.java
│   │   ├── LeakyBucketConfig.java
│   │   ├── SlidingWindowConfig.java
│   │   └── TokenBucketConfig.java
│   │
│   ├── Result/
│   │   └── RateLimitResult.java
│   │
│   ├── Strategy/
│   │   ├── fixedwindow/
│   │   │   ├── FixedWindowCounter.java
│   │   │   └── FixedWindowStrategy.java
│   │   │
│   │   ├── LeakyBucket/
│   │   │   ├── LeakyBucket.java
│   │   │   └── LeakyBucketStrategy.java
│   │   │
│   │   ├── slidingwindow/
│   │   │   ├── SlidingWindowCounter.java
│   │   │   └── SlidingWindowStrategy.java
│   │   │
│   │   ├── TokenBucket/
│   │   │   ├── TokenBucketStrategy.java
│   │   │   └── UserBucket.java
│   │   │
│   │   └── RateLimiterStrategy.java
│   │
│   ├── time/
│   │   ├── Clock.java
│   │   └── SystemClock.java
│   │
│   ├── util/
│   │   ├── RateLimiter.java
│   │   └── RateLimiterFactory.java
│   │
│   └── Main.java
│
└── test/
│   ├── FakeClock.java
│   ├── JUnitSetupTest.java
│   ├── TokenBucketStrategyTest.java
│   ├── LeakyBucketStrategyTest.java
│   ├── FixedWindowStrategyTest.java
│   ├── SlidingWindowStrategyTest.java
│   └── RateLimiterConcurrencyTest.java
├── README.md
└── .gitignore
```
## 6. Testing

The project uses JUnit 5 for automated testing.

### Current Test Coverage

  Test Category                   Tests
  ---------------------------- --------
  Token Bucket                        9
  Leaky Bucket                        9
  Fixed Window                        9
  Sliding Window Counter              9
  Same-user concurrency               4
  Different-user concurrency          4
  JUnit setup                         1
  **Total**                      **45**

**Latest test result: 45 tests passed, 0 failed.**

### Testing Scenarios

-   Request acceptance and rejection.
-   Initial and remaining capacity.
-   Token replenishment.
-   Water leakage.
-   Window transitions.
-   Previous-window weighting.
-   Independent user state.
-   Invalid configurations and user keys.
-   Concurrent requests from the same user.
-   Concurrent requests from different users.

### Deterministic Testing

`FakeClock` allows time-dependent behavior to be tested without using
`Thread.sleep()`.

This makes time-based tests faster and more predictable.

## 7. How to Run

This project currently uses plain Java without Maven or Gradle.

### Prerequisites

-   Java Development Kit (JDK).
-   VS Code or another Java IDE.
-   JUnit Platform Console Standalone JAR.

### Compile Source Files (Windows PowerShell)

``` powershell
$mainFiles = Get-ChildItem src -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

javac -d out\main $mainFiles
```

### Compile Test Files

``` powershell
$testFiles = Get-ChildItem test -Recurse -Filter *.java |
    ForEach-Object { $_.FullName }

javac -d out\test -cp "out\main;lib\junit-platform-console-standalone-1.14.0.jar" $testFiles
```

### Execute Tests

``` powershell
java -jar lib\junit-platform-console-standalone-1.14.0.jar execute --class-path "out\main;out\test" --scan-class-path
```

## 8. Current Development Roadmap

-   [x] Implement Token Bucket.
-   [x] Implement Leaky Bucket.
-   [x] Implement Fixed Window Counter.
-   [x] Implement Sliding Window Counter.
-   [x] Introduce Strategy Pattern.
-   [x] Introduce Factory Pattern.
-   [x] Implement per-user rate limiting.
-   [x] Add Clock abstraction.
-   [x] Implement FakeClock for deterministic testing.
-   [x] Configure JUnit 5.
-   [x] Add functional tests for all four algorithms.
-   [x] Add same-user concurrency tests.
-   [x] Add different-user concurrency tests.
-   [ ] Improve retry-after calculations.
-   [ ] Expand edge-case and boundary testing.
-   [ ] Improve concurrency stress testing.
-   [ ] Add inactive-user state cleanup.
-   [ ] Benchmark algorithm performance.
-   [ ] Explore distributed rate limiting with Redis.
-   [ ] Improve project documentation and deployment readiness.

## 9. Learning Objectives

Through this project, I am exploring:

-   Low-Level Design and object-oriented programming.
-   Strategy and Factory design patterns.
-   SOLID principles.
-   Thread safety and concurrent programming.
-   Time-based algorithms.
-   Automated and deterministic testing.
-   Backend system design and scalability.
-   Trade-offs between different rate-limiting algorithms.

## 10. Future Improvements

The next development phase focuses on accurate retry-after calculations,
stronger concurrency testing, memory management, and performance
optimization.

A future distributed implementation will explore Redis-based rate
limiting and atomic operations for coordinating requests across multiple
application instances.

------------------------------------------------------------------------

**Note:** This project is actively under development. The roadmap and
documentation will be updated as new features, improvements, and tests
are implemented.
