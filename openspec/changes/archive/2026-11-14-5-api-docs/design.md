# Design

## Context

The API is one endpoint with one controller, records for request and response, and one Problem Details advice.
STANDARDS.md §1 fixes springdoc 3.2.0; §4 requires an explicit `@ApiResponse` per status (including the 200's
media type, which springdoc otherwise documents as `*/*`), a `problem+json` example per `type`, the allowed
`operation` values generated in `config.OpenApiConfig`, and `ApiDocumentationTest`. See proposal.md for why.

## Goals / Non-Goals

**Goals:**
- Docs a developer can integrate from, that cannot silently drift from the API.

**Non-Goals:**
- Examples for framework-only errors (404/405/415/500) or for `type`s not yet returned. The description says the
  former are Problem Details with `type` `about:blank`; a JSON request example cannot show a wrong content type.

## Decisions

- **Annotations in the adapter, generated values in config.** `@Operation`, `@RequestBody` examples and
  `@ApiResponse`s live on `CalculationController`; `@Schema` on the records documents `a`/`b` as number-or-string
  (OpenAPI 3.1 `type: [number, string]`) and the response fields as strings. `config.OpenApiConfig` holds the API
  `info` and an `OpenApiCustomizer` that sets `CalculationRequest.operation`'s `enum` from `Operation.values()`,
  writes the endpoint description from the code's own numbers (`Calculation.MAX_DIGITS`, `MAX_SCALE`,
  `ROUNDING`; `RequestBodyLimit.MAX_CHARACTERS`; `JacksonConfig`'s limits), and builds every Problem Details
  example from the message the domain exception itself carries (`new OperandOutOfRangeException("a")
  .getMessage()` and so on), so no domain value is copied into an annotation (STANDARDS.md §4). The annotations
  keep only the example names. It also corrects springdoc's `ProblemDetail` schema: Spring flattens the
  `properties` map into the body, so that field is removed, and `type`/`instance` are `uri-reference`.
- **BR-3 rounding as a constant**: `Calculation.ROUNDING` (`DECIMAL128`), used by DIVIDE and by the docs. It lives
  in `Calculation` beside the BR-1 limits because an enum's constants cannot read the enum's own static fields.
- **Named examples pair requests with responses.** Each request example has a name (e.g. `numbers`, `strings`,
  `invalid-request`, `unknown-operation`, `operand-out-of-range`, `division-by-zero`) and the response example
  with the same name is what that request returns. This gives a developer a worked example per outcome and gives
  the test a mechanical way to replay every example.
- **`ApiDocumentationTest`** fetches `/v3/api-docs` and checks: OpenAPI 3.1; the `operation` enum equals
  `Operation.values()` in order; `a`/`b` types; the 200 schema and media type; one `problem+json` example per
  documented `type`; that every documented Operation is accepted by the API; that the description states each
  rule; the `ProblemDetail` schema corrections; and replays every named request example, comparing status and body (JSON equality) with the
  same-named response example. springdoc is not part of the `@WebMvcTest` slice, so this test is a
  `@SpringBootTest` with MockMvc. STANDARDS.md §6 gets a row for it, since §6 today allows one `@SpringBootTest`.
- **OpenAPI version**: set `springdoc.api-docs.version=openapi_3_1` in `application.yaml` only if springdoc 3.2.0
  does not already default to 3.1; the test pins it either way.

## Risks / Trade-offs

- [Example JSON in annotations is verbose] → It is the documentation; the replay test keeps it honest.
- [A second `@SpringBootTest` costs startup time] → One extra context, cached by Spring across its methods.
