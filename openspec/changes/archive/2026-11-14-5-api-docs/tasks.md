# Tasks

## 1. Serve the docs

- [x] 1.1 Write a failing `ApiDocumentationTest` (`@SpringBootTest` + MockMvc): `/v3/api-docs` is OpenAPI 3.1 and lists `/api/v1/calculations`; `/swagger-ui.html` is served
- [x] 1.2 Add `springdoc-openapi-starter-webmvc-ui:3.2.0` (and the 3.1 setting if not default); verify the test passes

## 2. Document the contract

- [x] 2.1 Extend the test: the `operation` enum equals `Operation.values()` in order; `a`/`b` are number-or-string with a numbers and a strings request example; the 200 is `application/json` with string properties and an example; 400 and 422 list `application/problem+json` with one example per `type`; verify it fails
- [x] 2.2 Add `config.OpenApiConfig` (info, `operation` enum from the domain) and the controller/record annotations with named request and response examples; verify the test passes

## 3. Examples match the API

- [x] 3.1 Extend the test to replay every named request example and compare status and JSON body with the same-named response example; make one example wrong and verify the test fails, then restore it

## 4. Docs

- [x] 4.1 Add the `ApiDocumentationTest` row to STANDARDS.md §6 and a change-log entry; verify it matches the test

## 5. CI

- [x] 5.1 Verify the PR for this change shows the `build` check passing

## 6. Integration

- [x] 6.1 Run `./gradlew clean build` and verify it passes

## Workflow follow-up

- Review, fix, draft PR, archive, retro, PR per `CLAUDE.md`.
