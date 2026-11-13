# Proposal

## Why

Backlog item 1, "Developers can build, check and ship the service". Nothing exists yet: no build, no service, no
CI. Until one command proves the service builds, starts, is healthy and keeps to the architecture rules, every
later item would mix behaviour with plumbing.

## What Changes

- A Gradle (Kotlin DSL) build with the wrapper, for Java 25 and Spring Boot 4, per STANDARDS.md §1.
- A runnable Spring Boot service that exposes `GET /actuator/health` with its liveness and readiness groups (PRD §3.3) and reports `UP`.
- Architecture rules (STANDARDS.md §2) checked on every build; a test proves a violating class is caught.
- A formatting check (STANDARDS.md §5) on every build.
- A CI workflow that runs `./gradlew build` on every PR and every push to `main` (STANDARDS.md §7).

**Out of scope:** any calculation or Operation, the `/api/v1` endpoints, Problem Details handling, API docs and
springdoc, configuration profiles (item 5a).

## Capabilities

### New Capabilities
- `service-health`: the running service reports whether it is healthy.
- `build-verification`: one command, run locally or in CI, gives the verdict on tests, architecture rules and
  formatting.

### Modified Capabilities
None.

## Impact

- New: `build.gradle.kts`, `settings.gradle.kts`, Gradle wrapper, `src/main`, `src/test`,
  `.github/workflows/build.yml`.
- Dependencies: Spring Boot webmvc and actuator, ArchUnit, Spotless with palantir-java-format, as listed in
  STANDARDS.md §1.
- Docs to update: none. STANDARDS.md already names the stack, the CI workflow file and the ArchUnit rules; PRD §3.3
  already names the health endpoint.
