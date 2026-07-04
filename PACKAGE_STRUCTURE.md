# Package Structure

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-04

---

## Purpose

This document defines the canonical package and directory structure for the Server Toolkit Android application.

Its purpose is to keep the codebase consistent, maintainable, testable, and scalable as features are added incrementally.

This document describes the intended implementation structure. It must remain synchronized with `docs/ARCHITECTURE.md`, the accepted ADRs, and the actual source code.

---

## Scope

This document applies to Android application source code.

It covers:

- Source package organization.
- Feature package boundaries.
- Shared package responsibilities.
- Dependency direction.
- Naming rules.
- File placement rules.
- Testing structure.
- Prohibited package patterns.
- Evolution policy.

This document does not define UI design, database schema details, release policy, or feature roadmap.

---

## Base Package

The application package root is:

```text
de.hamedtanha.servertoolkit
```

All application source code must live under this package.

---

## Canonical Package Layout

```text
app/src/main/java/de/hamedtanha/servertoolkit/

    MainActivity.kt
    ServerToolkitApplication.kt

    core/
        common/
        database/
            ServerToolkitDatabase.kt
        di/
            AppModule.kt
            DatabaseModule.kt

    feature/
        dashboard/
            presentation/
                component/
                event/
                screen/
                state/
                viewmodel/

        serverinventory/
            data/
                local/
                    dao/
                    entity/
                mapper/
                repository/
            di/
            domain/
                model/
                repository/
            presentation/
                component/
                event/
                screen/
                state/
                viewmodel/

        settings/
            presentation/
                component/
                event/
                screen/
                state/
                viewmodel/

    navigation/

    ui/
        theme/
```

Not every package must contain implementation files immediately. Empty packages may be preserved with `.gitkeep` only when the package is part of the approved structure.

---

## Top-Level Responsibilities

### Root Package

The root package contains only application entry points.

Allowed files:

- `MainActivity.kt`
- `ServerToolkitApplication.kt`

Rules:

- Do not place feature logic in the root package.
- Do not place repository implementations in the root package.
- Do not place database code in the root package.
- Do not place UI screens in the root package.
- Keep application entry points thin.

---

### core

The `core` package contains reusable infrastructure shared across the application.

```text
core/
    common/
    database/
    di/
```

#### core/common

Shared application-level utilities that are genuinely cross-cutting.

Rules:

- Keep this package small.
- Do not create generic helper classes without a proven use case.
- Avoid dumping unrelated utility functions here.
- Prefer precise packages over vague utility containers.

#### core/database

Shared Room database infrastructure.

Current files:

```text
core/database/ServerToolkitDatabase.kt
```

Rules:

- The application Room database class belongs here.
- Database migrations belong here when introduced.
- Shared database constants and converters may belong here.
- Feature-owned entities and DAOs should remain close to the owning feature unless intentionally shared.
- Room-specific types must not leak into presentation code.
- `ServerToolkitDatabase` may reference feature-owned Room entities and DAOs only for schema aggregation.

#### core/di

Application-wide dependency injection modules.

Current files:

```text
core/di/AppModule.kt
core/di/DatabaseModule.kt
```

Rules:

- Application-wide Hilt modules belong here.
- Feature-specific bindings should live inside the owning feature when practical.
- Do not use dependency injection as a service locator.
- Do not inject dependencies into domain models.

---

### feature

The `feature` package contains user-facing application features.

```text
feature/
    dashboard/
    serverinventory/
    settings/
```

Each feature should be as self-contained as practical.

Feature packages may contain:

- feature-level navigation definitions only when implemented.
- presentation state.
- presentation events.
- screens.
- UI components.
- ViewModels.
- feature-specific domain models.
- feature-specific repository contracts.
- feature-specific data models.
- feature-specific repository implementations.
- feature-specific Room entities and DAOs.
- feature-specific dependency injection modules.

Rules:

- Features must not depend directly on other features.
- Feature UI must not depend directly on Room DAOs.
- Feature UI must not depend directly on Room entities.
- Feature ViewModels may depend on feature domain repositories or use cases.
- Feature-specific components stay inside the feature package until reuse is proven.
- If two features need shared behavior, move the shared contract into `core`, `domain`, or `data` only when there is a clear engineering reason.

---

## Current Features

### dashboard

The dashboard feature owns the main overview screen.

```text
feature/dashboard/
    presentation/
        component/
        event/
        screen/
        state/
        viewmodel/
```

Rules:

- Dashboard must not own server inventory data.
- Dashboard may consume summarized data through repository contracts when required.
- Dashboard must not directly access Room DAOs or feature-local data sources from another feature.

---

### serverinventory

The server inventory feature owns server inventory behavior.

Current implementation:

```text
feature/serverinventory/
    data/
        local/
            dao/
            entity/
        mapper/
        repository/
    di/
    domain/
        model/
        repository/
    presentation/
        screen/
        state/
        viewmodel/
```

Implemented files:

```text
feature/serverinventory/data/local/dao/ServerDao.kt
feature/serverinventory/data/local/entity/ServerEntity.kt
feature/serverinventory/data/mapper/ServerEntityMapper.kt
feature/serverinventory/data/repository/InMemoryServerRepository.kt
feature/serverinventory/data/repository/RoomServerRepository.kt
feature/serverinventory/di/ServerInventoryDatabaseModule.kt
feature/serverinventory/di/ServerInventoryModule.kt
feature/serverinventory/domain/model/Server.kt
feature/serverinventory/domain/model/ServerEnvironment.kt
feature/serverinventory/domain/repository/ServerRepository.kt
feature/serverinventory/presentation/screen/AddServerScreen.kt
feature/serverinventory/presentation/screen/ServerInventoryScreen.kt
feature/serverinventory/presentation/state/AddServerUiState.kt
feature/serverinventory/presentation/state/ServerInventoryFilter.kt
feature/serverinventory/presentation/state/ServerInventoryUiState.kt
feature/serverinventory/presentation/viewmodel/AddServerViewModel.kt
feature/serverinventory/presentation/viewmodel/ServerInventoryViewModel.kt
```

Rules:

- Server inventory models belong to `feature/serverinventory/domain/model`.
- Server inventory repository contracts belong to `feature/serverinventory/domain/repository`.
- Server inventory repository implementations belong to `feature/serverinventory/data/repository`.
- Server inventory Room entities belong to `feature/serverinventory/data/local/entity`.
- Server inventory DAOs belong to `feature/serverinventory/data/local/dao`.
- Server inventory entity/domain mappers belong to `feature/serverinventory/data/mapper`.
- Server inventory Hilt modules belong to `feature/serverinventory/di` when they bind or provide feature-owned dependencies.
- Server inventory presentation state belongs to `feature/serverinventory/presentation/state`.
- Server inventory screens belong to `feature/serverinventory/presentation/screen`.
- Server inventory ViewModels belong to `feature/serverinventory/presentation/viewmodel`.
- Do not place server-specific classes in global `model`, `viewmodel`, or `ui/screens` packages.

Future packages may be added only when required by implementation:

```text
feature/serverinventory/navigation/
feature/serverinventory/presentation/component/
feature/serverinventory/presentation/event/
```

---

### settings

The settings feature owns application settings screens.

```text
feature/settings/
    presentation/
        component/
        event/
        screen/
        state/
        viewmodel/
```

Rules:

- Settings must not directly modify unrelated feature internals.
- Settings may expose application-level configuration through shared repository contracts when required.

---

## Dependency Direction

Dependencies must point toward stable abstractions and away from volatile implementation details.

Expected direction:

```text
presentation -> domain -> data -> persistence infrastructure
```

Allowed patterns:

```text
feature presentation -> feature domain
feature data -> feature domain
feature data -> core database infrastructure
feature di -> feature data
feature di -> core database infrastructure
app/root -> feature
app/root -> core
```

Narrow exception:

```text
core/database -> feature data/local entity and DAO
```

This exception exists only because Room requires the application database class to aggregate entity and DAO declarations. It must not be used for business logic, presentation code, or cross-feature communication.

Forbidden patterns:

```text
domain -> data
domain -> presentation
domain -> Android framework APIs
data -> presentation
feature A -> feature B
presentation -> DAO
presentation -> Room entity
```

Rules:

- Domain must remain stable and framework-independent.
- Data must not know Compose UI details.
- Features must communicate through shared contracts only when there is a justified need.
- Room schema aggregation must remain isolated to `core/database`.

---

## Naming Rules

### Domain Models

Use clear noun-based names.

Examples:

```text
Server
ServerEnvironment
ServerStatus
```

### Room Entities

Append `Entity`.

Examples:

```text
ServerEntity
CommandEntity
```

### DAOs

Append `Dao`.

Examples:

```text
ServerDao
CommandDao
```

### Repository Interfaces

Use business-facing names.

Examples:

```text
ServerRepository
CommandRepository
```

### Repository Implementations

Use a precise implementation name.

Examples:

```text
RoomServerRepository
InMemoryServerRepository
DefaultCommandRepository
```

### ViewModels

Append `ViewModel`.

Examples:

```text
ServerInventoryViewModel
ServerEditViewModel
```

### UI State

Append `UiState`.

Examples:

```text
ServerInventoryUiState
ServerEditUiState
```

### UI Events

Append `UiEvent` when explicit UI events are required.

Examples:

```text
ServerEditUiEvent
```

### Screens

Append `Screen`.

Examples:

```text
ServerInventoryScreen
DashboardScreen
```

---

## File Placement Rules

### Application Entry Points

Place application entry points in the root package:

```text
de/hamedtanha/servertoolkit/MainActivity.kt
de/hamedtanha/servertoolkit/ServerToolkitApplication.kt
```

### Screens

Place screens under the owning feature:

```text
feature/<feature-name>/presentation/screen/
```

Do not use a global `ui/screens` package.

### Components

Place feature-specific components under:

```text
feature/<feature-name>/presentation/component/
```

Only move components to shared UI packages after reuse is proven.

### UI State

Place feature-specific UI state classes under:

```text
feature/<feature-name>/presentation/state/
```

### UI Events

Place explicit UI event classes under:

```text
feature/<feature-name>/presentation/event/
```

This package is optional and should be used only when explicit event modeling improves clarity.

### ViewModels

Place ViewModels under:

```text
feature/<feature-name>/presentation/viewmodel/
```

Do not use a global `viewmodel` package.

### Feature Domain Models

Place feature-owned domain models under:

```text
feature/<feature-name>/domain/model/
```

### Repository Contracts

Place feature-specific repository contracts under:

```text
feature/<feature-name>/domain/repository/
```

### Repository Implementations

Place feature-local repository implementations under:

```text
feature/<feature-name>/data/repository/
```

### Room Entities and DAOs

Place feature-owned Room entities and DAOs close to the owning feature unless they are explicitly shared.

```text
feature/serverinventory/data/local/entity/ServerEntity.kt
feature/serverinventory/data/local/dao/ServerDao.kt
```

Shared Room infrastructure belongs in:

```text
core/database/
```

### Dependency Injection

Place app-wide Hilt modules under:

```text
core/di/
```

Place feature-specific Hilt modules close to the owning feature:

```text
feature/<feature-name>/di/
```

---

## Testing Structure

Unit tests should mirror the production package structure when practical.

Expected test roots:

```text
app/src/test/java/de/hamedtanha/servertoolkit
app/src/androidTest/java/de/hamedtanha/servertoolkit
```

Examples:

```text
app/src/test/java/de/hamedtanha/servertoolkit/feature/serverinventory/data/ServerEntityMapperTest.kt
app/src/androidTest/java/de/hamedtanha/servertoolkit/feature/serverinventory/data/local/ServerDaoTest.kt
```

Rules:

- Domain validation should be unit-tested.
- Mapping logic should be unit-tested when non-trivial.
- DAO behavior should be tested with Android instrumentation tests or an appropriate Room test setup.
- ViewModel behavior should be tested when business-relevant state transitions exist.
- UI tests should be added only when UI behavior is stable enough to justify maintenance cost.

---

## Prohibited Package Patterns

The following package patterns are not allowed unless explicitly justified:

```text
utils/
helpers/
managers/
model/
viewmodel/
ui/screens/
ui/components/
```

These package names usually hide unclear ownership and become dumping grounds.

Use precise package names that describe responsibility.

---

## Anti-Patterns

The following patterns are not allowed:

- Placing all screens in a global `ui/screens` package.
- Placing all ViewModels in a global `viewmodel` package.
- Placing all domain models in a vague global `model` package.
- Placing Room entities in domain packages.
- Placing repository implementations in presentation packages.
- Moving feature-specific UI components into shared UI packages prematurely.
- Creating remote/network packages before remote features exist.
- Mixing navigation route definitions across unrelated features.
- Allowing one feature package to depend directly on another feature package.

---

## Refactoring Rule

When an existing file does not match this structure, it must be moved to the correct package before being expanded.

Do not add new code to legacy, temporary, or prohibited package locations.

Existing nonconforming packages must be treated as cleanup targets, not as valid extension points.

---

## Evolution Policy

This structure may evolve as the project grows, but changes must be intentional.

A package structure change requires documentation updates when it affects:

- Layer boundaries.
- Dependency direction.
- Feature organization.
- Persistence organization.
- Navigation organization.
- Security-sensitive code placement.
- Testing structure.

Significant structural changes may require a new ADR.

Accepted ADRs must remain historically traceable. If a structural decision supersedes an accepted ADR, create a new ADR instead of silently rewriting architectural history.

---

## Related Documents

- `docs/ARCHITECTURE.md`
- `docs/adr/ADR-002-application-architecture.md`
- `docs/adr/ADR-003-local-persistence-with-room.md`
- `docs/adr/ADR-004-navigation-strategy.md`
- `docs/adr/ADR-005-dependency-injection-strategy.md`
- `docs/DEVELOPMENT.md`
- `docs/PROJECT_STATE.md`

---

## Notes

This document is intentionally strict.

A strict package structure reduces ambiguity, improves review quality, and prevents feature implementation from drifting into inconsistent architecture.
