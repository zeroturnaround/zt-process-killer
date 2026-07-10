# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed

- The project is now built and released with Gradle instead of Maven. The published artifact is otherwise unchanged: the same `org.zeroturnaround:zt-process-killer` coordinates, the same OSGi bundle metadata, and the same runtime dependencies.
- `zt-exec`, `commons-lang3` and `commons-io` are now runtime-scoped dependencies (previously the default `compile` scope), so they are no longer on the consumer compile classpath. `jna` and `slf4j-api` stay on the compile classpath because they appear in the public API. Add a direct dependency if your own code compiles against any of the now-runtime libraries.
- Documented Java 8 as the minimum supported version (the `jna` 5.13.0 dependency has required it since 1.11).

## [1.11] - 2023-08-03

### Added

- OSGi bundle metadata to the manifest.

### Changed

- Upgraded the JNA dependency to 5.13.0 to fix Apple M1 (Silicon) issues.

### Security

- Upgraded the logging dependencies to fix known vulnerabilities.

## [1.10] - 2019-04-22

### Added

- Support for Java 12.

## [1.9] - 2018-09-26

### Added

- Support for Java 9, 10 and 11.

### Changed

- Use system calls instead of spawning external commands.

## [1.8] - 2017-08-15

### Fixed

- Avoid calling `InetAddress.getLocalHost()` in `PidUtil.getMyPid()`.

## [1.7] - 2017-04-17

### Fixed

- Finding `WMIC.exe` again.
- Error handling of `isAlive()` on Windows.

## [1.6] - 2017-02-13

### Fixed

- Finding `WMIC.exe` again.

## [1.5] - 2017-01-04

### Fixed

- Finding `WMIC.exe` for checking process status on Windows.

## [1.4] - 2016-04-12

### Changed

- Renamed the project from `zt-process` to `zt-process-killer`.
- Bumped the JNA version.

## [1.3] - 2015-04-20

### Added

- Initial public version.

[Unreleased]: https://github.com/zeroturnaround/zt-process-killer/compare/e31e17e...HEAD
[1.11]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.10...e31e17e
[1.10]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.9...zt-process-killer-1.10
[1.9]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.8...zt-process-killer-1.9
[1.8]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.7...zt-process-killer-1.8
[1.7]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.6...zt-process-killer-1.7
[1.6]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.5...zt-process-killer-1.6
[1.5]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-killer-1.4...zt-process-killer-1.5
[1.4]: https://github.com/zeroturnaround/zt-process-killer/compare/zt-process-1.3...zt-process-killer-1.4
[1.3]: https://github.com/zeroturnaround/zt-process-killer/releases/tag/zt-process-1.3
