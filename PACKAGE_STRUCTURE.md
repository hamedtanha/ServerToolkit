# Package Structure

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-16

---

## Purpose

This document defines the canonical Android source-package structure for Server Toolkit.

Its purpose is to keep ownership, dependency direction, file placement, and feature boundaries explicit as the application evolves.

This document describes implemented structure and accepted placement rules. It must remain synchronized with the source tree, `docs/ARCHITECTURE.md`, and accepted ADRs.

---

## Base Package

All Android application code belongs under:

```text
de.hamedtanha.servertoolkit
```

Application source root:

```text
app/src/main/java/de/hamedtanha/servertoolkit/
```

Test source roots mirror production packages under:

```text
app/src/test/java/de/hamedtanha/servertoolkit/
app/src/androidTest/java/de/hamedtanha/servertoolkit/
```

---

## Current Canonical Layout

```text
app/src/main/java/de/hamedtanha/servertoolkit/
    MainActivity.kt
    ServerToolkitApplication.kt

    core/
        common/
        database/
            ServerToolkitDatabase.kt
            ServerToolkitDatabaseMigrations.kt
        di/
            AppModule.kt
            DatabaseModule.kt

    feature/
        dashboard/
            navigation/
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
            navigation/
            presentation/
                component/
                screen/
                state/
                viewmodel/

        ssh/
            data/
                local/
                    dao/
                    entity/
                mapper/
                repository/
                service/
                source/
            di/
            domain/
                model/
                repository/
                service/
                usecase/
            navigation/
            presentation/
                component/
                event/
                mapper/
                screen/
                state/
                viewmodel/

        savedcommands/
            data/
                factory/
                local/
                    dao/
                    entity/
                mapper/
                repository/
            di/
            domain/
                factory/
                model/
                repository/
            navigation/
            presentation/
                screen/
                state/
                viewmodel/

    navigation/
    ui/
        theme/
```

The layout intentionally summarizes verified responsibility packages rather than predicting every future subpackage.

Only packages backed by implemented responsibilities should exist. Do not add speculative presentation, navigation, use-case, or shared packages before a concrete class requires them.

---

## Top-Level Responsibilities

### Root Package

The root package contains application entry points only.

Allowed responsibilities:

- Android activity entry point.
- Hilt application entry point.

Feature logic, repositories, Room code, navigation destinations, and UI screens do not belong in the root package.

### `core`

`core` contains infrastructure that is genuinely shared across multiple features.

Current responsibilities:

- application-wide Room database aggregation;
- explicit database migrations;
- application-wide dependency-injection modules;
- narrowly scoped cross-cutting utilities when reuse is proven.

Rules:

- Keep `core/common` small.
- Do not use `core` as a miscellaneous helper container.
- Feature-owned entities and DAOs remain inside their owning feature.
- `ServerToolkitDatabase` may reference feature-owned Room entities and DAOs only because Room requires central schema aggregation.
- Business behavior must not be implemented in `core/database`.

### `feature`

Each concrete product capability owns a feature package.

A feature may contain:

- domain models and project-owned contracts;
- data implementations and external-library adapters;
- feature-owned Room entities and DAOs;
- feature-specific Hilt modules;
- navigation definitions when navigation exists;
- presentation state, events, mappers, screens, components, and ViewModels when UI exists.

Rules:

- Keep responsibilities inside the owning feature until reuse is demonstrated.
- Do not introduce a broad umbrella feature for anticipated future capabilities.
- Features must not directly access another feature's data implementation, DAO, Room entity, screen, or ViewModel.
- Cross-feature integration must use an explicit stable contract or app-level navigation boundary.

---

## Current Feature Boundaries

### Dashboard

`feature/dashboard` owns the application overview and entry actions.

It may consume stable summaries or navigation callbacks, but it must not own Server Inventory, SSH, or Saved Commands data.

### Server Inventory

`feature/serverinventory` owns local server records and inventory management.

Canonical placement:

```text
feature/serverinventory/domain/model/
feature/serverinventory/domain/repository/
feature/serverinventory/data/local/entity/
feature/serverinventory/data/local/dao/
feature/serverinventory/data/mapper/
feature/serverinventory/data/repository/
feature/serverinventory/di/
feature/serverinventory/navigation/
feature/serverinventory/presentation/
```

The name `serverinventory` remains intentional. A broader `inventory` boundary must wait until non-server assets or shared inventory behavior are implemented.

### SSH

`feature/ssh` owns SSH trust, authentication, connection, session lifecycle, non-interactive command execution, private-key source ownership, and connection-history behavior.

Verified responsibility packages include:

```text
feature/ssh/data/local/
feature/ssh/data/mapper/
feature/ssh/data/repository/
feature/ssh/data/service/
feature/ssh/data/source/
feature/ssh/domain/model/
feature/ssh/domain/repository/
feature/ssh/domain/service/
feature/ssh/domain/usecase/
feature/ssh/navigation/
feature/ssh/presentation/
```

Rules:

- SSHJ, Android content access, and cryptographic integration remain in the data layer.
- Project-owned service contracts, use cases, and result models remain in the domain layer.
- Credential-bearing objects must not leak into presentation state.
- Room-backed SSH trust and connection-history persistence remain feature-owned.
- Active-session ownership and cleanup stay behind project-owned boundaries.

### Saved Commands

`feature/savedcommands` owns reusable operational command definitions.

Implemented structure:

```text
feature/savedcommands/
    data/
        factory/
            DefaultSavedCommandFactory.kt
        local/
            dao/
                SavedCommandDao.kt
            entity/
                SavedCommandEntity.kt
        mapper/
            SavedCommandEntityMapper.kt
        repository/
            RoomSavedCommandRepository.kt
    di/
        SavedCommandsDatabaseModule.kt
        SavedCommandsModule.kt
    domain/
        factory/
            SavedCommandFactory.kt
        model/
            SavedCommand.kt
        repository/
            SavedCommandRepository.kt
    navigation/
        SavedCommandsDestination.kt
    presentation/
        screen/
            SavedCommandsScreen.kt
        state/
            SavedCommandsUiState.kt
        viewmodel/
            SavedCommandsViewModel.kt
```

Current rules:

- Saved commands are global; server-specific assignment is not implemented.
- The domain layer owns model invariants, the repository contract, and the minimal factory contract used for testable identifier and timestamp generation.
- Room types remain in the data layer.
- `DefaultSavedCommandFactory` owns concrete UUID and timestamp generation.
- Persistence stores command text exactly and does not parse, rewrite, or execute it.
- Presentation depends on `SavedCommandRepository` and `SavedCommandFactory`, not DAO, entity, or concrete Room repository types.
- Navigation remains feature-owned and is registered through the app-level Navigation Compose boundary.
- Create and delete workflows use immutable UI state and ViewModel-controlled unidirectional data flow.
- Saved Commands must not depend on SSHJ or SSH data-layer classes.
- Future SSH input integration must use a narrow project-owned boundary and must never trigger automatic execution.
- Do not introduce `feature/operations` until multiple implemented Operations capabilities require a shared boundary.

---

## Dependency Direction

Dependencies point toward stable project-owned abstractions:

```text
presentation -> domain
data -> domain
data -> core database infrastructure
feature DI -> feature data and domain contracts
app navigation -> feature navigation or presentation entry points
```

Narrow Room aggregation exception:

```text
core/database -> feature data/local entity and DAO
```

This exception exists only for Room schema aggregation.

Forbidden directions:

```text
domain -> data
domain -> presentation
domain -> Android framework APIs
data -> presentation
presentation -> DAO
presentation -> Room entity
feature A data -> feature B data
feature A presentation -> feature B presentation
```

---

## Naming Rules

### Domain Models

Use concise product nouns:

```text
Server
SavedCommand
SshConnectionRequest
```

Do not rename a concrete model to a broader abstraction before that broader concept exists.

### Repository Contracts

Use the owned capability followed by `Repository`:

```text
ServerRepository
SavedCommandRepository
SshConnectionHistoryRepository
```

Contracts belong in the domain layer. Concrete implementations identify their mechanism when useful:

```text
RoomServerRepository
RoomSavedCommandRepository
```

### Room Types

Append `Entity` and `Dao`:

```text
SavedCommandEntity
SavedCommandDao
```

Room column naming uses stable snake_case database names where explicit mapping improves schema clarity.

### Dependency-Injection Modules

Use feature-specific module names:

```text
SavedCommandsModule
SavedCommandsDatabaseModule
```

Application-wide modules stay in `core/di`.

### Presentation Types

Use responsibility-oriented suffixes:

```text
Screen
UiState
ViewModel
UiMapper
Event
```

Do not create placeholder presentation types solely to reserve package structure.

---

## Test Placement

Unit tests mirror production packages under `app/src/test`.

Use unit tests for:

- domain invariants;
- pure mappers;
- use cases;
- ViewModel behavior;
- presentation-state transformations;
- non-Android service logic.

Instrumentation tests mirror production packages under `app/src/androidTest`.

Use instrumentation tests for:

- Room DAOs;
- Room-backed repositories;
- database migrations;
- Android content and lifecycle boundaries;
- focused UI/runtime behavior requiring Android.

Migration tests belong under the `core/database` test package because they validate the aggregated application database.

---

## Evolution Rules

Before introducing or moving a package:

1. Identify the concrete owner and consumer.
2. Confirm the responsibility is implemented or part of the current accepted slice.
3. Preserve dependency direction.
4. Check whether the move changes an accepted architecture decision.
5. Update this document and related current-state documentation in the same pull request.
6. Add an ADR only when the decision is significant, not for routine file placement.

Avoid:

- generic `utils`, `helpers`, `managers`, or `common` packages without proven ownership;
- broad package renames during feature delivery;
- cross-feature implementation dependencies;
- parallel repositories for the same source of truth;
- placeholder packages for speculative roadmap items;
- architecture-layer folders that contain no real responsibility.

---

## Related Documents

- [Architecture](docs/ARCHITECTURE.md)
- [Project State](docs/PROJECT_STATE.md)
- [Saved Commands Status](docs/state/SAVED_COMMANDS_STATUS.md)
- [Server Inventory Status](docs/state/SERVER_INVENTORY_STATUS.md)
- [SSH Status](docs/state/SSH_STATUS.md)
- [Documentation Governance](docs/DOCUMENTATION.md)
- [ADR-002: Application Architecture](docs/adr/ADR-002-application-architecture.md)
- [ADR-003: Local Persistence with Room](docs/adr/ADR-003-local-persistence-with-room.md)
