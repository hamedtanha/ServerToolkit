# Package Structure

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Active  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines the package and directory structure for the Server Toolkit Android application.

Its purpose is to keep the codebase consistent, maintainable, and scalable as features are added incrementally.

This document describes the intended implementation structure. It must remain synchronized with `ARCHITECTURE.md`, the accepted ADRs, and the actual source code.

---

# Scope

This document applies to the Android application source code.

It covers:

- Source package organization
- Feature package boundaries
- Core package responsibilities
- Domain package responsibilities
- Data package responsibilities
- Naming rules
- Placement rules for common file types

This document does not define UI design, database schema details, navigation routes, release policy, or feature roadmap.

---

# Package Root

The application package root is:

```text
com.hamedtanha.servertoolkit
```

All application source code must be placed under this root package.

---

# Top-Level Package Structure

The project uses a layered package structure with feature-oriented UI organization.

```text
com.hamedtanha.servertoolkit
├── app
├── core
├── data
├── domain
└── feature
```

---

# Package Responsibilities

## app

The `app` package contains application-level wiring.

Allowed responsibilities:

- Application entry point
- Dependency graph setup
- Root navigation host setup
- Application-level configuration
- Theme initialization when required by application bootstrap

Expected examples:

```text
app/
├── ServerToolkitApp.kt
├── MainActivity.kt
└── AppState.kt
```

Rules:

- Do not place business logic in `app`.
- Do not place database entities in `app`.
- Do not place feature-specific UI in `app`.
- Keep `app` thin and focused on composition.

---

## core

The `core` package contains reusable infrastructure and shared utilities that are not owned by a single feature.

```text
core/
├── common
├── database
├── navigation
├── security
└── ui
```

### core.common

Shared non-Android or lightweight Android-independent utilities.

Examples:

```text
core/common/
├── Result.kt
├── ErrorType.kt
├── AppDispatchers.kt
└── TimeProvider.kt
```

Rules:

- Keep this package small.
- Do not create generic helper classes without a proven use case.
- Avoid dumping unrelated utilities into this package.

### core.database

Database-level configuration and shared database infrastructure.

Examples:

```text
core/database/
├── ServerToolkitDatabase.kt
├── DatabaseMigrations.kt
└── DatabaseConstants.kt
```

Rules:

- Database entities and DAOs may live in `data.local` when they are feature-specific.
- Shared database setup belongs here.
- Room database class belongs here.

### core.navigation

Application-level navigation contracts and route definitions.

Examples:

```text
core/navigation/
├── AppRoute.kt
├── ServerToolkitNavHost.kt
└── TopLevelDestination.kt
```

Rules:

- Navigation route definitions must be centralized.
- Feature screens must not define unrelated global routes.
- Navigation must remain type-safe where practical.

### core.security

Shared security-related infrastructure.

Examples:

```text
core/security/
├── SecureStorage.kt
├── CredentialEncryption.kt
└── SecurityConstants.kt
```

Rules:

- Sensitive data handling must be isolated here or in a dedicated security data source.
- Do not store secrets in plain Room entities.
- Do not introduce credential persistence without a documented security decision.

### core.ui

Reusable UI primitives and design system components.

Examples:

```text
core/ui/
├── theme
├── component
└── icon
```

Rules:

- Reusable components belong here only after they are genuinely shared.
- Feature-specific UI must stay inside its feature package.
- Avoid premature component abstraction.

---

## domain

The `domain` package contains business models and business-facing contracts.

```text
domain/
├── model
├── repository
├── usecase
└── validation
```

### domain.model

Pure Kotlin business models.

Examples:

```text
domain/model/
├── Server.kt
└── ServerEnvironment.kt
```

Rules:

- Domain models must not depend on Room, Compose, Retrofit, or Android framework APIs.
- Domain models represent business concepts, not database tables.
- Domain models must remain stable and readable.

### domain.repository

Repository interfaces used by ViewModels and use cases.

Examples:

```text
domain/repository/
└── ServerRepository.kt
```

Rules:

- Interfaces belong in `domain.repository`.
- Implementations belong in `data.repository`.
- UI must depend on repository interfaces, not concrete data implementations.

### domain.usecase

Business operations that are complex enough to justify a dedicated class.

Examples:

```text
domain/usecase/
├── AddServerUseCase.kt
├── UpdateServerUseCase.kt
└── DeleteServerUseCase.kt
```

Rules:

- Do not create use cases for trivial pass-through calls.
- Introduce use cases when they improve readability, validation, testability, or reuse.
- Use cases must not contain Android UI logic.

### domain.validation

Validation logic for domain input.

Examples:

```text
domain/validation/
├── ServerValidator.kt
└── ValidationResult.kt
```

Rules:

- Validation rules must be testable without Android framework dependencies.
- UI must not be the only place where business validation exists.

---

## data

The `data` package contains implementation details for persistence, networking, and repository implementations.

```text
data/
├── local
├── mapper
├── repository
└── remote
```

### data.local

Local persistence implementation.

```text
data/local/
├── dao
├── entity
└── datasource
```

Examples:

```text
data/local/dao/
└── ServerDao.kt

data/local/entity/
└── ServerEntity.kt

data/local/datasource/
└── ServerLocalDataSource.kt
```

Rules:

- Room entities belong in `data.local.entity`.
- Room DAOs belong in `data.local.dao`.
- Data sources may wrap DAOs when useful for abstraction or testability.
- Room-specific types must not leak into `domain` or `feature` packages.

### data.mapper

Mapping between data models and domain models.

Examples:

```text
data/mapper/
└── ServerMapper.kt
```

Rules:

- Mapping logic must be explicit.
- Do not hide mapping inside UI code.
- Avoid extension functions that obscure important transformations.

### data.repository

Concrete repository implementations.

Examples:

```text
data/repository/
└── DefaultServerRepository.kt
```

Rules:

- Implement domain repository interfaces here.
- Coordinate local and remote data sources here when required.
- Do not put UI state handling in repositories.

### data.remote

Remote communication implementation.

Examples:

```text
data/remote/
├── api
├── dto
└── datasource
```

Rules:

- This package may remain empty until remote features exist.
- Do not add remote abstractions before they are needed.
- Network DTOs must not leak into the domain layer.

---

## feature

The `feature` package contains user-facing feature implementations.

```text
feature/
├── dashboard
├── servers
└── settings
```

Each feature may contain:

```text
feature/<feature-name>/
├── navigation
├── screen
├── component
├── state
└── viewmodel
```

### feature.dashboard

Dashboard-related UI.

Expected initial structure:

```text
feature/dashboard/
├── navigation
├── screen
└── viewmodel
```

### feature.servers

Server Inventory UI.

Expected initial structure:

```text
feature/servers/
├── navigation
├── screen
├── component
├── state
└── viewmodel
```

Examples:

```text
feature/servers/screen/
├── ServerListScreen.kt
├── ServerDetailScreen.kt
└── ServerEditScreen.kt

feature/servers/viewmodel/
├── ServerListViewModel.kt
└── ServerEditViewModel.kt

feature/servers/state/
├── ServerListUiState.kt
└── ServerEditUiState.kt
```

Rules:

- Feature UI must not depend directly on Room DAOs.
- Feature UI must not depend directly on Room entities.
- Feature ViewModels may depend on domain repositories or use cases.
- Feature-specific components stay inside the feature package until reused.

### feature.settings

Settings-related UI.

Expected initial structure:

```text
feature/settings/
├── navigation
├── screen
└── viewmodel
```

---

# Initial Sprint 1 Package Target

Sprint 1 focuses on Server Inventory.

The minimum expected implementation structure is:

```text
com.hamedtanha.servertoolkit
├── app
│   ├── MainActivity.kt
│   └── ServerToolkitApp.kt
├── core
│   ├── database
│   │   └── ServerToolkitDatabase.kt
│   ├── navigation
│   │   ├── AppRoute.kt
│   │   └── ServerToolkitNavHost.kt
│   └── ui
│       └── theme
├── data
│   ├── local
│   │   ├── dao
│   │   │   └── ServerDao.kt
│   │   └── entity
│   │       └── ServerEntity.kt
│   ├── mapper
│   │   └── ServerMapper.kt
│   └── repository
│       └── DefaultServerRepository.kt
├── domain
│   ├── model
│   │   ├── Server.kt
│   │   └── ServerEnvironment.kt
│   ├── repository
│   │   └── ServerRepository.kt
│   └── validation
│       └── ServerValidator.kt
└── feature
    ├── dashboard
    │   └── screen
    │       └── DashboardScreen.kt
    ├── servers
    │   ├── component
    │   ├── screen
    │   │   ├── ServerListScreen.kt
    │   │   └── ServerEditScreen.kt
    │   ├── state
    │   │   ├── ServerListUiState.kt
    │   │   └── ServerEditUiState.kt
    │   └── viewmodel
    │       ├── ServerListViewModel.kt
    │       └── ServerEditViewModel.kt
    └── settings
        └── screen
            └── SettingsScreen.kt
```

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
ServerListViewModel
ServerEditViewModel
```

## UI State

Append `UiState`.

Examples:

```text
ServerListUiState
ServerEditUiState
```

## UI Events

Append `UiEvent` when required.

Examples:

```text
ServerEditUiEvent
```

## Screens

Append `Screen`.

Examples:

```text
ServerListScreen
ServerEditScreen
```

## Reusable Components

Use descriptive component names.

Examples:

```text
ServerCard
EmptyServerList
EnvironmentChip
```

---

# Dependency Direction

Dependencies must point inward toward stable business concepts.

Allowed dependency direction:

```text
feature → domain
feature → core

data → domain
data → core

app → feature
app → domain
app → data
app → core
```

Forbidden dependency direction:

```text
domain → data
domain → feature
domain → app

data → feature

core → feature
core → data
```

Rules:

- `domain` must remain the most stable layer.
- `feature` must not know Room implementation details.
- `data` must not know Compose UI details.
- `core` must not become a dumping ground for feature logic.

---

# File Placement Rules

## Where to Place a New Screen

Place feature-specific screens under:

```text
feature/<feature-name>/screen
```

Example:

```text
feature/servers/screen/ServerListScreen.kt
```

## Where to Place a New ViewModel

Place feature-specific ViewModels under:

```text
feature/<feature-name>/viewmodel
```

Example:

```text
feature/servers/viewmodel/ServerListViewModel.kt
```

## Where to Place a Domain Model

Place business models under:

```text
domain/model
```

Example:

```text
domain/model/Server.kt
```

## Where to Place a Room Entity

Place Room entities under:

```text
data/local/entity
```

Example:

```text
data/local/entity/ServerEntity.kt
```

## Where to Place a DAO

Place Room DAOs under:

```text
data/local/dao
```

Example:

```text
data/local/dao/ServerDao.kt
```

## Where to Place a Repository Interface

Place repository contracts under:

```text
domain/repository
```

Example:

```text
domain/repository/ServerRepository.kt
```

## Where to Place a Repository Implementation

Place repository implementations under:

```text
data/repository
```

Example:

```text
data/repository/DefaultServerRepository.kt
```

## Where to Place Shared UI Components

Feature-specific UI components remain inside the feature package.

Only move components to `core.ui.component` when they are reused by at least two features or clearly belong to the design system.

---

# Testing Structure

Unit tests should mirror the production package structure when practical.

Expected test roots:

```text
src/test/java/com/hamedtanha/servertoolkit
src/androidTest/java/com/hamedtanha/servertoolkit
```

Examples:

```text
src/test/java/com/hamedtanha/servertoolkit/domain/validation/ServerValidatorTest.kt
src/test/java/com/hamedtanha/servertoolkit/data/mapper/ServerMapperTest.kt
src/androidTest/java/com/hamedtanha/servertoolkit/data/local/dao/ServerDaoTest.kt
```

Rules:

- Domain validation should be unit-tested.
- Mapping logic should be unit-tested when non-trivial.
- DAO behavior should be tested with Android instrumentation tests or an appropriate Room test setup.
- UI tests should be added only when the UI behavior is stable enough to justify maintenance cost.

---

# Anti-Patterns

The following patterns are not allowed:

- Placing all files in a single `ui` package.
- Placing Room entities in the domain layer.
- Placing repository implementations in the UI layer.
- Exposing DAOs directly to ViewModels.
- Creating generic `utils` packages filled with unrelated functions.
- Creating use cases for every simple repository call without justification.
- Moving feature-specific UI components into `core.ui` prematurely.
- Creating remote/network packages before remote features exist.
- Mixing navigation route definitions across unrelated features.

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

Significant structural changes may require a new ADR.

---

# Related Documents

- `ARCHITECTURE.md`
- `docs/adr/ADR-002-application-architecture.md`
- `docs/adr/ADR-003-local-persistence-with-room.md`
- `docs/adr/ADR-004-navigation-strategy.md`
- `DEVELOPMENT.md`
- `PROJECT_STATE.md`

---

# Notes

This document is intentionally strict.

A strict package structure reduces ambiguity, improves review quality, and prevents feature implementation from drifting into inconsistent architecture.
