# Architecture

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Architecture Baseline  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines the high-level software architecture of the Server Toolkit Android application.

Its purpose is to establish a stable architectural foundation that supports long-term maintainability, scalability, security, and testability while remaining aligned with modern Android development practices.

This document describes the overall architecture. Individual technology selections are documented separately through Architecture Decision Records (ADRs).

---

# Architecture Status

The architecture baseline has been established for the current stage of the project.

Status:

```text
Architecture Baseline
```

This document defines the architectural direction of the project.

Major architectural changes require an accepted ADR.

Minor clarifications may be made during implementation as long as they remain consistent with accepted architectural decisions.

---

# Architecture Goals

The architecture is designed to prioritize:

- Maintainability
- Readability
- Scalability
- Testability
- Security
- Simplicity
- Clear separation of concerns
- Incremental evolution

Architectural decisions should favor long-term maintainability over short-term convenience.

---

# Architectural Style

Server Toolkit follows Google's recommended Android application architecture.

The application is based on:

- Kotlin
- Jetpack Compose
- Single Activity architecture
- MVVM
- Repository Pattern
- Unidirectional Data Flow (UDF)

The Domain layer is optional and will only be introduced when justified by implementation complexity.

---

# High-Level Architecture

```text
                 UI
                  │
                  ▼
             ViewModel
                  │
                  ▼
             Repository
          ┌──────────────┐
          │              │
          ▼              ▼
   Local Data      Remote Services
```

The Repository layer separates presentation logic from implementation details.

---

# Application Layers

## UI Layer

Responsibilities:

- Render Compose UI
- Collect UI state
- Send user actions
- Handle navigation

Rules:

- Declarative only
- No business logic
- No direct database access
- No direct SSH access
- No networking

---

## Presentation Layer

Responsibilities:

- ViewModels
- Screen state
- User interaction handling
- Coordination between UI and repositories

Rules:

- Expose immutable UI state
- Keep presentation logic independent from implementation details

---

## Domain Layer (Optional)

The Domain layer is not part of the initial implementation.

It may be introduced later if business logic becomes sufficiently complex.

Possible responsibilities include:

- Use cases
- Business rules
- Validation
- Cross-feature coordination

The Domain layer must solve a real engineering problem rather than satisfy architectural purity.

---

## Data Layer

Responsibilities:

- Repository implementations
- Local data sources
- Remote data sources
- External service adapters
- Persistence abstraction

Rules:

- Hide implementation details
- Expose stable interfaces
- Centralize data access
- Protect sensitive information

---

# Package Structure

The initial project uses a single Android application module.

Package organization:

```text
de.hamedtanha.servertoolkit

├── data
│   ├── local
│   ├── remote
│   └── repository
│
├── model
│
├── navigation
│
├── ui
│   ├── components
│   ├── screens
│   └── theme
│
├── utils
│
└── viewmodel
```

Packages should evolve gradually based on implementation needs.

---

# Module Strategy

Version 1.x uses a single application module.

The package structure is intentionally designed to support future modularization without major refactoring.

Multi-module architecture is deferred until justified by project size or build complexity.

---

# Data Flow

Application data follows unidirectional flow.

```text
User
 │
 ▼
Compose UI
 │
 ▼
ViewModel
 │
 ▼
Repository
 │
 ├── Local Data
 └── Remote Services
```

State changes should always move in one direction.

---

# Dependency Rules

Allowed dependency direction:

```text
UI
    ↓
ViewModel
    ↓
Repository
    ↓
Data Sources
```

Rules:

- UI never accesses repositories directly.
- UI never communicates with databases.
- UI never communicates with SSH libraries.
- ViewModels communicate only with repositories.
- Repository implementations own data access.
- Lower layers must not depend on higher layers.

---

# Security Principles

Architecture must support secure handling of:

- Credentials
- Private keys
- SSH sessions
- Server metadata
- Sensitive configuration

Security implementation details are defined separately in SECURITY.md and related ADRs.

---

# Future Architecture Decisions

The following technologies have not yet been selected:

- Dependency Injection framework
- Local persistence technology
- Secure storage implementation
- SSH library
- Background work strategy
- HTTP networking framework

Each of these requires an individual ADR before adoption.

---

# Design Principles

Server Toolkit follows:

- SOLID
- Separation of Concerns
- Single Responsibility Principle
- Dependency Inversion Principle
- Single Source of Truth
- Unidirectional Data Flow
- Explicit Architecture Decisions
- Incremental Development

---

# Architecture Evolution

Architecture should evolve conservatively.

Changes are justified only when:

- An accepted ADR supersedes an earlier decision.
- Implementation exposes architectural limitations.
- Platform recommendations materially change.
- Security requirements demand architectural changes.

Frequent redesign should be avoided.

---

# Related ADRs

Current:

- ADR-001 — Project Vision

Planned:

- ADR-002 — Navigation Strategy
- ADR-003 — Dependency Injection
- ADR-004 — Persistence Strategy
- ADR-005 — SSH Integration
- ADR-006 — Project Structure

---

# Related Documents

- README.md
- PRODUCT_VISION.md
- PROJECT_STATE.md
- ROADMAP.md
- DEVELOPMENT.md
- SECURITY.md
- docs/adr/README.md

---

# Notes

This document describes the architectural baseline of the project.

It should remain stable throughout implementation.

Detailed technology selections belong in ADRs rather than this document.