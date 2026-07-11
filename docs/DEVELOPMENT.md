# Development Process

**Project:** Server Toolkit
**Document Baseline:** 0.2.0-alpha
**Status:** Foundational
**Last Updated:** 2026-07-11

---

## Purpose

This document defines the engineering process used throughout the development of the Server Toolkit project.

The objective is to ensure that development remains consistent, maintainable, scalable, and production-ready throughout the entire lifecycle of the project.

This document is the engineering playbook for the project.

---

## Development Philosophy

Server Toolkit is developed as a production-quality software project.

The goal is not only to build a functional Android application, but also to demonstrate professional software engineering practices.

The project prioritizes:

- Maintainability.
- Readability.
- Scalability.
- Security.
- Documentation.
- Long-term sustainability.

Fast implementation is never preferred over good engineering.

---

## Development Methodology

Server Toolkit follows a Documentation-Driven Agile Development methodology.

The development process combines proven software engineering practices while remaining lightweight for a single-maintainer project.

The project is based on:

- Agile principles.
- Lightweight Scrum.
- GitHub Flow.
- Architecture Decision Records.
- Living Documentation.
- Semantic Versioning.

Core principles:

- Deliver small incremental improvements.
- Keep the project releasable.
- Document important decisions.
- Prefer engineering quality over development speed.
- Continuously improve the codebase.

---

## Development Workflow

Every feature follows the same engineering workflow.

```text
Roadmap
↓
Milestone
↓
Sprint
↓
GitHub Issue
↓
Architecture Decision, if required
↓
Documentation
↓
Feature Branch
↓
Implementation
↓
Testing
↓
Code Review
↓
Merge
↓
Release
↓
Git Tag
```

Every completed feature should leave the project in a releasable state.

---

## Git Workflow

The project follows GitHub Flow.

Permanent branch:

```text
main
```

Feature branches:

```text
feature/<feature-name>
```

Examples:

```text
feature/android-project
feature/server-inventory
feature/ssh
```

Bug fixes:

```text
fix/<bug-name>
```

Documentation:

```text
docs/<topic>
```

Rules:

- Never develop directly on `main`.
- Every feature must have its own branch.
- Merge using `--no-ff` when preserving explicit merge history is useful.
- Keep commit history meaningful.
- The `main` branch must always remain releasable.

---

## Commit Convention

The project follows Conventional Commits.

Examples:

```text
feat: add server inventory route
fix: resolve ssh timeout
docs: update architecture
refactor: simplify server inventory screen
test: add repository tests
chore: update dependencies
```

Commit messages should be concise, descriptive, and written in English.

---

## Branch Rules

- Never commit directly to `main`.
- Every feature starts from an up-to-date base branch.
- Every feature is developed in a dedicated branch.
- Every feature is merged only after review.
- The branch must build successfully before merge.

---

## Continuous Integration

GitHub Actions validates every pull request targeting `main` and every push to `main`.

The validation workflow is:

```text
.github/workflows/android-validation.yml
```

The workflow uses the committed Gradle Wrapper and the project's Java 17 toolchain to run:

```text
:app:compileDebugKotlin
:app:compileDebugAndroidTestKotlin
:app:testDebugUnitTest
:app:lintDebug
:app:assembleDebug
:app:assembleDebugAndroidTest
```

A failed validation must be resolved before merge.

Continuous integration complements local validation and manual Android runtime verification. It does not replace device- or emulator-based testing for user-facing workflows.

---

## Coding Standards

The project follows these engineering principles:

- Readability over cleverness.
- Self-documenting code.
- Small classes.
- Small functions.
- Single Responsibility Principle.
- Separation of Concerns.
- Consistent naming.
- Prefer immutable data.
- Prefer composition over inheritance.

---

## Comment Guidelines

Comments should explain **why**, not **what**.

Good comments explain:

- Architectural decisions.
- Business rules.
- Non-obvious algorithms.
- Important implementation details.

Avoid comments that simply repeat the code.

Good:

```kotlin
// Retry only for transient network failures.
```

Bad:

```kotlin
// Increment counter
counter++
```

---

## Documentation Rules

Documentation is considered part of the implementation.

Every significant feature should update the relevant documentation.

Possible documents include:

- `PRODUCT_VISION.md`
- `PROJECT_STATE.md`
- `ARCHITECTURE.md`
- `ROADMAP.md`
- `CHANGELOG.md`
- `SECURITY.md`
- `RELEASES.md`
- ADR documents

Documentation should evolve together with the source code.

---

## Living Documentation

The project follows the Living Documentation approach.

No document should describe functionality that does not yet exist.

Likewise, no implemented feature should remain undocumented.

Documentation always reflects the current state of the project.

---

## Architecture Decisions

Architecture Decision Records are created only after a significant technical decision has been accepted.

Typical ADR topics include:

- Navigation framework.
- Database technology.
- SSH library.
- Security architecture.
- Dependency injection.
- Networking framework.

ADRs document accepted decisions, not future ideas.

---

## Testing Strategy

Testing will be introduced gradually.

Planned testing levels:

- Unit tests.
- Integration tests.
- UI tests.

Testing coverage will increase as the project matures.

---

## Sprint Strategy

Development is organized into short, goal-oriented sprints.

Each sprint should deliver one cohesive feature area or engineering improvement.

Current sprint direction:

```text
Sprint 1  Android Architecture and Navigation
Sprint 2  Server Inventory Foundation
Sprint 3  Server Inventory Persistence
Sprint 4  Server Inventory Add/Edit Workflow
Sprint 5  SSH Connectivity
```

A sprint is complete only when:

- Implementation is complete for the agreed scope.
- Documentation is updated.
- Review is finished.
- The project builds successfully.
- The working tree is clean.

---

## Versioning

The project follows Semantic Versioning.

```text
MAJOR.MINOR.PATCH
```

Examples:

```text
0.1.0
0.2.0
1.0.0
1.1.0
2.0.0
```

Version numbers represent stable milestones in the project's evolution.

---

## Release Strategy

Every milestone may produce a tagged release.

Current release roadmap:

```text
v0.1.0  Foundation
v0.2.0  Android Architecture and Navigation
v0.3.0  Server Inventory Foundation
v0.4.0  SSH
v0.5.0  Operations
v0.6.0  Dashboard Evolution
v0.7.0  Monitoring
v0.8.0  Infrastructure Helpers
v0.9.0  Stabilization
v1.0.0  Initial Release
```

Every tagged version represents a stable checkpoint in the project's history.

---

## Security Principles

Security is a fundamental engineering requirement.

The following information must never be committed to Git:

- Passwords.
- API keys.
- SSH private keys.
- Access tokens.
- Certificates.
- Sensitive configuration files.

Sensitive data should always be stored using secure Android mechanisms.

---

## Project Quality Goals

Every commit should improve at least one of the following:

- Functionality.
- Maintainability.
- Readability.
- Reliability.
- Performance.
- Security.
- Documentation.

The project should become better after every completed feature.

---

## Definition of Done

A task is considered complete only when all of the following conditions are satisfied:

- Implementation is finished for the agreed scope.
- The project builds successfully.
- Code follows project standards.
- Documentation has been updated.
- ADRs have been updated when necessary.
- `CHANGELOG.md` has been updated when applicable.
- `PROJECT_STATE.md` reflects the current project status.
- Commit messages follow Conventional Commits.
- The project remains releasable.

Merging into `main` and tagging are release-level activities, not mandatory for every local feature commit.

---

## Engineering Rules

The following engineering rules are mandatory:

- Always develop in feature branches.
- Keep the `main` branch stable.
- Prefer readability over clever code.
- Keep documentation synchronized with implementation.
- Record important technical decisions using ADRs.
- Never introduce breaking architectural changes without an ADR.
- Every release should be reproducible.
- Every milestone should be taggable from a releasable state.

---

## Frozen Engineering Decisions

The following engineering decisions are considered stable and should not change without strong technical justification.

Development process:

- Documentation-Driven Agile Development.
- Lightweight Scrum.

Version control:

- Git.
- GitHub.
- GitHub Flow.

Architecture:

- MVVM.
- Single Activity.
- Repository Pattern.
- Unidirectional Data Flow.

Android:

- Kotlin.
- Jetpack Compose.
- Hilt.
- Navigation Compose.

Documentation:

- Living Documentation.
- Architecture Decision Records.

Versioning:

- Semantic Versioning.

Commits:

- Conventional Commits.

These decisions form the engineering foundation of the project.

Future changes to these decisions should be documented through a new ADR when architecturally significant.

---

## Continuous Improvement

Engineering is an iterative process.

The project should continuously improve through:

- Better architecture.
- Cleaner code.
- Improved documentation.
- Better testing.
- Better developer experience.

Continuous improvement is preferred over large-scale rewrites.

---

## Documentation Governance

Project documentation is divided into governance levels.

### Level A — Foundational Documents

These documents define stable project rules and should rarely change:

- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `DEVELOPMENT.md`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `RELEASES.md`

### Level B — Living Project Documents

These documents must change whenever implementation state changes:

- `PROJECT_STATE.md`
- `ROADMAP.md`
- `CHANGELOG.md`

### Level C — Supporting Documents

These documents support implementation and review:

- ADR documents.
- AI collaboration documents.
- Package structure documentation.
- Review checklists.

---

## Related Documents

- [Architecture](ARCHITECTURE.md)
- [Roadmap](ROADMAP.md)
- [Project State](PROJECT_STATE.md)
- [Changelog](CHANGELOG.md)
- [Release Process](RELEASES.md)
