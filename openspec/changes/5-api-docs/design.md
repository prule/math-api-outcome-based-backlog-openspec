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
- Docs for framework-only errors (404/405/415/500) or for `type`s not yet returned.

## Decisions

- **Annotations in the adapter, generated values in config.** `@Operation`, `@RequestBody` examples and
  `@ApiResponse`s live on `CalculationController`; `@Schema` on the records documents `a`/`b` as number-or-string
  (OpenAPI 3.1 `type: [number, string]`) and the response fields as strings. `config.OpenApiConfig` holds the API
  `info` and an `OpenApiCustomizer` that sets `CalculationRequest.operation`'s `enum` from `Operation.values()`,
  so no Operation name is written by hand (STANDARDS.md §4).
- **Named examples pair requests with responses.** Each request example has a name (e.g. `numbers`, `strings`,
  `invalid-request`, `unknown-operation`, `operand-out-of-range`, `division-by-zero`) and the response example
  with the same name is what that request returns. This gives a developer a worked example per outcome and gives
  the test a mechanical way to replay every example.
- **`ApiDocumentationTest`** fetches `/v3/api-docs` and checks: OpenAPI 3.1; the `operation` enum equals
  `Operation.values()` in order; `a`/`b` types; the 200 schema and media type; one `problem+json` example per
  documented `type`; and replays every named request example, comparing status and body (JSON equality) with the
  same-named response example. springdoc is not part of the `@WebMvcTest` slice, so this test is a
  `@SpringBootTest` with MockMvc. STANDARDS.md §6 gets a row for it, since §6 today allows one `@SpringBootTest`.
- **OpenAPI version**: set `springdoc.api-docs.version=openapi_3_1` in `application.yaml` only if springdoc 3.2.0
  does not already default to 3.1; the test pins it either way.

## Risks / Trade-offs

- [Example JSON in annotations is verbose] → It is the documentation; the replay test keeps it honest.
- [A second `@SpringBootTest` costs startup time] → One extra context, cached by Spring across its methods.
