# calculation Specification

## Purpose
Lets a client service apply one arithmetic Operation to its Operands over HTTP and get back an exact decimal Result,
or a clear, machine-readable rejection.

## Requirements

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
`operation` or `a` is missing, `operation` is not a JSON string, an Operand is not a decimal number, or a binary Operation has no `b` (BR-9).

#### Scenario: Body is not valid JSON
- **WHEN** a client sends a body that is not valid JSON
- **THEN** the response is 400 `application/problem+json` with `type` `/problems/invalid-request`

#### Scenario: Required field missing
- **WHEN** a client omits `operation` or `a`
- **THEN** the response is 400 with `type` `/problems/invalid-request`

#### Scenario: Operation is not a string
- **WHEN** a client sends `"operation": 123` or `"operation": true`
- **THEN** the response is 400 with `type` `/problems/invalid-request`, not `unknown-operation`

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
- **THEN** the response is 400 with `type` `/problems/unknown-operation` and a `detail` naming `ADD`, `SUBTRACT`,
  `MULTIPLY` and `DIVIDE`

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

#### Scenario: Unexpected failure
- **WHEN** handling a request fails for a reason the service did not plan for
- **THEN** the response is 500 `application/problem+json` with `type` `about:blank` and a fixed `detail` that
  says nothing about the cause

#### Scenario: Framework errors carry a type
- **WHEN** the framework rejects a request (e.g. 404, 405, 415)
- **THEN** the Problem Details body has `"type": "about:blank"` written out

#### Scenario: Detail exposes no internals
- **WHEN** any of the rejections above is returned
- **THEN** its `detail` names no Java class, exception or stack frame

### Requirement: Division rounded the one documented way
`POST /api/v1/calculations` with `operation` `DIVIDE` SHALL return 200 with `a ÷ b` rounded to 34 significant
digits with `HALF_EVEN` rounding (BR-3), then normalised (BR-4); a quotient that fits is returned exactly.

#### Scenario: Non-terminating quotient
- **WHEN** a client sends `{"operation": "DIVIDE", "a": 1, "b": 3}`
- **THEN** the response is 200 with `"result": "0.3333333333333333333333333333333333"` (34 significant digits)

#### Scenario: Rounding of the 34th digit
- **WHEN** a client divides `2` by `3`, or `1` by `7`
- **THEN** the Results are `"0.6666666666666666666666666666666667"` and `"0.1428571428571428571428571428571429"`

#### Scenario: Ties round to even
- **WHEN** a client divides the 35-digit `10000000000000000000000000000000005` or
  `10000000000000000000000000000000015` by `1`
- **THEN** the Results are `"10000000000000000000000000000000000"` and `"10000000000000000000000000000000020"`

#### Scenario: Exact quotient
- **WHEN** a client divides `1` by `4`
- **THEN** the response has `"result": "0.25"`

#### Scenario: Negative divisor
- **WHEN** a client divides `1` by `-3`, or `-7` by `2`
- **THEN** the Results are `"-0.3333333333333333333333333333333333"` and `"-3.5"`

### Requirement: Division by zero is rejected
A `DIVIDE` whose `b` is zero in any representation SHALL be answered 422 Problem Details with `type`
`/problems/division-by-zero` (BR-2).

#### Scenario: Every zero representation
- **WHEN** a client divides by `0`, `0.00`, `-0` or `"0E+5"`
- **THEN** the response is 422 `application/problem+json` with `type` `/problems/division-by-zero`

#### Scenario: Missing b is still an invalid request
- **WHEN** a client sends `{"operation": "DIVIDE", "a": 1}`
- **THEN** the response is 400 with `type` `/problems/invalid-request` (BR-9 is checked before BR-2)

### Requirement: Other Operations are unchanged by DIVIDE
Adding `DIVIDE` SHALL NOT change the Result or rejection of any `ADD`, `SUBTRACT` or `MULTIPLY` request.

#### Scenario: Existing behaviour holds
- **WHEN** the existing tests for `ADD`, `SUBTRACT` and `MULTIPLY` run after `DIVIDE` is added
- **THEN** they pass unchanged

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
- **WHEN** a client sends a body of at most 16,384 characters that is more than 16,384 bytes in UTF-8, or whose
  emoji make it more than 16,384 UTF-16 units
- **THEN** it is not rejected for its size (a character is a code point)

#### Scenario: The body is counted in its declared charset
- **WHEN** a client sends a 10,000-character body encoded as UTF-16LE with `charset=UTF-16LE`
- **THEN** it is not rejected for its size

#### Scenario: A huge body is not read whole
- **WHEN** a client sends a body of several megabytes
- **THEN** it is rejected as over 16,384 characters within a second

#### Scenario: Huge numbers and strings
- **WHEN** a client sends an Operand as a JSON number of 1,001 or 15,000 digits, or a JSON string of 1,001
  characters
- **THEN** the response is 400 `invalid-request` whose `detail` names the 1,000-character limit for strings and
  numbers (one limit, one detail: the parser cannot always tell a very long number from a string)

#### Scenario: Deeply nested body
- **WHEN** a client sends a body nested more than 500 levels deep
- **THEN** the response is 400 `invalid-request` whose `detail` names the 500-level nesting limit
