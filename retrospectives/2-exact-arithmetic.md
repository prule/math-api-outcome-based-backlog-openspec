# Retro: 2 — Clients get exact results for add, subtract and multiply

OpenSpec change archived at `openspec/changes/archive/2026-11-14-2-exact-arithmetic/`. PR #3.

## Caught by review

**`/opsx:verify`**
- Task 5.1 (CI on the PR) open: closed by the draft PR's green check before archive.
- "Detail exposes no internals" not asserted for 415 and 405: both now go through the same Problem Details
  matcher as every other error.

**`/ponytail:ponytail-review`**
1. An unplanned exception fell through to Boot's default error JSON (`application/json` with `timestamp`/`error`),
   not Problem Details. Reproduced with `MULTIPLY 1e-2000000000 × 1e-2000000000` (BigDecimal scale overflow → 500).
   Added a catch-all handler: 500 Problem Details, fixed `detail`, cause logged. Tested in `UnexpectedErrorTest`
   with a mocked use case, because item 4's BR-1 check will turn the real trigger into a 400.
2. 415/405 details not checked for leaks: fixed (as verify).

**`/code-review` — Standards**
- `spring-boot-starter-webmvc-test` not in STANDARDS §1: justified in the PR body.
- No OpenAPI annotations on the controller (STANDARDS §4): not fixed; API docs are item 5.
- BR-9 and BR-4 web tests cited no BR id: comments added.
- `calculation.b().toPlainString()` would throw for a unary Operation, and PRD §3.2 wants no `b` key then: not
  fixed. No unary Operation exists; item 7 must change the response.
- `PerformCalculation` is a Middle Man: not fixed. STANDARDS §2 requires the use case layer.
- `UnknownOperationException`'s message is unused while `ArityMismatchException`'s is shown: not fixed. Harmless;
  the unknown-operation detail is built from `Operation.values()` on purpose so the client's input is never
  echoed.
- `perform(String operation, ...)` takes a String (Primitive Obsession): not fixed. Doing the lookup inside the use
  case is what puts unknown-operation before BR-9 (PRD §3.2 order).

**`/code-review` — Spec**
- 405/415 assertions partial: fixed (as verify).
- No-internals check weak (only `Exception` and `java.`): now also rejects `springframework`.
- Edge-case examples tested in the domain, only some end to end: not fixed. The design puts every DOMAIN-MODEL
  §3 edge case in domain unit tests (STANDARDS §6); the web slice covers parsing and rendering.
- `"operation": 123` or `true` was coerced to a string and reported as `unknown-operation`, not
  `invalid-request`: confirmed against the running app, fixed with a Jackson coercion config (see Surprises).
- `-0` is echoed as `"0"`: not fixed. `BigDecimal` has no negative zero, so the sign cannot survive parsing;
  `"0"` is the plain-notation value received.

## Surprises

- Jackson coerces JSON numbers and booleans into `String` fields by default. `@JsonFormat(lenient = FALSE)` on the
  field did not stop it in Jackson 3 (both new tests still failed); a `JsonMapperBuilderCustomizer` setting
  Integer/Float/Boolean → Textual coercion to Fail did.
- Spring's `ProblemDetail` leaves `type` null for framework errors, and its Jackson mixin is `NON_EMPTY`, so the
  JSON had no `type` at all (404, 405, 415, and the new 500). The first run of the tightened tests failed on
  `$.type`. Fixed by writing `about:blank` explicitly in `createResponseEntity` and the catch-all.
- Every other check passed first time; the web slice's 20 tests went green on the first run of the controller.

## Spec drift

- `calculation` gained three scenarios during fix: "Operation is not a string", "Unexpected failure" and
  "Framework errors carry a type".
- Design gained `config.JacksonConfig` (strict text fields), the catch-all handler, explicit `about:blank`, and a
  second web slice test class with a mocked use case.
- The advice's planned two-branch arity message was cut to the domain exception's own message: the unary branch
  was unreachable until item 7.

## Shortcuts taken

None.

## Lessons

- Probe the running app with hostile-but-valid JSON (wrong scalar types, extreme scales) during apply, not only
  after review; both real bugs this item were found that way.
- When a spec says "every error", test a framework error and an unplanned one, not just the ones we raise.
- Check what JSON a library's own type serialises to (here `ProblemDetail.type`) before assuming a field is there.
