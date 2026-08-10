# Development Process

**Project:** Server Toolkit
**Document Baseline:** 0.2.0-alpha
**Status:** Foundational
**Last Updated:** 2026-08-10

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

Engineering maintenance follows the same review discipline but may be initiated by security, supportability, platform, compatibility, CI, or release-toolchain requirements rather than by product roadmap scope.

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

Engineering maintenance:

```text
chore/update-<component>
chore/upgrade-<toolchain-area>
```

Rules:

- Never develop directly on `main`.
- Every feature must have its own branch.
- Every independently reviewable maintenance change must have its own branch.
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
chore(build): upgrade Gradle wrapper
chore(deps): update Room dependencies
```

Commit messages should be concise, descriptive, and written in English.

---

## Branch Rules

- Never commit directly to `main`.
- Every feature starts from an up-to-date base branch.
- Every feature is developed in a dedicated branch.
- Every toolchain or dependency update is developed in a dedicated branch unless it is inseparable from one documented compatibility cluster.
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

## Build Toolchain and Dependency Maintenance

Build tools and dependencies must be updated through controlled, independently reviewable engineering changes.

The complete strategy is defined in [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md). The currently implemented versions are recorded in [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md).

Core rules:

- Update only with a concrete security, supportability, compatibility, reliability, platform, or maintenance justification.
- Do not update merely because a newer version exists.
- Treat Java, Gradle, Android Gradle Plugin, Kotlin, and KSP as a compatibility cluster.
- Treat Android SDK, Build Tools, NDK, packaging, and release scripts as a release-toolchain cluster.
- Keep toolchain maintenance separate from product-feature implementation unless technical coupling is explicit and unavoidable.
- Classify update risk before implementation.
- Run validation proportional to the affected layer and risk.
- Update the current technical baseline after accepted changes.
- Review ADR need when a significant baseline or policy decision changes.

The repository-controlled implementation is authoritative. Proposed versions must not be written into current-state documentation before they are implemented and merged.

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

Every significant feature or engineering baseline change should update the relevant documentation.

Possible documents include:

- `PRODUCT_VISION.md`
- `PROJECT_STATE.md`
- `ARCHITECTURE.md`
- `ROADMAP.md`
- `CHANGELOG.md`
- `SECURITY.md`
- `RELEASES.md`
- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md`
- `state/BUILD_TOOLCHAIN_STATUS.md`
- ADR documents

Documentation should evolve together with the source code.

---

## Living Documentation

The project follows the Living Documentation approach.

No document should describe functionality that does not yet exist.

Likewise, no implemented feature should remain undocumented.

Documentation always reflects the current state of the project.

Long-term policy and current implementation state must remain separated so that stable rules do not become stale version inventories and living status documents do not become accidental governance.

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
- Significant Java, platform, build-system, dependency-governance, native-code, or compatibility baseline decisions.

Routine compatible dependency updates do not require an ADR.

ADRs document accepted decisions, not future ideas.

---

## Testing Strategy

Testing will be introduced gradually.

Planned testing levels:

- Unit tests.
- Integration tests.
- UI tests.

Testing coverage will increase as the project matures.

Build-toolchain and dependency updates must additionally validate the layers they can affect, including generated code, persistence schemas, Android runtime behavior, security boundaries, CI, packaging, and release tooling where applicable.

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

Toolchain and dependency version changes do not automatically define a project release number. Release impact is determined by the accepted milestone and resulting application artifact.

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
v0.7.0  Remote Capability Foundation
v0.8.0  Operational Insights
v0.9.0  Stabilization
v1.0.0  Initial Production Release
```

Every tagged version represents a stable checkpoint in the project's history.

A toolchain change that can affect artifact bytes, packaging, signing, native-library processing, or application metadata invalidates prior candidate evidence for the pending release and requires complete rebuild and verification.

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

Security-sensitive dependency updates may be prioritized ahead of roadmap work but must remain reviewable and validated.

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
- `state/BUILD_TOOLCHAIN_STATUS.md` reflects accepted toolchain or dependency baseline changes when applicable.
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
- Follow `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md` for toolchain and dependency changes.
- Keep current technical versions synchronized in `state/BUILD_TOOLCHAIN_STATUS.md`.
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
- Separation of foundational policy from living current-state documents.

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
- Controlled toolchain and dependency maintenance.

Continuous improvement is preferred over large-scale rewrites.

---

## Documentation Governance

Project documentation is divided into governance levels.

### Level A — Foundational Documents

These documents define stable project rules and should rarely change:

- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `DEVELOPMENT.md`
- `ENGINEERING_STRATEGY.md`
- `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md`
- `SECURITY.md`
- `CONTRIBUTING.md`
- `RELEASES.md`

### Level B — Living Project Documents

These documents must change whenever implementation state changes:

- `PROJECT_STATE.md`
- Focused current-state documents under `docs/state/`.
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
- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md)
- [Roadmap](ROADMAP.md)
- [Project State](PROJECT_STATE.md)
- [Changelog](CHANGELOG.md)
- [Release Process](RELEASES.md)
