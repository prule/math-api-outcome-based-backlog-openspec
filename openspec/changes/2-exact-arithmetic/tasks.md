# Tasks

## 1. Domain

- [x] 1.1 Write failing domain tests for ADD, SUBTRACT and MULTIPLY covering every DOMAIN-MODEL.md §3 edge case (`0.1 + 0.2 = 0.3`, negatives, large precision; zero and negative results; × 0, sign rules, scale growth) and BR-4 trailing-zero stripping; verify they fail to compile
- [x] 1.2 Write failing tests for `Operation.named` (exact names resolve; `add`, `POWER`, null raise `UnknownOperationException`) and for BR-9 (binary Operation with no `b` raises `ArityMismatchException`)
- [x] 1.3 Add `Operation`, `Calculation`, `UnknownOperationException`, `ArityMismatchException` in `domain`; verify the domain tests pass

## 2. Application and wiring

- [x] 2.1 Add `PerformCalculation` in `application` and `UseCaseConfig` in `config`; verify `ArchitectureTest` passes
- [x] 2.2 Add the "adapter must not depend on config" ArchUnit rule with its own violating fixture; verify the fixture test fails if the rule is loosened and the main classes pass

## 3. Web adapter

- [x] 3.1 Write failing `@WebMvcTest` tests: exact Results for the three Operations; number and string Operands; `a`/`b` echoed with their scale (`"1.50"`, `1e2` → `"100"`); Result in plain notation (`1E+3` → `"1000"`)
- [x] 3.2 Write failing `@WebMvcTest` tests for every error path: malformed JSON, missing `operation`, missing `a`, `"abc"`, `true`, missing or null `b` (all `invalid-request`); `POWER`, `add` (`unknown-operation`, `detail` lists the three names); `FOO` with no `b` (`unknown-operation` first); 415 and 405 as Problem Details; no `detail` contains a Java class or exception name
- [x] 3.3 Add the controller, request/response records and the Problem Details advice; add `spring-boot-starter-validation` and `spring-boot-starter-webmvc-test`; verify the web tests pass

## 4. Smoke

- [x] 4.1 Add one happy-path `POST /api/v1/calculations` (`0.1 + 0.2`) to `MathApiApplicationTest`; verify it passes against the running app

## 5. CI

- [ ] 5.1 Verify the PR for this change shows the `build` check passing

## 6. Integration

- [x] 6.1 Run `./gradlew clean build` and verify it passes

## Workflow follow-up

- Docs: none expected (PRD §3.2 and DOMAIN-MODEL.md already cover this item); if the build changes the contract, update the doc and its change log in this change.
- Review, fix, draft PR, archive, retro, PR per `CLAUDE.md`.
