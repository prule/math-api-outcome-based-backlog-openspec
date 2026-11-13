# Spec Delta

## Purpose

Lets a client service apply one arithmetic Operation to its Operands over HTTP and get back an exact decimal Result,
or a clear, machine-readable rejection.

## ADDED Requirements

### Requirement: Exact addition, subtraction and multiplication
`POST /api/v1/calculations` with `operation` `ADD`, `SUBTRACT` or `MULTIPLY` SHALL return 200 with the exact Result
of `a + b`, `a − b` or `a × b`, with no rounding (BR-3) and never a binary floating-point approximation.

#### Scenario: Decimal addition is exact
- **WHEN** a client sends `{"operation": "ADD", "a": 0.1, "b": 0.2}`
- **THEN** the response is 200 with `"result": "0.3"`

#### Scenario: Addition edge cases
- **WHEN** a client adds negative Operands, or Operands with many significant digits
- **THEN** the Result is their exact sum, e.g. `-1.5 + -2.25 = "-3.75"` and
  `12345678901234567890.123456789 + 0.000000001 = "12345678901234567890.12345679"`

#### Scenario: Subtraction edge cases
- **WHEN** a client subtracts equal Operands, or a larger Operand from a smaller one
- **THEN** the Result is `"0"` for `1.50 − 1.5`, and `"-1.75"` for `1.25 − 3`

#### Scenario: Multiplication edge cases
- **WHEN** a client multiplies by zero, mixes signs, or multiplies Operands with fractional digits
- **THEN** `123.45 × 0 = "0"`, `-2 × 3 = "-6"`, `-2 × -3 = "6"`, and `1.5 × 1.5 = "2.25"`, `0.1 × 0.1 = "0.01"`

### Requirement: Operands as numbers or strings, Results as strings
The service SHALL accept each Operand as a JSON number or a JSON string holding a decimal number, and SHALL return
`operation`, `a`, `b` and `result` as JSON strings (PRD §3.2).

#### Scenario: String and number Operands give the same Result
- **WHEN** a client sends `{"operation": "ADD", "a": "0.1", "b": 0.2}`
- **THEN** the response is 200 with `"a": "0.1"`, `"b": "0.2"` and `"result": "0.3"`, all JSON strings

### Requirement: Operands echoed as received, Result normalised
The response SHALL echo `a` and `b` in plain notation with the scale they were sent with, and SHALL return the
Result normalised: trailing zeros stripped, plain (non-scientific) notation, and zero as `0` (BR-4).

#### Scenario: Operands keep their written scale
- **WHEN** a client sends `{"operation": "ADD", "a": "1.50", "b": 1e2}`
- **THEN** the response has `"a": "1.50"`, `"b": "100"` and `"result": "101.5"`

#### Scenario: Result is never in scientific notation
- **WHEN** a client sends `{"operation": "MULTIPLY", "a": "1E+3", "b": "1"}`
- **THEN** the response has `"result": "1000"`

### Requirement: Malformed requests are rejected as invalid
The service SHALL answer 400 Problem Details with `type` `/problems/invalid-request` when the body is not valid JSON,
`operation` or `a` is missing, an Operand is not a decimal number, or a binary Operation has no `b` (BR-9).

#### Scenario: Body is not valid JSON
- **WHEN** a client sends a body that is not valid JSON
- **THEN** the response is 400 `application/problem+json` with `type` `/problems/invalid-request`

#### Scenario: Required field missing
- **WHEN** a client omits `operation` or `a`
- **THEN** the response is 400 with `type` `/problems/invalid-request`

#### Scenario: Operand is not a number
- **WHEN** a client sends `"a": "abc"` or `"a": true`
- **THEN** the response is 400 with `type` `/problems/invalid-request`

#### Scenario: Binary Operation without b
- **WHEN** a client sends `{"operation": "ADD", "a": 1}` or `"b": null`
- **THEN** the response is 400 with `type` `/problems/invalid-request` (BR-9)

### Requirement: Unknown Operations are rejected
The service SHALL answer 400 Problem Details with `type` `/problems/unknown-operation` when `operation` is not
exactly the name of a supported Operation (case-sensitive), and its `detail` SHALL list the supported Operations.

#### Scenario: Unsupported name
- **WHEN** a client sends `"operation": "POWER"` or `"operation": "add"`
- **THEN** the response is 400 with `type` `/problems/unknown-operation` and a `detail` naming `ADD`, `SUBTRACT`
  and `MULTIPLY`

#### Scenario: Unknown Operation is reported before a missing b
- **WHEN** a client sends `{"operation": "FOO", "a": 1}`
- **THEN** the response has `type` `/problems/unknown-operation`, the first failure in PRD §3.2's order

### Requirement: Every error response is Problem Details
Every error response, including those raised by the framework, SHALL be `application/problem+json` with `type`,
`title`, `status` and a human-readable `detail` that exposes no internals (PRD §3.2).

#### Scenario: Unsupported media type
- **WHEN** a client posts the calculation with `Content-Type: text/plain`
- **THEN** the response is 415 `application/problem+json` with `title`, `status` and `detail`

#### Scenario: Unsupported method
- **WHEN** a client sends `GET /api/v1/calculations`
- **THEN** the response is 405 `application/problem+json`

#### Scenario: Detail exposes no internals
- **WHEN** any of the rejections above is returned
- **THEN** its `detail` names no Java class, exception or stack frame
