# Tasks

## 1. Domain

- [x] 1.1 Write failing domain tests for DIVIDE: `1 ÷ 3`, `2 ÷ 3`, `1 ÷ 7` to 34 digits; HALF_EVEN ties (`…05 ÷ 1`, `…15 ÷ 1`); exact `1 ÷ 4 = 0.25`; negative divisor; `÷ 0`, `0.00`, `-0`, `0E+5` raise `DivisionByZeroException` (BR-2); verify they fail
- [x] 1.2 Add the `DIVIDE` constant and `DivisionByZeroException`; verify the domain tests, including the unchanged ADD/SUBTRACT/MULTIPLY ones, pass

## 2. Web adapter

- [x] 2.1 Write failing web tests: `1 ÷ 3` and `1 ÷ 4` Results; each zero form returns 422 `/problems/division-by-zero`; DIVIDE with no `b` is still `invalid-request`; the unknown-operation `detail` names `DIVIDE`
- [x] 2.2 Add the division-by-zero Problem Details handler; verify the web tests pass

## 3. CI

- [ ] 3.1 Verify the PR for this change shows the `build` check passing

## 4. Integration

- [x] 4.1 Run `./gradlew clean build` and verify it passes with no existing test changed except the unknown-operation `detail` check

## Workflow follow-up

- Docs: none expected (PRD and DOMAIN-MODEL.md already define DIVIDE, BR-2, BR-3); if the build changes the contract, update the doc and its change log in this change.
- Review, fix, draft PR, archive, retro, PR per `CLAUDE.md`.
