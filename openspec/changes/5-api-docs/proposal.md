# Proposal

## Why

Backlog item 5, "Developers can integrate from the API docs alone". Today a developer must read the code or the
PRD to call the API. This item makes the service describe itself (PRD §3.3), completing v1 scope.

## What Changes

- springdoc serves an OpenAPI 3.1 document at `/v3/api-docs` and an interactive UI at `/swagger-ui.html`.
- `POST /api/v1/calculations` is documented with: the allowed `operation` values, generated from the domain;
  Operands as a JSON number or string, with an example of each; the all-string 200 response with examples; and
  every Problem Details `type` the API returns today, each as `application/problem+json` with an example.
- A test fails if the documented Operations differ from the real ones, or if any documented example differs from
  the response the API actually sends for its example request.
- STANDARDS.md §6 gains the `ApiDocumentationTest` row (the test level the docs check needs).

**Out of scope:** hand-written docs outside the generated spec; turning docs off in production (item 5a);
documenting `type`s the API does not return yet (`invalid-exponent`, `negative-square-root`,
`result-out-of-range`, items 6–7); documenting framework-only errors (404, 405, 415, 500), which are generic
`about:blank` Problem Details, not part of the PRD's error table.

## Capabilities

### New Capabilities
- `api-documentation`: the machine-readable and interactive description of the API that the service serves, and
  its guarantee of matching the real API.

### Modified Capabilities
None.

## Impact

- Dependency: `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0` (STANDARDS.md §1).
- `adapter.in.web`: OpenAPI annotations on the controller and request/response records.
- `config.OpenApiConfig`: API info and the `operation` values generated from `Operation.values()` (STANDARDS.md §4).
- New `ApiDocumentationTest`.
- Docs to update: STANDARDS.md §6 (test levels) and its change log. PRD §3.3 already describes the docs.
