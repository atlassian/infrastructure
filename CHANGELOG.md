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
[Unreleased]: https://github.com/atlassian/infrastructure/compare/release-4.18.0...master

## [4.18.0] - 2021-04-14
[4.18.0]: https://github.com/atlassian/infrastructure/compare/release-4.17.5...release-4.18.0

### Added
- Point to remote files on SSH hosts via `RemotePath`.

Fix [JPERF-273]:
- Allow multiple ways of installing Jira via `JiraInstallation` or starting it via `JiraStart`.
- Represent the information required to use an already installed Jira via `InstalledJira` or `JiraStart` if started.
- Represent a brand-new Jira instance via `EmptyJiraHome`.
- Hook into Jira installation via `PreInstallHooks` and `PostInstallHooks`.
- Hook into Jira start via `PreStartHooks` and `PostStartHooks`.
- Let hooks insert new hooks.
- Locate and download any logs, charts, profiles and other reports via `Report` (rather than hardcoding the paths).

### Fixed
- Increase network-level retries for Jira/browser downloads. Decrease flakiness of such downloads on Ubuntu on WSL2.
- Download ChromeDriver version that matches installed Chrome version. Fix [JPERF-732].

[JPERF-273]: https://ecosystem.atlassian.net/browse/JPERF-273
[JPERF-732]: https://ecosystem.atlassian.net/browse/JPERF-732

## [4.17.5] - 2020-12-15
[4.17.5]: https://github.com/atlassian/infrastructure/compare/release-4.17.4...release-4.17.5

### Fixed
- Increase timeout for logstash Docker image installation.

## [4.17.4] - 2020-12-14
[4.17.4]: https://github.com/atlassian/infrastructure/compare/release-4.17.3...release-4.17.4

### Fixed
- Bump version of the docker to `5:19.03.13`. Fix [JPERF-664].

[JPERF-664]: https://ecosystem.atlassian.net/browse/JPERF-664

## [4.17.3] - 2020-11-25
[4.17.3]: https://github.com/atlassian/infrastructure/compare/release-4.17.2...release-4.17.3

### Fixed
- Bump MySQL version to 5.7.32. Address [JPERF-671].

[JPERF-671]: https://ecosystem.atlassian.net/browse/JPERF-671

## [4.17.2] - 2020-11-05
[4.17.2]: https://github.com/atlassian/infrastructure/compare/release-4.17.1...release-4.17.2

### Fixed
- Reduce max `Ubuntu.install` backoff from 1270 seconds to 105 seconds. Fix [JPERF-619].
- Change `Ubuntu.install` backoff from exponential to static + jitter.

[JPERF-619]: https://ecosystem.atlassian.net/browse/JPERF-619

## [4.17.1] - 2020-08-27
[4.17.1]: https://github.com/atlassian/infrastructure/compare/release-4.17.0...release-4.17.1

This is just a technical, test release that contains CI-related changes only.

## [4.17.0] - 2020-06-03
[4.17.0]: https://github.com/atlassian/infrastructure/compare/release-4.16.0...release-4.17.0

### Fixed
- Fix Ubuntu codename detection.

### Added
- Add `Ubuntu.getDistributionCodename`.

## [4.16.1] - 2020-06-03
[4.16.1]: https://github.com/atlassian/infrastructure/compare/release-4.16.0...release-4.16.1

This is an erroneous release. Don't use new APIs from this version, switch to `4.17.0` instead.

## [4.16.0] - 2020-06-02
[4.16.0]: https://github.com/atlassian/infrastructure/compare/release-4.15.1...release-4.16.0

### Fixed
- Eliminate spurious apt warnings. Fix [JPERF-642](https://ecosystem.atlassian.net/browse/JPERF-642).

# Added
- Add a new recommended method to add new repositories to Ubuntu: `com.atlassian.performance.tools.infrastructure.api.os.Ubuntu.addRepository(com.atlassian.performance.tools.ssh.api.SshConnection, java.lang.String, java.lang.String)`.

## [4.15.1] - 2020-05-13
[4.15.1]: https://github.com/atlassian/infrastructure/compare/release-4.15.0...release-4.15.1

### Fixed
- Fix compatibility with Ubuntu Eoan ([JPERF-625](https://ecosystem.atlassian.net/browse/JPERF-625)).
- Upgrade Docker to 19.03, which works with Ubuntu 16-19.
- Fix dataset location generation.

## [4.15.0] - 2020-03-03
[4.15.0]: https://github.com/atlassian/infrastructure/compare/release-4.14.5...release-4.15.0

### Added
- Added parametrized `CustomChromium`.

## [4.14.5] - 2020-03-03
[4.14.5]: https://github.com/atlassian/infrastructure/compare/release-4.14.4...release-4.14.5

### Fixed
- Use non-deprecated Kotlin stdlib. Fix [JPERF-466].

[JPERF-466]: https://ecosystem.atlassian.net/browse/JPERF-466

## [4.14.4] - 2019-12-09
[4.14.4]: https://github.com/atlassian/infrastructure/compare/release-4.14.3...release-4.14.4

### Fixed
- Harden `apt-get` retry cleanup when the process is gone. Help fix [JPERF-219].
- Clean up corrupt `dpkg` state. Help fix [JPERF-219].
- Have more patience for `OpenJDK.install`.

## [4.14.3] - 2019-11-27
[4.14.3]: https://github.com/atlassian/infrastructure/compare/release-4.14.2...release-4.14.3

### Added
- Offer `Docker` and `DockerImage` for customization and injection. Unblock Docker to work with other Ubuntu versions.
- Add `DockerMysqlServer`.

### Fixed
- Quote VU CLI args. Fix [JPERF-569].
- Clean up after `apt-get install` flakes. Help fix [JPERF-219].
- Harden dataset HTTP downloads.
- Harden `apt-get` downloads.
- Detect VU -> Jira connectivity early.

[JPERF-569]: https://ecosystem.atlassian.net/browse/JPERF-569
[JPERF-219]: https://ecosystem.atlassian.net/browse/JPERF-219

## [4.14.2] - 2019-08-23
[4.14.2]: https://github.com/atlassian/infrastructure/compare/release-4.14.1...release-4.14.2

### Fixed
- Increase timeout of apt-get. Fix [JPERF-552].

### Added
- Add support for wall-clock profiler. Resolve [JPERF-531].

[JPERF-531]: https://ecosystem.atlassian.net/browse/JPERF-531
[JPERF-552]: https://ecosystem.atlassian.net/browse/JPERF-552

## [4.14.1] - 2019-07-24
[4.14.1]: https://github.com/atlassian/infrastructure/compare/release-4.14.0...release-4.14.1

### Fixed
- Respect the overhead declared by virtual users. Fix [JPERF-530].

[JPERF-530]: https://ecosystem.atlassian.net/browse/JPERF-530

## [4.14.0] - 2019-07-17
[4.14.0]: https://github.com/atlassian/infrastructure/compare/release-4.13.0...release-4.14.0

### Deprecated
- Deprecate the handling of licenses as strings within `LicenseOverridingMysql`. Avoid [JPERF-523].

### Fixed
- Handle Jira license data as files within `LicenseOverridingMysql`. Fix [JPERF-523].

[JPERF-523]: https://ecosystem.atlassian.net/browse/JPERF-523

## [4.13.0] - 2019-07-05
[4.13.0]: https://github.com/atlassian/infrastructure/compare/release-4.12.6...release-4.13.0

### Added
- Facilitate Jira license overrides via `LicenseOverridingMysql` and `Dataset.Builder`. Unblock [JPERF-519].

[JPERF-519]: https://ecosystem.atlassian.net/browse/JPERF-519

## [4.12.6] - 2019-06-26
[4.12.6]: https://github.com/atlassian/infrastructure/compare/release-4.12.5...release-4.12.6

### Fixed
- Fix dataset unzipping permissions problem. Fix [JPERF-504].

## [4.12.5] - 2019-06-25
[4.12.5]: https://github.com/atlassian/infrastructure/compare/release-4.12.4...release-4.12.5

### Fixed
- Fix dataset unzipping performance. Fix [JPERF-504].

[JPERF-504]: https://ecosystem.atlassian.net/browse/JPERF-504

## [4.12.4] - 2019-06-25
[4.12.4]: https://github.com/atlassian/infrastructure/compare/release-4.12.3...release-4.12.4

### Fixed
- Fix race condition when downloading HTTP package. Fix [JPERF-496].

[JPERF-496]: https://ecosystem.atlassian.net/browse/JPERF-496

## [4.12.3] - 2019-05-09
[4.12.3]: https://github.com/atlassian/infrastructure/compare/release-4.12.2...release-4.12.3

### Fixed
- Parallelize Ubuntu installs across SSH connections. Fix [JPERF-478].

[JPERF-478]: https://ecosystem.atlassian.net/browse/JPERF-478

## [4.12.2] - 2019-04-23
[4.12.2]: https://github.com/atlassian/infrastructure/compare/release-4.12.1...release-4.12.2

### Fixed
- Make `Ubuntu` thread safe. Resolves [JPERF-468].

[JPERF-468]: https://ecosystem.atlassian.net/browse/JPERF-468

## [4.12.1] - 2019-04-19
[4.12.1]: https://github.com/atlassian/infrastructure/compare/release-4.12.0...release-4.12.1

### Fixed
- Increase `AdoptOpenJDK11` download timeout. Fix [JPERF-465]
- Have more patience for `lftp` installation. Fix [JPERF-460].
- Fix Docker install failures. Fix [JPERF-446].
- Quiet down the multicast VU log context. Resolve [JPERF-453].

[JPERF-465]: https://ecosystem.atlassian.net/browse/JPERF-465
[JPERF-460]: https://ecosystem.atlassian.net/browse/JPERF-460
[JPERF-446]: https://ecosystem.atlassian.net/browse/JPERF-446
[JPERF-453]: https://ecosystem.atlassian.net/browse/JPERF-453

## [4.12.0] - 2019-03-22
[4.12.0]: https://github.com/atlassian/infrastructure/compare/release-4.11.0...release-4.12.0

### Added
- Respect `VirtualUserLoad.maxOverallLoad` when spreading load across `MulticastVirtualUsers`. Fix [JPERF-429].

### Fixed
- Rely on `virtual-users` to slice the load. Transparently support any future sliceable VU load features.

[JPERF-429]: https://ecosystem.atlassian.net/browse/JPERF-429

## [4.11.0] - 2019-03-12
[4.11.0]: https://github.com/atlassian/infrastructure/compare/release-4.10.0...release-4.11.0

### Added
- Set GC params based on Java version. Resolve [JPERF-408].

[JPERF-408]: https://ecosystem.atlassian.net/browse/JPERF-408

## [4.10.0] - 2019-03-07
[4.10.0]: https://github.com/atlassian/infrastructure/compare/release-4.9.0...release-4.10.0

### Added
- Add `HttpDatasetPackage` constructor which enables performance improvements ([JPERF-412], [JPERF-413]). 

### Deprecated
- Deprecate `HttpDatasetPackage` three-parameter constructor in favor of two-parameter constructor.

### Fixed
- Download HTTP resources in parallel instead of on a single thread. Resolve [JPERF-412].
- Unzip tar.bz2 archive files in parallel. Resolve [JPERF-413].

[JPERF-412]: https://ecosystem.atlassian.net/browse/JPERF-412
[JPERF-413]: https://ecosystem.atlassian.net/browse/JPERF-413

## [4.9.0] - 2019-02-28
[4.9.0]: https://github.com/atlassian/infrastructure/compare/release-4.8.0...release-4.9.0

### Added
- A way to gather thread dumps over ssh. Resolve [JPERF-405].

### Fixed
- Download HTTP resources in parallel instead of on a single thread. Resolve [JPERF-412].

[JPERF-405]: https://ecosystem.atlassian.net/browse/JPERF-405
[JPERF-405]: https://ecosystem.atlassian.net/browse/JPERF-412

## [4.8.0] - 2019-02-22
[4.8.0]: https://github.com/atlassian/infrastructure/compare/release-4.7.0...release-4.8.0

### Added
- Support for Jira Service Desk installer. Unlocks JPERF-277.

## Deprecated
- Deprecate `ProductDistribution` in favor of `ProductDistribution` in `distribution` package.
- Deprecate `PublicJiraSoftwareDistributions` in favor of `PublicJiraSoftwareDistribution`.

[JPERF-277]: https://ecosystem.atlassian.net/browse/JPERF-277

## [4.7.0] - 2019-01-29
[4.7.0]: https://github.com/atlassian/infrastructure/compare/release-4.6.1...release-4.7.0

### Added
- Support for OpenJDK 11.  Resolve [JPERF-370].

[JPERF-370]: https://ecosystem.atlassian.net/browse/JPERF-370

## [4.6.1] - 2019-01-28
[4.6.1]: https://github.com/atlassian/infrastructure/compare/release-4.6.0...release-4.6.1

### Fixed
- Bump Oracle JDK download timeout. Fix [JPERF-374].

[JPERF-374]: https://ecosystem.atlassian.net/browse/JPERF-374

## [4.6.0] - 2019-01-18
[4.6.0]: https://github.com/atlassian/infrastructure/compare/release-4.5.1...release-4.6.0

### Fixed
- Bump MySQL to 5.6.42. Resolve [JPERF-345].
- Set up VUs only once per node in `MulticastVirtualUsers`. Fix [JPERF-346].
- Chromedriver continues to work after a page load timeout. Fix [JPERF-249].

### Added
- Separate Chromium implementations, like `Chromium69` or `Chromium70`. Resolve [JPERF-350].
- Install Chromium with a compatible chromedriver. Resolve [JPERF-352].
- Add a way to specify how to download an archive.
- Add a way to download a public Jira Software tar.gz distribution. Resolve [JPERF-277].

## Deprecated
- Deprecate Chromium in favor of specific implementations like Chromium69 or Chromium70.

[JPERF-249]: https://ecosystem.atlassian.net/browse/JPERF-249
[JPERF-277]: https://ecosystem.atlassian.net/browse/JPERF-277
[JPERF-345]: https://ecosystem.atlassian.net/browse/JPERF-345
[JPERF-346]: https://ecosystem.atlassian.net/browse/JPERF-346
[JPERF-350]: https://ecosystem.atlassian.net/browse/JPERF-350
[JPERF-352]: https://ecosystem.atlassian.net/browse/JPERF-352

## [4.5.1] - 2019-01-04
[4.5.1]: https://github.com/atlassian/infrastructure/compare/release-4.5.0...release-4.5.1

### Fixed
- Use latest chromedriver for Chrome Browser. Resolve [JPERF-331].

[JPERF-331]:https://ecosystem.atlassian.net/browse/JPERF-331

## [4.5.0] - 2018-12-20
[4.5.0]: https://github.com/atlassian/infrastructure/compare/release-4.4.0...release-4.5.0

### Added
- Expose SPI for profiler. Resolve [JPERF-318].
- Add profiler implementation (`AsyncProfiler`).

## Deprecated
- `startMonitoring` method in `Jstat`.
- `startMonitoring` method in `OsMetric`.
- MonitoringProcess class.

## Fixed
- Increase `Chrome` download timeout.

[JPERF-318]:https://ecosystem.atlassian.net/browse/JPERF-318

## [4.4.0] - 2018-12-19
[4.4.0]: https://github.com/atlassian/infrastructure/compare/release-4.3.0...release-4.4.0

### Added
- Expose API for choosing JDK Jira will run on. Resolve [JPERF-305].

## [4.3.0] - 2018-12-18
[4.3.0]: https://github.com/atlassian/infrastructure/compare/release-4.2.0...release-4.3.0

### Added
- Jstat support for every JavaDevelopmentKit.
- Support for provisioning Jira on AdoptOpenJDK. Resolve [JPERF-305].

[JPERF-305]:https://ecosystem.atlassian.net/browse/JPERF-305

## [4.2.0] - 2018-12-04
[4.2.0]: https://github.com/atlassian/infrastructure/compare/release-4.1.0...release-4.2.0

### Added
- Support providing collectd configurations via `collectdConfigs` in `JiraNodeConfig`. Unblock [JPERF-285].

[JPERF-285]: https://ecosystem.atlassian.net/browse/JPERF-285

## [4.1.0] - 2018-12-04
[4.1.0]: https://github.com/atlassian/infrastructure/compare/release-4.0.0...release-4.1.0

## Added
- Add builder for `JiraNodeConfig` which is part of [JPERF-282].
- Add builder for `JiraLaunchTimeouts` which is part of [JPERF-282].

[JPERF-282]: https://ecosystem.atlassian.net/browse/JPERF-282

## [4.0.0] - 2018-11-28
[4.0.0]: https://github.com/atlassian/infrastructure/compare/release-3.1.0...release-4.0.0

### Removed
- Drop support of `virtual-users:1`.
- Drop support of `virtual-users:2`.

### Fixed
- Cease to rewrite `VirtualUserOptions` parameters, allowing `infrastructure` to forward new parameters
  without releasing new rewrite code every time `virtual-users` releases a new parameter. Resolve [JPERF-252].

[JPERF-252]: https://ecosystem.atlassian.net/browse/JPERF-252

## [3.1.0] - 2018-11-26
[3.1.0]: https://github.com/atlassian/infrastructure/compare/release-3.0.0...release-3.1.0

### Added
- Add support for jira-actions:3
- Add support for virtual-users:3

## [3.0.0] - 2018-11-20
[3.0.0]: https://github.com/atlassian/infrastructure/compare/release-2.4.1...release-3.0.0

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
[2.4.1]: https://github.com/atlassian/infrastructure/compare/release-2.4.0...release-2.4.1

### Fixed
- Fix chromium installation flakes. Fix [JPERF-250].
- Avoid `VirtualUserOptions.copy`. Avoid [JPERF-253].

[JPERF-250]: https://ecosystem.atlassian.net/browse/JPERF-250
[JPERF-253]: https://ecosystem.atlassian.net/browse/JPERF-253

## [2.4.0] - 2018-10-31
[2.4.0]: https://github.com/atlassian/infrastructure/compare/release-2.3.0...release-2.4.0

### Added
- Customisable timeouts for Jira launch which is required for [JPERF-216].

[JPERF-216]: https://ecosystem.atlassian.net/browse/JPERF-216

## [2.3.1] - 2018-10-30
[2.3.1]: https://github.com/atlassian/infrastructure/compare/release-2.3.0...release-2.3.1

### Fixed
- Download chromium v69. Fix [JPERF-242].

[JPERF-242]: https://ecosystem.atlassian.net/browse/JPERF-242

## [2.3.0] - 2018-10-26
[2.3.0]: https://github.com/atlassian/infrastructure/compare/release-2.2.0...release-2.3.0

### Added
- Chromium support which resolves [JPERF-238].

[JPERF-238]: https://ecosystem.atlassian.net/browse/JPERF-238

## [2.2.0] - 2018-09-21
[2.2.0]: https://github.com/atlassian/infrastructure/compare/release-2.1.1...release-2.2.0

### Added
- Add the `CPU_UTILISATION` dimension.
- Parametrize the log path for Splunk forwarding.
- Parametrize the Atlassian Splunk forwarder log fields.
- Parametrize the Atlassian Splunk forwarder Kinesis ARN.

## [2.1.1] - 2018-09-11
[2.1.1]: https://github.com/atlassian/infrastructure/compare/release-2.1.0...release-2.1.1

### Fixed
- Respect customer's log configuration. Fix [JPERF-11](https://ecosystem.atlassian.net/browse/JPERF-11).

## [2.1.0] - 2018-09-06
[2.1.0]: https://github.com/atlassian/infrastructure/compare/release-2.0.0...release-2.1.0

### Added
- Add `virtual-users:2` compatibility.

## [2.0.0] - 2018-09-04
[2.0.0]: https://github.com/atlassian/infrastructure/compare/release-1.1.2...release-2.0.0

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
[1.1.2]: https://github.com/atlassian/infrastructure/compare/release-1.1.1...release-1.1.2

### Fixed
- Restore `MyslqDatabase` binary compatibility with 1.0.0.

## [1.1.1] - 2018-08-29
[1.1.1]: https://github.com/atlassian/infrastructure/compare/release-1.1.0...release-1.1.1

### Fixed
- Restore `VirtualUsers` binary compatibility with 1.0.0.

## [1.1.0] - 2018-08-28
[1.1.0]: https://github.com/atlassian/infrastructure/compare/release-1.0.0...release-1.1.0

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
[1.0.0]: https://github.com/atlassian/infrastructure/compare/release-0.2.0...release-1.0.0

### Changed
- Define the public API.

## [0.2.0] - 2018-08-24
[0.2.0]: https://github.com/atlassian/infrastructure/compare/release-0.1.0...release-0.2.0

### Added
- Grant access to `MavenApp` fields.

## [0.1.0] - 2018-08-21
[0.1.0]: https://github.com/atlassian/infrastructure/compare/release-0.0.2...release-0.1.0

### Added
- License.
- Use the latest Marketplace version of an app.
- Add [CHANGELOG.md](CHANGELOG.md).
- Run Jira without any apps installed.

## [0.0.2] - 2018-08-03
[0.0.2]: https://github.com/atlassian/infrastructure/compare/release-0.0.1...release-0.0.2

### Fixed
- Specify module name.

## [0.0.1] - 2018-08-03
[0.0.1]: https://github.com/atlassian/infrastructure/compare/initial-commit...release-0.0.1

### Added
- Add [README.md](README.md).
- Enable Bitbucket Pipelines.
