# api-documentation Specification

## Purpose
Lets a developer who has never seen the code learn every Operation, request shape, Result and rejection of the
API from documentation the service serves itself, and guarantees that documentation matches the real API.

## Requirements

### Requirement: The service serves its API description
The service SHALL serve an OpenAPI 3.1 JSON document at `GET /v3/api-docs` and an interactive UI at
`GET /swagger-ui.html` (PRD §3.3).

#### Scenario: OpenAPI document
- **WHEN** a developer requests `GET /v3/api-docs`
- **THEN** the response is 200 JSON whose `openapi` field starts with `3.1` and whose `paths` include
  `/api/v1/calculations`

#### Scenario: Interactive UI
- **WHEN** a developer opens `/swagger-ui.html`
- **THEN** the service serves the interactive UI for that document

### Requirement: Every Operation is documented from the domain
The documented allowed values of `operation` SHALL be exactly the supported Operations, in their declared order,
taken from the domain rather than written by hand.

#### Scenario: Documented Operations match the real ones
- **WHEN** the documented `operation` values are compared with the Operations the service accepts
- **THEN** they are the same list, so adding or removing an Operation without the docs following fails a test

#### Scenario: Every documented Operation is accepted
- **WHEN** each documented `operation` value is sent with two valid Operands
- **THEN** the API returns 200 for every one

### Requirement: The rules are stated, not referenced
The endpoint description SHALL state the rules a client needs, with the numbers the service enforces: how each
Operation rounds, the Result's form, the Operand limits, the request limits, and the order in which failures are
reported. It SHALL NOT send the reader to BR or PRD numbers they cannot see.

#### Scenario: Rules a client needs
- **WHEN** a developer reads the description of `POST /api/v1/calculations`
- **THEN** it states DIVIDE's 34-digit HALF_EVEN rounding, the 100-digit and ±1000-scale Operand limits, the
  16,384-character body, 1,000-character string or number and 500-level nesting limits, and the failure order

### Requirement: Request shape documented with both Operand forms
The request body SHALL be documented with `operation`, `a` and `b`, with `a` and `b` accepting a JSON number or a
JSON string, and SHALL include an example request of each form.

#### Scenario: Operand forms
- **WHEN** a developer reads the `a` and `b` properties
- **THEN** each is documented as a number or a string, and the request examples include one with JSON numbers and
  one with JSON strings

### Requirement: Result documented as all strings
The 200 response SHALL be documented as `application/json` whose `operation`, `a`, `b` and `result` are all
strings, with an example response.

#### Scenario: 200 response
- **WHEN** a developer reads the 200 response of `POST /api/v1/calculations`
- **THEN** its media type is `application/json`, every property is a required string, and it has an example

### Requirement: Every rejection documented with an example
Every Problem Details `type` the API returns SHALL be documented under its status as `application/problem+json`
with an example: 400 `invalid-request`, `unknown-operation` and `operand-out-of-range`; 422 `division-by-zero`.

#### Scenario: Each type has an example
- **WHEN** a developer reads the 400 and 422 responses
- **THEN** each lists `application/problem+json` with one named example per `type` above, and the 400 description
  lists every cause of `invalid-request`

#### Scenario: The Problem Details schema matches the body
- **WHEN** a developer reads the Problem Details schema
- **THEN** it has no field the API never sends, and `type` and `instance` are `uri-reference` (relative paths)

### Requirement: Examples match the real API
Each documented response example SHALL equal the response the API actually sends for the documented request
example of the same name.

#### Scenario: Examples are replayed
- **WHEN** each named request example is sent to the API
- **THEN** the status and JSON body equal the response example with that name, so a drifted example fails a test
