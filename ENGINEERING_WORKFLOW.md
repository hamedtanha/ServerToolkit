# Engineering Workflow

**Project:** ServerToolkit  
**Document Version:** 1.1  
**Status:** Active  
**Last Updated:** 2026-07-03  
**Owner:** Maintainer

---

# Purpose

This document defines the engineering workflow used throughout the ServerToolkit project.

Its purpose is to ensure that every architectural decision, source code change, documentation update, and release activity follows a consistent, maintainable, and production-quality process.

The workflow described in this document is mandatory for all future development within this repository.

This document defines how development work is performed. It does not replace architecture documentation, Architecture Decision Records, Definition of Done, or code review checklists.

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

---

# Git Workflow

ServerToolkit follows GitHub Flow.

The main branch must remain releasable at all times.

All development work must happen on short-lived feature, documentation, maintenance, refactoring, build, or fix branches.

Work must not be committed directly to the main branch unless explicitly required for emergency recovery.

Branch names should describe the purpose of the work.

Recommended branch prefixes:

- feature/
- docs/
- fix/
- refactor/
- chore/
- build/

Examples:

- feature/server-inventory
- feature/dashboard-mvvm
- feature/navigation-foundation
- docs/engineering-workflow
- docs/documentation-governance
- fix/navigation-start-destination
- refactor/package-structure
- build/hilt-integration

Branch names should be specific enough to describe the main purpose of the work.

Broad names such as feature/android-project should be avoided for new branches unless the branch represents an initial project setup or foundation phase.

The current feature/android-project branch is accepted as a foundation branch because it was created during the initial Android project setup and architecture foundation phase.

Future branches should use narrower names.

Commits must follow Conventional Commits.

Examples:

- feat: add dashboard view model
- build: integrate Hilt dependency injection
- docs: define documentation governance
- refactor: align source packages with architecture
- fix: correct navigation start destination

A commit should contain one logical change.

Avoid mixing unrelated changes in the same commit.

Interactive rebase may be used before pushing to clean local history.

Interactive rebase must not be used on commits that have already been pushed unless the branch is private and force-push impact is fully understood.

Before pushing, verify:

1. The project builds successfully.
2. The working tree is clean.
3. The recent Git history is readable.
4. No empty or meaningless commits exist.
5. No accidental IDE, build, or temporary files are staged.

After pushing, verify that the local branch is up to date with the remote branch.

---

# Build Verification

Every implementation commit must be created from a buildable project state.

Before committing code changes, run the project build.

Documentation-only changes should also be committed from a known buildable state unless there is a clear reason not to do so.

Build failures must be fixed before continuing with additional feature work.

Warnings should be reviewed and addressed when they indicate maintainability, compatibility, or security issues.

Known temporary build workarounds must be documented and revisited later.

The current Gradle setting android.disallowKotlinSourceSets=false is a temporary compatibility workaround and must remain visible as technical debt until a cleaner AGP, Kotlin, or KSP configuration removes the need for it.

---

# Documentation Workflow

Documentation is part of the implementation.

Documentation must be updated when a change affects:

- Project behavior
- Architecture
- Development workflow
- Public feature scope
- Build requirements
- Security assumptions
- Release process
- Known limitations

Documentation must not describe functionality that does not exist.

When one document changes, related documents must be reviewed for consistency according to Documentation Governance.

Large documentation changes should be split into smaller commits.

Empty documentation files must not be committed.

---

# Architecture Governance

Architecture changes must be intentional.

A significant architecture decision requires an Architecture Decision Record.

Examples of significant decisions include:

- Introducing a persistence technology
- Introducing a networking stack
- Changing dependency injection strategy
- Changing navigation architecture
- Introducing repository abstractions
- Changing package structure
- Changing security strategy

Accepted ADRs must not be rewritten.

If an accepted decision changes, a new ADR must be created and linked to the previous one.

Implementation must remain aligned with accepted ADRs unless a new decision supersedes them.

---

# Related Documents

- [Documentation Governance](DOCUMENTATION_GOVERNANCE.md)
- [Definition of Done](DEFINITION_OF_DONE.md)
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md)
- [AI Rules](AI_RULES.md)
- [AI Memory](AI_MEMORY.md)
- [ADR Template](ADR-TEMPLATE.md)
