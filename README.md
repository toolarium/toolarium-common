[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-common/1.1.0)](https://search.maven.org/artifact/com.github.toolarium/toolarium-common/1.1.0/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-common/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-common)

# toolarium-common

Java library with common missing data structures and helpers.
Some of the classes of the [jpTools](https://jptools.sourceforge.net/) have been adopted with the permission of the project.


## Built With

* [cb](https://github.com/toolarium/common-build) - The toolarium common build

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-common/tags).


## Installation

### Gradle

```groovy
dependencies {
    implementation "com.github.toolarium:toolarium-common:1.1.0"
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-common</artifactId>
    <version>1.1.0</version>
</dependency>
```


---

## Major Concepts

### Security — `SecuredValue` / `SecuredSecretValue`

The security package provides two complementary tools for handling sensitive data safely.

#### `ISecuredValue<T>` — masked display wrapper

`SecuredValue<T>` wraps any value and replaces it with a configurable mask string (default `"..."`) whenever
`toString()`, logging, or serialization would reveal it. The real value is only accessible programmatically
via `getValue()`. It implements `AutoCloseable` and `Destroyable` so the real value can be zeroed on demand.

```java
// via factory (recommended)
ISecuredValue<String> token = SecuredValueFactory.getInstance().create(apiToken);
log.info("Token: {}", token);   // logs: Token: ...
String real = token.getValue(); // access the real value

// with custom mask
ISecuredValue<String> token2 = SecuredValueFactory.getInstance().create(apiToken, "[REDACTED]");

// try-with-resources to zero on close
try (ISecuredValue<String> t = SecuredValueFactory.getInstance().create(apiToken)) {
    callApi(t.getValue());
}
// value nulled after close()
```

#### `ISecuredSecretValue` — off-heap secret storage

`SecuredSecretValue` combines display masking with off-heap memory protection via `SecureSecret`. The secret
is **never stored on the Java heap** — it lives in native memory (`ByteBuffer.allocateDirect`) and is
deterministically zeroed when `close()` or `destroy()` is called.

The secret is consumed through short-lived lambda callbacks that receive a temporary copy, zeroed automatically
after the function returns:

```java
// from char[] — array is zeroed by the constructor
try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(passwordChars)) {
    log.info("Credential: {}", secret); // logs: Credential: ...

    // consume as byte[] — copy is zeroed after the lambda returns
    SecretKeySpec key = secret.use(bytes -> new SecretKeySpec(bytes, "AES"));

    // consume as char[] — useful for PBE APIs
    PBEKeySpec spec = secret.useChars(chars -> new PBEKeySpec(chars, salt, 65536, 256));
}
// off-heap memory is zeroed here
```

```java
// from byte[]
try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(rawBytes)) {
    boolean ok = secret.use(bytes -> authenticate(bytes));
}
```

#### `SecureSecret` — low-level off-heap container

`SecureSecret` is the underlying off-heap container. Use `ISecuredSecretValue` / `SecuredValueFactory` in
application code; use `SecureSecret` directly only when you need full control.

Key properties:
- Stores data in `ByteBuffer.allocateDirect` — the JVM never copies or GC-moves it.
- All constructors take **ownership** of the input array: it is zeroed before the constructor returns.
- Thread-safe: concurrent reads via shared read lock; `destroy()` uses an exclusive fair write lock.
- Registers a `Cleaner` safety net — memory is zeroed even if `close()` is never called.
- `toString()` always returns `"[SecureSecret]"`.
- `equals()` is identity-based; use `contentEquals(SecureSecret)` for constant-time content comparison.

```java
try (SecureSecret secret = new SecureSecret(passwordChars)) {
    byte[] derivedKey = secret.use(bytes -> PBKDF2.derive(bytes, salt, iterations));
}
```

#### `SecuredValueFactory`

Singleton factory — the single entry point for creating both masked values and secrets:

| Method | Returns |
|--------|---------|
| `create(value)` | `ISecuredValue<T>` with default mask `"..."` |
| `create(value, mask)` | `ISecuredValue<T>` with custom mask |
| `createSecret(byte[])` | `ISecuredSecretValue` backed by off-heap storage |
| `createSecret(byte[], mask)` | same, with custom mask |
| `createSecret(char[])` | `ISecuredSecretValue` (UTF-8 encoded off-heap) |
| `createSecret(char[], mask)` | same, with custom mask |


---

### Version Management — `SemanticVersion`

Parses, compares, and manipulates [SemVer](https://semver.org) version strings. Supports strict SemVer and
npm-style range expressions.

```java
SemanticVersion v = new SemanticVersion("1.2.3-beta.1+sha123");
v.getMajor();       // 1
v.getMinor();       // 2
v.getPatch();       // 3
v.getSuffixTokens();// ["beta", "1"]
v.getBuild();       // "sha123"
v.isStable();       // false (has suffix)

// Comparisons
v.isGreaterThan("1.2.2");          // true
v.isLowerThanOrEqualTo("2.0.0");   // true
v.diff("1.3.0");                   // VersionDiff.MINOR

// Immutable builders
v.withIncrementPatch();            // 1.2.4-beta.1+sha123
v.nextMajor();                     // 2.0.0
v.withClearedSuffixAndBuild();     // 1.2.3

// npm range satisfaction (parse with VersionType.NPM)
SemanticVersion npm = new SemanticVersion("1.2.3", VersionType.NPM);
npm.satisfies("^1.0.0");   // true
npm.satisfies("~1.2.0");   // true
npm.satisfies(">=2.0.0");  // false
npm.satisfies("1.x || 2.x"); // true

// Sorting / bulk conversion
List<String> invalidVersions = new ArrayList<>();
List<SemanticVersion> sorted = SemanticVersion.convert(rawList, invalidVersions);
```

**`VersionType`**: `STRICT` (default, full `major.minor.patch` required) or `NPM` (partial versions and range
expressions allowed).

**`VersionDiff`**: enum returned by `diff()` — one of `NONE`, `MAJOR`, `MINOR`, `PATCH`, `SUFFIX`, `BUILD`.


---

### `ByteArray`

A mutable, auto-growing byte buffer that implements `WritableByteChannel` and `ReadableByteChannel`.
Useful for building binary data without repeated array copies.

```java
ByteArray buf = new ByteArray();
buf.append("Hello").append(ByteArray.SPACE).append("World");
buf.append((byte) 0x0A);

// Searching
buf.indexOf(ByteArray.SPACE);   // 5
buf.startsWith(new ByteArray("Hello")); // true

// Modification
buf.trim((byte) ' ');
buf.replace(ByteArray.NL, ByteArray.CR);
buf.chopRight(1);   // remove last byte
buf.reverse();

// Conversion
String s  = buf.toString();
byte[] b  = buf.toBytes();
String hex = buf.toHex();
long   l  = buf.toLong();

// NIO channel usage
WritableByteChannel ch = buf;
ch.write(byteBuffer);
```

Pre-defined constants: `ByteArray.NL`, `CR`, `TAB`, `SPACE`, `DOT`, `EMPTY`.


---

### `StatisticCounter`

Thread-safe accumulator that computes running statistics for a stream of numbers in a single pass.
Average and variance use **Welford's online algorithm** (and Chan's parallel algorithm when merging),
which is numerically stable even for large values with a small variance. Negative values are fully supported.
Concurrent reads run in parallel; writes are exclusive (`ReadWriteLock`).

```java
StatisticCounter stats = new StatisticCounter();
for (long measurement : measurements) {
    stats.add(measurement);
}

stats.getCounter();           // number of samples
stats.getMinValue();          // minimum
stats.getMaxValue();          // maximum
stats.getSum();               // total
stats.getAverage();           // mean
stats.getVariance();          // variance
stats.getStandardDeviation(); // std dev
stats.getRange();             // max - min

// Merge two counters (Chan's algorithm — numerically stable)
StatisticCounter combined = new StatisticCounter();
combined.add(stats);

// Clone a snapshot
StatisticCounter snapshot = stats.clone();

System.out.println(stats.toString("Request latency (ms):"));
```


---

### `PercentileHistogram`

Streaming percentile estimator backed by a log-scale histogram. Estimates P50–P99.9 with a guaranteed
relative error of less than 0.1% of the value. Constant series (e.g. all measurements identical) are
reported exactly.

**The constructor argument is the largest value you need to measure precisely.** The histogram sizes its
memory to guarantee < 0.1% relative error up to that value. Whether you insert 100 or 100 million samples,
the footprint stays the same (memory is allocated lazily on first use). Values above the configured maximum
are still counted but land in the same last bucket — they are included in percentile calculations with
reduced precision. Only negative values are rejected.

**Sizing guide** — pick the constructor argument that matches your measurement domain:

| I measure… | Constructor argument | Histogram footprint |
|------------|----------------------|---------------------|
| Latency up to ~60 s in ms | `60_000` | ~64 KB |
| Latency up to ~1 s in ns | `1_000_000_000L` | ~176 KB |
| Memory up to 16 GB in bytes | `16L * 1024 * 1024 * 1024` | ~208 KB |
| Anything up to 64 TB *(default)* | — | ~304 KB |

```java
// Measuring JVM heap usage — values range from 0 to at most 16 GB
PercentileHistogram histogram = new PercentileHistogram(16L * 1024 * 1024 * 1024);
// histogram's own footprint: ~208 KB, fixed regardless of how many samples are added

// Every 5 seconds: record the current heap reading
histogram.add(currentMemoryBytes);

// Every 10 minutes: read percentiles, then reset for the next window
long p50 = histogram.getValueAtQuantile(0.50);
long p99 = histogram.getValueAtQuantile(0.99);
histogram.clear(); // resets counts; the 208 KB stays allocated for reuse
```

Thread-safe: concurrent reads (quantile queries) run in parallel; writes are exclusive (`ReadWriteLock`).


---

### `BandwidthThrottling`

Rate-limits a processing loop to a configurable number of calls per second. Internally sleeps in 100 ms
increments until the measured throughput drops back below the limit.

```java
// Throttle to 500 calls/second
BandwidthThrottling throttle = new BandwidthThrottling(500);

for (Item item : items) {
    throttle.bandwidthCheck(); // blocks when rate is exceeded
    process(item);
}

// Inspect behaviour
throttle.getCounter();                  // total calls so far
throttle.getBandwidthStatisticCounter();// stats on measured bandwidth
throttle.getSleepStatisticCounter();    // stats on wait times
System.out.println(throttle);          // human-readable summary

// Change limit at runtime
throttle.setBandwidth(1000);
throttle.reset();
```

`BandwidthThrottling.NO_BANDWIDTH` (`-1`) disables throttling.


---

### `ObjectLockManager`

Key-based locking that allows at most N concurrent holders of the same logical key. Useful for
throttling concurrent access to external resources keyed by identifier.

```java
ObjectLockManager mgr = new ObjectLockManager(/* maxLockSize */ 3, /* unlockTimeoutMs */ 5000L);

String key = "resource-id-42";
mgr.lock(key);
try {
    accessResource(key);
} finally {
    mgr.unlock(key);
}

// Inspect
mgr.getLockStatistic();         // StatisticCounter for lock acquisitions
mgr.getUnlockStatistic();       // StatisticCounter for unlocks
mgr.getIgnoreLockStatistic();   // StatisticCounter for lock-size-exceeded cases
```


---

### `StackTrace`

Utility for inspecting the current call stack, filtering out JVM internals and test-framework frames.

```java
// Get filtered stack as a 2D array [frame][field]
String[][] trace = StackTrace.getTrace();

// Get as list of StackTraceElement
List<StackTraceElement> elements = StackTrace.getStackTrace(StackTrace.DEFAULT_EXCLUDES);

// Convert a Throwable to string
String dump = StackTrace.toString(exception);

// Walk frames with a consumer
StackTrace.forEachElement(StackTrace.DEFAULT_EXCLUDES, el -> log.debug(el.toString()));
```

`DEFAULT_EXCLUDES` filters: `java.base/`, `sun.*`, `com.sun.*`, `java.lang.reflect.*`, JUnit, TestNG, Gradle,
and Eclipse JDT frames.


---

### `CompareMaps`

Computes the difference between two maps (or sorted maps), identifying entries that are only in the left map,
only in the right map, or present in both with different values.

```java
Map<String, Integer> left  = Map.of("a", 1, "b", 2, "c", 3);
Map<String, Integer> right = Map.of("b", 2, "c", 99, "d", 4);

IMapDifference<String, Integer> diff = CompareMaps.getInstance().difference(left, right);

diff.entriesOnlyOnLeft();       // {a=1}
diff.entriesOnlyOnRight();      // {d=4}
diff.entriesInCommon();         // {b=2}
diff.entriesDiffering();        // {c=(3, 99)}

// IValueDifference gives both sides
diff.entriesDiffering().get("c").leftValue();  // 3
diff.entriesDiffering().get("c").rightValue(); // 99

// SortedMap variant
ISortedMapDifference<String, Integer> sortedDiff =
    CompareMaps.getInstance().difference(new TreeMap<>(left), new TreeMap<>(right));
```


---

### `PropertyExpander`

Expands `${variable}` and `$variable` placeholders in strings from system properties, environment variables,
or a custom `Properties` object.

```java
System.setProperty("app.name", "my-app");

String result = PropertyExpander.getInstance().expand("Service: ${app.name}");
// result: "Service: my-app"

// With explicit properties
Properties props = new Properties();
props.setProperty("env", "prod");
String r2 = PropertyExpander.getInstance().expand("Env: ${env}", props);
// r2: "Env: prod"
```


---

### Utilities

| Class | Description |
|-------|-------------|
| `StringUtil` | String padding, splitting, truncation helpers |
| `TextUtil` | Constants (`NL`, `CR`, `TAB`) and text manipulation |
| `DateUtil` | Date parsing, formatting, and arithmetic |
| `ByteUtil` | Low-level byte array operations (indexOf, toLong, …) |
| `RoundUtil` | Numeric rounding with configurable precision |
| `RandomGenerator` | Cryptographically-seeded random string / byte generation |
| `RegularExpressionUtil` | Regex convenience helpers |
| `EnumUtil` | Enum lookup by name/ordinal with fallback |
| `MapUtil` | Map creation and merge helpers |
| `FileUtil` | File read/write helpers |
| `ChannelUtil` | NIO channel copy utilities |
| `ClassPathUtil` | Classpath resource resolution |
| `ClassInstanceUtil` | Reflective object instantiation |
| `ReflectionUtil` | Field/method access via reflection |
| `ExceptionWrapper` | Wrap checked exceptions into unchecked ones |
| `ThreadUtil` | `sleep` without checked exceptions, thread naming |
| `Assert` | Precondition checks (non-null, non-empty, …) |
| `JavaVersion` | Detect and compare the current JVM version |
| `TimeDifferenceFormatter` | Format millisecond durations as human-readable strings |
| `MemoryUtil` | JVM memory usage reporting |
| `ToolariumInstrumentation` | Java agent instrumentation bootstrap |
