# Development Process

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Frozen

**Last Updated:** 2026-07-02

---

# Purpose

This document defines the engineering process used throughout the development of the Server Toolkit project.

The objective is to ensure that development remains consistent, maintainable, scalable, and production-ready throughout the entire lifecycle of the project.

This document is considered the engineering playbook for the project.

---

# Development Philosophy

Server Toolkit is developed as a production-quality software project.

The goal is not only to build a functional Android application, but also to demonstrate professional software engineering practices.

The project prioritizes:

- Clean Architecture
- Maintainability
- Readability
- Scalability
- Security
- Documentation
- Long-term sustainability

Fast implementation is never preferred over good engineering.

---

# Development Methodology

Server Toolkit follows a Documentation-Driven Agile Development (DDAD) methodology.

The development process combines proven software engineering practices while remaining lightweight for a single-maintainer project.

The project is based on:

- Agile Principles
- Lightweight Scrum
- GitHub Flow
- Architecture Decision Records (ADR)
- Living Documentation
- Semantic Versioning

Core principles:

- Deliver small incremental improvements.
- Keep the project releasable.
- Document important decisions.
- Prefer engineering quality over development speed.
- Continuously improve the codebase.

---

# Development Workflow

Every feature follows the same engineering workflow.

```
Roadmap

↓

Milestone

↓

Sprint

↓

GitHub Issue

↓

Architecture Decision (if required)

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

# Git Workflow

The project follows GitHub Flow.

Permanent branch

```
main
```

Feature branches

```
feature/<feature-name>
```

Examples

```
feature/navigation

feature/dashboard

feature/server-management

feature/ssh
```

Bug fixes

```
fix/<bug-name>
```

Documentation

```
docs/<topic>
```

Rules

- Never develop directly on `main`.
- Every feature must have its own branch.
- Merge using `--no-ff`.
- Keep commit history meaningful.
- The `main` branch must always remain releasable.

---

# Commit Convention

The project follows Conventional Commits.

Examples

```
feat: add server dashboard

fix: resolve ssh timeout

docs: update architecture

refactor: simplify repository

style: format compose code

test: add repository tests

chore: update dependencies
```

Commit messages should be concise, descriptive, and written in English.

---

# Branch Rules

- Never commit directly to `main`.
- Every feature starts from `main`.
- Every feature is developed in a dedicated branch.
- Every feature is merged only after review.
- Merge commits should preserve project history.

---

# Coding Standards

The project follows these engineering principles.

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

# Comment Guidelines

Comments should explain **why**, not **what**.

Good comments explain:

- Architectural decisions
- Business rules
- Non-obvious algorithms
- Important implementation details

Avoid comments that simply repeat the code.

Good

```kotlin
// Retry only for transient network failures.
```

Bad

```kotlin
// Increment counter
counter++
```

---

# Documentation Rules

Documentation is considered part of the implementation.

Every significant feature should update the relevant documentation.

Possible documents include:

- PRODUCT_VISION.md
- PROJECT_STATE.md
- ARCHITECTURE.md
- ROADMAP.md
- CHANGELOG.md
- SECURITY.md
- RELEASES.md
- ADR documents

Documentation should evolve together with the source code.

---

# Living Documentation

The project follows the Living Documentation approach.

No document should describe functionality that does not yet exist.

Likewise, no implemented feature should remain undocumented.

Documentation always reflects the current state of the project.

---

# Architecture Decisions

Architecture Decision Records (ADR) are created only after a significant technical decision has been accepted.

Typical ADR topics include:

- Navigation framework
- Database technology
- SSH library
- Security architecture
- Dependency Injection
- Networking framework

ADRs document accepted decisions, not future ideas.

---

# Testing Strategy

Testing will be introduced gradually.

Planned testing levels:

- Unit Tests
- Integration Tests
- UI Tests

Testing coverage will increase as the project matures.

---

# Sprint Strategy

Development is organized into short, goal-oriented sprints.

Each sprint delivers one complete feature.

Examples

Sprint 1

Navigation

Sprint 2

Dashboard

Sprint 3

Server Management

Sprint 4

Room Database

Sprint 5

SSH

A sprint is complete only when:

- Implementation is complete.
- Documentation is updated.
- Review is finished.
- The project builds successfully.

---

# Versioning

The project follows Semantic Versioning.

```
MAJOR.MINOR.PATCH
```

Examples

```
0.1.0

0.2.0

1.0.0

1.1.0

2.0.0
```

Version numbers represent stable milestones in the project's evolution.

---

# Release Strategy

Every milestone may produce a tagged release.

Current release roadmap

```
v0.1.0  Project Foundation

v0.2.0  Navigation

v0.3.0  Dashboard

v0.4.0  Server Inventory

v0.5.0  Local Storage

v0.6.0  SSH Connectivity

v0.7.0  Monitoring

v0.8.0  Xray Integration

v0.9.0  Beta Stabilization

v1.0.0  First Stable Release
```

Every tagged version represents a stable checkpoint in the project's history.

---

# Security Principles

Security is a fundamental engineering requirement.

The following information must never be committed to Git:

- Passwords
- API Keys
- SSH Private Keys
- Access Tokens
- Certificates
- Sensitive configuration files

Sensitive data should always be stored using secure Android mechanisms.

---

# Project Quality Goals

Every commit should improve at least one of the following:

- Functionality
- Maintainability
- Readability
- Reliability
- Performance
- Security
- Documentation

The project should become better after every completed feature.

---

# Definition of Done

A task is considered complete only when all of the following conditions are satisfied.

- Implementation is finished.
- The project builds successfully.
- Code follows project standards.
- Documentation has been updated.
- ADRs have been updated when necessary.
- CHANGELOG has been updated when applicable.
- PROJECT_STATE.md reflects the current project status.
- Commit messages follow Conventional Commits.
- Changes have been merged into `main`.
- The project remains releasable.

---

# Engineering Rules

The following engineering rules are mandatory.

- Always develop in feature branches.
- Keep the `main` branch stable.
- Prefer readability over clever code.
- Keep documentation synchronized with implementation.
- Record important technical decisions using ADRs.
- Never introduce breaking architectural changes without an ADR.
- Every release should be reproducible.
- Every milestone should be tagged.

---

# Frozen Engineering Decisions

The following engineering decisions are considered stable and should not change without strong technical justification.

Development Process

- Documentation-Driven Agile Development
- Lightweight Scrum

Version Control

- Git
- GitHub
- GitHub Flow

Architecture

- MVVM
- Single Activity
- Repository Pattern

Android

- Kotlin
- Jetpack Compose

Documentation

- Living Documentation
- Architecture Decision Records (ADR)

Versioning

- Semantic Versioning

Commits

- Conventional Commits

These decisions form the engineering foundation of the project.

Future changes to these decisions should be documented through a new ADR.

---

# Continuous Improvement

Engineering is an iterative process.

The project should continuously improve through:

- Better architecture
- Cleaner code
- Improved documentation
- Better testing
- Better developer experience

Continuous improvement is preferred over large-scale rewrites.


---

# Documentation Governance

Project documentation is divided into three governance levels.

## Level A — Foundational Documents

These documents define stable project rules and should rarely change:

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- SECURITY.md
- CONTRIBUTING.md
- RELEASES.md

Status:

```text
Frozen
```

Changes to foundational documents require at least one of the following:

- Accepted Architecture Decision Record
- Major product scope change
- Security requirement
- Release process correction
- Documented inconsistency that would mislead development

Cosmetic edits should be avoided unless they improve clarity without changing meaning.

## Level B — Planning Documents

Planning documents may change when milestones, priorities, or sequencing change:

- ROADMAP.md

Roadmap changes should remain consistent with PRODUCT_VISION.md and PROJECT_STATE.md.

## Level C — Operational Documents

Operational documents are expected to change frequently:

- PROJECT_STATE.md
- CHANGELOG.md

These documents track current work, completed work, release notes, and short-term project state.

---

# Documentation Freeze Rule

Foundational documents are frozen after version `0.1.0`.

A frozen document must not be edited casually during normal feature development.

Before changing a frozen document, verify:

- The change is necessary.
- The reason is documented.
- Related documents remain consistent.
- The change does not describe functionality that does not exist.
- The Last Updated field is updated.

---

# Current Milestone Model

The project uses versioned milestones:

```text
v0.1.0  Project Foundation
v0.2.0  Navigation
v0.3.0  Dashboard
v0.4.0  Server Inventory
v0.5.0  Local Storage
v0.6.0  SSH Connectivity
v0.7.0  Monitoring
v0.8.0  Xray Integration
v0.9.0  Beta Stabilization
v1.0.0  First Stable Release
```

This model is the single reference for milestone sequencing. ROADMAP.md and PROJECT_STATE.md must remain consistent with it.

---

# Related Documents

- PRODUCT_VISION.md
- PROJECT_STATE.md
- ARCHITECTURE.md
- ROADMAP.md
- CHANGELOG.md
- SECURITY.md
- RELEASES.md
- adr/README.md

---

# Revision History

| Version | Date | Description |
|----------|------------|-------------------------------------------|
| 0.1.0 | 2026-07-01 | Initial engineering process documentation. |