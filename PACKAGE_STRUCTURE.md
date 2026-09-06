# Package Structure

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-09-06

---

## Purpose

This document defines the canonical Android source-package ownership for Server Toolkit.

It describes implemented structure and accepted placement rules. It must remain synchronized with the production source tree, [Architecture](docs/ARCHITECTURE.md), and accepted ADRs.

Only packages backed by implemented responsibilities should exist. Empty package scaffolding must not be retained for speculative future layers or features.

---

## Source Roots

Production source:

```text
app/src/main/java/de/hamedtanha/servertoolkit/
```

Tests mirror production ownership under:

```text
app/src/test/java/de/hamedtanha/servertoolkit/
app/src/androidTest/java/de/hamedtanha/servertoolkit/
```

---

## Current Canonical Layout

```text
de.hamedtanha.servertoolkit/
├── MainActivity.kt
├── ServerToolkitApplication.kt
├── core/
│   ├── connection/
│   │   └── domain/
│   │       ├── model/
│   │       └── resolver/
│   ├── database/
│   └── di/
├── feature/
│   ├── dashboard/
│   │   ├── navigation/
│   │   └── presentation/
│   ├── savedcommands/
│   │   ├── data/
│   │   ├── di/
│   │   ├── domain/
│   │   ├── navigation/
│   │   └── presentation/
│   ├── serverinventory/
│   │   ├── data/
│   │   │   ├── connection/
│   │   │   ├── local/
│   │   │   ├── mapper/
│   │   │   └── repository/
│   │   ├── di/
│   │   ├── domain/
│   │   └── presentation/
│   └── ssh/
│       ├── data/
│       │   ├── local/
│       │   ├── mapper/
│       │   ├── repository/
│       │   ├── service/
│       │   └── source/
│       ├── di/
│       ├── domain/
│       ├── navigation/
│       └── presentation/
├── navigation/
└── ui/
    └── designsystem/
        └── theme/
```

The tree intentionally stops at responsibility-bearing package groups. It does not predict every leaf directory.

---

## Top-Level Ownership

### Application root

The root package contains Android application entry points only.

Feature logic, repositories, Room behavior, SSHJ behavior, and feature screens do not belong here.

### `core/connection`

`core/connection` owns non-sensitive application-wide connection-target contracts shared across feature boundaries.

Current responsibilities include:

- `RemoteConnectionTarget`;
- target-resolution result models;
- target validation reasons;
- the `ConnectionTargetResolver` contract.

It does not own credentials, host trust, active SSH sessions, or transport-specific behavior.

### `core/database`

`core/database` owns application-wide Room schema aggregation and explicit database migrations.

Room requires one database schema, so this package may reference feature-owned Room entities and DAOs for aggregation. That exception does not transfer feature business ownership into `core`.

### `core/di`

`core/di` owns application-wide dependency construction only.

Feature-specific bindings remain inside their feature.

### `feature/*`

Each product capability owns its domain, data, DI, navigation, and presentation responsibilities only when those responsibilities are implemented.

Current feature boundaries are:

- `feature/dashboard`;
- `feature/serverinventory`;
- `feature/ssh`;
- `feature/savedcommands`.

Do not add a broad `operations`, `provider`, `gateway`, `platform`, `common`, or plugin package until concrete implemented behavior requires it.

### `navigation`

The app-level navigation package composes feature destinations. It does not own feature state or persistence.

### `ui/designsystem/theme`

The design-system theme package owns the accepted application-wide color, typography, shape, spacing, theme, and visual-profile definitions.

It is not `ui/theme`; the implemented ownership path is `ui/designsystem/theme`.

---

## Feature Boundaries

### Dashboard

Dashboard owns the application overview and entry actions. It may consume stable summaries and navigation callbacks but does not own Server Inventory, SSH, or Saved Commands persistence.

### Server Inventory

Server Inventory owns local `Server` records, inventory editing/search/filtering, Room persistence, and the concrete resolver that maps inventory records into the project-owned `core/connection` target contract.

The name `serverinventory` remains intentional. A broader inventory abstraction requires concrete non-server asset behavior before adoption.

### SSH

SSH owns:

- host-key observation and trust;
- ephemeral authentication input;
- SSHJ adapters;
- active session ownership and cleanup;
- explicit non-interactive command execution;
- command output/result mapping;
- connection history;
- the SSH workflow presentation.

SSH presentation may consume Saved Commands **domain** contracts for selector data. It must not consume Saved Commands DAO, entity, concrete repository, screen, or ViewModel implementations.

### Saved Commands

Saved Commands owns reusable operational command definitions, validation, Room persistence, management presentation, and navigation.

Saved Commands are currently global. Persistence preserves command text exactly and never executes it. SSH selection replaces editable command input only; explicit Run remains the sole execution trigger.

---

## Executable Dependency Contract

The production import contract is enforced by:

```text
scripts/architecture/check-dependencies.sh
```

The normal pull-request validation path runs this checker before Gradle build/unit validation.

### Domain rules

Domain code may depend on project-owned domain/core contracts and ordinary platform-neutral language/runtime libraries.

Domain code must not import:

- Android or AndroidX APIs;
- Room;
- SSHJ;
- project data implementations;
- project presentation implementations.

### Presentation rules

Presentation may depend on its owning feature's domain contracts and explicitly accepted foreign-feature **domain** contracts.

Presentation must not depend on:

- another feature's presentation;
- another feature's data or DI implementation;
- Room APIs or entities;
- concrete repository implementations.

Current accepted cross-feature contract:

```text
SSH presentation -> Saved Commands domain
```

### Data rules

Data implementations depend inward on project-owned contracts.

Data must not depend on presentation. One feature's data implementation must not depend on another feature's data implementation except for explicitly named persistence metadata required to preserve relational integrity.

### Named narrow exceptions

The checker contains exactly the currently justified source/import exceptions:

1. `feature/ssh/presentation/screen/SshScreen.kt` may reference `AndroidSshPrivateKeySourceFactory` at the Android document-picker composition boundary. This does not permit general Presentation -> Data dependencies.
2. `SshTrustedHostKeyEntity.kt` may reference Server Inventory `ServerEntity` only for Room foreign-key metadata.
3. `SshConnectionHistoryEntity.kt` may reference Server Inventory `ServerEntity` only for Room foreign-key metadata.

Any new exception requires focused review and synchronized checker/documentation changes. Broad package allowlists are not accepted.

### Central Room aggregation exception

```text
core/database -> feature-owned Room entities and DAOs
```

This remains a schema-composition exception only.

---

## Naming and Placement Rules

- Domain models use concise product language such as `Server`, `SavedCommand`, and `SshConnectionRequest`.
- Repository contracts belong in domain packages and use the owned capability plus `Repository`.
- Concrete repositories identify their mechanism when useful, such as `RoomSavedCommandRepository`.
- Room types use `Entity` and `Dao` suffixes.
- Feature-specific Hilt modules stay in the owning feature.
- Presentation types use responsibility-oriented names such as `Screen`, `UiState`, `ViewModel`, `UiMapper`, and `Event`.
- Do not create placeholder packages to reserve future architecture.

---

## Test Placement

Use JVM tests for deterministic domain, mapper, use-case, ViewModel, state, and non-Android adapter behavior.

Use Android instrumentation for Room migrations/DAOs/repositories, Android content or lifecycle boundaries, and Compose behavior requiring Android runtime semantics.

Migration tests belong under the application database test ownership because they validate the aggregated Room schema.

---

## Evolution Rules

Before introducing or moving a package:

1. Identify the concrete owner and consumer.
2. Confirm the responsibility exists in the current accepted implementation slice.
3. Preserve the executable dependency contract.
4. Reassess whether a named exception is genuinely required.
5. Update this document and affected living architecture documentation in the same pull request.
6. Create an ADR only when the change introduces a significant durable decision.

Avoid:

- generic utility/helper/manager packages without proven ownership;
- empty package scaffolding;
- cross-feature implementation dependencies;
- broad package renames during unrelated feature delivery;
- premature module splits;
- generic registries or plugin infrastructure without a concrete feature need.

---

## Related Documents

- [Architecture](docs/ARCHITECTURE.md)
- [Architecture Atlas](docs/ARCHITECTURE_ATLAS.md)
- [Project State](docs/PROJECT_STATE.md)
- [Documentation Governance](docs/DOCUMENTATION.md)
- [ADR Index](docs/adr/README.md)
- [ADR-002: Application Architecture](docs/adr/ADR-002-application-architecture.md)
- [ADR-003: Local Persistence with Room](docs/adr/ADR-003-local-persistence-with-room.md)
- [ADR-017: Scalable Collection UX Contract](docs/adr/ADR-017-scalable-collection-ux-contract.md)
