# Contributing Guide

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Active

**Last Updated:** 2026-07-01

---

# Welcome

Thank you for your interest in contributing to Server Toolkit.

This document describes the development workflow, coding standards, and contribution guidelines for the project.

Our goal is to maintain a clean, consistent, and professional codebase.

---

# Development Philosophy

Every contribution should improve at least one of the following:

- Functionality
- Readability
- Maintainability
- Performance
- Security
- Documentation

Quality is always preferred over speed.

---

# Before You Start

Please read the following documents first.

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md

---

# Workflow

Every contribution should follow this workflow.

```

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

Feature

```

feature/home-dashboard

feature/navigation

feature/ssh

```

Fix

```

fix/login-crash

fix/database

```

Documentation

```

docs/readme

docs/architecture

```

---

# Commit Messages

The project follows Conventional Commits.

Examples

```

feat: add server dashboard

fix: resolve ssh timeout

docs: update roadmap

refactor: simplify repository

style: reformat compose code

test: add repository tests

chore: update dependencies

```

---

# Coding Style

General rules

- Write readable code.
- Prefer simplicity.
- Keep functions small.
- Keep classes focused.
- Avoid duplicated code.
- Prefer immutable data.
- Use meaningful names.

---

# Kotlin Guidelines

Use

- data class
- sealed class
- enum class
- object

when appropriate.

Avoid unnecessary complexity.

---

# Jetpack Compose

Compose UI should remain declarative.

Business logic must never be placed inside composables.

Composable functions should be as small as possible.

---

# Documentation

Documentation is part of the implementation.

Every significant feature should update:

- CHANGELOG.md
- ROADMAP.md
- ADRs (when necessary)

---

# Comments

Comments should explain

- Why

not

- What

Good

```kotlin
// Retry only for temporary network failures.
```

Bad

```kotlin
// Increment i
i++
```

---

# Security

Never commit

- Passwords
- Private keys
- API tokens
- Certificates
- Sensitive server information

---

# Pull Requests

A pull request should

- Build successfully
- Pass tests
- Update documentation
- Follow project standards

---

# Code Review

During review we evaluate

- Architecture
- Readability
- Maintainability
- Security
- Performance
- Documentation

---

# Thank You

Every contribution helps improve the project.

Thank you for making Server Toolkit better.