# Proposal

## Why

Backlog item 2, "Clients get exact results for add, subtract and multiply". The service has no API yet. This item
delivers the product's core promise (PRD §1, §4 Exactness) for the three Operations that need no rounding, and the
machine-readable rejections every later Operation will reuse.

## What Changes

- `POST /api/v1/calculations` (PRD §3.2) accepting `operation`, `a` and `b`, with Operands as JSON numbers or strings.
- The `ADD`, `SUBTRACT` and `MULTIPLY` Operations, exact (BR-3), with the Result normalised and returned as a
  plain-notation string (BR-4); `a` and `b` echoed as received.
- Arity checking for these binary Operations (BR-9): a missing `b` is an invalid request.
- RFC 9457 Problem Details for every error response: `invalid-request` (malformed body, missing field, non-numeric
  Operand, BR-9) and `unknown-operation`; framework errors (e.g. 415, 405) are Problem Details too.
- The ArchUnit rule that keeps `adapter` from depending on `config`, now that both packages exist (retro 1).

**Out of scope:** `DIVIDE` (item 3), Operand limits BR-1 and request size limits (item 4), API docs (item 5),
unary Operations and their response without `b` (item 7), `GET /api/v1/operations` (item 8).

## Capabilities

### New Capabilities
- `calculation`: performing one Operation on its Operands over HTTP, the Result it returns, and how a bad
  Calculation request is rejected.

### Modified Capabilities
None.

## Impact

- New code in `domain` (`Operation`, `Calculation`, domain exceptions), `application` (`PerformCalculation`),
  `adapter.in.web` (controller, request/response records, Problem Details advice) and `config` (use case wiring).
- Dependencies: `spring-boot-starter-validation` (already in STANDARDS.md §1) for required fields;
  `spring-boot-starter-webmvc-test` for the MockMvc web slice (STANDARDS.md §6).
- Docs to update: none. PRD §3.2 and DOMAIN-MODEL.md already define this contract, these Operations and rules.
