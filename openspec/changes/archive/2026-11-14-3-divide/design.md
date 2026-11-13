# Design

## Context

Item 2 built `Operation` as an enum of `(arity, BinaryOperator)` constants, `Calculation.result()` stripping
trailing zeros, and one `ProblemDetailsAdvice`. DOMAIN-MODEL.md §2 says adding an Operation is one constant, its
rules and tests, plus a domain exception and handler for a new kind of failure. See proposal.md for why.

## Goals / Non-Goals

**Goals:**
- Add DIVIDE exactly the way DOMAIN-MODEL.md §2 says an Operation is added; nothing outside the enum, the new
  exception and its handler changes.

**Non-Goals:**
- A shared zero-check helper for MODULO and POWER (item 6 can extract one when a second user exists).

## Decisions

- **`DIVIDE(2, (a, b) -> ...)`** throws `DivisionByZeroException` when `b.signum() == 0`, else returns
  `a.divide(b, MathContext.DECIMAL128)`. `signum()` treats `0`, `0.00`, `-0` and `0E+5` alike (BR-2).
  `BigDecimal.divide(BigDecimal, MathContext)` is correctly rounded and returns an exact quotient unrounded when
  it fits in 34 digits, so `1 ÷ 4` is `0.25`; `Calculation.result()` then strips trailing zeros (BR-4) as for
  every Operation.
- **Failure order (PRD §3.2)** holds with no new code: BR-9 is checked in the `Calculation` constructor, BR-2 in
  `apply`, which runs later in `result()`. Item 4 adds BR-1 to the constructor, so it will also precede BR-2.
- **422 handler** in `ProblemDetailsAdvice`: `type` `/problems/division-by-zero`, title "Division by zero",
  `detail` from the exception's own client-safe message "Cannot divide by zero.", as the arity handler does, so the
  rule's wording lives in one place (STANDARDS.md §4: 422 for valid input that breaks a domain rule).
- **Tests**: domain tests in `CalculationTest` (one home for Operation tests) for every DOMAIN-MODEL.md §3 DIVIDE edge case plus HALF_EVEN ties (35-digit Operands
  divided by 1 force a tie at the 35th digit); web tests for a rounded Result, an exact one, every zero form and
  the BR-9-before-BR-2 order. The unchanged ADD/SUBTRACT/MULTIPLY tests are the "changed nothing" check; the
  unknown-operation web test gains `DIVIDE`.

## Risks / Trade-offs

- [Huge Operands still unbounded until item 4] → Same as item 2; DIVIDE adds no new exposure because its work is
  bounded to 34 digits of precision.
