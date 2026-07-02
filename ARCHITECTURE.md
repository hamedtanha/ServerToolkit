# Architecture

**Project:** Server Toolkit  
**Version:** 0.2.0-alpha  
**Status:** Active  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines the practical application architecture for the Server Toolkit Android application.

It translates accepted Architecture Decision Records into implementation rules for project structure, layer responsibilities, dependency direction, data flow, navigation, and persistence.

This document is not a product roadmap. It must describe the architecture that the project currently follows or has explicitly accepted for implementation.

---

# Architecture Baseline

Server Toolkit is a modern Android application for Linux server administration and infrastructure management.

The application follows:

- Kotlin
- Jetpack Compose
- Single Activity architecture
- MVVM
- Repository Pattern
- Unidirectional Data Flow
- Room for local persistence
- Jetpack Navigation Compose for screen navigation

The architecture is intentionally simple at the current project stage.

The project must not introduce additional layers, frameworks, or abstractions unless they solve a real implementation problem.

---

# Accepted Architecture Decisions

The current architecture is governed by the following ADRs:

| ADR | Decision | Status |
|---|---|---|
| ADR-001 | Project Vision | Accepted |
| ADR-002 | Application Architecture | Accepted |
| ADR-003 | Local Persistence with Room | Accepted |
| ADR-004 | Navigation Strategy | Accepted |

Accepted ADRs are the source of truth for architectural decisions. This document explains how those decisions are applied in the codebase.

---

# Design Goals

The architecture prioritizes:

- Maintainability
- Readability
- Testability
- Security
- Incremental scalability
- Low accidental complexity
- Clear ownership of responsibilities

When trade-offs are required, Server Toolkit prioritizes long-term maintainability over short-term implementation speed.

---

# Application Layers

Server Toolkit uses a pragmatic layered architecture.

```text
UI Layer
   ↓
Presentation Layer
   ↓
Domain Contracts / Models
   ↓
Data Layer
   ↓
Local Persistence
```

The dependency direction must remain downward. Lower-level implementation details must not leak into higher layers.

---

# UI Layer

The UI layer is responsible for rendering application screens and handling user interaction events.

## Contains

- Compose screens
- Reusable Compose components
- Theme definitions
- Navigation host integration
- UI-specific state rendering

## Rules

- Composables must remain declarative.
- Composables must not contain business logic.
- Composables must not access Room DAOs directly.
- Composables must not access repositories directly.
- Composables receive state and emit events.
- Screen-level behavior belongs in ViewModels.

## Example Responsibilities

A server list screen may:

- Display a list of servers
- Display an empty state
- Show loading or error state
- Emit an event when the user selects a server
- Emit an event when the user taps an add button

It must not:

- Query the database directly
- Construct database entities
- Perform validation rules directly when those rules are shared beyond the screen
- Decide persistence behavior

---

# Presentation Layer

The presentation layer coordinates UI state and user actions.

## Contains

- ViewModels
- UI state classes
- UI event handlers
- Screen-specific state mapping

## Rules

- ViewModels expose immutable observable UI state.
- ViewModels call repository contracts instead of data-source implementations.
- ViewModels must not depend on Room DAOs.
- ViewModels must not contain Android UI rendering logic.
- ViewModels may perform simple presentation-specific validation.
- Shared validation should be extracted into reusable domain or utility components when needed.

## State Management

State should follow a predictable model:

```text
User Action
   ↓
ViewModel
   ↓
Repository
   ↓
Data Source
   ↓
State Update
   ↓
Compose UI
```

---

# Domain Model and Contracts

At the current stage, Server Toolkit uses a lightweight domain model approach.

A full domain layer with use cases is not mandatory yet.

## Contains

- Core business models
- Repository interfaces when useful
- Shared validation rules when they become reusable

## Rules

- Do not create use cases only for theoretical purity.
- Introduce use cases only when business logic becomes complex or reused.
- Domain models must not depend on Room annotations.
- Domain models must not depend on Compose, Android framework classes, or database implementation details.

## Current Direction

For Sprint 1, the server inventory feature should use a clean server model that represents application meaning, not database storage mechanics.

Room entities may mirror the domain model initially, but the project must preserve the option to separate them when persistence concerns become more complex.

---

# Data Layer

The data layer provides application data through repositories.

## Contains

- Repository implementations
- Local data sources
- Room database definitions
- DAOs
- Entity mapping when needed

## Rules

- Repository implementations hide data-source details from ViewModels.
- Room DAOs are internal persistence details.
- ViewModels must depend on repositories, not DAOs.
- Database entities must not leak into UI code.
- Mapping should be introduced when database models and domain models diverge.

## Repository Responsibility

Repositories are responsible for:

- Reading data from persistence
- Writing data to persistence
- Coordinating local data sources
- Returning domain-oriented data
- Hiding persistence implementation details

Repositories are not responsible for:

- Rendering UI
- Owning screen state
- Holding Android navigation logic
- Performing unrelated feature orchestration

---

# Local Persistence

Server Toolkit uses Room for local persistence.

## Current Scope

The initial persistence scope is server inventory data.

Room is used for:

- Structured local data
- Queryable server records
- Persistence across app restarts
- Future migration support

## Persistence Rules

- Room database access must go through DAOs.
- DAOs must be accessed through repository implementations.
- Database schema changes require migrations unless destructive migration is explicitly justified for a pre-release stage.
- Sensitive credentials must not be stored casually in plain Room tables.
- Credential storage requires a separate security decision before implementation.

## Initial Database Direction

The initial database should contain a `servers` table for server inventory metadata.

Credential handling is intentionally outside the first persistence implementation unless a dedicated security strategy is accepted.

---

# Navigation

Server Toolkit uses Single Activity architecture with Jetpack Navigation Compose.

## Navigation Goals

Navigation must be:

- Type-conscious
- Centralized
- Readable
- Testable where practical
- Safe from scattered route strings

## Rules

- Route definitions must be centralized.
- Composables must not hard-code route strings.
- Navigation actions should be passed down as lambdas where practical.
- Feature screens should not know the internal structure of the navigation graph beyond the actions they can trigger.
- Navigation arguments must be explicit and minimal.

## Initial Navigation Destinations

The first navigation structure should support:

- Dashboard
- Server list
- Add server
- Edit server
- Server details
- Settings

A destination may exist as a placeholder during early implementation only when it is necessary to support navigation flow. Placeholder screens must be clearly identified and must not be documented as completed functionality.

---

# Package Structure

The package structure should reflect feature ownership and layer responsibilities.

Recommended baseline:

```text
de.hamedtanha.servertoolkit

├── core
│   ├── database
│   ├── navigation
│   ├── security
│   └── common
│
├── data
│   ├── local
│   └── repository
│
├── domain
│   ├── model
│   └── repository
│
├── feature
│   ├── dashboard
│   ├── server
│   └── settings
│
└── ui
    └── theme
```

This structure is the target baseline for the next implementation phase.

Package names may be adjusted during implementation if doing so improves consistency, but structural changes must remain aligned with the accepted ADRs.

---

# Feature Package Rules

Feature packages own feature-specific UI and presentation logic.

Example:

```text
feature/server
├── presentation
│   ├── list
│   ├── detail
│   └── edit
└── components
```

Rules:

- Feature UI belongs inside the relevant feature package.
- Shared UI components belong in a shared UI or core UI package only after reuse is real.
- Do not create shared abstractions before at least two features need them.
- Keep feature packages cohesive.

---

# Dependency Direction

Allowed dependency direction:

```text
feature/ui → presentation → domain → data implementation
```

More specifically:

```text
Compose Screen
   ↓
ViewModel
   ↓
Repository Interface
   ↓
Repository Implementation
   ↓
DAO / Database
```

Rules:

- UI must not depend on database implementation.
- UI must not depend on DAO classes.
- Domain models must not depend on Room.
- Data implementations may depend on Room.
- Repository interfaces should be stable once consumed by ViewModels.

---

# Error Handling

Error handling should be explicit and user-safe.

Rules:

- Do not expose raw exceptions directly to the UI.
- Convert technical failures into meaningful UI states.
- Log internal errors only when logging infrastructure exists and is appropriate.
- Avoid swallowing errors silently.

For early implementation, simple sealed UI state or result models are acceptable.

---

# Validation

Validation must be consistent and testable.

For server inventory, initial validation should include:

- Server name is required.
- Host is required.
- Port must be within `1..65535`.

Rules:

- Validation used by multiple screens must not be duplicated.
- UI may display validation messages but should not own shared validation policy.
- Validation rules should be covered by unit tests.

---

# Testing Strategy

The architecture must support testing from the beginning.

## Initial Test Targets

- Server validation
- Repository behavior
- ViewModel state transitions
- Room DAO behavior where useful

## Rules

- Business rules must be unit-testable without Android UI.
- Repository implementations should be testable with fake or in-memory data sources.
- UI tests are useful later, but they are not the first priority for Sprint 1.

---

# Security Boundaries

Server Toolkit is an infrastructure management application. Security mistakes are expensive.

Rules:

- Do not store SSH passwords or private keys in plain Room tables.
- Do not log credentials, tokens, private keys, server secrets, or sensitive infrastructure identifiers.
- Credential storage requires a dedicated security design before implementation.
- Host fingerprint verification requires its own architecture decision before SSH connectivity is implemented.

The first server inventory implementation must store only non-secret metadata unless a separate security decision is accepted.

---

# Dependency Injection

A dependency injection framework is not required yet.

Manual dependency wiring is acceptable during early implementation if it remains simple and centralized.

Hilt may be introduced later when object graph complexity justifies it.

Introducing Hilt requires an ADR if it becomes a significant architecture decision.

---

# Future Architecture Candidates

The following are not part of the current implementation baseline unless accepted by future ADRs:

- Hilt
- SSH client library
- Secure credential storage abstraction
- WorkManager
- Retrofit or another HTTP client
- Background monitoring
- Synchronization across devices
- Cloud provider integrations
- Docker management
- Kubernetes support

These are future candidates, not current functionality.

---

# Governance

This document must stay synchronized with accepted ADRs and actual implementation.

Update this document when:

- A new ADR changes architecture.
- The implementation adopts a new architectural pattern.
- Package structure changes materially.
- Persistence or navigation strategy changes.
- Security boundaries change.

Do not update this document to advertise planned features as if they already exist.

---

# Related Documents

- PRODUCT_VISION.md
- ROADMAP.md
- PROJECT_STATE.md
- DEVELOPMENT.md
- SECURITY.md
- docs/adr/ADR-001-project-vision.md
- docs/adr/ADR-002-application-architecture.md
- docs/adr/ADR-003-local-persistence-with-room.md
- docs/adr/ADR-004-navigation-strategy.md
