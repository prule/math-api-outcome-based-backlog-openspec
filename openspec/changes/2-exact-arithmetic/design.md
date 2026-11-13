# Design

## Context

Item 1 left an empty app with ArchUnit rules for `domain` and `application`. DOMAIN-MODEL.md §2 already fixes the
domain shape (`Operation` with `arity()`, `apply`, `named`; `Calculation` record checking BR-9 then BR-1;
`result()` normalising per BR-4; domain exceptions). STANDARDS.md §2–§4 fix the layers, records and Problem
Details advice. See proposal.md for why.

## Goals / Non-Goals

**Goals:**
- The domain shape from DOMAIN-MODEL.md §2, with only the three constants this item needs.
- One Problem Details path for every error, domain and framework alike.

**Non-Goals:**
- Omitting `b` from responses for unary Operations (no unary Operation exists until item 7).
- Bounding Operand size or body size. Until item 4 a huge Operand such as `1e999999999` can make `toPlainString()`
  and the arithmetic allocate enormous strings; item 4 closes this.

## Decisions

- **Domain**: `enum Operation { ADD, SUBTRACT, MULTIPLY }`, each constant with `arity()` 2 and an `apply(a, b)`
  lambda; `Operation.named(String)` wraps `valueOf` and turns `IllegalArgumentException` (and null) into
  `UnknownOperationException`, so lookup is exact and case-sensitive. `record Calculation(Operation operation,
  BigDecimal a, BigDecimal b)` throws `ArityMismatchException` from its compact constructor when `b`'s presence
  does not match the arity; `result()` returns `operation.apply(a, b).stripTrailingZeros()` (zero normalises to
  `0` on Java 8+). The domain returns `BigDecimal`; plain notation is the adapter's job (DOMAIN-MODEL.md §2).
- **BR-1 hook**: DOMAIN-MODEL.md says the constructor checks BR-9 then BR-1. Only BR-9 is added now; item 4 adds
  BR-1 in the same constructor, after the arity check.
- **Application**: `PerformCalculation.perform(String operation, BigDecimal a, BigDecimal b)` looks the Operation
  up, builds the `Calculation` and returns it. It is a plain class wired as a bean in
  `config.UseCaseConfig` (STANDARDS.md §2). Returning the `Calculation` lets the adapter read `result()`.
- **Web adapter**: `CalculationController` maps `POST /api/v1/calculations`. The request record has
  `@NotNull String operation`, `@NotNull BigDecimal a`, `BigDecimal b`, validated with `@Valid`.
  Jackson reads a JSON number straight into `BigDecimal` from its text, so `0.1` stays exact, and reads a JSON
  string through `new BigDecimal(String)`. The response record holds strings: `a` and `b` via `toPlainString()`
  of the received values (keeps their scale), `result` via `toPlainString()` of `result()`.
- **Strict text fields**: Jackson by default coerces a JSON number or boolean into a `String`, so
  `"operation": 123` would reach the lookup as `"123"` and be reported as `unknown-operation`. A
  `JsonMapperBuilderCustomizer` in `config.JacksonConfig` sets the coercion of Integer, Float and Boolean into
  textual types to Fail, making it an invalid request. (`@JsonFormat(lenient = FALSE)` on the field does not stop
  it in Jackson 3.) Numeric targets keep accepting JSON strings, so `"a": "0.1"` still works.
- **Error order (PRD §3.2)** falls out of the flow: body parse and `@Valid` (invalid request) run before the
  controller; inside it `named` (unknown operation) runs before the `Calculation` constructor (BR-9).
- **Problem Details**: one `@RestControllerAdvice` that extends `ResponseEntityExceptionHandler`, so framework
  errors (415, 405, 406, 404) are already `ProblemDetail`. It overrides the handlers for unreadable bodies and
  failed validation to give `type` `/problems/invalid-request` and a fixed, internals-free `detail`, and adds
  handlers for `UnknownOperationException` (`/problems/unknown-operation`, `detail` lists `Operation.values()`)
  and `ArityMismatchException` (`/problems/invalid-request`). The unknown name is not echoed, so the `detail`
  cannot leak or amplify client input. A catch-all `Exception` handler returns 500 Problem Details with a fixed
  `detail` and logs the cause, so nothing falls through to Boot's non-Problem-Details error JSON (e.g. a
  `BigDecimal` scale overflow before item 4's limits). Spring omits a `null` type when serialising, so
  `createResponseEntity` and the catch-all write `about:blank` explicitly (PRD §3.2: every error has a `type`).
- **ArchUnit**: add "adapter must not depend on config", with its own violating fixture, following item 1's
  pattern.
- **Tests** (STANDARDS.md §6): domain unit tests for every DOMAIN-MODEL.md §3 edge case of the three Operations,
  BR-4 and BR-9 and the name lookup; a `@WebMvcTest` slice for parsing, echoing, rendering and every error path;
  one happy-path request added to the existing `@SpringBootTest` smoke test; a second `@WebMvcTest` with a mocked
  use case for the catch-all, because item 4 will turn today's real trigger into a 400.

## Risks / Trade-offs

- [Huge Operands can exhaust memory before item 4] → Accepted for one item; the service is not public (PRD §2).
- [Jackson 3 coercion rules could turn `""` or `true` into a value] → Web tests pin both to `invalid-request`.
