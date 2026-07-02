# Architecture

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Frozen  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines the high-level software architecture of the Server Toolkit Android application.

The goal of this architecture is to keep the application maintainable, scalable, testable, secure, and aligned with modern Android development practices.

---

# Architecture Status

The architecture baseline is stable for the current development stage.

Status:

```text
Frozen
```

Architectural changes require a clear technical reason and must be documented through an Architecture Decision Record.

---

# Design Goals

The architecture is designed to support:

- Maintainability
- Scalability
- Readability
- Testability
- Security
- Performance

Every significant architectural decision should support at least one of these goals.

---

# Architecture Pattern

Server Toolkit follows Google's recommended Android application architecture.

The application uses MVVM as the primary presentation architecture.

```text
UI
│
▼
ViewModel
│
▼
Repository
│
├── Local Data Source
└── Remote Data Source
```

---

# Application Style

Server Toolkit uses:

- Kotlin
- Jetpack Compose
- Single Activity architecture
- MVVM
- Repository Pattern
- Unidirectional Data Flow

---

# Application Layers

## UI Layer

Responsible for rendering the user interface.

Contains:

- Compose screens
- Reusable UI components
- Navigation
- Theme

Rules:

- The UI layer must remain declarative.
- The UI layer must not contain business logic.
- The UI layer must not communicate directly with databases, SSH clients, or network services.

---

## Presentation Layer

Responsible for presentation logic.

Contains:

- ViewModels
- UI state models
- User action handling

Responsibilities:

- Prepare UI state
- Handle user interactions
- Call repositories
- Expose observable state to the UI
- Coordinate screen-level behavior

---

## Domain Layer

The domain layer is not implemented in version `0.1.0`.

It may be introduced later if business logic becomes complex enough to justify it.

Possible future responsibilities:

- Use cases
- Business rules
- Validation rules
- Cross-feature orchestration

Rule:

The domain layer must not be introduced only for theoretical purity. It must solve a real complexity problem.

---

## Data Layer

Responsible for providing data to the rest of the application.

Contains:

- Repositories
- Local data sources
- Remote data sources
- Persistence adapters
- External service adapters

Rules:

- The data layer hides implementation details from ViewModels.
- ViewModels must depend on repository contracts, not database or network implementation details.
- Credentials and sensitive data must be handled according to SECURITY.md.

---

# Package Structure

Current baseline package structure:

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

Package changes should be incremental and justified by real implementation needs.

---

# Navigation

The application follows Single Activity architecture.

Navigation is implemented using Jetpack Compose Navigation.

Initial planned screens:

- Home
- Add Server
- Server Details
- Settings

Navigation implementation is part of milestone:

```text
v0.2.0 — Navigation
```

---

# Data Flow

Application data follows one-way flow.

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
  ├── Local Data Source
  └── Remote Data Source
```

This avoids tight coupling between layers and keeps state changes easier to reason about.

---

# Dependency Rules

Allowed direction:

```text
UI → ViewModel → Repository → Data Sources
```

Rules:

- Higher-level components must not depend on lower-level implementation details.
- Composables must not directly access repositories, databases, SSH clients, or storage APIs.
- Repository implementations may depend on data sources.
- Public contracts should remain stable once consumed by higher layers.

---

# Design Principles

The project follows:

- SOLID
- Separation of Concerns
- Single Responsibility Principle
- Dependency Inversion
- Single Source of Truth
- Unidirectional Data Flow

---

# Future Architecture Candidates

The following components may be introduced when required by implementation needs:

- Hilt for dependency injection
- Room for local persistence
- SSH client library
- Secure storage abstraction
- WorkManager for background tasks
- Retrofit or another HTTP client when HTTP APIs are required
- Background synchronization

These are candidates, not accepted architecture decisions.

Accepted decisions must be documented through ADRs.

---

# Architecture Decision Records

Significant architecture decisions are documented in:

```text
docs/adr/
```

ADR examples:

- Navigation framework
- Database technology
- SSH library
- Secure storage strategy
- Dependency injection strategy
- Background work strategy

ADRs document accepted decisions, not casual ideas.

---

# Document Governance

This document is foundational and frozen.

Changes are allowed only when:

- A new ADR changes architecture.
- An implementation reveals a serious architectural flaw.
- Android platform recommendations materially change.
- A security requirement forces an architectural update.

---

# Related Documents

- PRODUCT_VISION.md
- DEVELOPMENT.md
- ROADMAP.md
- PROJECT_STATE.md
- SECURITY.md
- docs/adr/README.md
