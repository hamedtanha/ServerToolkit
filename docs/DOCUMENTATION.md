# Documentation Governance

**Project:** Server Toolkit
**Status:** Active
**Last Updated:** 2026-07-15

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
- Separate long-term policy from current implementation state.
- Prefer small, precise documentation updates over broad rewrites.
- Avoid mass replacement of version numbers.
- Review related documents whenever one document changes.

---

## Source of Truth

`PROJECT_STATE.md` is the primary source-of-truth entry point for the current implementation state.

Detailed feature and engineering baseline state may be maintained in focused documents under `docs/state/`.

When documentation conflicts exist, resolve them in this order:

1. `PROJECT_STATE.md`
2. Focused current-state documents under `docs/state/`
3. Accepted ADRs
4. Foundational policy and process documents
5. `ROADMAP.md`
6. `CHANGELOG.md`
7. Feature-specific documents
8. `README.md`

Repository configuration and source code remain authoritative implementation evidence. Current-state documents summarize that evidence and must be corrected when they diverge from the repository.

`README.md` should summarize the current public-facing state. It must not replace detailed project documentation.

---

## Policy and Current-State Separation

Stable engineering rules and the currently implemented baseline must not be mixed into one document.

Use this model:

| Information type | Document type |
|---|---|
| Long-term rules and decision criteria | Foundational policy or process document |
| Currently implemented versions and behavior | Living current-state document under `docs/state/` or `PROJECT_STATE.md` |
| Significant accepted decision and rationale | ADR |
| Planned milestone outcome | `ROADMAP.md` or an approved planning document |
| Notable completed change | `CHANGELOG.md` |

For build-toolchain and dependency maintenance:

- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md` defines how updates are evaluated and performed.
- `state/BUILD_TOOLCHAIN_STATUS.md` records the currently implemented versions and constraints.
- Actual Gradle, CI, Android, and release configuration remains the implementation evidence.

Proposed versions must not be recorded as current state before implementation and merge.

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

### Technical Baseline Versions

Toolchain and dependency versions are current implementation metadata, not project release numbers.

They must be read from repository-controlled configuration and summarized in `state/BUILD_TOOLCHAIN_STATUS.md`.

Do not infer a technical baseline from a release tag alone. Historical release evidence must remain unchanged after later toolchain updates.

---

## Changelog Rules

`CHANGELOG.md` records notable project changes.

The changelog should follow these rules:

- Keep an `Unreleased` section.
- Group changes by type, such as Added, Changed, Fixed, Security, Deprecated, and Removed.
- Write user-facing or project-facing changes, not raw git log entries.
- Keep historical release entries unchanged.
- Add documentation changes when they affect project governance, architecture, security, release process, technical baseline, or source-of-truth state.

---

## Semantic Versioning Rules

Server Toolkit follows Semantic Versioning.

Version numbers should follow this structure:

    MAJOR.MINOR.PATCH

Pre-release identifiers may be used during active development:

    0.x.y-alpha

During pre-1.0 development, minor versions represent major project milestones.

Version metadata in documentation must not be confused with Android Gradle `versionCode` or `versionName`. Android application version metadata must be reviewed separately during release preparation.

Toolchain and dependency updates do not automatically require a project-version change. Their release impact is determined by the resulting product artifact and release plan.

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
- Significant build-toolchain baseline or dependency-governance decisions.

Do not use ADRs for minor implementation details or routine compatible dependency updates.

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

## Document Classification

### Level A — Foundational Documents

These documents define stable project rules and should change only after focused review:

- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `DEVELOPMENT.md`
- `ENGINEERING_STRATEGY.md`
- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `RELEASES.md`

### Level B — Living Project Documents

These documents must change when the implementation or current baseline changes:

- `PROJECT_STATE.md`
- Focused status documents under `docs/state/`
- `ROADMAP.md`
- `CHANGELOG.md`

### Level C — Supporting Documents

These documents support implementation, decision history, review, and collaboration:

- ADR documents.
- AI collaboration documents.
- Package structure documentation.
- Review checklists.
- Feature-specific operator and verification documents.

---

## Update Rules

When a document changes, check related documents for consistency.

At minimum:

- Current implementation changes should update `PROJECT_STATE.md`.
- Detailed feature-state changes should update the relevant documents under `docs/state/`.
- Build-toolchain or dependency baseline changes should update `state/BUILD_TOOLCHAIN_STATUS.md`.
- Build-toolchain update-policy changes should update `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md` and affected process documents.
- Notable changes should update `CHANGELOG.md`.
- Roadmap changes should update `ROADMAP.md`.
- Architecture decisions should update ADR files and `docs/adr/README.md`.
- Security decisions should update `SECURITY.md`.
- Release process or release-toolchain changes should update `RELEASES.md`.
- AI collaboration rule changes should update files under `docs/ai/`.

---

## Review Checklist

Before committing documentation changes, verify:

- The working branch is not `main`.
- The changed documents match the current implementation.
- Related documents were reviewed.
- Policy and current-state information remain separated.
- Current toolchain versions match repository declarations.
- Proposed versions are not presented as implemented.
- ADR index entries are synchronized when applicable.
- `PROJECT_STATE.md` remains factual and current.
- `CHANGELOG.md` includes a relevant unreleased entry when needed.
- Historical release evidence remains unchanged.
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
- state/SERVER_INVENTORY_STATUS.md
- state/SSH_STATUS.md
- state/BUILD_TOOLCHAIN_STATUS.md
- BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md
- ROADMAP.md
- CHANGELOG.md
- ARCHITECTURE.md
- SECURITY.md
- RELEASES.md
- DEVELOPMENT.md
- ENGINEERING_STRATEGY.md
- docs/adr/README.md
- docs/ai/AI_RULES.md
- docs/ai/AI_MEMORY.md
