# Engineering Strategy

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-06

---

## Purpose

This document defines how Server Toolkit work is selected, sliced, designed, implemented, reviewed, and documented.

It does not replace the project state, architecture documentation, security documentation, contribution rules, or ADRs.

Current implementation status is defined in [Project State](PROJECT_STATE.md).

Architecture decisions are defined in the ADRs.

---

## Core Principle

Server Toolkit development follows a standards-aligned, business-roadmap-first, and shortest-safe-job-first strategy.

Work must first pass security, architecture, documentation, and releasability gates.

After those gates pass, tasks are selected and sliced according to product value, modularity, dependency consistency, and modern Android engineering practices.

---

## Non-Negotiable Engineering Gates

A task must not proceed if it violates any of these gates:

- Security and data-safety requirements.
- Accepted architecture decisions.
- Main branch releasability.
- Factual documentation.
- Clear ownership and lifecycle boundaries.
- Maintainability and testability expectations.

These gates apply before task size, convenience, or implementation speed.

---

## Task Selection Order

After the non-negotiable gates pass, task selection follows this order:

1. Business roadmap value.
2. Architecture and modularity fit.
3. Dependency consistency.
4. Shortest safe job first.
5. Modern Android alignment.

Shortest-job-first is not a reason to implement work that is unsafe, premature, undocumented, or disconnected from the product roadmap.

---

## Business Roadmap First

Implementation work must support the accepted product direction.

A task should not be selected only because it is technically interesting, quick to implement, or convenient.

Before starting a task, verify:

- The task supports the current product direction.
- The user-visible or architecture-visible value is clear.
- The task does not introduce premature abstractions.
- The task does not create orphan models, unused dependencies, or duplicated ownership.
- The task does not conflict with the current project state.

---

## Shortest Safe Job First

Work should be divided into the smallest safe slice that can be reviewed, tested, documented, and merged.

A safe slice must:

- Have one clear purpose.
- Keep `main` releasable.
- Avoid unrelated refactoring.
- Avoid broad rewrites unless explicitly justified.
- Include tests when behavior changes.
- Include documentation updates when behavior, architecture, security posture, roadmap status, or project scope changes.

Small pull requests are preferred because they reduce review risk and make architectural drift easier to detect.

---

## Architecture and Modularity

The project should preserve the existing Android architecture unless a stronger technical reason is documented.

The preferred structure is:

- Presentation layer for UI state, ViewModels, screens, and user events.
- Domain layer for models, use cases, and project-owned service contracts.
- Data layer for repository implementations, Room, SSHJ, Android-specific APIs, and external library integration.
- Dependency injection for wiring concrete implementations to contracts.
- Documentation and ADRs for significant architectural decisions.

Ownership must remain explicit.

A model, state value, dependency, or abstraction should not be introduced unless its owner, lifecycle, consumer, and boundary are clear.

---

## Dependency Strategy

Dependencies must be added conservatively.

Before adding a dependency, verify:

- The dependency solves a real current problem.
- The dependency has a clear architectural boundary.
- The dependency does not leak into layers that should remain independent.
- The dependency has acceptable maintenance, security, and compatibility characteristics.
- The project can test behavior that depends on it.
- The dependency is documented when it affects architecture or security.

Modern libraries are valuable only when they strengthen the product architecture.

They are not a substitute for clear boundaries, ownership, tests, and documentation.

---

## Android Engineering Standards

Server Toolkit should follow modern Android engineering practices.

Preferred technologies and practices include:

- Kotlin.
- Jetpack Compose.
- MVVM.
- StateFlow or lifecycle-aware observable state.
- Hilt for dependency injection.
- Room for local persistence.
- Navigation Compose.
- Coroutines for asynchronous work.
- KSP where annotation processing is required.
- Repository pattern for data access.
- Clear separation of UI, domain, and data concerns.
- Unit tests for domain and presentation behavior.
- Lint and build verification before merge.

New Android APIs or libraries should be introduced only when they fit the current architecture and solve a concrete problem.

---

## Documentation as Implementation

Documentation is part of the implementation.

When behavior, architecture, security posture, roadmap status, or project scope changes, documentation must be updated in the same pull request.

Documentation must remain factual:

- Do not document behavior that does not exist.
- Do not leave implemented behavior undocumented.
- Do not keep obsolete placeholder wording after real behavior is implemented.
- Prefer complete, coherent document updates over fragmented edits.

Significant architectural decisions require an ADR.

---

## Security and Data Safety

Security-sensitive work must be designed before implementation.

Credential handling, secure storage, backup behavior, SSH trust, session lifecycle, and command execution boundaries must remain explicit and reviewed.

No secret material may be stored unless a secure storage boundary, lifecycle, deletion behavior, tests, and documentation are already defined.

---

## Pull Request Standard

Each pull request should:

- Have one clear purpose.
- Use a short-lived branch.
- Keep the main branch releasable.
- Use Conventional Commits.
- Include tests when behavior changes.
- Include documentation updates when needed.
- Avoid unrelated cleanup.
- Avoid mixing feature work, refactoring, and documentation unless the connection is explicit.
- Explain scope, rationale, and verification.

---

## Task Selection Checklist

Before starting a task, confirm:

1. Does it support the current business roadmap or product direction?
2. Is the value clear?
3. Does it pass security and data-safety requirements?
4. Is the ownership boundary clear?
5. Does it fit the current architecture?
6. Is it the smallest safe slice?
7. Does it require a new dependency?
8. Does it require an ADR?
9. Does it require tests?
10. Does it require documentation updates?
11. Will `main` remain releasable after merge?

If the answer is unclear, reduce the scope or document the decision before implementation.
