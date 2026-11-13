# Design

## Context

Item 2 built `Calculation`'s compact constructor (BR-9) and `config.JacksonConfig`; DOMAIN-MODEL.md §2 says the
constructor checks BR-9, then BR-1 on each Operand present. Jackson 3's default stream-read limits are: nesting
500, number length 1,000, string length 100,000,000, document length unlimited. See proposal.md for why.

## Goals / Non-Goals

**Goals:**
- No request can make the service do work proportional to an attacker-chosen size beyond the documented limits.
- Each rejection says which limit was broken.

**Non-Goals:**
- Limits on headers, URLs or other endpoints (the servlet container bounds those already).
- Making the limits configurable.

## Decisions

- **BR-1 in the domain.** The `Calculation` constructor, after the arity check, checks each Operand present:
  `precision() <= 100` and `-1000 <= scale() <= 1000`, on the value as written (no normalisation). It throws
  `OperandOutOfRangeException(name)` with `name` `a` or `b`; the exception's message names the Operand and the
  limits, never the value. Both checks are O(1) on an existing `BigDecimal`, and `new BigDecimal("1e999999999")`
  is cheap, so the huge-exponent case is rejected before any arithmetic.
- **String, number and nesting limits in Jackson** (`JsonFactoryBuilderCustomizer` in `config.JacksonConfig`):
  `maxStringLength(1000)`, `maxNumberLength(1000)` and `maxNestingDepth(500)`, set explicitly even where they equal
  the default, so the PRD values are visible in code. Jackson checks them while tokenising, before a number is
  built, so parsing cannot become the attack.
- **Body size in characters via `RequestBodyAdvice`** in `adapter.in.web`. Jackson's `maxDocumentLength` counts
  bytes for a byte stream, but PRD §3.2 says characters. The advice's `beforeBodyRead` reads at most
  `4 × 16,384 + 1` bytes (a code point is at most 4 bytes in UTF-8, UTF-16 and UTF-32); more bytes, or more than
  16,384 code points once decoded in the `Content-Type` charset (UTF-8 if none), throws `RequestTooLargeException`;
  otherwise it hands the buffered bytes on. A character is a code point, so an emoji counts once. Working
  inside Spring MVC keeps every error in the one `@RestControllerAdvice` (STANDARDS.md §4); a servlet filter would
  not.
- **Naming the limit.** Jackson reports every limit as `StreamConstraintsException`, wrapped by Spring in
  `HttpMessageNotReadableException`. `handleHttpMessageNotReadable` walks the cause chain and maps the exception's
  message prefix (`String value length` or `Number value length` → one string-or-number detail, because Jackson
  reports a number longer than its first text buffer (~1,125 characters) as a string; `Document nesting depth`)
  to a fixed detail that names our limit; anything else keeps today's generic malformed-body detail. Jackson's own message is never sent
  (it names Jackson's classes). Tests pin each mapping, so a Jackson upgrade that rewords a message fails the build
  instead of silently losing the detail.
- **Problem Details**: `OperandOutOfRangeException` → 400 `/problems/operand-out-of-range`, title "Operand out of
  range", `detail` from the exception; `RequestTooLargeException` → 400 `invalid-request` naming the 16,384
  character limit.

## Risks / Trade-offs

- [An exponent beyond `int` range (`1E-2147483649`) is not a `BigDecimal` at all, so the parser rejects it as a
  generic invalid request before BR-1 can name it] → Accepted: no such value can be an Operand, it is rejected in
  constant time, and the detail still says operands must be decimal numbers.
- [Matching Jackson's message text is brittle] → Each mapping has a test; an unmapped message still returns a
  correct `invalid-request`, only less specific.
- [The advice buffers up to 64 KiB per request] → Bounded and small; the body was already read whole by Jackson.
- [A client can still send many valid requests] → Rate limiting is item 11, out of scope.
