# Development Process

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Active

**Last Updated:** 2026-07-01

---

# Purpose

This document defines the engineering workflow used throughout the development of the Server Toolkit project.

Every contributor should follow these guidelines to maintain consistency, code quality, and project stability.

---

# Development Philosophy

Server Toolkit is developed as a production-quality software project.

The objective is not only to create a functional Android application, but also to demonstrate professional software engineering practices.

Documentation, architecture, testing, version control, and maintainability are considered first-class citizens.

---

# Development Workflow

Every feature follows the same lifecycle.

```

Issue

↓

Discussion

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

Commit

↓

Push

↓

Merge

↓

Release Notes

```

---

# Git Workflow

The project follows a simplified Git Flow.

Main branch:

```

main

```

Feature branches:

```

feature/<feature-name>

```

Examples

```

feature/navigation

feature/home-screen

feature/add-server

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

---

# Commit Convention

Conventional Commits are mandatory.

Examples

```

feat: add home dashboard

fix: resolve ssh timeout

docs: update architecture

refactor: simplify repository

test: add server model tests

style: format code

chore: update dependencies

```

---

# Branch Rules

The **main** branch must always remain buildable.

Development is performed only in feature branches.

Features are merged after review.

---

# Coding Standards

The project follows these coding principles.

- Readability over cleverness.
- Self-documenting code.
- Small classes.
- Small functions.
- Single Responsibility Principle.
- Consistent naming.
- Immutable data whenever possible.

---

# Comment Guidelines

Comments are written only when they provide additional value.

Good comments explain:

- Why
- Architectural decisions
- Complex algorithms

Bad comments explain obvious code.

Example

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

Every significant feature should update the appropriate documentation.

Possible documents include:

- PRODUCT_VISION.md
- ARCHITECTURE.md
- ROADMAP.md
- CHANGELOG.md
- ADR documents

Documentation must evolve together with the source code.

---

# Architecture Decisions

Every important technical decision should be documented as an Architecture Decision Record (ADR).

Examples

- Navigation framework
- Database technology
- SSH library
- Security implementation

---

# Testing Strategy

Testing is introduced gradually.

Planned test types

- Unit Tests
- Integration Tests
- UI Tests

---

# Versioning

The project follows Semantic Versioning.

```

MAJOR.MINOR.PATCH

```

Example

```

1.2.3

```

---

# Security Principles

Sensitive information must never be committed to Git.

Examples

- Passwords
- API Keys
- Private Keys
- Tokens
- Certificates

---

# Project Quality Goals

Every commit should improve at least one of the following:

- Functionality
- Maintainability
- Readability
- Performance
- Security
- Documentation

---

# Definition of Done

A task is considered complete when:

- Implementation is finished.
- Code builds successfully.
- Documentation is updated.
- Relevant ADRs are updated.
- Commit message follows the project convention.
- Changes are pushed to GitHub.

---

# Related Documents

- PRODUCT_VISION.md
- ARCHITECTURE.md
- ROADMAP.md
- CHANGELOG.md
- adr/README.md