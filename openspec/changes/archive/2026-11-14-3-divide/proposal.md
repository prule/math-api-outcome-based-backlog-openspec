# Proposal

## Why

Backlog item 3, "Clients can divide". Clients have ADD, SUBTRACT and MULTIPLY; DIVIDE completes v1's four basic
Operations (PRD §3.1). Unlike the others its Result cannot always be exact, so it needs the one documented rounding
(BR-3) and a clear rejection for division by zero (BR-2).

## What Changes

- The `DIVIDE` Operation: `a ÷ b` rounded to 34 significant digits, `HALF_EVEN` (`MathContext.DECIMAL128`, BR-3),
  then normalised (BR-4).
- Division by any zero representation (`0`, `0.00`, `-0`) rejected with 422 Problem Details, `type`
  `/problems/division-by-zero` (BR-2).
- The unknown-operation `detail` now lists `DIVIDE` too (it is generated from the Operations).

**Out of scope:** client-chosen precision or rounding (item 9), `MODULO` and `POWER`'s use of BR-2 (item 6),
Operand limits BR-1 (item 4).

## Capabilities

### New Capabilities
None.

### Modified Capabilities
- `calculation`: adds division and its division-by-zero rejection; the unknown-operation requirement's `detail`
  now lists every supported Operation, `DIVIDE` included.

## Impact

- `domain`: one new `Operation` constant and `DivisionByZeroException`.
- `adapter.in.web`: one new Problem Details handler (422).
- Dependencies: none.
- Docs to update: none. PRD §3.1/§3.2 and DOMAIN-MODEL.md (§3 DIVIDE, BR-2, BR-3) already define DIVIDE.
