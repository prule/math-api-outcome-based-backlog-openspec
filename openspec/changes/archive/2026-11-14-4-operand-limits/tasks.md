# Tasks

## 1. BR-1 in the domain

- [x] 1.1 Write failing domain tests: 100-digit Operand, `1E+1000`, `1E-1000` accepted; 101 digits, `1E+1001`, `1E-1001` raise `OperandOutOfRangeException` naming `a` or `b`; the message never contains the value; BR-9 is still raised before BR-1
- [x] 1.2 Add `OperandOutOfRangeException` and the BR-1 check in the `Calculation` constructor after BR-9; verify all domain tests pass

## 2. Operand limits over HTTP

- [x] 2.1 Write failing web tests: at-limit Operands give 200; one past each limit gives 400 `/problems/operand-out-of-range` naming the Operand without its value; `1e999999999 × 1e999999999` rejected within 1 second; unknown operation and missing `b` still win over BR-1
- [x] 2.2 Add the operand-out-of-range handler; verify the web tests pass

## 3. Request limits

- [x] 3.1 Write failing web tests: body of exactly 16,384 characters accepted, 16,385 rejected naming the limit; a ≤16,384-character body over 16,384 UTF-8 bytes accepted; 1,001-digit JSON number and 1,001-character JSON string rejected naming their limit; nesting over 500 rejected naming the limit
- [x] 3.2 Add the Jackson stream-read limits to `JacksonConfig`, the body-size `RequestBodyAdvice`, `RequestTooLargeException`, and the limit-naming in `handleHttpMessageNotReadable`; verify the web tests pass

## 4. Docs

- [x] 4.1 Update PRD.md §3.2's request-limits line (nesting depth 500; the `detail` names the limit broken) and add a change-log row; verify the PRD matches the tests' limits

## 5. CI

- [x] 5.1 Verify the PR for this change shows the `build` check passing

## 6. Integration

- [x] 6.1 Run `./gradlew clean build` and verify it passes

## Workflow follow-up

- Review, fix, draft PR, archive, retro, PR per `CLAUDE.md`.
