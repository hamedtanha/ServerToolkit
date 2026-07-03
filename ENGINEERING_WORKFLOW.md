# Engineering Workflow

**Project:** ServerToolkit

**Document Version:** 1.0

**Status:** Active

**Last Updated:** 2026-07-03

---

# Purpose

This document defines the engineering workflow used throughout the ServerToolkit project.

Its purpose is to ensure that every architectural decision, source code change, documentation update, and release activity follows a consistent, maintainable, and production-quality process.

The workflow described in this document is mandatory for all future development within this repository.

---

# Engineering Principles

The ServerToolkit project follows a quality-first engineering philosophy.

The primary objectives are:

- Build maintainable software rather than quick solutions.
- Keep the project releasable at all times.
- Prefer readability over clever implementations.
- Prefer simplicity over unnecessary complexity.
- Treat documentation as part of the implementation.
- Minimize technical debt.
- Keep Git history meaningful and easy to review.
- Build features incrementally using small, reviewable commits.
- Validate every implementation through successful project builds.
- Protect long-term architecture consistency.

Every change must improve the overall quality of the project or preserve the existing quality level.

No implementation should intentionally introduce architectural inconsistencies.

---

# Development Workflow

Development in ServerToolkit follows a small-step, reviewable workflow.

The goal is to keep the project buildable, understandable, and easy to review after every change.

Every development task should follow this sequence:

1. Understand the scope of the change.
2. Verify the current Git status.
3. Make the smallest meaningful change.
4. Build the project.
5. Review the diff.
6. Commit with a clear Conventional Commit message.
7. Re-check the working tree.
8. Update related documentation when required.

Large changes must be split into smaller commits whenever possible.

A single commit should represent one clear engineering step.

Examples of acceptable commit scopes include:

- Adding a dependency.
- Creating an application bootstrap class.
- Adding a navigation destination.
- Creating a feature skeleton.
- Adding a UI state model.
- Connecting a screen to the navigation graph.
- Updating one documentation section.

Avoid commits that mix unrelated changes such as code, formatting, documentation, and architecture changes together.

The main branch must remain releasable at all times.

Feature work must happen on short-lived branches according to GitHub Flow.

---

# Feature Lifecycle

Every feature in ServerToolkit must be developed incrementally and consistently.

A feature should move through the following lifecycle:

1. Define the feature scope.
2. Create the feature package structure.
3. Add navigation destination if the feature is user-facing.
4. Add UI state.
5. Add ViewModel.
6. Add screen or UI components.
7. Add domain model when business logic is required.
8. Add repository abstraction when data access is required.
9. Add local or remote data source when needed.
10. Add tests when logic or data transformation exists.
11. Update related documentation.
12. Build and review before commit.

A feature must not introduce unrelated architectural changes.

If a feature requires a new architectural decision, an ADR must be created before or together with the implementation.

Feature implementation should remain aligned with the existing package structure.

Each feature should prefer the following structure when applicable:

- presentation/screen
- presentation/state
- presentation/viewmodel
- domain/model
- domain/repository
- data/local
- data/remote
- data/repository

Not every feature requires every layer.

Layers should be added only when they provide real value.

Empty packages, placeholder abstractions, and unused interfaces should be avoided unless they are part of an intentional feature skeleton with immediate follow-up implementation.
