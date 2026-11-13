# Spec Delta

## ADDED Requirements

### Requirement: Operands within BR-1 limits
An Operand, as written, SHALL have at most 100 significant digits and a scale between -1000 and 1000 inclusive
(BR-1). Otherwise the request SHALL be answered 400 Problem Details with `type` `/problems/operand-out-of-range`,
whose `detail` names the Operand (`a` or `b`) and the limits but never echoes its value.

#### Scenario: Operands exactly at the limits are accepted
- **WHEN** a client sends an Operand with exactly 100 significant digits, `1E+1000` (scale -1000) or `1E-1000`
  (scale 1000)
- **THEN** the Calculation is performed and the response is 200

#### Scenario: One past each limit is rejected
- **WHEN** a client sends an Operand with 101 significant digits (such as `1` followed by 100 zeros), `1E+1001`
  or `1E-1001`
- **THEN** the response is 400 `application/problem+json` with `type` `/problems/operand-out-of-range`

#### Scenario: The rejected Operand is named, not echoed
- **WHEN** `b` is out of range and `a` is not
- **THEN** the `detail` names `b` and does not contain the value sent

#### Scenario: Huge exponents are rejected before any arithmetic
- **WHEN** a client sends `{"operation": "MULTIPLY", "a": "1e999999999", "b": "1e999999999"}`
- **THEN** the response is 400 `/problems/operand-out-of-range`, returned in well under a second

#### Scenario: BR-1 comes after unknown operation and operand count
- **WHEN** a request has an out-of-range `a` and also an unknown `operation`, or a binary Operation with no `b`
- **THEN** the response is `unknown-operation` or `invalid-request` respectively (PRD §3.2 order)

### Requirement: Request limits are enforced before parsing numbers
`POST /api/v1/calculations` SHALL reject as 400 `/problems/invalid-request` a body over 16,384 characters, any
JSON string or number over 1,000 characters, and nesting deeper than 500 levels, before any Operand is parsed into
a number (PRD §3.2), and the `detail` SHALL name the limit broken.

#### Scenario: Body at and over the size limit
- **WHEN** a client sends a valid calculation padded to exactly 16,384 characters, and one padded to 16,385
- **THEN** the first succeeds and the second is 400 `invalid-request` whose `detail` names the 16,384-character
  limit

#### Scenario: The body limit counts characters, not bytes
- **WHEN** a client sends a body of at most 16,384 characters that is more than 16,384 bytes in UTF-8
- **THEN** it is not rejected for its size

#### Scenario: Huge numbers and strings
- **WHEN** a client sends an Operand as a JSON number of 1,001 or 15,000 digits, or a JSON string of 1,001
  characters
- **THEN** the response is 400 `invalid-request` whose `detail` names the 1,000-character limit for strings and
  numbers (one limit, one detail: the parser cannot always tell a very long number from a string)

#### Scenario: Deeply nested body
- **WHEN** a client sends a body nested more than 500 levels deep
- **THEN** the response is 400 `invalid-request` whose `detail` names the 500-level nesting limit
