[![License: MPL 2.0](https://img.shields.io/badge/License-MPL_2.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.toolarium/toolarium-common/1.2.1)](https://search.maven.org/artifact/com.github.toolarium/toolarium-common/1.2.1/jar)
[![javadoc](https://javadoc.io/badge2/com.github.toolarium/toolarium-common/javadoc.svg)](https://javadoc.io/doc/com.github.toolarium/toolarium-common)

# toolarium-common

Java library with common missing data structures and helpers. Its only dependency is `slf4j-api`.
Some of the classes of the [jpTools](https://jptools.sourceforge.net/) have been adopted with the permission of the project.


## Built With

* [cb](https://github.com/toolarium/common-build): the toolarium common build

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/toolarium/toolarium-common/tags).


## Installation

### Gradle

```groovy
dependencies {
    implementation "com.github.toolarium:toolarium-common:1.2.1"
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.toolarium</groupId>
    <artifactId>toolarium-common</artifactId>
    <version>1.2.1</version>
</dependency>
```


---

## Overview

| Package | Content |
|---------|---------|
| `security` | `SecuredValue`, `SecuredSecretValue`, `SecureSecret`, `SecuredValueFactory`: masked values and off-heap secrets |
| `version` | `SemanticVersion`, `Version`, `VersionType`, `VersionDiff`: parse, compare, filter and change versions |
| `common` | `ByteArray`: growing byte buffer and NIO channel |
| `statistic` | `StatisticCounter`, `PercentileHistogram`: running statistics and percentiles |
| `bandwidth` | `BandwidthThrottling`, `IBandwidthThrottlingTokenStore`: limit calls per second, also over multiple instances |
| `object` | `ObjectLockManager`: lock elements against parallel processing |
| `stacktrace` | `StackTrace`, `StackTraceElement`: read, filter and parse call stacks |
| `compare.map` | `CompareMaps`: differences of two maps |
| `memory` | `MemoryUtil`, `MemoryUsageCalculator`: memory footprint of objects and JVM memory |
| `runtime` | `ToolariumInstrumentation`: optional access to the toolarium-java-agent |
| `datetime` | `DateTimeFormat`: HTTP date formats (RFC 1123, RFC 1036, asctime, cookie) |
| `formatter` | `TimeDifferenceFormatter`: durations as readable text |
| `util` | `FileUtil`, `PropertyExpander`, `RandomGenerator`, `StringUtil`, `ClassPathUtil` and more, see [Utilities](#utilities) |


---

## Major Concepts

### Security: `SecuredValue` / `SecuredSecretValue`

The security package provides two complementary tools for handling sensitive data safely.

#### `ISecuredValue<T>`: masked display wrapper

`SecuredValue<T>` wraps any value and shows a mask (default `"..."`) instead of the value in `toString()`
and therefore in logs. The value itself is never returned by `toString()`, also without a mask. The real value
is only accessible via `getValue()`. The value is not serialized. It implements `AutoCloseable` and `Destroyable`:
`close()` / `destroy()` remove the value, a `char[]` or `byte[]` value is wiped.

```java
// via factory (recommended)
ISecuredValue<String> token = SecuredValueFactory.getInstance().create(apiToken);
log.info("Token: {}", token);   // logs: Token: ...
String real = token.getValue(); // access the real value

// with custom mask
ISecuredValue<String> token2 = SecuredValueFactory.getInstance().create(apiToken, "[REDACTED]");

// try-with-resources removes the value on close
try (ISecuredValue<char[]> password = SecuredValueFactory.getInstance().create(passwordChars)) {
    login(password.getValue());
}
// the char[] is wiped here
```

#### `ISecuredSecretValue`: off-heap secret storage

`SecuredSecretValue` combines display masking with off-heap memory protection via `SecureSecret`. The secret
is **never stored on the Java heap**: it lives in native memory (`ByteBuffer.allocateDirect`) and is
deterministically zeroed when `close()` or `destroy()` is called.

The secret is consumed through short-lived lambda callbacks that receive a temporary copy, zeroed automatically
after the function returns:

```java
// from char[]: the array is zeroed by the factory
try (ISecuredSecretValue secret = SecuredValueFactory.getInstance().createSecret(passwordChars)) {
    log.info("Credential: {}", secret); // logs: Credential: ...

    // consume as byte[]: the copy is zeroed after the lambda returns
    SecretKeySpec key = secret.use(bytes -> new SecretKeySpec(bytes, "AES"));

    // consume as char[]: useful for PBE APIs
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

#### `SecureSecret`: low-level off-heap container

`SecureSecret` is the underlying off-heap container. Use `ISecuredSecretValue` / `SecuredValueFactory` in
application code; use `SecureSecret` directly only when you need full control.

Key properties:
- Stores data in `ByteBuffer.allocateDirect`: the JVM never copies or moves it.
- All constructors take **ownership** of the input array: it is zeroed before the constructor returns, also if encoding fails.
- Thread-safe: concurrent reads via shared read lock; `destroy()` uses an exclusive fair write lock.
- Registers a `Cleaner` safety net: memory is zeroed even if `close()` is never called.
- `toString()` always returns `"[SecureSecret]"`.
- `equals()` is identity-based; use `contentEquals(SecureSecret)` for constant-time content comparison.
- `toByteArray()`, `toCharArray()` and `toInsecureString()` return heap copies, use them only if an API requires it.

```java
try (SecureSecret secret = new SecureSecret(passwordChars)) {
    byte[] derivedKey = secret.use(bytes -> PBKDF2.derive(bytes, salt, iterations));
}
```

#### `SecuredValueFactory`

Singleton factory, the single entry point for creating both masked values and secrets:

| Method | Returns |
|--------|---------|
| `create(value)` | `ISecuredValue<T>` with default mask `"..."` |
| `create(value, mask)` | `ISecuredValue<T>` with custom mask |
| `createSecret(byte[])` | `ISecuredSecretValue` backed by off-heap storage |
| `createSecret(byte[], mask)` | same, with custom mask |
| `createSecret(char[])` | `ISecuredSecretValue` (UTF-8 encoded off-heap) |
| `createSecret(char[], mask)` | same, with custom mask |


---

### Version Management: `SemanticVersion` / `Version`

#### `SemanticVersion`

Parses, compares, and manipulates [SemVer](https://semver.org) version strings. Ordering and equality follow
SemVer: build metadata is ignored. Supports npm-style range expressions.

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
v.isEquivalentTo("1.2.3-beta.1");  // true (build metadata is ignored)
v.diff("1.3.0");                   // VersionDiff.MINOR

// Immutable builders
v.withIncrementPatch();            // 1.2.4-beta.1+sha123
v.nextMajor();                     // 2.0.0
v.withClearedSuffixAndBuild();     // 1.2.3
v.withSuffix("rc.1");              // 1.2.3-rc.1+sha123

// npm range satisfaction (parse with VersionType.NPM)
SemanticVersion npm = new SemanticVersion("1.2.3", VersionType.NPM);
npm.satisfies("^1.0.0");           // true
npm.satisfies("~1.2.0");           // true
npm.satisfies(">=2.0.0");          // false
npm.satisfies("1.x || 2.x");       // true
npm.satisfies("1.0.0 - 1.5.0");    // true

// Sorting / bulk conversion
List<String> invalidVersions = new ArrayList<>();
List<SemanticVersion> sorted = SemanticVersion.convert(rawList, invalidVersions);
```

Supported range forms: `*`, wildcard (`3.x`), exact (`1.2.3`), partial (`1.2`), hyphen (`1.2.3 - 2.3.4`),
tilde (`~1.2.3`), caret (`^1.2.3`), comparators (`>=1.0.0 <2.0.0`) and OR (`1.x || 2.x`). An invalid range throws
`IllegalArgumentException`.

#### `Version`

A tolerant version with a suffix per number and a configurable separator, e.g. for Maven or OS versions
like `1.0.0-SNAPSHOT`, `2.1rc1` or `1_2_3`.

```java
Version v = new Version("1.2.3-SNAPSHOT");
v.getMajorNumber();                // 1
v.getPatchSuffix();                // "-SNAPSHOT"
v.isStable();                      // false (SNAPSHOT, alpha, beta and rc are not stable)
v.isGreaterThan("1.2.2");          // true
v.withIncrementMinor();            // 1.3.0
new Version("1_2_3", "_");         // custom separator
```

#### Version types, differences and lists

**`VersionType`**:
- `STRICT` (default): major, minor and patch are required; suffix and build are optional. Extra parts like `1.2.3.4` are rejected.
- `LOOSE`: only the major part is required.
- `NPM`: npm rules, supports `^`, `x`, `*`, `~` and ranges.
- `IVY`: ivy rules, supports dynamic parts like `4.2.+` and ranges.

**`VersionDiff`**: returned by `diff()`, one of `NONE`, `MAJOR`, `MINOR`, `PATCH`, `SUFFIX`, `BUILD`.

`AbstractVersion.sort(list)` sorts a list of versions; `filter(...)` / `invertFilter(...)` keep (or remove) the newest
versions per major, minor and patch, e.g. to clean up old releases.


---

### `ByteArray`

A mutable, auto-growing byte buffer that implements `WritableByteChannel` and `ReadableByteChannel`.
Useful for building binary data without repeated array copies. Strings are converted as UTF-8.

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

Pre-defined read-only constants: `ByteArray.NL`, `CR`, `TAB`, `SPACE`, `DOT`, `EMPTY`.


---

### `StatisticCounter`

Thread-safe accumulator that computes running statistics for a stream of numbers in a single pass.
Average and variance use **Welford's online algorithm** (and Chan's parallel algorithm when merging),
which is numerically stable even for large values with a small variance. Negative values are fully supported;
NaN and infinite values are ignored. Concurrent reads run in parallel; writes are exclusive (`ReadWriteLock`).

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

// Merge two counters (numerically stable)
StatisticCounter combined = new StatisticCounter();
combined.add(stats);

// Clone a snapshot
StatisticCounter snapshot = stats.clone();

System.out.println(stats.toString("Request latency (ms):"));
```

`StatisticCounter` implements the interface `IStatisticCounter`.


---

### `PercentileHistogram`

Streaming percentile estimator backed by a log-scale histogram. Estimates P50 to P99.9 with a guaranteed
relative error of less than 0.1% of the value. Constant series (e.g. all measurements identical) are
reported exactly.

**The constructor argument is the largest value you need to measure precisely.** The histogram sizes its
memory to guarantee < 0.1% relative error up to that value. Whether you insert 100 or 100 million samples,
the footprint stays the same (memory is allocated lazily on first use). Values above the configured maximum
are still counted but land in the same last bucket: they are included in percentile calculations with
reduced precision. Only negative values are rejected.

**Sizing guide**: pick the constructor argument that matches your measurement domain:

| I measure... | Constructor argument | Histogram footprint |
|------------|----------------------|---------------------|
| Latency up to ~60 s in ms | `60_000` | ~64 KB |
| Latency up to ~1 s in ns | `1_000_000_000L` | ~176 KB |
| Memory up to 16 GB in bytes | `16L * 1024 * 1024 * 1024` | ~208 KB |
| Anything up to 64 TB *(default)* | (none) | ~304 KB |

```java
// Measuring JVM heap usage: values range from 0 to at most 16 GB
PercentileHistogram histogram = new PercentileHistogram(16L * 1024 * 1024 * 1024);
// histogram's own footprint: ~208 KB, fixed regardless of how many samples are added

// Every 5 seconds: record the current heap reading
histogram.add(currentMemoryBytes);

// Every 10 minutes: read percentiles, then reset for the next window
long p50 = histogram.getValueAtQuantile(0.50);
long[] p = histogram.getValuesAtQuantiles(0.90, 0.99, 0.999);
histogram.clear(); // resets counts; the 208 KB stays allocated for reuse
```

Thread-safe: concurrent reads (quantile queries) run in parallel; writes are exclusive (`ReadWriteLock`).


---

### `BandwidthThrottling`

Limits a processing loop to a configurable number of calls per second. Every call is checked: a burst passes
at most the calls of one second, afterwards each call waits until it is allowed (at most 60 seconds). The waiting
thread does not block other threads which read the state, and an interrupt stops the wait.

```java
// Throttle to 500 calls/second
BandwidthThrottling throttle = new BandwidthThrottling(500);

for (Item item : items) {
    throttle.bandwidthCheck(); // waits when the rate is exceeded
    process(item);
}

// Inspect behaviour
throttle.getCounter();                  // total calls so far
throttle.getBandwidthStatisticCounter();// statistic of the number of calls of each second
throttle.getSleepStatisticCounter();    // statistic of the wait times
System.out.println(throttle);           // human-readable summary

// Change limit at runtime
throttle.setBandwidth(1000);
throttle.reset();
```

`BandwidthThrottling.NO_BANDWIDTH` (`-1`) disables throttling. `BandwidthThrottling` implements the interface `IBandwidthThrottling`.

#### Bandwidth over multiple instances

To share one limit over multiple processes or servers, pass an `IBandwidthThrottlingTokenStore`. It keeps the
token bucket in a shared place (e.g. Redis with a Lua script) and takes a token atomically. If the store fails,
the local bandwidth of the instance is used (a warning is logged once) and the store is tried again after 5 seconds.
The store is not serialized: after a deserialization set it again with `setTokenStore(...)`.

```java
IBandwidthThrottlingTokenStore store = bandwidth -> redisTokenBucket.tryAcquire(bandwidth); // 0 or the ms to wait
BandwidthThrottling throttle = new BandwidthThrottling(500, store); // 500 calls/second over all instances
```


---

### `ObjectLockManager`

Locks elements (e.g. ids of records) inside the same JVM, so that parallel threads do not process the same
element at the same time. `lock(...)` returns only the elements which could be locked, it never throws.
Optional limits:
- **lock size**: the maximum number of elements locked per call (`null` = no limit).
- **unlock timeout**: an element can not be locked again for this time (ms) after it was unlocked (`null` = no timeout).

```java
IObjectLockManager lockManager = new ObjectLockManager(100, 5_000L);

List<String> locked = lockManager.lock(List.of("order-1", "order-2", "order-3"));
try {
    process(locked);             // only the elements this thread could lock
} finally {
    lockManager.unlock(locked);
}

// Inspect
lockManager.getLockStatistic();          // number of locked elements per call
lockManager.getUnlockStatistic();        // elements refused because of the unlock timeout
lockManager.getIgnoreLockStatistic();    // elements refused because they are already locked
lockManager.getNumberOfLockSizeReached();// how often the lock size was reached
```

`null` elements are ignored.


---

### `StackTrace`

Static helpers to read the current call stack, filter out JVM, test framework and build tool frames and parse
stack traces of exceptions or text.

```java
// Current call stack as text, filtered with DEFAULT_EXCLUDES
String trace = StackTrace.getStackTrace();

// Current call stack, only the last 5 callers
StackTraceElement[] elements = StackTrace.getStackTraceElements(5);

// Log the call stack line by line
StackTrace.processStackTrace(LOG::debug, StackTrace.DEFAULT_EXCLUDES, "Called by: ", 10);

// Parse the stack trace of an exception or of a text
StackTraceElement[] parsed = StackTrace.parseStackTraceElements(exception, 0, 20);
StackTraceElement element = StackTrace.parseStackTraceElement("\tat com.foo.Bar.run(Bar.java:42)");
```

`DEFAULT_EXCLUDES` filters: `java.base/`, `jdk.internal.`, `sun.`, `com.sun.`, `java.lang.reflect.`, TestNG,
JUnit, Gradle and Eclipse JDT frames.


---

### `CompareMaps`

Computes the difference between two maps, identifying entries that are only in the left map,
only in the right map, or present in both with different values. If the left map is a `SortedMap`, its
key comparison is used (e.g. a case-insensitive `TreeMap`) and the result is an `ISortedMapDifference`.

```java
Map<String, Integer> left  = Map.of("a", 1, "b", 2, "c", 3);
Map<String, Integer> right = Map.of("b", 2, "c", 99, "d", 4);

IMapDifference<String, Integer> diff = CompareMaps.getInstance().compare(left, right);

diff.areEqual();                // false
diff.entriesOnlyOnLeft();       // {a=1}
diff.entriesOnlyOnRight();      // {d=4}
diff.entriesInCommon();         // {b=2}
diff.entriesDiffering();        // {c=(3, 99)}

// IValueDifference gives both sides
diff.entriesDiffering().get("c").leftValue();  // 3
diff.entriesDiffering().get("c").rightValue(); // 99

// SortedMap variant
ISortedMapDifference<String, Integer> sortedDiff =
    (ISortedMapDifference<String, Integer>) CompareMaps.getInstance().compare(new TreeMap<>(left), new TreeMap<>(right));
```

The results are implemented by `MapDifference`, `SortedMapDifference` and `ValueDifference`.


---

### `FileUtil`

File helpers for reading, writing, searching and path handling.

```java
FileUtil fileUtil = FileUtil.getInstance();

// Read / write (UTF-8 by default)
String content = fileUtil.readFileContent(Path.of("config.txt"));
fileUtil.writeFileContent(Path.of("out.txt"), "hello");

// Search files whose content matches a regular expression
List<Path> found = fileUtil.searchFiles(rootDir, ".java", "TODO");

// Search and replace: the replacement is inserted literally
List<Path> changed = fileUtil.searchAndReplaceFiles(rootDir, "1\\.2\\.0", "1.2.1", LinkFollowMode.WITHIN_ROOT);

// Paths
fileUtil.simplifyPath("/a/./b/../c");           // "/a/c"
fileUtil.simplifyPath("\\\\server\\share\\dir"); // "//server/share/dir" (network paths are kept)
fileUtil.slashify("C:\\temp\\file.txt");        // "C:/temp/file.txt"
fileUtil.extractURLPath(url);                  // file path of a file: or jar: URL

// Remove
fileUtil.removeFile("old.txt");
fileUtil.removeDirectory(Path.of("tmp"));       // symbolic links are removed, not followed
```

`searchFiles` and `searchAndReplaceFiles`:
- **`LinkFollowMode`** defines how symbolic links are handled: `NONE` (skipped), `WITHIN_ROOT` (followed only if the
  target is inside the root directory) or `ALL` (always followed, the default of the methods without a mode).
- Files which can not be read or are larger than 2 GB are skipped with a warning; `searchAndReplaceFiles` also skips
  files which are not valid UTF-8, so they are never corrupted.
- `searchAndReplaceFiles` writes a file in place (hard links, owner and permissions stay) and restores the original
  content if writing fails, e.g. on a full disk.


---

### `PropertyExpander`

Expands `${NAME}` and `$NAME` references in strings. The value is resolved from the context-based properties,
then from the system properties and at last from the environment variables. A reference which can not be
resolved is kept as it is.

```java
System.setProperty("app.name", "my-app");

String result = PropertyExpander.getInstance().expand("Service: ${app.name}");
// result: "Service: my-app"

// With properties for the current thread only
Properties props = new Properties();
props.setProperty("env", "prod");
PropertyExpanderContextBasedProperties.runWith(props, () -> {
    String r2 = PropertyExpander.getInstance().expand("Env: $env");
    // r2: "Env: prod"
});
```


---

### `RandomGenerator`

Random numbers, strings, GUIDs and dates based on `SecureRandom`. All methods are thread-safe.

```java
RandomGenerator random = RandomGenerator.getInstance();
random.getRandomString(16);                                       // letters and numbers
random.getRandomString(6, random.getValidNumberCharacters());     // e.g. a PIN
random.getRandomNumber(100, false);                               // 1 .. 100
random.nextBytes(32);
random.createUUID();                                              // standard UUID
random.createGUID();
random.getRandomDayInFuture(30);

// Own instance with an own SecureRandom
RandomGenerator own = RandomGenerator.getInstance(SecureRandom.getInstanceStrong());
```

The getters of the valid characters return a copy. The public arrays (e.g. `validNumberCharacters`) are deprecated.


---

### `MemoryUtil` / `ToolariumInstrumentation`

`MemoryUtil` measures the deep memory footprint of an object graph and reports the JVM memory. Measuring objects
requires the toolarium-java-agent (`-javaagent:`); without it `isEnabled()` is false and `memoryUsageOf(...)`
returns `null`. The JVM memory is always available.

```java
MemoryUtil memoryUtil = MemoryUtil.getInstance();
if (memoryUtil.isEnabled()) {
    Long bytes = memoryUtil.memoryUsageOf(myCache);
}

MemoryUtil.RuntimeMemoryInformation info = memoryUtil.getRuntimeMemoryInformation();
info.getUsedMemory();
info.getMaxMemory();

ToolariumInstrumentation.getInstance().isAgentAvailable();   // agent loaded?
```

The default calculator is `MemoryUsageCalculator`. An own implementation of `IMemoryUsageCalculator` can be set with
`MemoryUtil.getInstance().setMemoryUsageCalculator(...)`.


---

### Utilities

| Class | Description |
|-------|-------------|
| `Assert` | Precondition checks (`isTrue`, `isNull`, `isNotEmpty`, `isEqual`, ...) which throw a given exception class; configurable log level and stack trace logging |
| `ByteUtil` | Byte array operations: compare and copy blocks, convert `int` / `long` (big-endian), hex strings, `prettyPrintByteSize` |
| `ChannelUtil` | Copy streams and NIO channels, optionally with a `MessageDigest`; the given streams are not closed |
| `ClassInstanceUtil` | Create instances and arrays by class name, check if a class is available or implements an interface |
| `ClassPathUtil` | Search classes, packages, archives and files in the class path by name or regular expression |
| `DateUtil` | Create, parse and format dates; convert between `Date`, `LocalDate` and `LocalDateTime` |
| `DateTimeFormat` | HTTP date formats (RFC 1123, RFC 1036, asctime, old cookie format); each getter returns a new instance |
| `EnumUtil` | Enum constant by name: trimmed, exact match first, otherwise case-insensitive |
| `ExceptionWrapper` | Convert an exception into another exception class, with optional logging |
| `JavaVersion` | Java versions 1.1 to 28 with class file versions; resolve the version of the JVM or of a class file |
| `MapUtil` | Create (sorted) maps and add maps |
| `ReflectionUtil` | Load classes, create instances, find and call methods via reflection |
| `RegularExpressionUtil` | Compile regular expressions with an LRU cache |
| `RoundUtil` | Round `double` values with a scale, to `int` or `long` |
| `StringUtil` | Repeat, pad, trim, split strings; camel case and snake case conversion |
| `TextUtil` | Format text as block with a maximum line length, with indent and title |
| `ThreadUtil` | `sleep` without checked exception, returns true if the thread was interrupted |
| `TimeDifferenceFormatter` | Format millisecond durations as readable text, e.g. `2sec 999ms` |
