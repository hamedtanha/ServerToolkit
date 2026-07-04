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

## Naming Decision: Server Inventory and Future Inventory Scope

`feature/serverinventory` remains the canonical package for the current implementation.

The current implemented asset type is `Server`. The current feature scope is local server inventory management.

A broader `inventory` feature package must not be introduced until the application implements additional non-server asset types or shared inventory behavior that is no longer server-specific.

Examples of future asset types that may justify a broader inventory boundary include:

- Network devices.
- Services.
- Certificates.
- Endpoints.
- Clusters.

Until such implementation exists, package names must reflect the current product reality rather than anticipated future scope.

Do not introduce package structures such as the following yet:

```text
feature/inventory/serverdevice/
feature/inventory/device/
feature/inventory/common/
```

These structures may be reconsidered through an ADR or documented architecture review when more than one concrete inventory asset type exists.

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

The server inventory feature owns local server inventory behavior.

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
feature/serverinventory/presentation/screen/ServerFormScreen.kt
feature/serverinventory/presentation/screen/ServerInventoryScreen.kt
feature/serverinventory/presentation/state/ServerFormUiState.kt
feature/serverinventory/presentation/state/ServerInventoryFilter.kt
feature/serverinventory/presentation/state/ServerInventoryFilterMatcher.kt
feature/serverinventory/presentation/state/ServerInventoryUiState.kt
feature/serverinventory/presentation/viewmodel/AddServerViewModel.kt
feature/serverinventory/presentation/viewmodel/EditServerViewModel.kt
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
- Shared Add/Edit server form code should use neutral `ServerForm` naming.
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

Do not rename the current `Server` domain model to `Device`, `ServerDevice`, or `InventoryItem` until the broader concept is implemented.

### Room Entities

Append `Entity`.

Examples:

```text
ServerEntity
```
