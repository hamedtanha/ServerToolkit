# Package Structure

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-03

---

# Purpose

This document defines the canonical package and directory structure for the Server Toolkit Android application.

Its purpose is to keep the codebase consistent, maintainable, testable, and scalable as features are added incrementally.

This document describes the intended implementation structure. It must remain synchronized with `ARCHITECTURE.md`, the accepted ADRs, and the actual source code.

---

# Scope

This document applies to Android application source code.

It covers:

- Source package organization
- Feature package boundaries
- Shared package responsibilities
- Dependency direction
- Naming rules
- File placement rules
- Testing structure
- Prohibited package patterns
- Evolution policy

This document does not define UI design, database schema details, release policy, or feature roadmap.

---

# Base Package

The application package root is:

```text
de.hamedtanha.servertoolkit
```

All application source code must live under this package.

---

# Canonical Package Layout

```text
app/src/main/java/de/hamedtanha/servertoolkit/

    MainActivity.kt
    ServerToolkitApplication.kt

    core/
        common/
        database/
        di/

    data/
        local/
        remote/
        repository/

    domain/
        model/
        repository/

    feature/
        dashboard/
            presentation/
                component/
                event/
                screen/
                state/
                viewmodel/

        serverinventory/
            domain/
                model/
            presentation/
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

    ui/
        theme/
```

Not every package must contain implementation files immediately. Empty packages may be preserved with `.gitkeep` only when the package is part of the approved structure.

---

# Top-Level Responsibilities

## Root Package

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

## core

The `core` package contains reusable infrastructure shared across the application.

```text
core/
    common/
    database/
    di/
```

### core/common

Shared application-level utilities that are genuinely cross-cutting.

Examples:

```text
core/common/
    Result.kt
    AppDispatchers.kt
    TimeProvider.kt
```

Rules:

- Keep this package small.
- Do not create generic helper classes without a proven use case.
- Avoid dumping unrelated utility functions here.
- Prefer precise packages over vague utility containers.

### core/database

Shared database infrastructure.

Examples:

```text
core/database/
    ServerToolkitDatabase.kt
    DatabaseMigrations.kt
    DatabaseConstants.kt
```

Rules:

- The Room database class belongs here.
- Database migrations belong here.
- Shared database constants and converters may belong here.
- Feature-owned entities and DAOs should remain close to the owning feature unless intentionally shared.
- Room-specific types must not leak into presentation code.

### core/di

Application-wide dependency injection modules.

Examples:

```text
core/di/
    AppModule.kt
    DatabaseModule.kt
    DispatcherModule.kt
```

Rules:

- Application-wide Hilt modules belong here.
- Feature-specific bindings should live inside the owning feature when practical.
- Do not use dependency injection as a service locator.
- Do not inject dependencies into domain models.

## data

The top-level `data` package is reserved for shared data infrastructure.

```text
data/
    local/
    remote/
    repository/
```

Rules:

- Do not place every repository implementation here by default.
- Use this package only for data components shared across multiple features.
- Feature-owned data logic should live inside the owning feature.
- Data models must not leak into presentation.

### data/local

Shared local persistence components.

Examples:

```text
data/local/
    SharedDao.kt
    SharedEntity.kt
```

### data/remote

Shared remote communication components.

Examples:

```text
data/remote/
    api/
    dto/
    datasource/
```

This package may remain empty until remote features exist.

### data/repository

Shared repository implementations.

Feature-specific repository implementations should live inside the owning feature unless the repository represents application-wide data.

---

## domain

The top-level `domain` package is reserved for shared business models and repository contracts.

```text
domain/
    model/
    repository/
```

Rules:

- Keep this package small.
- Feature-specific domain models should live inside the owning feature.
- Shared domain models belong here only when multiple features truly use them.
- Domain types must not depend on Android framework APIs, Compose, Room, Retrofit, or Hilt.

### domain/model

Shared domain models used by multiple features.

Examples:

```text
domain/model/
    AppEnvironment.kt
```

### domain/repository

Shared repository contracts used by multiple features.

Examples:

```text
domain/repository/
    SettingsRepository.kt
```

---

## feature

The `feature` package contains user-facing application features.

```text
feature/
    dashboard/
    serverinventory/
    settings/
```

Each feature should be as self-contained as practical.

Feature packages may contain:

- feature-level navigation definitions only when implemented
- presentation state
- presentation events
- screens
- UI components
- ViewModels
- feature-specific domain models
- feature-specific repository contracts
- feature-specific data models
- feature-specific repository implementations

Rules:

- Features must not depend directly on other features.
- Feature UI must not depend directly on Room DAOs.
- Feature UI must not depend directly on Room entities.
- Feature ViewModels may depend on feature domain repositories or use cases.
- Feature-specific components stay inside the feature package until reuse is proven.
- If two features need shared behavior, move the shared contract into `core`, `domain`, or `data` only when there is a clear engineering reason.

---

## ui

The `ui` package contains application-wide design system primitives.

```text
ui/
    theme/
```

### ui/theme

Application-wide theme definitions.

Examples:

```text
ui/theme/
    Color.kt
    Theme.kt
    Type.kt
```

Rules:

- Application-wide colors, typography, and theme setup belong here.
- Feature-specific UI components do not belong here.
- Only genuinely reusable design system elements may be promoted to shared UI packages.

---

# Feature Package Structure

Each feature should follow this structure when applicable:

```text
feature/<feature-name>/
    data/
    domain/
        model/
        repository/
    presentation/
        component/
        event/
        screen/
        state/
        viewmodel/
```

Not every feature must use every subpackage from day one.

---

# Current Features

## dashboard

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

## serverinventory

The server inventory feature owns server inventory behavior.

Current implementation:

```text
feature/serverinventory/
    domain/
        model/
    presentation/
        screen/
        state/
        viewmodel/
```

Implemented files:

```text
feature/serverinventory/domain/model/Server.kt
feature/serverinventory/domain/model/ServerEnvironment.kt
feature/serverinventory/presentation/screen/ServerInventoryScreen.kt
feature/serverinventory/presentation/state/ServerInventoryFilter.kt
feature/serverinventory/presentation/state/ServerInventoryUiState.kt
feature/serverinventory/presentation/viewmodel/ServerInventoryViewModel.kt
```

Rules:

- Server inventory models belong to `feature/serverinventory/domain/model`.
- Server inventory presentation state belongs to `feature/serverinventory/presentation/state`.
- Server inventory screens belong to `feature/serverinventory/presentation/screen`.
- Server inventory ViewModels belong to `feature/serverinventory/presentation/viewmodel`.
- Repository contracts, persistence classes, DAO classes, entity classes, edit events, feature-local navigation classes, and list item components must not be added until the related behavior is implemented.
- Do not place server-specific classes in global `model`, `viewmodel`, or `ui/screens` packages.

Future packages may be added only when required by implementation:

```text
feature/serverinventory/data/
feature/serverinventory/data/local/dao/
feature/serverinventory/data/local/entity/
feature/serverinventory/domain/repository/
feature/serverinventory/navigation/
feature/serverinventory/presentation/component/
feature/serverinventory/presentation/event/
```

## settings

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

# Dependency Direction

Dependencies must point toward stable abstractions and away from volatile implementation details.

Expected direction:

```text
presentation -> domain -> data -> core
```

Allowed patterns:

```text
feature presentation -> feature domain
feature presentation -> core
feature data -> feature domain
feature data -> core
app/root -> feature
app/root -> core
shared data -> shared domain
shared data -> core
```

Forbidden patterns:

```text
domain -> data
domain -> presentation
domain -> Android framework APIs
data -> presentation
core -> feature
feature A -> feature B
```

Rules:

- Domain must remain stable and framework-independent.
- Data must not know Compose UI details.
- Core must not depend on feature packages.
- Features must communicate through shared contracts only when there is a justified need.

---

# Naming Rules

## Domain Models

Use clear noun-based names.

Examples:

```text
Server
ServerEnvironment
ServerStatus
```

## Room Entities

Append `Entity`.

Examples:

```text
ServerEntity
CommandEntity
```

## DAOs

Append `Dao`.

Examples:

```text
ServerDao
CommandDao
```

## Repository Interfaces

Use business-facing names.

Examples:

```text
ServerRepository
CommandRepository
```

## Repository Implementations

Prefix with `Default` unless a more precise implementation name is justified.

Examples:

```text
DefaultServerRepository
DefaultCommandRepository
```

## ViewModels

Append `ViewModel`.

Examples:

```text
ServerInventoryViewModel
ServerEditViewModel
```

## UI State

Append `UiState`.

Examples:

```text
ServerInventoryUiState
ServerEditUiState
```

## UI Events

Append `UiEvent` when explicit UI events are required.

Examples:

```text
ServerEditUiEvent
```

## Screens

Append `Screen`.

Examples:

```text
ServerInventoryScreen
ServerEditScreen
DashboardScreen
SettingsScreen
```

## Components

Use descriptive component names.

Examples:

```text
ServerCard
ServerInventoryEmptyContent
EnvironmentChip
```

---

# File Placement Rules

## Application Entry Points

Place application entry points in the root package:

```text
de/hamedtanha/servertoolkit/MainActivity.kt
de/hamedtanha/servertoolkit/ServerToolkitApplication.kt
```

## Screens

Place screens under the owning feature:

```text
feature/<feature-name>/presentation/screen/
```

Example:

```text
feature/serverinventory/presentation/screen/ServerInventoryScreen.kt
```

Do not use a global `ui/screens` package.

## Components

Place feature-specific components under:

```text
feature/<feature-name>/presentation/component/
```

Example:

```text
feature/serverinventory/presentation/component/ServerCard.kt
```

Only move components to shared UI packages after reuse is proven.

## UI State

Place feature-specific UI state classes under:

```text
feature/<feature-name>/presentation/state/
```

Example:

```text
feature/serverinventory/presentation/state/ServerInventoryUiState.kt
```

## UI Events

Place explicit UI event classes under:

```text
feature/<feature-name>/presentation/event/
```

Example:

```text
feature/serverinventory/presentation/event/ServerEditUiEvent.kt
```

This package is optional and should be used only when explicit event modeling improves clarity.

## ViewModels

Place ViewModels under:

```text
feature/<feature-name>/presentation/viewmodel/
```

Example:

```text
feature/serverinventory/presentation/viewmodel/ServerInventoryViewModel.kt
```

Do not use a global `viewmodel` package.

## Feature Domain Models

Place feature-owned domain models under:

```text
feature/<feature-name>/domain/model/
```

Example:

```text
feature/serverinventory/domain/model/Server.kt
```

## Shared Domain Models

Place shared domain models under:

```text
domain/model/
```

Only move a model here when more than one feature truly owns or consumes the concept.

## Repository Contracts

Place feature-specific repository contracts under:

```text
feature/<feature-name>/domain/repository/
```

Example:

```text
feature/serverinventory/domain/repository/ServerRepository.kt
```

Place shared repository contracts under:

```text
domain/repository/
```

## Repository Implementations

Place feature-local repository implementations under:

```text
feature/<feature-name>/data/
```

Example:

```text
feature/serverinventory/data/DefaultServerRepository.kt
```

Place shared repository implementations under:

```text
data/repository/
```

## Room Entities and DAOs

Place feature-owned Room entities and DAOs close to the owning feature unless they are explicitly shared.

Example:

```text
feature/serverinventory/data/local/entity/ServerEntity.kt
feature/serverinventory/data/local/dao/ServerDao.kt
```

Shared Room infrastructure belongs in:

```text
core/database/
```

## Navigation

Place app-level navigation infrastructure under:

```text
navigation/
```

Current examples:

```text
navigation/AppNavHost.kt
navigation/AppDestinations.kt
navigation/NavigationDestination.kt
```

Feature-level navigation packages are optional.

Use feature-level navigation only when a feature owns a sufficiently complex navigation flow that justifies separate route definitions.

Do not create placeholder navigation packages without implemented navigation behavior.

## Dependency Injection

Place app-wide Hilt modules under:

```text
core/di/
```

Place feature-specific Hilt modules close to the owning feature when practical:

```text
feature/<feature-name>/data/di/
```

---

# Testing Structure

Unit tests should mirror the production package structure when practical.

Expected test roots:

```text
app/src/test/java/de/hamedtanha/servertoolkit
app/src/androidTest/java/de/hamedtanha/servertoolkit
```

Examples:

```text
app/src/test/java/de/hamedtanha/servertoolkit/feature/serverinventory/domain/ServerValidatorTest.kt
app/src/test/java/de/hamedtanha/servertoolkit/feature/serverinventory/data/ServerMapperTest.kt
app/src/androidTest/java/de/hamedtanha/servertoolkit/feature/serverinventory/data/local/ServerDaoTest.kt
```

Rules:

- Domain validation should be unit-tested.
- Mapping logic should be unit-tested when non-trivial.
- DAO behavior should be tested with Android instrumentation tests or an appropriate Room test setup.
- ViewModel behavior should be tested when business-relevant state transitions exist.
- UI tests should be added only when UI behavior is stable enough to justify maintenance cost.

---

# Prohibited Package Patterns

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

# Anti-Patterns

The following patterns are not allowed:

- Placing all screens in a global `ui/screens` package.
- Placing all ViewModels in a global `viewmodel` package.
- Placing all domain models in a vague global `model` package.
- Placing Room entities in domain packages.
- Placing repository implementations in presentation packages.
- Exposing DAOs directly to ViewModels.
- Creating generic `utils` packages filled with unrelated functions.
- Moving feature-specific UI components into shared UI packages prematurely.
- Creating remote/network packages before remote features exist.
- Mixing navigation route definitions across unrelated features.
- Allowing one feature package to depend directly on another feature package.

---

# Refactoring Rule

When an existing file does not match this structure, it must be moved to the correct package before being expanded.

Do not add new code to legacy, temporary, or prohibited package locations.

Existing nonconforming packages must be treated as cleanup targets, not as valid extension points.

---

# Evolution Policy

This structure may evolve as the project grows, but changes must be intentional.

A package structure change requires documentation updates when it affects:

- Layer boundaries
- Dependency direction
- Feature organization
- Persistence organization
- Navigation organization
- Security-sensitive code placement
- Testing structure

Significant structural changes may require a new ADR.

Accepted ADRs must remain historically traceable. If a structural decision supersedes an accepted ADR, create a new ADR instead of silently rewriting architectural history.

---

# Related Documents

- `ARCHITECTURE.md`
- `docs/adr/ADR-002-application-architecture.md`
- `docs/adr/ADR-003-local-persistence-with-room.md`
- `docs/adr/ADR-004-navigation-strategy.md`
- `docs/adr/ADR-005-dependency-injection-strategy.md`
- `docs/DEVELOPMENT.md`
- `docs/PROJECT_STATE.md`

---

# Notes

This document is intentionally strict.

A strict package structure reduces ambiguity, improves review quality, and prevents feature implementation from drifting into inconsistent architecture.
