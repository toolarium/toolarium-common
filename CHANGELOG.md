# toolarium-common

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [ 0.9.2 ] - 2025-01-01

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
