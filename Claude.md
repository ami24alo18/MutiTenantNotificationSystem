# Claude.md

This file mirrors [AGENTS.md](AGENTS.md) for tools that look for `Claude.md` / `CLAUDE.md`.

Follow the phased delivery rules in `AGENTS.md` exactly:

- Implement only the currently approved phase.
- Stop and wait for human confirmation before the next phase.
- Do not add out-of-scope infrastructure (Docker, Kafka, Redis, etc.) unless a later phase requires it.
- Prefer clean architecture, SOLID, Flyway-owned schema, and consistent API error envelopes.

Current completed phase: **Phase 3 – Template Management**.
