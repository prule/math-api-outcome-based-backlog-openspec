# Spec Delta

## ADDED Requirements

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

## MODIFIED Requirements

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
