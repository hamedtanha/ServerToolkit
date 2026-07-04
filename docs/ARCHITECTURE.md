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
- Add Server route, ViewModel, shared Server Form screen, form state, form fields, and validation.
- Edit Server route and ViewModel using the shared Server Form screen.
- Delete server UI action with confirmation dialog.
- Search and filtering behavior.
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
- DAO, repository, mapper, and filter matcher tests.
- Manual and automated verification for add, edit, delete, search, filtering, and shared server form naming cleanup.

The following items are intentionally not implemented yet:

- SSH connection workflow.
- Monitoring workflow.
- Command execution workflow.
- Secure credential storage.
- Room migration beyond database version 1.

---

## Naming Scope

The current inventory-related implementation is intentionally named `serverinventory` because it manages one concrete asset type: `Server`.

A broader `inventory` package or model should be introduced only after the application implements additional non-server asset types or shared inventory behavior that is no longer server-specific.

The current naming rules are:

- Use `Server` for the implemented domain model.
- Use `ServerInventory` for the implemented feature scope.
- Use `ServerForm` for UI and state shared by Add Server and Edit Server.
- Do not introduce `Device`, `ServerDevice`, `InventoryItem`, or `feature/inventory` naming until the broader concept is implemented.

This prevents premature abstraction while keeping the future inventory direction open.

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
