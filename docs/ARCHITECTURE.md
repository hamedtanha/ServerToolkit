# Architecture

**Project:** Server Toolkit  
**Version:** 0.2.0-alpha  
**Status:** Active  
**Last Updated:** 2026-07-04

---

## Purpose

This document defines the practical application architecture for the Server Toolkit Android application.

It translates accepted Architecture Decision Records into implementation rules for project structure, layer responsibilities, dependency direction, data flow, navigation, dependency injection, and persistence.

This document describes the current implementation and accepted implementation direction. It must not advertise planned functionality as completed functionality.

---

## Architecture Baseline

Server Toolkit is a modern Android application for Linux server administration and infrastructure management.

The application follows:

- Kotlin.
- Jetpack Compose.
- Single Activity architecture.
- MVVM.
- Repository Pattern.
- Unidirectional Data Flow.
- Hilt for dependency injection.
- Jetpack Navigation Compose for screen navigation.
- Room for local structured persistence.

The architecture is intentionally simple at the current project stage.

The project must not introduce additional layers, frameworks, or abstractions unless they solve a real implementation problem.

---

## Accepted Architecture Decisions

The current architecture is governed by the following ADRs:

| ADR | Decision | Status |
|---|---|---|
| ADR-001 | Project Vision | Accepted |
| ADR-002 | Application Architecture | Accepted |
| ADR-003 | Local Persistence with Room | Accepted |
| ADR-004 | Navigation Strategy | Accepted |
| ADR-005 | Dependency Injection Strategy | Accepted |

Accepted ADRs are the source of truth for architectural decisions. This document explains how those decisions are applied in the codebase.

---

## Current Implementation Status

The current implementation includes:

- Single Activity application entry point.
- Hilt-enabled application setup.
- App-level Navigation Compose infrastructure.
- Dashboard route and screen.
- Dashboard ViewModel and UI state.
- Dashboard navigation action to Server Inventory.
- Server Inventory route, screen, ViewModel, UI state, and filter state.
- Add Server route, screen, ViewModel, UI state, form fields, and validation.
- Server Inventory domain model and environment model.
- Server repository contract.
- In-memory Server repository implementation retained for development and testing support.
- Room dependency setup with KSP.
- Room schema export location configuration.
- Server Toolkit Room database class.
- Server entity.
- Server DAO.
- Server entity/domain mapper.
- Room-backed Server repository implementation.
- Hilt database, DAO, and repository wiring.
- Basic Server Inventory list rendering.

The following items are intentionally not implemented yet:

- Edit server workflow.
- Delete server UI action.
- Search and filtering behavior.
- DAO and repository automated tests.
- SSH connection workflow.
- Monitoring workflow.
- Command execution workflow.
- Secure credential storage.

---

## Application Layers

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
Local Persistence / External Services
```

The dependency direction must remain downward from UI and presentation toward stable domain contracts and concrete data implementations.

Lower-level implementation details must not leak into higher layers.

---

## UI Layer

The UI layer is responsible for rendering application screens and handling user interaction events.

### Contains

- Compose screens.
- Reusable Compose components when reuse is real.
- Theme definitions.
- Navigation host integration.
- UI-specific state rendering.

### Rules

- Composables must remain declarative.
- Composables must not contain business logic.
- Composables must not access Room DAOs directly.
- Composables must not access repositories directly.
- Composables receive state and emit events.
- Screen-level behavior belongs in ViewModels.
- Navigation actions should be passed down as lambdas where practical.

---

## Presentation Layer

The presentation layer coordinates UI state and user actions.

### Contains

- ViewModels.
- UI state classes.
- UI event handlers when needed.
- Screen-specific state mapping.

### Rules

- ViewModels expose immutable observable UI state.
- ViewModels call repository contracts instead of data-source implementations.
- ViewModels must not depend on Room DAOs.
- ViewModels must not contain Android UI rendering logic.
- ViewModels may perform simple presentation-specific validation.
- Shared validation should be extracted into reusable domain or utility components when needed.

---

## Domain Model and Contracts

At the current stage, Server Toolkit uses a lightweight domain model approach.

A full domain layer with use cases is not mandatory yet.

### Contains

- Feature-owned domain models.
- Repository interfaces when persistence or external data access is introduced.
- Shared validation rules when they become reusable.

### Rules

- Do not create use cases only for theoretical purity.
- Introduce use cases only when business logic becomes complex or reused.
- Domain models must not depend on Room annotations.
- Domain models must not depend on Compose, Android framework classes, or database implementation details.

### Current Direction

The Server Inventory feature uses clean domain models that represent application meaning, not database storage mechanics.

Room entities are separate persistence models and are mapped explicitly to domain models.

---

## Data Layer

The data layer provides application data through repositories.

### Contains

- Repository implementations.
- Local data sources.
- Room database definitions.
- DAOs.
- Entities.
- Entity/domain mapping.

### Rules

- Repository implementations hide data-source details from ViewModels.
- Room DAOs are internal persistence details.
- ViewModels must depend on repositories, not DAOs.
- Database entities must not leak into UI code.
- Domain models and Room entities must remain separate.
- Mapping must remain explicit when data crosses the persistence/domain boundary.

### Repository Responsibility

Repositories are responsible for:

- Reading data from persistence.
- Writing data to persistence.
- Coordinating local data sources.
- Returning domain-oriented data.
- Hiding persistence implementation details.

Repositories are not responsible for:

- Rendering UI.
- Owning screen state.
- Holding Android navigation logic.
- Performing unrelated feature orchestration.

---

## Local Persistence

Room is the accepted persistence technology for local structured data.

### Current Scope

The initial persistence scope is server inventory data.

The Room persistence skeleton currently includes:

- `ServerToolkitDatabase`.
- `ServerEntity`.
- `ServerDao`.
- `RoomServerRepository`.
- Entity/domain mapping.
- Hilt providers for the database and DAO.
- KSP schema export configuration.

The database version is `1`.

The initial Room table stores server metadata only. It must not store credentials, private keys, passphrases, access tokens, certificates, or other secrets.

### Persistence Rules

- Room database access must go through DAOs.
- DAOs must be accessed through repository implementations.
- Database schema changes require migrations unless destructive migration is explicitly justified for a pre-release stage.
- Sensitive credentials must not be stored casually in plain Room tables.
- Credential storage requires a separate security decision before implementation.
- The Room schema export directory is configured under `app/schemas` and generated schema files should be committed after local build verification.

### Database Aggregation Boundary

`ServerToolkitDatabase` belongs to shared database infrastructure.

Because Room requires a database class to aggregate entities and DAOs, the database class may reference feature-owned Room entities and DAOs for schema registration only.

This is a narrow persistence exception. It must not be used as permission for general `core` code to depend on feature UI, presentation, domain workflows, or feature services.

---

## Navigation

Server Toolkit uses Single Activity architecture with Jetpack Navigation Compose.

### Rules

- Route definitions must be centralized.
- Composables must not hard-code route strings.
- Navigation actions should be passed down as lambdas where practical.
- Feature screens should not know the internal structure of the navigation graph beyond the actions they can trigger.
- Navigation arguments must be explicit and minimal.

### Current Navigation Destinations

The current navigation graph supports:

- Dashboard.
- Server Inventory.
- Add Server.

Future destinations may include:

- Edit Server.
- Server Details.
- Settings.

Placeholder screens must be clearly identified and must not be documented as completed functionality.

---

## Package Structure

The package structure should reflect feature ownership and layer responsibilities.

Current baseline:

```text
de.hamedtanha.servertoolkit

├── core
│   ├── common
│   ├── database
│   └── di
│
├── feature
│   ├── dashboard
│   │   └── presentation
│   ├── serverinventory
│   │   ├── data
│   │   │   ├── local
│   │   │   │   ├── dao
│   │   │   │   └── entity
│   │   │   ├── mapper
│   │   │   └── repository
│   │   ├── di
│   │   ├── domain
│   │   │   ├── model
│   │   │   └── repository
│   │   └── presentation
│   │       ├── screen
│   │       ├── state
│   │       └── viewmodel
│   └── settings
│       └── presentation
│
├── navigation
│
└── ui
    └── theme
```

Package names may be adjusted during implementation only when doing so improves consistency and remains aligned with accepted ADRs and package structure documentation.

---

## Dependency Direction

Allowed dependency direction:

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
- `core/database` may reference feature-owned Room persistence classes only for Room schema aggregation.

---

## Dependency Injection

Server Toolkit uses Hilt for dependency injection.

### Current Scope

The current implementation includes:

- Hilt-enabled application setup.
- Hilt-enabled ViewModel integration.
- `DatabaseModule` for the Room database.
- Server Inventory database module for the feature-owned DAO provider.
- Server Inventory repository binding from `ServerRepository` to `RoomServerRepository`.

### Rules

- Dependency wiring should remain centralized by responsibility.
- ViewModels should receive dependencies through constructor injection.
- Modules should be small and grouped by responsibility.
- Do not introduce bindings before a real dependency exists.
- Do not use Hilt to hide unnecessary abstraction.

---

## Error Handling

Error handling should be explicit and user-safe.

Rules:

- Do not expose raw exceptions directly to the UI.
- Convert technical failures into meaningful UI states.
- Log internal errors only when logging infrastructure exists and is appropriate.
- Avoid swallowing errors silently.

For early implementation, simple UI state or result models are acceptable.

---

## Validation

Validation must be consistent and testable.

For server inventory, initial validation includes:

- Server name is required.
- Host is required.
- Username is required in the current Add Server form.
- Port must be within `1..65535`.

Rules:

- Validation used by multiple screens must not be duplicated indefinitely.
- UI may display validation messages but should not own shared validation policy long-term.
- Validation rules should be covered by unit tests when stabilized.

---

## Testing Strategy

The architecture must support testing from the beginning.

### Initial Test Targets

- Server validation.
- Repository behavior.
- Entity/domain mapping.
- Room DAO behavior.
- ViewModel state transitions.

### Rules

- Business rules must be unit-testable without Android UI.
- Repository implementations should be testable with fake or in-memory data sources.
- DAO behavior should be tested with Room-backed tests after the persistence skeleton stabilizes.
- UI tests are useful later, but they are not the first priority for the current scaffold stage.

---

## Security Boundaries

Server Toolkit is an infrastructure management application. Security mistakes are expensive.

Rules:

- Do not store SSH passwords or private keys in plain Room tables.
- Do not log credentials, tokens, private keys, server secrets, or sensitive infrastructure identifiers.
- Credential storage requires a dedicated security design before implementation.
- Host fingerprint verification requires its own architecture decision before SSH connectivity is implemented.

The first server inventory persistence implementation stores only non-secret server metadata.

---

## Future Architecture Candidates

The following are not part of the current implementation baseline unless accepted by future ADRs:

- SSH client library.
- Secure credential storage abstraction.
- WorkManager.
- Retrofit or another HTTP client.
- Background monitoring.
- Synchronization across devices.
- Cloud provider integrations.
- Docker management.
- Kubernetes support.

These are future candidates, not current functionality.

---

## Governance

This document must stay synchronized with accepted ADRs and actual implementation.

Update this document when:

- A new ADR changes architecture.
- The implementation adopts a new architectural pattern.
- Package structure changes materially.
- Persistence or navigation strategy changes.
- Security boundaries change.

Do not update this document to advertise planned features as if they already exist.

---

## Related Documents

- [Product Vision](PRODUCT_VISION.md)
- [Roadmap](ROADMAP.md)
- [Project State](PROJECT_STATE.md)
- [Development](DEVELOPMENT.md)
- [Security](SECURITY.md)
- [Package Structure](../PACKAGE_STRUCTURE.md)
- [ADR-001: Project Vision](adr/ADR-001-project-vision.md)
- [ADR-002: Application Architecture](adr/ADR-002-application-architecture.md)
- [ADR-003: Local Persistence with Room](adr/ADR-003-local-persistence-with-room.md)
- [ADR-004: Navigation Strategy](adr/ADR-004-navigation-strategy.md)
- [ADR-005: Dependency Injection Strategy](adr/ADR-005-dependency-injection-strategy.md)
