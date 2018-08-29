# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## API
The API consists of all public Java types from `com.atlassian.performance.tools.infrastructure.api` and its subpackages:

  * [source compatibility]
  * [binary compatibility]
  * [behavioral compatibility] with behavioral contracts expressed via Javadoc

[source compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#source_compatibility
[binary compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#binary_compatibility
[behavioral compatibility]: http://cr.openjdk.java.net/~darcy/OpenJdkDevGuide/OpenJdkDevelopersGuide.v0.777.html#behavioral_compatibility

## [Unreleased]
[Unreleased]: https://bitbucket.org/atlassian/infrastructure/branches/compare/master%0Drelease-1.1.0

## [1.1.1]
[1.1.1]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.1.1%0Drelease-1.1.0

### Fixed
- Restore binary compatibility with 1.0.0.

## [1.1.0]
[1.1.0]: https://bitbucket.org/atlassian/infrastructure/branches/compare/release-1.1.0%0Drelease-1.0.0

### INCOMPATIBILITY WARNING
Break binary compatibility for `com.atlassian.performance.tools.infrastructure.api.virtualusers.VirtualUsers`.
Switch to `1.1.1` to restore this compatibility.
See [JPERF-39](https://ecosystem.atlassian.net/browse/JPERF-39) for details.

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
