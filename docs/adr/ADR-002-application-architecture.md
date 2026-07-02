# ADR-002: Application Architecture

**Status:** Accepted

**Date:** 2026-07-02

---

# Context

Server Toolkit is intended to become an infrastructure management application for Linux server administration, not a simple SSH terminal client.

The application will need to support multiple operational workflows over time, including:

- Server inventory
- Dashboard views
- SSH-based administration
- Predefined command execution
- Monitoring
- Xray and x-ui management
- Certificate helper workflows
- Environment separation between production, testing, and other server groups

These capabilities require a stable architecture that supports incremental feature development without turning the codebase into tightly coupled UI, business logic, and persistence code.

The first implementation sprint introduces Server Inventory. This feature will become a foundation for later features, because most future workflows depend on a server record.

A clear application architecture is therefore required before implementation starts.

---

# Decision

Server Toolkit will use a modern Android architecture based on:

- Kotlin
- Jetpack Compose
- MVVM
- Repository Pattern
- Unidirectional UI state flow
- Coroutines and Flow for asynchronous data handling
- Clear separation between UI, presentation, domain-oriented logic, and data access

The application will be organized feature-first where practical, while preserving clear architectural layers inside each feature.

The default structure for a feature should be:

```text
feature/<feature-name>/
├── data/
├── domain/
├── presentation/
└── ui/
```

Shared application-level code may live in dedicated common packages when it is genuinely reusable.

The UI layer must not directly access persistence, networking, SSH clients, or platform-specific data sources.

ViewModels are responsible for exposing screen state and handling UI events. They must not contain database implementation details.

Repositories are responsible for coordinating data access and hiding implementation details from the presentation layer.

Data source implementations are responsible for concrete persistence, network, SSH, or platform integrations.

The initial Sprint 1 implementation will apply this architecture to the Server Inventory feature.

---

# Architectural Rules

## UI Layer

The UI layer contains Jetpack Compose screens and reusable composables.

Responsibilities:

- Render state
- Forward user events to the ViewModel
- Remain stateless where possible
- Avoid business logic
- Avoid direct access to repositories, DAOs, databases, or network clients

## Presentation Layer

The presentation layer contains ViewModels and UI state models.

Responsibilities:

- Expose immutable UI state
- Process UI events
- Call use cases or repositories
- Coordinate loading, validation, and error states

## Domain-Oriented Logic

Domain-oriented logic contains application rules that should not depend on Android framework classes.

Responsibilities:

- Validation
- Business rules
- Domain models where needed
- Use cases when logic becomes non-trivial

Use cases are not mandatory for every small operation. Introducing use cases only to satisfy a pattern is unnecessary abstraction and should be avoided.

## Data Layer

The data layer contains repositories, data sources, DAOs, database entities, and mapping logic.

Responsibilities:

- Persist data
- Retrieve data
- Hide storage and integration details
- Map between database entities and application models when needed

---

# Alternatives Considered

## Simple UI-Driven Architecture

Build features directly inside Compose screens and call persistence logic from UI code.

### Pros

- Fastest initial implementation
- Less boilerplate
- Easy for very small prototypes

### Cons

- Poor testability
- Tight coupling between UI and data access
- Difficult to maintain as features grow
- High risk of duplicated logic
- Not suitable for a production-quality portfolio project

Rejected.

---

## Classic Layered Architecture

Organize the project strictly by technical layers such as `ui`, `viewmodel`, `repository`, `database`, and `network` at the application root.

### Pros

- Familiar structure
- Clear technical separation
- Works for small applications

### Cons

- Features become scattered across the codebase
- Navigation between related files becomes harder as the app grows
- Large shared packages often become dumping grounds
- Weaker feature ownership

Rejected as the default structure, but individual shared packages may still use layered organization when appropriate.

---

## Feature-First MVVM with Repository Pattern

Organize code around product features while keeping clear internal layers.

### Pros

- Scales better as features are added
- Keeps related code close together
- Supports incremental development
- Maintains separation of concerns
- Compatible with Jetpack Compose and Android recommendations
- Improves testability
- Reduces long-term maintenance cost

### Cons

- Requires discipline to avoid inconsistent feature structures
- Slightly more initial structure than a prototype
- Some shared abstractions must be introduced carefully

Accepted.

---

# Consequences

## Positive

- The codebase has a stable structure before feature implementation begins.
- Server Inventory can be implemented without blocking future SSH, monitoring, and command features.
- UI code remains decoupled from persistence and infrastructure integrations.
- Business rules can be tested independently from Android UI.
- The architecture supports long-term maintainability and portfolio-quality review.

## Negative

- Initial implementation requires more files than a prototype.
- Contributors must follow architectural boundaries consistently.
- Some decisions, such as whether to introduce use cases for simple operations, require engineering judgment.

---

# Implementation Guidance

Sprint 1 should apply this ADR without over-engineering.

For Server Inventory, the recommended starting structure is:

```text
serverinventory/
├── data/
│   ├── local/
│   ├── mapper/
│   └── repository/
├── domain/
│   ├── model/
│   └── validation/
├── presentation/
│   ├── addedit/
│   └── list/
└── ui/
    ├── addedit/
    └── list/
```

The first implementation should avoid premature abstractions such as generic base repositories, generic base ViewModels, or complex use-case hierarchies.

Use cases may be introduced later when business logic becomes complex enough to justify them.

---

# References

- ADR-001-project-vision.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md
- AI_RULES.md

---

# Notes

This ADR establishes the default architecture for application features.

Future ADRs may refine specific technical decisions such as dependency injection, local persistence, navigation, SSH integration, background work, and security strategy.

If the application architecture changes significantly, a new ADR should supersede this document instead of rewriting it.
