# toolarium-common

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 1.1.0 ] - 2026-09-18
### Added
- Added `ISecuredSecretValue` interface and `SecureSecret` for memory-safe secret storage (off-heap, deterministically zeroed on close/destroy).
- Added `SecuredValueFactory` singleton for creating `ISecuredValue` and `ISecuredSecretValue` instances via `SecuredValueFactory.getInstance().create()` / `createSecret()`.
- Added `MemoryUtil` and `MemoryUsageCalculator` for runtime memory inspection with optional Java agent support.
- Added `ToolariumInstrumentation` in runtime package for optional JVM instrumentation access.

### Changed
- Moved security implementations to `security.impl` sub-package (`SecuredValue<T>`, `SecuredSecretValue`); public API remains in `security` package.
- `SecuredSecretValue` now extends `SecuredValue<SecureSecret>` — `getValue()` returns the `SecureSecret` object directly.
- `ISecuredValue.close()` and `destroy()` narrowed to throw no checked exceptions, eliminating boilerplate at call sites.
- Applied consistent catch-block logging pattern (debug with stacktrace, warn without).
- Deprecated `com.github.toolarium.common.security.SecuredValue` stub kept for backward compatibility; use `SecuredValueFactory` instead.

### Fixed
- BandwidthThrottlingTest timing stability on Windows.
- `FileUtil.readFileContent(File, Charset)` was ignoring the `charset` parameter and always using UTF-8.
- `Assert.isNotEqual(Object, Object)` threw `NullPointerException` when `object1` was `null` and `object2` was non-null.
- `RandomGenerator.getRandomString(int, char[])` had a dead `getRandomNumber()` call and a biased rejection-sampling loop; replaced with uniform `nextInt(array.length)` index selection.
- `StatisticCounter.clone()` read mutable fields without synchronization, risking a torn snapshot under concurrent `add()`/`clear()`; method is now `synchronized`.
- `StackTrace.DEFAULT_EXCLUDES` was a mutable public `ArrayList` allowing callers to corrupt the global exclusion filter; wrapped in `Collections.unmodifiableList`.
- `ObjectLockManager.cleanup()` called `unlockMap.entrySet()` without a null-check; `unlockMap` can be `null` after `releaseResource()`.
- `BandwidthThrottling.toString()` constructed a new `SimpleDateFormat` on every call; replaced with a static `DateTimeFormatter` constant.

## [ 1.0.0 ] - 2026-04-26
### Added
- Added RegularExpressionUtil with LRU-cached pattern compilation.
- Added PropertyExpanderContextBasedProperties.runWith() for safe thread-pool usage.
- Added MapUtilTest, RegularExpressionUtilTest and extended existing test coverage.

### Changed
- License changed to MPL-2.0.
- DateTimeFormat: replaced thread-unsafe static SimpleDateFormat with ThreadLocal-backed accessors.
- AbstractDifferenceFormatter: replaced DecimalFormat field with ThreadLocal for thread safety.
- DateUtil: cached common DateTimeFormatter instances as static fields.
- ObjectLockManager: replaced synchronized methods with ReadWriteLock.
- BandwidthThrottling: added 60-second timeout to busy-wait loop.
- StackTrace: replaced new Throwable() with Thread.currentThread().getStackTrace(), Vector with ArrayList, and += with StringBuilder.
- ByteArray.reverse(): rewritten for O(n) performance.
- ByteArray.replace(): uses direct byte array operations to avoid intermediate copies.
- ByteArray.toHex(): fixed to use logical length instead of buffer length.
- AbstractVersion.internalFilter(): no longer mutates the input list.
- EnumUtil.valueOf(): cached enum lookups for O(1) performance.
- ExceptionWrapper: cached constructor lookups via ConcurrentMap.
- RoundUtil: pre-computed powers of ten for common scales.
- StatisticCounter: fixed min tracking to use Double.MAX_VALUE sentinel, added defensive copy in add(StatisticCounter).
- FileUtil.simplifyPath(): rewritten with stack-based algorithm preventing path traversal on absolute paths.
- FileUtil/ClassPathUtil: regex compilation delegated to RegularExpressionUtil.

### Fixed
- SecuredValue: value field marked transient to prevent secret leakage during serialization.
- FileUtil.removeDirectory(): added null check for File.list() return value.
- FileUtil.readFileContent(URL): fixed resource leak with try-with-resources.
- ChannelUtil.channelCopy(): fixed resource leak with try-with-resources.
- JavaVersion: fixed resource leaks with try-with-resources, fixed toString() copy-paste bug.
- RandomGenerator: added fallback SID when InetAddress.getLocalHost() fails.
- SemanticVersion.isGreaterThan(): null-safe getMajor() handling.
- SemanticVersion: replaced Integer subtraction with Integer.compare() to prevent overflow.
- Assert: made static fields volatile for thread-safe reads.
- ThreadUtil.sleep(): now returns boolean indicating whether sleep was interrupted.
- MapUtil.add(): throws IllegalArgumentException on null target map instead of silent data loss.

## [ 0.9.2 ] - 2025-01-25
### Added
- Added static convert method on Version adn SemanticVersion classes to convert from string representation.
- Added sort and filter functions for Version and SemanticVersion classes.
- Added unit tests for convert, sort and filter.

## [ 0.9.1 ] - 2025-01-01
### Added
- Added existFile, simplifyPath, removeFile, removeDirectory, writeFileContent in FileUtil.

### Changed
- Optimized trimLeft and trimRight in StringUtil.

## [ 0.9.0 ] - 2024-12-26
### Added
- Added isReadable and isWritable in FileUtil.
- Added Assert util class.
- Added splitAsList by length in StringUtil.
- Added ExceptionWrapper.
- Added JavaVersion.

### Fixed
- Java 1.8 support.
- Bugfix ClassInstanceUtil.isClassAvailable.

## [ 0.8.2 ] - 2024-08-02
### Changed
- Updated javadoc.

### Fixed
- BandwidthThrottling toString method.
- ObjectLockManager changed unlockMap to transient and releaseResource keep statistic.
- ClassPathUtilTest method testSearchFileByRegExp.

## [ 0.8.1 ] - 2024-07-18
### Added
- Added ObjectLockManager incl. tests.

### Fixed
- ClassPathUtilTest method testSearchFileByRegExp.

## [ 0.8.0 ] - 2024-06-28
### Added
- Added ClassPathUtil, ReflectionUtil, ClassInstanceUtil, ChannelUtil, FileUtil incl. tests.

## [ 0.7.1 ] - 2024-05-28
### Added
- Added BandwidthThrottling, TimeDiffereneceFormatter, StatisticCounter, RoundUtil incl. tests.

## [ 0.7.0 ] - 2024-05-24
### Added
- DateUtil, ThreadUtil and PropertyExpander incl. tests.

## [ 0.6.1 ] - 2024-01-22
### Fixed
- Null handling.

## [ 0.6.0 ] - 2024-01-18
### Added
- CompareMaps added.

## [ 0.5.0 ] - 2024-01-07
### Added
- TextUtil added.

## [ 0.4.0 ] - 2023-11-07
### Added
- DateTiemFormat added.

## [ 0.3.0 ] - 2023-08-03
### Added
- StackTrace, StringUtil and tests from jpTools.
- Test cases.

## [ 0.2.0 ] - 2023-07-29
### Added
- Enhanced by SemanticVersion inspired by semver4j.
- Version class and tests from jpTools.

## [ 0.1.1 ] - 2023-07-24
### Fixed
- Naming issue solved.

## [ 0.1.0 ] - 2023-07-24
### Added
- ByteArray class and tests from jpTools.
- RandomGeneratir and tests from jpTools.
- EnumUtil and tests from jpTools.
