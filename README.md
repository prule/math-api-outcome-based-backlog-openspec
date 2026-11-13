# math-api (outcome-based backlog, OpenSpec)

A rebuild of [`math-api`](https://github.com/prule/math-api) from the same PRD, domain model, standards and
**outcome-based** [backlog](docs/BACKLOG.md) as
[`math-api-outcome-based-backlog-vibe`](https://github.com/prule/math-api-outcome-based-backlog-vibe). The backlog
states outcomes (who gets what, and how we know); the builder chooses the design.

This is the OpenSpec build: each item is turned into an OpenSpec change (`/opsx:propose`), implemented
(`/opsx:apply`), reviewed (`/opsx:verify`, ponytail and `/code-review`), fixed, archived into `openspec/specs/`
(`/opsx:archive`), given a retro in `retrospectives/`, and merged by PR (see [CLAUDE.md](CLAUDE.md)). No agent
pipeline. The vibe build works the same backlog without OpenSpec, so the two can be compared.

A stateless REST service for exact decimal arithmetic. See [docs/PRD.md](docs/PRD.md).

## Comparing the builds

Once both builds have finished the same backlog items, both are scanned with SonarQube using the same quality
profile (bugs, code smells, duplication, cognitive complexity, coverage). Neither build runs Sonar during its own
process, so the scan is a neutral yardstick, not something either build was tuned against.
