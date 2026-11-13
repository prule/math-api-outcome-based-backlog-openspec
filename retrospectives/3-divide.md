# Retro: 3 — Clients can divide

OpenSpec change archived at `openspec/changes/archive/2026-11-14-3-divide/`. PR #4.

## Caught by review

**`/opsx:verify`**
- Task 3.1 (CI on the PR) open: closed by the draft PR's green check before archive. Nothing else found.

**`/ponytail:ponytail-review`**
- Nothing found. Checked quotients against Python's `decimal` (prec 34, HALF_EVEN), every zero form, zero
  numerators, BR-9 before BR-2, and extreme scales on the running app.

**`/code-review` — Standards**
1. Adding DIVIDE needed an exception and a handler besides the constant ("nothing else should need to change",
   STANDARDS §3): not fixed. DOMAIN-MODEL §2 says a new kind of failure needs exactly a domain exception and its
   Problem Details handler. A shared base exception for the coming 422s (BR-5, BR-6, BR-8) would be speculative
   with one user; reconsider at item 6.
2. The handler hard-coded "Cannot divide by zero." while the exception said "Division by zero", unlike the arity
   handler, which shows the exception's message: fixed. The domain exception now owns the client-safe message.
3. `DivideTest` copied `CalculationTest`'s helper: fixed. DIVIDE cases moved into `CalculationTest`.
4. DIVIDE missing from `OperationTest`'s arity and lookup cases: fixed.
5. The BR-2 zero check lives in the DIVIDE lambda and will recur in MODULO and POWER: not fixed, by design (no
   abstraction with one user); extract it in item 6.

**`/code-review` — Spec**
- Several scenarios (`2 ÷ 3`, `1 ÷ 7`, ties, negative divisors) are tested only in the domain: not fixed. The tasks
  split domain edge cases from HTTP parsing on purpose; the controller only delegates.

## Surprises

- None. Every test went red for the expected reason (missing classes; 500 instead of 422 before the handler) and
  green on the first implementation. The 500 in the red step showed item 2's catch-all working.

## Spec drift

- `design.md`: the 422 `detail` comes from the domain exception, not a string in the advice; domain tests live in
  `CalculationTest`, not a separate class.

## Shortcuts taken

None.

## Lessons

- Adding an Operation was as small as DOMAIN-MODEL §2 promises: one constant, one exception, one handler, no edit
  to any existing test except the generated unknown-operation list. Keep the enum the only place an Operation is
  defined.
- Put client-facing wording for a domain failure in the domain exception from the start; the arity exception set
  that pattern in item 2.
- Probing hostile input against the running app during review (retro 2's lesson) found nothing new this time,
  but confirmed the catch-all holds for DIVIDE's scale overflow.
