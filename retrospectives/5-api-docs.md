# Retro: 5 — Developers can integrate from the API docs alone

OpenSpec change archived at `openspec/changes/archive/2026-11-14-5-api-docs/`. PR #6.

## Caught by review

**`/opsx:verify`**
- Task 5.1 (CI on the PR) open: closed by the draft PR's green check before archive. Nothing else.

**`/ponytail:ponytail-review`**
- The unknown-operation detail format is written in both `OpenApiConfig` and `ProblemDetailsAdvice`: not fixed.
  The advice is package-private in the adapter; the replay test fails if the two drift.

**`/code-review` — Standards**
1. **Hard (STANDARDS §4):** the `operand-out-of-range` example copied BR-1's limits (100 digits, ±1000) into an
   annotation: fixed. Every Problem Details example is now built in `OpenApiConfig` from the domain exception's
   own message (`new OperandOutOfRangeException("a").getMessage()`), so the annotations hold only example names.
2. **Hard (STANDARDS §4):** the `invalid-request` example hard-coded ADD's arity: fixed the same way
   (`new ArityMismatchException(Operation.ADD).getMessage()`).
3. Request examples use literal Operation names: not fixed. They are example requests, not the allowed-value list
   §4 is about, and the replay test fails if a named Operation disappears.
4. The Operation-name list is built three times (advice, config, test): not fixed; each is one line from
   `Operation.values()`.
5. Long navigation chains in `OpenApiConfig`: partly fixed with `schemaProperty` and `examples` helpers; the rest
   is the swagger-models API.
6. `Info` mixed into the domain-values bean: fixed, its own `apiInfo` bean.
7. Two parallel maps in the replay test: fixed, one `Expected(status, body)` record.

**`/code-review` — Spec**
1. The docs sent readers to "BR-3", "BR-1" and "PRD §3.2 order", which a developer without the repo cannot read,
   so the outcome ("from the docs alone") was not met: fixed. The endpoint description now states DIVIDE's
   rounding, the Result form, the Operand limits, the request limits and the failure order, each number taken
   from the code that enforces it; `statesTheRulesAClientNeeds` also checks no BR/PRD id remains.
2. `invalid-request` has five causes but one example and no list: fixed, the 400 description lists every cause.
3. 404/405/415/500 not documented: partly fixed. The description says they are Problem Details with `type`
   `about:blank`; no example, because a JSON request example cannot carry a wrong content type or method.
4. The drift check was near-tautological (enum and test both from `Operation.values()`): fixed,
   `everyDocumentedOperationIsAccepted` sends every documented name to the API.
5. springdoc's `ProblemDetail` schema listed a `properties` field the API never sends: fixed in the customizer.
6. `type` and `instance` documented as `format: uri`, but the API sends relative paths: fixed, `uri-reference`.
7. Every example had `description` equal to its name (springdoc's default): not fixed; harmless noise.
8. `CalculationResponse` marked no property required: fixed.

## Surprises

- Without annotations springdoc documented `a` and `b` as numbers only and the 200 as `*/*`, and no errors at
  all: exactly what STANDARDS §4 warns about.
- springdoc 3.2.0 already defaults to OpenAPI 3.1, so no version setting was needed.
- swagger-models exposes schema properties as raw `Schema`, so typed calls failed to compile until cast.
- An enum constant's lambda cannot read the enum's own static field, even qualified (`illegal forward reference`),
  so the BR-3 `ROUNDING` constant moved to `Calculation`.
- Two Python edit scripts stopped halfway because Spotless had reflowed the text they were matching; rewrote the
  test file whole instead.

## Spec drift

- `api-documentation` gained "The rules are stated, not referenced", "Every documented Operation is accepted",
  "The Problem Details schema matches the body", and required response properties.
- Design: Problem Details examples and the endpoint description generated in `OpenApiConfig` (not annotations);
  `Calculation.ROUNDING` added; `RequestBodyLimit` and its limit made public, and `JacksonConfig`'s limits
  package-private, so the docs can read them.

## Shortcuts taken

None.

## Lessons

- Read the outcome literally: "a developer who has never seen the code" rules out pointing at BR or PRD numbers.
  Check the docs as that developer would, not as the author.
- Generate docs from the same objects that produce the responses (exception messages, limit constants); then a
  replay test is a backstop, not the only guard.
- A drift test that derives both sides from the same source proves only that the wiring exists; make one side
  hit the real API.
