# ServerToolkit Engineering Handbook

## Purpose

This handbook is the navigation entry point for the engineering policies used to keep ServerToolkit maintainable, secure, reviewable, and factually documented as the product grows.

It complements rather than replaces:

- `docs/PRODUCT_VISION.md`;
- `docs/ARCHITECTURE.md`;
- `docs/ENGINEERING_STRATEGY.md`;
- `docs/DOCUMENTATION.md`;
- `docs/PROJECT_STATE.md`;
- focused documents under `docs/state/`;
- accepted Architecture Decision Records under `docs/adr/`.

## Governing Rule

> Every implementation unit must have an obvious owner, responsibility, lifecycle, and dependency direction.

When ownership is unclear, resolve the boundary before adding another model, dependency, package, state holder, repository, gateway, provider, or navigation destination.

## Source-of-Truth Navigation

| Concern | Authoritative source |
|---|---|
| Product direction and product principles | `docs/PRODUCT_VISION.md` |
| Accepted durable decisions and rationale | `docs/adr/` |
| Practical application and remote-capability architecture | `docs/ARCHITECTURE.md` |
| Integrated current-state architecture map | `docs/ARCHITECTURE_ATLAS.md` |
| Engineering selection and delivery rules | `docs/ENGINEERING_STRATEGY.md` |
| Documentation authority and synchronization | `docs/DOCUMENTATION.md` |
| Current implemented project state | `docs/PROJECT_STATE.md` |
| Detailed implemented feature and toolchain state | `docs/state/` |
| Canonical Android package ownership | `PACKAGE_STRUCTURE.md` |
| Planned milestone direction | `docs/ROADMAP.md` |
| Notable project-facing changes | `docs/CHANGELOG.md` |
| Time-bound evidence, findings, and proposals | `review/` |

When sources overlap, the more specific authoritative source governs its concern. Repository implementation and configuration remain the primary evidence for executable behavior.

## Change-Control Loop

Every behavior, architecture, security, persistence, workflow, build, or documentation change should follow this sequence:

1. Verify the current `main` baseline.
2. Define scope and explicit non-goals.
3. Build an impact matrix covering source, tests, documentation, security, persistence, build, and release concerns as applicable.
4. Inspect the relevant source-of-truth files and accepted ADRs.
5. Classify current support as architecturally permitted, implemented, or verified.
6. Implement only within the approved scope.
7. Run risk-appropriate validation.
8. Synchronize affected current-state documentation.
9. Record durable boundary decisions in an ADR when required.
10. Commit and review the change as an isolated GitHub Flow slice.

## Current Handbook Scope

This directory currently provides navigation and governance context only. Focused engineering policy documents should be added here only when a concrete review or implementation need demonstrates distinct ownership and avoids duplicating existing authoritative documents.

Issue `#135` governs the initial Architecture Atlas and engineering-review framework work.

Published architecture reviews are indexed in `review/INDEX.md`. Review records provide time-bound evidence and proposals; they do not replace living current-state documents or accepted ADRs.
