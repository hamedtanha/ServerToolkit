# Documentation Governance

**Project:** Server Toolkit
**Status:** Active
**Last Updated:** 2026-07-16

---

## Purpose

This document defines how Server Toolkit documentation is written, reviewed, versioned, and synchronized with implementation and accepted decisions.

Documentation is part of the product. It must be accurate, current, traceable, and useful for future development.

---

## Core Principles

Server Toolkit follows living documentation.

Documentation must:

- Describe implemented behavior accurately.
- Avoid describing functionality that does not exist.
- Avoid leaving implemented functionality undocumented.
- Keep current-state documents synchronized with source code and repository configuration.
- Keep product and architecture decisions traceable through ADRs.
- Distinguish long-term policy from current implementation state.
- Distinguish architectural extensibility from implemented and verified support.
- Preserve historical release evidence and accepted ADR context.
- Review related documents whenever one document changes.
- Prefer focused precise updates, but replace materially stale documents when a patch would preserve contradictions.
- Avoid mass replacement of versions, terminology, or architecture labels without contextual review.

---

## Source of Truth

`PROJECT_STATE.md` is the primary documentation entry point for the current implementation state.

Detailed feature and engineering baseline state may be maintained under `docs/state/`.

When documentation conflicts exist, resolve them in this order:

1. Repository source code and configuration as implementation evidence.
2. `PROJECT_STATE.md`.
3. Focused current-state documents under `docs/state/`.
4. Accepted ADRs for decision authority.
5. Foundational policy and process documents.
6. `ROADMAP.md`.
7. `CHANGELOG.md`.
8. Feature-specific documents.
9. `README.md`.

Current-state documents summarize repository evidence and must be corrected when they diverge from it.

`README.md` summarizes the public-facing state and must not replace detailed project documentation.

---

## Product, Architecture, and State Separation

Use the following document ownership model:

| Information type | Primary document type |
|---|---|
| Long-term product direction | `PRODUCT_VISION.md` and product-direction ADRs |
| Significant accepted decision and rationale | ADR |
| Practical implementation architecture | `ARCHITECTURE.md` |
| Engineering selection and delivery rules | `ENGINEERING_STRATEGY.md` |
| Currently implemented versions and behavior | `PROJECT_STATE.md` and `docs/state/` |
| Planned milestone outcome | `ROADMAP.md` or accepted planning Issue |
| Notable completed or accepted change | `CHANGELOG.md` |
| Public project summary | `README.md` |

Proposed capabilities must not be presented as current implementation before merge and verification.

Architecture examples must not be interpreted as roadmap commitments.

---

## Support Claim Model

Platform and capability support statements must use three distinct meanings.

### Architecturally Permitted

The accepted architecture can accommodate a platform or capability without violating Core boundaries.

This does not mean an implementation exists.

### Implemented

A concrete workflow, provider, adapter, or transport exists in the repository.

This does not automatically mean every target environment has been verified.

### Verified

Automated tests, runtime evidence, or both confirm documented behavior for an identified environment.

Rules:

- Do not convert architectural extensibility into a universal support claim.
- Do not describe a named platform, service manager, transport, vendor, or integration as supported without implementation and evidence.
- Record verification scope precisely enough that future maintainers understand what was tested.
- Distinguish supported, unsupported, unknown, and unavailable capability states when the feature exposes them.
- Preserve unsupported or unverified limitations instead of hiding them behind generic wording.

---

## Policy and Current-State Separation

Stable engineering rules and the currently implemented baseline must not be mixed carelessly.

For build-toolchain and dependency maintenance:

- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md` defines evaluation and update rules.
- `state/BUILD_TOOLCHAIN_STATUS.md` records implemented versions and constraints.
- Gradle, CI, Android, and release configuration remain implementation evidence.

For feature implementation:

- `PROJECT_STATE.md` summarizes project-wide capability state.
- Focused documents under `docs/state/` record detailed implemented boundaries and explicit non-goals.
- `ROADMAP.md` records accepted milestone sequencing and remaining direction.
- Proposed UI, provider, transport, or integration behavior is not described as implemented before verification.

For remote capabilities:

- ADR-015 defines the platform-neutral product direction.
- ADR-016 defines Core, Capability Gateway, and Provider/Adapter responsibilities.
- Practical package and dependency application is described in `ARCHITECTURE.md` only when accepted or implemented.
- Support claims remain limited to available evidence.

---

## Version Metadata Rules

Documentation must distinguish current project version, historical milestones, document baselines, related milestones, and technical baseline versions.

### Current Project Version

The current project version is defined by `PROJECT_STATE.md` and summarized by `README.md`.

Do not duplicate the current project version in governance documents unless the document explicitly describes current project state.

### Document Baseline

Use `Document Baseline` when a document was introduced or stabilized during a specific milestone but remains valid.

```text
**Document Baseline:** 0.2.0-alpha
```

A document baseline is historical metadata and must not be mass-updated during routine synchronization.

### Related Milestone

Use `Related Milestone` when a document or ADR belongs to a specific roadmap milestone.

```text
**Related Milestone:** Version 0.4.0 — SSH
```

### Historical Version References

Do not rewrite historical version references during routine updates.

```text
v0.1.0 — Foundation
v0.2.0 — Android Architecture and Navigation
## [0.1.0] - 2026-07-01
```

### Technical Baseline Versions

Toolchain and dependency versions are current implementation metadata, not project release numbers.

Read them from repository-controlled configuration and summarize them in `state/BUILD_TOOLCHAIN_STATUS.md`.

Do not infer a current technical baseline from a release tag alone.

---

## Changelog Rules

`CHANGELOG.md` records notable project-facing changes.

Rules:

- Keep an `Unreleased` section.
- Group changes by type, such as Added, Changed, Fixed, Security, Deprecated, Removed, and Not Changed.
- Write project-facing changes rather than raw git log entries.
- Keep historical release entries unchanged.
- Record documentation changes that affect product direction, governance, architecture, security, support claims, release process, technical baseline, feature state, or source-of-truth state.
- Remove or revise an `Unreleased` statement when later unreleased work makes it false.

---

## Semantic Versioning Rules

Server Toolkit follows Semantic Versioning:

```text
MAJOR.MINOR.PATCH
```

Pre-release identifiers may be used during active development:

```text
0.x.y-alpha
```

During pre-1.0 development, minor versions represent major project milestones.

Project version metadata must not be confused with Android Gradle `versionCode` or `versionName`.

Toolchain and documentation changes do not automatically require a project-version change. Release impact is determined by the resulting artifact and accepted release plan.

---

## ADR Rules

Architecture Decision Records document accepted significant decisions.

Use ADRs for:

- Product-direction changes.
- Architecture and dependency-direction changes.
- Security decisions.
- Persistence strategy.
- Dependency injection strategy.
- Remote transport and networking decisions.
- Capability Gateway, Provider, Adapter, and support-state decisions with broad architectural impact.
- Backup, restore, credential, or trust boundaries.
- Significant release or compatibility decisions.
- Significant build-toolchain or dependency-governance decisions.
- Significant feature ownership, execution-safety, synchronization, or secure-storage boundaries.

Do not use ADRs for:

- Minor implementation details.
- Routine compatible dependency updates.
- A new entity that follows an accepted persistence and repository pattern.
- Speculative package structures without a concrete implementation need.

Accepted ADRs must not be rewritten casually.

When a decision changes:

- Create a new ADR.
- State whether it refines, extends, or supersedes an earlier ADR.
- Identify exactly which earlier decisions remain active.
- Update `docs/adr/README.md`.

---

## Document Status Values

### Active

The document is current and expected to evolve with the project.

### Foundational

The document defines long-term rules or product direction and changes only after focused review.

### Accepted

A decision, proposal, or policy has been accepted.

### Frozen

The document is intentionally preserved as a historical snapshot.

### Superseded

A newer document or ADR replaces all or a defined part of the document.

### Deprecated

The document is retained for reference but should not guide new work.

---

## Document Classification

### Level A — Foundational Documents

These define stable project rules and require focused review:

- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `DEVELOPMENT.md`
- `ENGINEERING_STRATEGY.md`
- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `RELEASES.md`

### Level B — Living Project Documents

These change with implementation or the current baseline:

- `PROJECT_STATE.md`
- Focused status documents under `docs/state/`
- `ROADMAP.md`
- `CHANGELOG.md`

### Level C — Supporting Documents

These support implementation, decision history, review, and collaboration:

- ADR documents and index.
- AI collaboration documents.
- Package structure documentation.
- Review checklists.
- Feature-specific operator and verification documents.

---

## Update Rules

When a document changes, review related documents for consistency.

At minimum:

- Product-direction changes update Product Vision, relevant ADRs, Project State, Roadmap, README, changelog, and the ADR index.
- Architecture decisions update ADR files, `ARCHITECTURE.md`, `ENGINEERING_STRATEGY.md` when selection rules change, and `docs/adr/README.md`.
- Current implementation changes update `PROJECT_STATE.md`.
- Detailed feature-state changes update the relevant `docs/state/` document.
- Package-boundary changes update `PACKAGE_STRUCTURE.md`.
- Build-toolchain baseline changes update `state/BUILD_TOOLCHAIN_STATUS.md`.
- Build-toolchain policy changes update the policy and affected process documents.
- Notable changes update `CHANGELOG.md`.
- Roadmap changes update `ROADMAP.md`.
- Security decisions update `SECURITY.md`.
- Release-process changes update `RELEASES.md`.
- AI collaboration rule changes update files under `docs/ai/`.

Documents intentionally left unchanged should be named in the pull request with the reason.

---

## Review Checklist

Before merging documentation changes, verify:

- The work is performed on a non-`main` branch.
- Changed documents match repository evidence and accepted decisions.
- Related documents were reviewed.
- Policy and current-state information remain separated.
- Architectural examples are not presented as implemented support.
- Support claims distinguish architecturally permitted, implemented, and verified behavior.
- Current toolchain versions match repository declarations.
- Proposed versions are not presented as implemented.
- Proposed UI, Gateway, Provider, transport, and integration behavior is not presented as implemented.
- ADR relationships and index entries are synchronized.
- `PROJECT_STATE.md` remains factual and current.
- Relevant focused state documents remain indexed.
- `CHANGELOG.md` includes the relevant `Unreleased` entry and contains no newly stale statements.
- Historical release evidence remains unchanged.
- No backup files, generated local files, or empty speculative packages are committed.
- Changed-file scope matches the pull request purpose.
- Repository validation passes.
- `git diff --check` passes when a local checkout is available.

---

## References

- Semantic Versioning
- Keep a Changelog
- Architecture Decision Records

---

## Related Documents

- `../README.md`
- `PRODUCT_VISION.md`
- `PROJECT_STATE.md`
- `ARCHITECTURE.md`
- `ENGINEERING_STRATEGY.md`
- `ROADMAP.md`
- `CHANGELOG.md`
- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md`
- `state/SERVER_INVENTORY_STATUS.md`
- `state/SSH_STATUS.md`
- `state/SAVED_COMMANDS_STATUS.md`
- `state/BUILD_TOOLCHAIN_STATUS.md`
- `SECURITY.md`
- `RELEASES.md`
- `DEVELOPMENT.md`
- `../PACKAGE_STRUCTURE.md`
- `adr/README.md`
- `ai/AI_RULES.md`
- `ai/AI_MEMORY.md`
