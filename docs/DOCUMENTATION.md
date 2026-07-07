# Documentation Governance

**Project:** Server Toolkit
**Status:** Active
**Last Updated:** 2026-07-07

---

## Purpose

This document defines how Server Toolkit documentation is written, reviewed, versioned, and synchronized with the implementation.

Documentation is part of the product. It must be accurate, current, traceable, and useful for future development.

---

## Core Principles

Server Toolkit follows living documentation.

Documentation must follow these principles:

- Document implemented behavior accurately.
- Do not document functionality that does not exist.
- Do not leave implemented functionality undocumented.
- Keep documentation synchronized with source code.
- Keep architectural decisions traceable through ADRs.
- Prefer small, precise documentation updates over broad rewrites.
- Avoid mass replacement of version numbers.
- Review related documents whenever one document changes.

---

## Source of Truth

`PROJECT_STATE.md` is the source of truth for the current implementation state.

When documentation conflicts exist, resolve them in this order:

1. `PROJECT_STATE.md`
2. Accepted ADRs
3. `ROADMAP.md`
4. `CHANGELOG.md`
5. Feature-specific documents
6. `README.md`

`README.md` should summarize the current public-facing state. It must not replace detailed project documentation.

---

## Version Metadata Rules

Documentation must distinguish between current project version, historical milestone references, document baselines, and related milestones.

### Current Project Version

The current project version is defined by `PROJECT_STATE.md` and summarized by `README.md`.

Do not duplicate the current project version in governance documents unless the document itself is explicitly about the current project state.

### Document Baseline

Use `Document Baseline` when a document was introduced or stabilized during a specific project milestone but still remains valid.

Example:

    **Document Baseline:** 0.2.0-alpha

A document baseline is historical metadata. It must not be mass-updated during routine version synchronization.

### Related Milestone

Use `Related Milestone` when a document or ADR belongs to a specific roadmap milestone.

Example:

    **Related Milestone:** Version 0.4.0 — SSH

This is common for ADRs and milestone-specific planning documents.

### Historical Version References

Do not rewrite historical version references during routine documentation updates.

Examples:

    v0.1.0 — Foundation
    v0.2.0 — Android Architecture and Navigation
    ## [0.1.0] - 2026-07-01

These references describe project history, not the current project version.

---

## Changelog Rules

`CHANGELOG.md` records notable project changes.

The changelog should follow these rules:

- Keep an `Unreleased` section.
- Group changes by type, such as Added, Changed, Fixed, Security, Deprecated, and Removed.
- Write user-facing or project-facing changes, not raw git log entries.
- Keep historical release entries unchanged.
- Add documentation changes when they affect project governance, architecture, security, release process, or source-of-truth state.

---

## Semantic Versioning Rules

Server Toolkit follows Semantic Versioning.

Version numbers should follow this structure:

    MAJOR.MINOR.PATCH

Pre-release identifiers may be used during active development:

    0.x.y-alpha

During pre-1.0 development, minor versions represent major project milestones.

Version metadata in documentation must not be confused with Android Gradle `versionCode` or `versionName`. Android application version metadata must be reviewed separately during release preparation.

---

## ADR Rules

Architecture Decision Records document accepted significant technical decisions.

Use ADRs for:

- Architecture changes.
- Security decisions.
- Persistence strategy.
- Dependency injection strategy.
- SSH and networking decisions.
- Backup, restore, credential, or trust-boundary decisions.
- Major release or compatibility decisions.

Do not use ADRs for minor implementation details.

Accepted ADRs should not be rewritten casually. If a decision changes, create a new ADR that supersedes or updates the previous decision.

---

## Document Status Values

Use status values consistently.

### Active

The document is current and expected to evolve with the project.

### Foundational

The document defines long-term rules or product direction. It may change only after review.

### Accepted

A decision, proposal, or policy has been accepted.

### Frozen

The document is intentionally preserved as a historical snapshot. Do not use `Frozen` for documents that must stay synchronized with active development.

### Superseded

A newer document or ADR replaces this document.

### Deprecated

The document is retained for reference but should not guide new work.

---

## Update Rules

When a document changes, check related documents for consistency.

At minimum:

- Current implementation changes should update `PROJECT_STATE.md`.
- Notable changes should update `CHANGELOG.md`.
- Roadmap changes should update `ROADMAP.md`.
- Architecture decisions should update ADR files and `docs/adr/README.md`.
- Security decisions should update `SECURITY.md`.
- Release process changes should update `RELEASES.md`.
- AI collaboration rule changes should update files under `docs/ai/`.

---

## Review Checklist

Before committing documentation changes, verify:

- The working branch is not `main`.
- The changed documents match the current implementation.
- Related documents were reviewed.
- ADR index entries are synchronized.
- `PROJECT_STATE.md` remains factual and current.
- `CHANGELOG.md` includes a relevant unreleased entry when needed.
- No backup files are committed.
- `git diff --check` passes.

---

## References

- Semantic Versioning
- Keep a Changelog
- Architecture Decision Records

---

## Related Documents

- README.md
- PROJECT_STATE.md
- ROADMAP.md
- CHANGELOG.md
- ARCHITECTURE.md
- SECURITY.md
- RELEASES.md
- DEVELOPMENT.md
- docs/adr/README.md
- docs/ai/AI_RULES.md
- docs/ai/AI_MEMORY.md
