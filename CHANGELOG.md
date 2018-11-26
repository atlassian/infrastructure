# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## Compatibility
The library offers compatibility contracts on the Java API and the POM.

### Java API
The API covers all public Java types from `com.atlassian.performance.tools.infrastructure.api` and its subpackages:

  * [source compatibility]
  * [binary compatibility]
  * [behavioral compatibility] with behavioral contracts expressed via Javadoc

[source compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#source_compatibility
[binary compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#binary_compatibility
[behavioral compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#behavioral_compatibility

### POM
Changing the license is breaking a contract.
Adding a requirement of a major version of a dependency is breaking a contract.
Dropping a requirement of a major version of a dependency is a new contract.

## [Unreleased]
[Unreleased]: https://bitbucket.org/atlassian/infrastructure/branches/compare/master%0Drelease-3.0.0

### Added
- Add support for jira-actions:3
- Add support for virtual-users:3

## [3.0.0] - 2018-11-20
[3.0.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-3.0.0%0Drelease-2.4.1

### Added
- Customisable status timeout for Jira upgrades endpoint which is required for [JPERF-271].
- Support `ssh:2`

### Fixed
- Increase pull timeout for MySQL docker image. Fix [JPERF-265].
- Increase Docker install timeout. Fix [JPERF-264].

### Removed
- Drop support of `ssh:1`
- Remove Kotlin data-class generated methods from API.
- Remove all deprecated API.

[JPERF-271]: https://ecosystem.atlassian.net/browse/JPERF-271
[JPERF-265]: https://ecosystem.atlassian.net/browse/JPERF-265
[JPERF-264]: https://ecosystem.atlassian.net/browse/JPERF-264

## [2.4.1] - 2018-11-02
[2.4.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.4.1%0Drelease-2.4.0

### Fixed
- Fix chromium installation flakes. Fix [JPERF-250].
- Avoid `VirtualUserOptions.copy`. Avoid [JPERF-253].

[JPERF-250]: https://ecosystem.atlassian.net/browse/JPERF-250
[JPERF-253]: https://ecosystem.atlassian.net/browse/JPERF-253

## [2.4.0] - 2018-10-31
[2.4.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.4.0%0Drelease-2.3.0

### Added
- Customisable timeouts for Jira launch which is required for [JPERF-216].

[JPERF-216]: https://ecosystem.atlassian.net/browse/JPERF-216

## [2.3.1] - 2018-10-30
[2.3.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.3.1%0Drelease-2.3.0

### Fixed
- Download chromium v69. Fix [JPERF-242].

[JPERF-242]: https://ecosystem.atlassian.net/browse/JPERF-242

## [2.3.0] - 2018-10-26
[2.3.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.3.0%0Drelease-2.2.0

### Added
- Chromium support which resolves [JPERF-238].

[JPERF-238]: https://ecosystem.atlassian.net/browse/JPERF-238

## [2.2.0] - 2018-09-21
[2.2.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.2.0%0Drelease-2.1.1

### Added
- Add the `CPU_UTILISATION` dimension.
- Parametrize the log path for Splunk forwarding.
- Parametrize the Atlassian Splunk forwarder log fields.
- Parametrize the Atlassian Splunk forwarder Kinesis ARN.

## [2.1.1] - 2018-09-11
[2.1.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.1.1%0Drelease-2.1.0

### Fixed
- Respect customer's log configuration. Fix [JPERF-11](https://ecosystem.atlassian.net/browse/JPERF-11).

## [2.1.0] - 2018-09-06
[2.1.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.1.0%0Drelease-2.0.0

### Added
- Add `virtual-users:2` compatibility.

## [2.0.0] - 2018-09-04
[2.0.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-2.0.0%0Drelease-1.1.2

### Changed
- Require APT `io:1`.
- Require APT `concurrency:1`.
- Require APT `jvm-tasks:1`.
- Require APT `ssh:1`.
- Require APT `jira-actions:2`.
- Require APT `virtual-users:1`
- Remove `LoadProfile` in favour of new `virtual-users` API

### Added
- Include the POM in the compatibility contract.
- License via POM.
- Gain freedom from APT `io:0`.
- Gain freedom from APT `concurrency:0`.
- Gain freedom from APT `jvm-tasks:0`.
- Gain freedom from APT `ssh:0`.
- Gain freedom from APT `jira-actions:0`.
- Added means to run load test locally.

## [1.1.2] - 2018-08-30
[1.1.2]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.1.2%0Drelease-1.1.1

### Fixed
- Restore `MyslqDatabase` binary compatibility with 1.0.0.

## [1.1.1] - 2018-08-29
[1.1.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.1.1%0Drelease-1.1.0

### Fixed
- Restore `VirtualUsers` binary compatibility with 1.0.0.

## [1.1.0] - 2018-08-28
[1.1.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.1.0%0Drelease-1.0.0

### INCOMPATIBILITY BUG
Break binary compatibility for `com.atlassian.performance.tools.infrastructure.api.virtualusers.VirtualUsers`.
Switch to `1.1.1` to restore this compatibility.
See [JPERF-39](https://ecosystem.atlassian.net/browse/JPERF-39) for details.

### INCOMPATIBILITY BUG
Break binary compatibility for `com.atlassian.performance.tools.infrastructure.api.database.MysqlDatabase`.
Switch to `1.1.2` to restore this compatibility.
See [JPERF-40](https://ecosystem.atlassian.net/browse/JPERF-40) for details.

### Added 
- Add diagnosticsLimit parameter to limit how many times diagnostics can be executed.
- Add maxConnections parameter to override MySQL max_connections value.
- Explain [contribution guidelines](CONTRIBUTING.md). 

## [1.0.0] - 2018-08-24
[1.0.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.0.0%0Drelease-0.2.0

### Changed
- Define the public API.

## [0.2.0] - 2018-08-24
[0.2.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-0.2.0%0Drelease-0.1.0

### Added
- Grant access to `MavenApp` fields.

## [0.1.0] - 2018-08-21
[0.1.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-0.1.0%0Drelease-0.0.2

### Added
- License.
- Use the latest Marketplace version of an app.
- Add [CHANGELOG.md](CHANGELOG.md).
- Run Jira without any apps installed.

## [0.0.2] - 2018-08-03
[0.0.2]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-0.0.2%0Drelease-0.0.1

### Fixed
- Specify module name.

## [0.0.1] - 2018-08-03
[0.0.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-0.0.1%0Dinitial-commit

### Added
- Add [README.md](README.md).
- Enable Bitbucket Pipelines.
