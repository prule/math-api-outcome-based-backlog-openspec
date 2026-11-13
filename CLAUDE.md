# math-api (outcome-based backlog, OpenSpec)

Stateless Spring Boot 4 / Java 25 REST API for exact decimal arithmetic. Same PRD, domain model, standards and
outcome backlog as `../math-api-outcome-based-backlog-vibe`, but each item goes through OpenSpec (propose → apply →
archive). No agent pipeline: this is the OpenSpec half of a comparison with the vibe build of the same backlog.

Read before working:
- `docs/PRD.md` — scope, API contract, non-goals, roadmap
- `docs/DOMAIN-MODEL.md` — glossary and business rules (BR-n)
- `docs/STANDARDS.md` — architecture, conventions, testing
- `docs/BACKLOG.md` — ordered outcomes; update an item's status when you start it and when its PR merges
- `openspec/specs/` — the current behaviour, as built; `openspec/config.yaml` — rules for every OpenSpec artifact

Any change that alters scope, the API contract, domain terms or business rules, or standards MUST update the
matching doc in `docs/` in the same change, including its change log. Never renumber or reuse a BR id.

## Process

One backlog item at a time, on a branch `feat/<item>` from an up-to-date `main`. The OpenSpec change is named
`<n>-<item>` (e.g. `1-walking-skeleton`).

Run all seven steps in one go, from the user's prompt to the merged PR, without pausing between steps. Stop and ask
only when you cannot go on without the user:
- a review finding is a real trade-off with no clear answer in the docs (fix the clear ones, then ask about the rest);
- the item's outcome is ambiguous, or meeting it needs a change to the PRD, the domain rules or the standards that
  the item does not already call for;
- CI or the build fails and you cannot find the cause.

A finding you choose not to fix is not a reason to stop: give the reason in the retro.

1. **Propose**: the user prompts with the item's outcome from `docs/BACKLOG.md`. Run `/opsx:propose` with it to
   write `openspec/changes/<n>-<item>/` (proposal, design, spec deltas, tasks). You choose the design, within
   `docs/STANDARDS.md`; every "We know it's done when" check must map to a spec scenario. `openspec validate <change>`
   must pass.
2. **Apply**: run `/opsx:apply` to work the tasks; done when every task is ticked, every scenario has a passing test
   and `./gradlew clean build` passes.
3. **Review**: run all three on the change since `main`:
   - `/opsx:verify`: does the code match the change's specs, design and tasks?
   - `/ponytail:ponytail-review`: is it correct, safe, tested and minimal?
   - `/code-review`: does it follow `docs/STANDARDS.md` and achieve the backlog item's outcome?
4. **Fix**: fix every finding from any review (or record in the retro why not), rerun `./gradlew build`. If a fix
   changes the behaviour, update the change's spec deltas and tasks to match (`/opsx:update`).
5. **Archive**: run `/opsx:archive` so the spec deltas merge into `openspec/specs/` and the change moves to
   `openspec/changes/archive/`.
6. **Retro**: run `/ponytail:ponytail-debt`, then write `retrospectives/<n>-<item>.md` with five sections:
   - **Caught by review**: each finding from the three reviews and how it was fixed, or why it was not.
   - **Surprises**: each failed check or bug found while building, with its cause, or "cause unknown".
   - **Spec drift**: each place the code had to differ from the proposed specs, design or tasks, and why.
   - **Shortcuts taken**: each `shortcut:` comment this item added, with its file, its limit and when to upgrade.
   - **Lessons**: what to do differently next time.

   Write "None" under a section with nothing to report.
7. **PR**: commit (Conventional Commits) the code, the archived change, the updated specs and the retro together,
   push, open a PR filling in `.github/pull_request_template.md`, squash merge once CI is green.
