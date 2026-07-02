# AI Bootstrap

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Foundation Complete

**Last Updated:** 2026-07-01

---

# Purpose

This document is intended exclusively for AI assistants participating in the development of Server Toolkit.

Its purpose is to provide sufficient engineering context for an AI model to become productive within minutes, without requiring the full conversation history.

This document should always reflect the current state of the project.

---

# Project Summary

Server Toolkit is a modern Android application for Linux infrastructure management.

Unlike traditional Android SSH clients, Server Toolkit focuses on infrastructure workflows rather than terminal access alone.

SSH connectivity is considered one capability of the application—not its primary purpose.

The long-term objective is to build a production-quality infrastructure management companion for Android.

---

# Current Status

Project Version

```
v0.1.0
```

Current Phase

```
Foundation Complete
```

Current Sprint

```
Sprint 1
```

Sprint Goal

```
Navigation
```

Current Branch

```
main
```

Current Development State

```
Ready for feature development.
```

---

# Completed Milestones

Completed

- Android Studio project initialized
- Git repository configured
- GitHub repository created
- SSH authentication configured
- GitHub Flow established
- Conventional Commits adopted
- Semantic Versioning adopted
- Initial documentation completed
- Project architecture defined
- Development workflow documented
- ADR process established
- Foundation tagged as v0.1.0

---

# Architecture

Current architecture

- Kotlin
- Jetpack Compose
- MVVM
- Repository Pattern
- Single Activity Architecture
- Navigation Compose (planned)

Domain Layer

Currently omitted.

May be introduced later if business logic becomes sufficiently complex.

---

# Engineering Principles

Always prioritize

- Maintainability
- Readability
- Simplicity
- Scalability
- Security

Avoid unnecessary abstraction.

Do not introduce complexity before it becomes necessary.

---

# Documentation Philosophy

Documentation is treated as part of the product.

The project follows Living Documentation principles.

Documentation must always describe the current implementation.

Implemented functionality must never remain undocumented.

Future functionality must not be documented as completed.

---

# Documentation Hierarchy

Priority

1. PROJECT_STATE.md
2. ROADMAP.md
3. CHANGELOG.md

PROJECT_STATE represents reality.

ROADMAP represents future plans.

CHANGELOG represents historical changes.

These documents must remain consistent.

---

# Frozen Engineering Decisions

The following decisions are considered stable.

Language

- Kotlin

UI

- Jetpack Compose

Architecture

- MVVM
- Repository Pattern
- Single Activity

Git

- GitHub Flow
- Conventional Commits

Versioning

- Semantic Versioning

Documentation

- Living Documentation
- ADR
- English documentation
- English code comments

These decisions should not change without strong technical justification.

---

# ADR Summary

Accepted ADRs

- ADR-001 Project Vision

Future ADRs may include

- Navigation
- Room Database
- Secure Storage
- SSH Library
- Logging Strategy

---

# Current Roadmap

Current target

```
v0.2.0
```

Goal

```
Navigation
```

Future milestones

- Dashboard
- Server Management
- Room Database
- SSH Connectivity
- Monitoring
- Xray Integration
- Beta
- Production

---

# Development Workflow

The project follows Documentation-Driven Agile Development.

Workflow

Roadmap

↓

Sprint

↓

Issue

↓

ADR (if required)

↓

Documentation

↓

Feature Branch

↓

Implementation

↓

Testing

↓

Documentation Review

↓

Merge

↓

Release

↓

Tag

---

# AI Responsibilities

When assisting this project:

- Review existing documentation before making recommendations.
- Preserve architectural consistency.
- Protect long-term maintainability.
- Prefer production-quality solutions.
- Identify documentation inconsistencies.
- Identify technical debt.
- Recommend improvements with clear engineering justification.

Never redesign architecture without a compelling technical reason.

---

# Immediate Next Tasks

Current development should focus on:

1. Create navigation package.
2. Implement application navigation.
3. Create Screen definitions.
4. Build Navigation Graph.
5. Prepare Home screen placeholder.
6. Update PROJECT_STATE.md.
7. Update CHANGELOG.md.

---

# Files to Read First

When starting a new session, read these documents in order:

1. AI_BOOTSTRAP.md
2. PROJECT_STATE.md
3. DEVELOPMENT.md
4. PRODUCT_VISION.md
5. ARCHITECTURE.md

These files provide sufficient context for almost all engineering discussions.

---

# Notes for AI

This project values engineering quality over implementation speed.

When multiple valid solutions exist:

- Compare alternatives.
- Explain trade-offs.
- Recommend the best long-term solution.

Always act as a Technical Lead, Software Architect, Documentation Maintainer, and Code Reviewer—not merely as a code generator.