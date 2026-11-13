# Design

## Context

Empty repo apart from docs. STANDARDS.md §1 fixes the stack and versions, §2 the layers and their ArchUnit rules,
§5 the formatter, §6 the test levels, §7 the CI file. See proposal.md for why.

## Goals / Non-Goals

**Goals:**
- The smallest build that meets every STANDARDS.md rule that applies before there is any behaviour.
- Each "done when" check has an automated test, except the formatting check (see Risks).

**Non-Goals:**
- Empty placeholder classes in `domain`, `application`, `adapter.in.web` or `config`. Packages appear when
  behaviour needs them.
- Health groups, probes, or custom health indicators.

## Decisions

- **Spring Boot BOM via `platform(SpringBootPlugin.BOM_COORDINATES)`** rather than the
  `io.spring.dependency-management` plugin: one plugin fewer, same managed versions.
- **Health from Actuator as shipped.** `/actuator/health` is exposed over HTTP by default; no config needed. The
  smoke test (the one `@SpringBootTest` of STANDARDS.md §6) starts the app on a random port and calls the endpoint
  with the JDK `HttpClient`, so it tests the real HTTP path without choosing a Spring test client.
- **ArchUnit rules as plain `ArchRule` builders taking the root package.** The same rules run against the main
  classes (must pass) and against a deliberately violating fixture under `src/test` in its own root package (must
  report violations). That proves the rule can fail, which a passing rule on empty packages cannot. Rules use
  `allowEmptyShould(true)` because the layers have no classes yet.
  - Domain: may depend only on `java..` and the domain package.
  - Application: must not depend on `org.springframework..`, nor on `adapter..` or `config..`.
  - Domain and application: must not depend on `adapter..` or `config..` (arrows point inwards).
  - Alternative considered: `layeredArchitecture()`; rejected because it cannot express "only `java..`" for the
    domain, and the per-rule form reads closer to STANDARDS.md §2.
- **Spotless** with palantir-java-format for Java and ktlint for `*.gradle.kts`, versions from STANDARDS.md §1. The
  Spotless plugin hooks `spotlessCheck` into `check`, so `build` fails on unformatted code with no extra wiring.
- **CI**: GitHub Actions, `actions/setup-java` (Temurin 25) and `gradle/actions/setup-gradle`, running
  `./gradlew build` on `pull_request` and on `push` to `main`.

## Risks / Trade-offs

- [The formatting check has no automated test; a JUnit test would need to run Gradle from inside Gradle] →
  Verified once by hand during apply (format a file wrongly, see `./gradlew build` fail, revert), and recorded in
  the retro. The check itself is the plugin's own, not our code.
- [Java 25 needs Gradle 9.x and a toolchain] → Gradle 9.8.2 wrapper and a Java 25 toolchain, as STANDARDS.md §1.
