# Tasks

## 1. Build setup

- [x] 1.1 Add `settings.gradle.kts`, `build.gradle.kts` (Java 25 toolchain, Spring Boot 4.1.2, webmvc, actuator, test deps, ArchUnit 1.5.1 (plain `archunit`), Spotless 8.11.0 with palantir-java-format 2.103.0 and ktlint) and the Gradle 9.8.2 wrapper; verify `./gradlew --version` reports 9.8.2

## 2. Service health

- [x] 2.1 Write the smoke test: start the app on a random port, `GET /actuator/health`, `/health/liveness` and `/health/readiness` each return 200 with `status` `UP`; verify it fails (no application class yet)
- [x] 2.2 Add `com.paulrule.mathapi.MathApiApplication`; verify the smoke test passes and `./gradlew bootRun` serves `/actuator/health`

## 3. Architecture rules

- [x] 3.1 Write tests that run each layer rule against its own fixture package under `src/test` (a domain class and an application class that each use a Spring type) and assert a violation is reported; verify it fails before the rules exist
- [x] 3.2 Add the layer rules (domain only `java..` and itself; application no Spring, no adapter or config) and a test applying them to the main classes; verify both tests pass

## 4. Formatting

- [x] 4.1 Run `./gradlew spotlessApply`, then make one Java file and one Gradle Kotlin script unformatted and verify `./gradlew build` fails naming each; revert and verify it passes

## 5. CI

- [ ] 5.1 Add `.github/workflows/build.yml` running `./gradlew build` on `pull_request` and on `push` to `main` with a read-only token; verify the PR for this change shows the check passing

## 6. Integration

- [x] 6.1 Run `./gradlew clean build` and verify it passes

## Workflow follow-up

- Review (`/opsx:verify`, `/ponytail:ponytail-review`, `/code-review`), fix, archive, retro, PR per `CLAUDE.md`.
