# build-verification Specification

## Purpose
Gives developers and CI one command whose pass or fail verdict covers the tests, the architecture rules and the
code format, so every push is judged the same way.

## Requirements

### Requirement: One command gives the build verdict
`./gradlew build` SHALL compile the service, run every test, check the architecture rules and check the code format,
and SHALL fail if any of them fails.

#### Scenario: Clean checkout builds
- **WHEN** a developer runs `./gradlew build` on a clean checkout of `main` with JDK 25
- **THEN** the build passes

### Requirement: Domain stays free of frameworks
The build SHALL fail if any class in the domain layer depends on anything outside the Java standard library and the
domain layer itself (no Spring, Jakarta, Jackson or other framework), per STANDARDS.md §2.

#### Scenario: Framework dependency in the domain is rejected
- **WHEN** a domain class depends on a Spring, Jakarta or Jackson type
- **THEN** the architecture check reports a violation and the build fails

#### Scenario: Current code keeps to the layer rules
- **WHEN** the architecture check runs on the service's own classes
- **THEN** it reports no violation

### Requirement: Unformatted code fails the build
The build SHALL fail if any Java source or Gradle Kotlin script is not formatted to the project's formatter, per
STANDARDS.md §5.

#### Scenario: Unformatted Java source is rejected
- **WHEN** a Java source file is not in the formatter's output form and `./gradlew build` runs
- **THEN** the build fails and names the file

### Requirement: CI gives the same verdict on every PR and push to main
CI SHALL run `./gradlew build` on every pull request and every push to `main`, and SHALL report the build's
pass or fail as the check result.

#### Scenario: Pull request is checked
- **WHEN** a pull request is opened or updated
- **THEN** CI runs `./gradlew build` and the PR shows its pass or fail

#### Scenario: Push to main is checked
- **WHEN** a commit is pushed to `main`
- **THEN** CI runs `./gradlew build` and reports its pass or fail
