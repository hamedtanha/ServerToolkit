# Contributing Guide

**Project:** Server Toolkit
**Document Baseline:** 0.1.0
**Status:** Foundational
**Last Updated:** 2026-07-07

---

# Purpose

This document defines contribution rules, development expectations, and review standards for Server Toolkit.

The goal is to keep the codebase clean, consistent, maintainable, secure, and professionally documented.

---

# Development Philosophy

Every contribution should improve at least one of the following:

- Functionality
- Readability
- Maintainability
- Performance
- Security
- Documentation

Quality is preferred over speed.

---

# Before You Start

Read the following documents before contributing:

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md
- SECURITY.md
- PROJECT_STATE.md

---

# Workflow

Every contribution should follow this workflow:

```text
Issue
↓
Discussion
↓
Feature Branch
↓
Implementation
↓
Documentation
↓
Testing
↓
Commit
↓
Pull Request
↓
Review
↓
Merge
```

---

# Branch Naming

Feature branches:

```text
feature/navigation
feature/dashboard
feature/server-inventory
feature/ssh-connectivity
```

Fix branches:

```text
fix/navigation-crash
fix/database-migration
```

Documentation branches:

```text
docs/readme
docs/architecture
docs/project-state
```

Rules:

- Do not commit directly to `main`.
- Start feature branches from `main`.
- Keep branch names short and descriptive.

---

# Commit Messages

The project follows Conventional Commits.

Examples:

```text
feat: add app navigation graph
fix: resolve server form validation
refactor: simplify repository contract
docs: update project state
test: add navigation route tests
chore: update dependencies
```

Commit messages must be written in English.

---

# Coding Style

General rules:

- Write readable code.
- Prefer simple solutions.
- Keep functions small.
- Keep classes focused.
- Avoid duplicated code.
- Prefer immutable data.
- Use meaningful names.
- Avoid clever code that reduces maintainability.

---

# Kotlin Guidelines

Use the following constructs when appropriate:

- data class
- sealed class
- enum class
- object

Avoid unnecessary abstraction.

Abstractions must solve a real maintainability or testability problem.

---

# Jetpack Compose Guidelines

Compose UI should remain declarative.

Rules:

- Do not put business logic inside composables.
- Keep composables small.
- Hoist state where appropriate.
- Prefer immutable UI state.
- Keep navigation concerns separated from screen rendering logic.

---

# Documentation Requirements

Documentation is part of the implementation.

Every significant feature should update relevant documents, including:

- PROJECT_STATE.md
- CHANGELOG.md
- ROADMAP.md when milestone status changes
- ADRs when an architectural decision is accepted

Foundational documents are frozen and must not be edited casually.

---

# Comment Guidelines

Comments should explain why, not what.

Good:

```kotlin
// Retry only for temporary network failures.
```

Bad:

```kotlin
// Increment i.
i++
```

All code comments must be written in English.

---

# Security Rules

Never commit:

- Passwords
- Private keys
- API tokens
- Certificates
- Sensitive server information
- Real production credentials

Security-sensitive code must follow SECURITY.md.

---

# Pull Request Requirements

A pull request should:

- Build successfully
- Pass relevant tests
- Follow architecture rules
- Update documentation
- Avoid unrelated changes
- Keep the project releasable

---

# Code Review Criteria

During review, changes are evaluated for:

- Architecture
- Readability
- Maintainability
- Security
- Performance
- Testability
- Documentation accuracy

A change should not be merged only because it works.

It should also fit the long-term architecture of the project.

---

# Document Governance

This document is foundational and frozen.

Changes are allowed only when:

- The development workflow changes.
- The contribution process changes.
- A new mandatory quality rule is accepted.
- An ADR requires an update.

---

# Related Documents

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- SECURITY.md
- PROJECT_STATE.md
