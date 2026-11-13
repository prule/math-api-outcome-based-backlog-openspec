# Retro: 4 — Hostile operands cannot take the service down

OpenSpec change archived at `openspec/changes/archive/2026-11-14-4-operand-limits/`. PR #5.

## Caught by review

**`/opsx:verify`**
- Task 5.1 (CI on the PR) open: closed by the draft PR's green check before archive. Nothing else.

**`/ponytail:ponytail-review`**
1. `JacksonConfig`'s limit constants were `public` but read nowhere else, and the adapter may not import `config`:
   fixed. Now one private 1,000 constant for strings and numbers, plus a private depth.
2. The limit numbers are written twice (constants and detail text): partly fixed. The body message is built from
   `RequestBodyLimit.MAX_CHARACTERS`. The 1,000 and 500 details stay literal, because the adapter cannot read
   `config`'s constants (ArchUnit). The boundary tests check both value and wording.

**`/code-review` — Standards**
- Limits duplicated as constant and text: as ponytail 2.
- `MAX_STRING_LENGTH` and `MAX_NUMBER_LENGTH` were two public constants for one PRD limit: fixed (ponytail 1).
- `OperandOutOfRangeException` reads `Calculation`'s BR-1 constants (Feature Envy): not fixed. The exception
  owns its client-safe message, like the other domain exceptions; the constants stay next to the check.
- Detail mapping by Jackson message prefix is brittle: not fixed. Jackson 3 has one `StreamConstraintsException`
  type for every limit; each mapping has a test, and an unmapped message still gives a correct `invalid-request`.
- BR-1 fixtures duplicated between `OperandLimitsTest` and `HostileRequestTest`: not fixed. Different test levels,
  different packages; sharing would couple them.
- `rejectsAOutside…` read badly: renamed `rejectsOperandAOutside…` (and the `b` twin).
- The body count used `String.length()`, UTF-16 units: fixed (see Spec 3).

**`/code-review` — Spec**
1. No test sent a body much larger than the limit: added `rejectsAHugeBodyQuickly` (5 MB, under 1 s).
2. An exponent beyond `int` range (`1E-2147483649`) gets the generic `invalid-request` detail, not a named
   limit: not fixed. Such a value is not a `BigDecimal`, so the parser rejects it before BR-1 can run; it is
   rejected in constant time and the detail says operands must be decimal numbers. Recorded in `design.md`.
3. Emoji-heavy bodies of ≤16,384 characters were rejected (UTF-16 units counted): fixed, code points counted;
   `countsCodePointsNotUtf16Units`.
4. The body was always decoded as UTF-8, ignoring `charset`: fixed, decoded in the declared charset;
   `countsTheBodyInItsDeclaredCharset` (UTF-16LE).
5. The "Number value length" branch might never fire: checked directly against Jackson. A 1,001-digit number
   reports "Number value length", a 5,000-digit one "String value length". Both map to the same detail.
6. The body limit applies to every `@RequestBody`, wider than the proposal's scope: not fixed. This is the only
   endpoint with a body, and any future one would want the same bound.

## Surprises

- `Math.abs(scale)` for BR-1 let `1E+2147483648` through: its scale is `Integer.MIN_VALUE`, whose absolute value
  is still negative. Found while writing the check, confirmed with a one-line Java program, and fixed with an
  explicit range check plus a test case.
- Jackson reports a JSON number longer than its first text buffer (~1,125 characters) as a *string* length
  violation. Found by probing the running app with a 15,000-digit number: the detail said "JSON string". Fixed
  with one detail for both limits; test added for 15,000 digits.
- JUnit `@ValueSource` needs compile-time constants, so `"9".repeat(100)` would not compile there; switched to
  `@MethodSource`.
- Jackson 3's defaults already matched two PRD limits (number length 1,000, nesting 500); only string length
  (default 100,000,000) and body size needed setting.

## Spec drift

- `calculation` "Request limits": strings and numbers share one detail; three scenarios were added in fix (code
  points not UTF-16 units, declared charset, a huge body not read whole).
- `design.md`: body counted in code points in the declared charset; one string-or-number detail; the
  beyond-`int` exponent case recorded as an accepted risk.

## Shortcuts taken

None.

## Lessons

- When a check guards an `int`, test `Integer.MIN_VALUE`: `Math.abs` is not safe there.
- A library's error *type* is a better hook than its message; where only the message exists, write a test per
  message, and probe the boundaries well past the limit, not just one past it.
- "Characters" in a contract needs pinning down (code points? which charset?) at propose time, not in review. Ask
  the spec to say it in a scenario.
