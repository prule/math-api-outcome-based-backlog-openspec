# Retro: 1 — Developers can build, check and ship the service

OpenSpec change archived at `openspec/changes/archive/2026-11-14-1-walking-skeleton/`. PR #1.

## Caught by review

**`/opsx:verify`**
- Task 5.1 (CI passes on the PR) open: closed by opening the PR as a draft before archive so CI could run.
- "Unformatted Java source is rejected" has no automated test: not fixed. A JUnit test would need to run Gradle
  from inside Gradle; checked by hand instead (see below), as `design.md` planned.
- Third ArchUnit rule (domain must not depend on adapter/config) redundant with "domain depends only on `java..`":
  deleted.

**`/ponytail:ponytail-review`**
1. Application rule had no test proving it can fail (empty package + `allowEmptyShould` would hide a typo): added
   fixture `archfixture.application.SpringAwareUseCase` and `applicationClassDependingOnSpringIsRejected`;
   loosening the rule now fails the build (checked by mutation).
2. CI `GITHUB_TOKEN` had the repo's default (possibly write) permissions: added `permissions: contents: read`.
3. Redundant domain rule: deleted (same as verify).
4. Fixture test chose its rule with `getFirst()`, so reordering would silently test another rule: rules are now
   named methods, each evaluated against its own fixture package.

**`/code-review` — Standards**
- New dependencies must be justified in the PR (STANDARDS §1): done in the PR body.
- Proposal cited PRD §3.2 for `/actuator/health`; it is §3.3: fixed.
- PRD says "liveness/readiness", design said probes were a non-goal: Boot 4 already serves both groups, so the
  spec gained a probes requirement and the smoke test covers all three endpoints.
- `getFirst()` coupling and untested application rule: fixed (ponytail 1 and 4).
- Nothing enforces adapter → config direction or DTOs staying out of the domain beyond the existing rules: not
  fixed. There are no adapter or config classes yet; add the rule when `adapter.in.web` first appears (item 2).
- Health test matched a raw `"status":"UP"` substring: now reads `$.status` with JsonPath.
- `archunit-junit5` pulled in for no `@ArchTest`: switched to plain `archunit`.

**`/code-review` — Spec**
- Formatting check had no automated test and was never tried on a Gradle Kotlin script: hand check extended to
  `settings.gradle.kts` (`spotlessKotlinGradleCheck` fails naming it). Still no automated test, for the reason
  above.
- Domain fixture uses only Spring, not Jakarta or Jackson: not fixed. The rule is an allowlist (`java..` and the
  domain only), so every non-JDK package is rejected the same way; one fixture proves the mechanism.
- Application rule bans all of `org.springframework..`, stricter than STANDARDS §2's "Spring web": not fixed.
  STANDARDS §2 also says use cases are plain classes wired in `config`, which needs no Spring at all. No doc
  change.
- Only one of three rules proven able to fail: fixed (ponytail 1 and 3).

## Surprises

- The ArchUnit rules and their fixture test were written together, so the red step of task 3.1 never ran. Proved
  instead by mutation: widening each rule to allow Spring made its fixture test fail.
- `pkill -f MathApiApplication` killed its own shell (exit 144): the pattern matched the command line running it.
  Switched to killing by port.
- First application-rule mutation check printed `0`: the grep looked for the test name in `-q` output, which
  prints none. Rerun reading the JUnit XML showed the failure. Cause: wrong place to look, not a test problem.
- Boot 4's Actuator exposes `/actuator/health/liveness` and `/readiness` with no configuration. The design had
  assumed it would not.

## Spec drift

- `service-health` gained "Service reports liveness and readiness" (PRD §3.3 asks for it; Boot gives it for free).
- Design: the third ArchUnit rule was dropped; rules became named builders with one fixture package each; plain
  `archunit` instead of `archunit-junit5`; CI token read-only; formatting hand check covers a Gradle Kotlin script.
- Process: task 5.1's evidence (CI green on the PR) only exists once a PR exists, but CLAUDE.md puts archive before
  the PR. Opened the PR as a draft after the fix step to get it.
- Process: `/opsx:propose` says to stop after planning, and `/opsx:update` says to confirm every edit with the
  user. CLAUDE.md says to run every step without pausing, so both were run straight through.

## Shortcuts taken

None.

## Lessons

- Check what the framework already does (Actuator groups) before writing a non-goal about it.
- For a rule that guards something empty, write the violating fixture per rule up front; a passing rule on an
  empty package proves nothing.
- Push and open the draft PR right after the fix step, so CI evidence is in hand before `/opsx:archive`.
