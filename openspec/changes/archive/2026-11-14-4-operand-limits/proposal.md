# Proposal

## Why

Backlog item 4, "Hostile operands cannot take the service down". Today any Operand size is accepted:
`1e999999999 × 1e999999999` or a scale overflow reaches `BigDecimal` and either exhausts memory or ends in a 500,
and the body, its strings and its numbers are unbounded. This closes that resource-exhaustion hole before anyone
else calls the API (PRD §4 Safety).

## What Changes

- BR-1 enforced: an Operand, as written, with more than 100 significant digits or a scale outside ±1000 is
  rejected with 400 Problem Details `/problems/operand-out-of-range`, naming the Operand (`a` or `b`) but never
  echoing its value, before any arithmetic.
- Request limits enforced before any number is parsed (PRD §3.2): a body over 16,384 characters, a JSON string or
  number over 1,000 characters, or nesting deeper than 500 levels is rejected as `invalid-request`.
- Every limit rejection's `detail` names the limit broken, so the client knows which one it hit.
- PRD §3.2 request-limits line updated: the nesting limit (500) and "the `detail` names the limit" are added.

**Out of scope:** configurable limits; limits on any endpoint other than `POST /api/v1/calculations` (it is the
only one that reads a body); POWER's exponent and result limits (BR-5, BR-6; item 6).

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `calculation`: adds Operand limits (BR-1) and request limits; the existing requirements are unchanged.

## Impact

- `domain`: `OperandOutOfRangeException`; the `Calculation` constructor checks BR-1 after BR-9.
- `adapter.in.web`: a request-body advice that bounds the body in characters; Problem Details mapping for
  operand-out-of-range and for each request limit.
- `config.JacksonConfig`: Jackson stream-read limits for string length, number length and nesting depth.
- Dependencies: none.
- Docs to update: PRD.md §3.2 (request limits line) and its change log. DOMAIN-MODEL.md already defines BR-1.
