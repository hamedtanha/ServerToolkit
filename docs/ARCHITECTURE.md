# Architecture

**Project:** Server Toolkit  
**Version:** 0.4.0\
**Status:** Active  
**Last Updated:** 2026-09-06

---

## Purpose

This document defines the practical application architecture for the Server Toolkit Android application.

It translates accepted Architecture Decision Records into implementation rules for project structure, layer responsibilities, dependency direction, data flow, navigation, dependency injection, persistence, remote-capability boundaries, support claims, and executable architecture enforcement.

This document describes current implementation and accepted implementation direction. It must not advertise planned functionality as completed functionality.

---

## Product and Architecture Baseline

Server Toolkit is a platform-neutral remote systems operations application.

The current verified remote-access capability is SSH. The architecture may support additional platforms, transports, and providers over time, but architectural extensibility is not a support claim.

The application follows:

- Kotlin;
- Jetpack Compose;
- Single Activity architecture;
- MVVM;
- Repository Pattern;
- Unidirectional Data Flow;
- Hilt for dependency injection;
- Jetpack Navigation Compose;
- Room for local structured persistence;
- feature-first ownership where practical;
- platform-neutral Core concepts;
- incremental remote-capability gateways and providers only when justified by concrete features.

The project must not introduce additional layers, frameworks, generic base classes, empty package hierarchies, registries, or plugin abstractions unless they solve a real implementation problem.

---

## Accepted Architecture Decisions

| ADR | Decision | Status / Relationship |
|---|---|---|
| ADR-001 | Project Vision | Accepted; Linux-specific scope assumptions superseded by ADR-015 |
| ADR-002 | Application Architecture | Accepted; refined by ADR-016 |
| ADR-003 | Local Persistence with Room | Accepted |
| ADR-004 | Navigation Strategy | Accepted |
| ADR-005 | Dependency Injection Strategy | Accepted |
| ADR-006 | SSH Workflow and Security Boundaries | Accepted |
| ADR-007 | Secure Storage Strategy | Accepted |
| ADR-008 | SSH Client Library Selection | Accepted |
| ADR-009 | SSH Host Trust and Authentication Input Strategy | Accepted |
| ADR-010 | SSH Command Channel Execution Strategy | Accepted |
| ADR-011 | SSH Credential Ownership and Secure Storage Strategy | Accepted |
| ADR-012 | Android Backup and Data Extraction Policy | Accepted |
| ADR-013 | Ephemeral SSH Private-Key Authentication Boundary | Accepted |
| ADR-014 | Android Release Signing Strategy | Accepted |
| ADR-015 | Platform-Neutral Remote Systems Product Direction | Accepted |
| ADR-016 | Three-Level Remote Capability Architecture | Accepted |
| ADR-017 | Scalable Collection UX Contract | Accepted |

Accepted ADRs are the source of truth for durable architectural decisions. The canonical index is `docs/adr/README.md`.

---

## Two Complementary Architecture Views

### Android Application Architecture

```text
UI
 ↓
Presentation
 ↓
Domain contracts and models
 ↑
Data implementations
```

This view governs UI, presentation state, domain contracts, repositories, persistence, navigation, and Android integration.

### Remote Capability Architecture

```text
Presentation / Use Case
        ↓
Core capability contract
        ↓
Capability Gateway
        ↓
Provider / Adapter
        ↓
Transport or external system
```

This view applies only when a concrete remote capability requires discovery, translation, routing, normalization, orchestration, policy enforcement, or provider isolation. Purely local features do not receive speculative Gateway abstractions.

---

## Current Implementation Status

### Foundation

- Single Activity application entry point.
- Hilt-enabled application setup.
- App-level Navigation Compose infrastructure.
- Dashboard workflow.
- Repository-defined build/dependency policy.
- GitHub Actions validation with executable architecture checks, JVM/build validation, and managed-device Android instrumentation.
- Fail-closed Android release-signing workflow.
- Accepted application-wide design-system theme under `ui/designsystem/theme`.

### Server Inventory

- Add, edit, delete, search, and filtering workflows.
- Server domain and environment models.
- Project-owned Server repository contract.
- Room-backed Server persistence.
- Inventory-backed resolution into the project-owned `core/connection` target contract.
- Automated persistence, mapper, validation, presentation, and Android coverage.

### SSH

- Inventory-backed connection target resolution.
- Host-key observation, explicit review, confirmation, changed-key blocking, and trusted-host persistence.
- Canonical OpenSSH SHA-256 fingerprints for new observation/trust with explicit legacy trust compatibility.
- Ephemeral password authentication.
- Ephemeral Android document-picker private-key authentication.
- Project-owned session handles and lifecycle contracts.
- Cancellation-safe connection ownership handoff and terminal workflow abandonment cleanup.
- Explicit disconnect and reconnection.
- Non-interactive explicit command execution.
- Concurrent bounded stdout/stderr draining with one operation deadline and explicit truncation state.
- Inline Saved Command selection that replaces editable command input without automatic execution.
- Room-backed SSH connection history and recoverable repository observation.

### Saved Commands

- Global `SavedCommand` domain model.
- Project-owned repository and factory contracts.
- Room persistence and migration `4 -> 5`.
- Dashboard/navigation entry.
- Create, edit, delete, loading, empty, content, failure, retry, and cancellation behavior.
- Exact command-text preservation.
- SSH input integration through Saved Commands domain contracts only.

### Local Persistence

- Room database version `5`.
- Explicit migrations `1 -> 2 -> 3 -> 4 -> 5`.
- Persisted server inventory, trusted SSH host keys, SSH connection history, and Saved Commands.

---

## Support Claims

Architecture and user-facing copy distinguish:

- **Architecturally permitted:** the design can accommodate the capability or platform.
- **Implemented:** a concrete workflow/provider/adapter/transport exists.
- **Verified:** automated or runtime evidence confirms documented behavior.

Platform-neutral architecture does not imply universal operating-system, service-manager, transport, vendor, or cloud support.

---

## Application Layers

### UI Layer

Contains Compose screens/components, theme definitions, app-level navigation composition, and UI-specific rendering.

Rules:

- Composables remain declarative.
- Composables receive state and emit events.
- UI must not parse raw provider output into domain meaning.
- Feature navigation actions should use narrow callbacks or feature destination contracts.

### Presentation Layer

Contains ViewModels, immutable UI state, UI events, and presentation-specific mappers/coordination.

Rules:

- Presentation uses project-owned domain contracts/use cases.
- Presentation does not consume Room DAOs/entities, concrete repositories, provider clients, or raw SDK models.
- Secret values, private keys, passphrases, and raw provider errors do not enter observable UI state.
- Simple view-specific validation may remain local; reusable business rules belong in domain-owned components when justified.

### Domain Contracts and Models

Contains feature-owned models, repository/capability contracts, justified use cases, and project-owned results/errors.

Rules:

- Domain is platform-neutral.
- Domain must not depend on Android, AndroidX, Compose, Room, SSHJ, provider SDKs, project data implementations, or project presentation implementations.
- Do not create use cases or generic models solely for theoretical purity.
- Domain language represents application meaning, not transport commands or external payload shape.

### Data Layer

Contains repository implementations, Room DAOs/entities, mapping, local/remote sources, SSHJ adapters, Android platform adapters, and concrete integration code.

Rules:

- Data depends inward on project-owned contracts.
- Room entities and external SDK models do not leak into presentation.
- Data does not depend on presentation.
- Cross-feature data implementation dependencies are prohibited except explicitly named metadata relationships required for application persistence integrity.

---

## Three-Level Remote Capability Architecture

### Level 1 — Core

Core owns platform-neutral capability meaning: contracts, models, normalized results/errors, support states, security/lifecycle policy, and justified use cases.

Core does not own Android/Room/SDK types, shell commands, platform enums, or raw payloads.

### Level 2 — Capability Gateway

A Gateway may resolve target context, consume support evidence, select providers, translate Core requests, orchestrate provider operations, apply policy, and normalize results.

A Gateway is introduced only when a concrete capability requires it.

Saved Commands management, local favorites/tags, local filtering, and Room-backed history do not require a Gateway.

### Level 3 — Providers and Adapters

Providers/Adapters own transports, platform behavior, service/vendor APIs, command construction, output parsing, third-party clients, and provider-level failure mapping.

Named technologies are examples of possible boundaries only unless separately accepted for implementation.

---

## Capability Support States

Gateway-backed features use explicit project-owned states:

- **Supported**;
- **Unsupported**;
- **Unknown**;
- **Unavailable**.

Unknown is not supported. Unsupported and unknown states must not trigger guessed commands or unsafe fallback behavior. Presentation consumes project-owned states; provider parsing stays in provider boundaries.

---

## Dependency Direction

Allowed directions:

```text
UI -> Presentation
Presentation -> owning Domain contracts / Use cases
Presentation -> explicitly accepted foreign-feature Domain contracts
Data -> Domain contracts
Gateway -> Core contracts
Provider / Adapter -> narrow provider contracts and external libraries
Feature DI -> contracts and concrete implementations
App navigation -> feature destinations / route entry points
```

Current accepted cross-feature domain integration:

```text
SSH presentation -> Saved Commands domain
```

Narrow Room aggregation exception:

```text
core/database -> feature-owned Room entities and DAOs
```

This exists because Room requires central schema aggregation. It does not transfer feature business ownership to `core/database`.

Forbidden directions include:

```text
Domain -> Data
Domain -> Presentation
Domain -> Android / AndroidX / Room / SSHJ
Data -> Presentation
Presentation -> DAO / Room entity / concrete repository
Feature A data -> Feature B data
Feature A presentation -> Feature B presentation
```

### Named Composition and Persistence Exceptions

The current single-module topology has three narrowly named import exceptions:

1. `SshScreen.kt -> AndroidSshPrivateKeySourceFactory` at the Android document-picker composition boundary.
2. `SshTrustedHostKeyEntity.kt -> ServerEntity` only for Room foreign-key metadata.
3. `SshConnectionHistoryEntity.kt -> ServerEntity` only for Room foreign-key metadata.

These exceptions do not authorize broader layer crossing. New exceptions require focused review and synchronized validation/documentation changes.

### Executable Architecture Enforcement

Production Kotlin dependency rules are enforced by:

```text
scripts/architecture/check-dependencies.sh
```

The normal `Android Validation` build job executes the checker immediately after checkout, before Gradle build/unit validation. Because managed-device instrumentation depends on that build job and `Validate Android project` is a fail-closed aggregate, an architecture violation cannot produce a successful required gate.

The checker validates production Kotlin imports for:

- Domain platform/data/presentation isolation;
- Presentation implementation isolation and cross-feature boundaries;
- Data-to-presentation prohibition;
- cross-feature data implementation prohibition;
- the exact named exceptions above.

The rule is intentionally source-level and repository-owned. The project remains one `:app` module; a Gradle multi-module migration is not required merely to enforce architectural intent.

Do not add generic allowlists. A new exception must identify the exact ownership need and remain reviewable.

---

## Feature-First Ownership

Current feature boundaries:

- `feature/dashboard`;
- `feature/serverinventory`;
- `feature/ssh`;
- `feature/savedcommands`.

Rules:

- Features own implemented domain, data, navigation, and presentation responsibilities.
- Empty speculative packages are not retained.
- Shared contracts move to `core` only after genuine cross-feature/application-wide ownership is demonstrated.
- Cross-feature interaction uses explicit stable contracts or app-level navigation.
- Do not create speculative `gateway`, `provider`, `platform`, `operations`, or plugin hierarchies.

`core/connection` is the implemented shared connection-target contract boundary. It contains non-sensitive target meaning/resolution contracts and does not own authentication, host trust, session resources, or transport implementations.

---

## Naming Scope

`Server` and `ServerInventory` remain the current implemented inventory concepts.

Platform-neutral product direction does not justify prematurely renaming them to broader abstractions. Broader naming requires concrete implementation evidence and focused review.

---

## Local Persistence

Room is the accepted local structured-persistence technology.

Current database baseline:

```text
Database version: 5
Latest migration: 4 -> 5
Latest exported schema: 5.json
```

Persistence rules:

- DAO access occurs through owned repository implementations.
- Room entities remain separate from domain models.
- Schema changes require explicit migrations unless a separately reviewed decision accepts destructive behavior.
- Migration tests use exported schemas where appropriate.
- Ordinary Room tables must not contain credential secrets.
- Persistent credential storage requires a separately accepted secure-storage implementation.
- Existing Server updates remain non-destructive; explicit Server deletion retains child-cascade behavior.

---

## Navigation

Navigation Compose is the accepted screen-navigation mechanism.

Rules:

- Feature destinations own route arguments and entry contracts.
- Screens receive narrow navigation callbacks where practical.
- Navigation does not occur until security/lifecycle-critical cleanup completes.
- Permanent exit from an active SSH workflow waits for session cleanup.

---

## Dependency Injection

Hilt is the accepted DI framework.

Rules:

- Domain models are not injected.
- Feature-specific bindings remain in the owning feature.
- Application-wide Room aggregation remains in `core/di` and `core/database` as required.
- Gateways/providers are bound only after concrete implementations exist.
- DI does not become a service locator or hide provider-selection policy.

---

## Security Boundaries

- Secrets remain transient and outside observable UI state.
- Private-key material remains one-attempt, bounded, and non-persistent.
- Host trust remains explicit and fail-closed.
- New host fingerprints use canonical OpenSSH SHA-256; historical trust compatibility remains explicit and is never silently rewritten.
- Command execution remains explicit and non-interactive.
- Selecting a Saved Command never executes automatically.
- Unsupported/unknown capabilities do not trigger guessed commands.
- Automatic/background execution requires a separate accepted decision.

---

## Testing and Verification

Behavior-changing work uses the smallest relevant validation set, including as applicable:

- Domain/validation unit tests;
- ViewModel/UI-state tests;
- mapper/adapter tests;
- DAO/repository instrumentation tests;
- Room migration tests;
- Compose instrumentation tests;
- runtime evidence for new support claims;
- architecture dependency validation;
- Kotlin/Android-test compilation, JVM tests, lint, debug app/test assembly;
- managed-device Android instrumentation.

A provider does not create a support claim until its documented target environment has verification evidence.

---

## Evolution Rules

Before adding a gateway-backed capability:

1. Define the platform-neutral user operation.
2. Define Core models/results/support states.
3. Explain why a Gateway is required.
4. Identify the first Provider/Adapter and transport.
5. Define security/lifecycle boundaries.
6. Define unsupported/unknown/unavailable behavior.
7. Define automated/runtime verification.
8. Update ADR or architecture documentation when the decision is significant.

Before adding a local feature, use the existing feature-owned domain/repository/presentation structure and avoid unnecessary capability abstractions.

Any package/dependency exception must be reflected in both the executable checker and living architecture/package documentation.

---

## Current Non-Goals

The current implementation does not include:

- universal multi-platform support;
- interactive terminal UI;
- persistent credential storage;
- background monitoring/execution;
- operating-system/service-manager discovery;
- production Capability Gateway abstractions;
- additional remote transports;
- service-management/monitoring providers;
- Xray, x-ui, Docker, Kubernetes, certificate-authority, cloud-provider, or other named integrations;
- a public plugin system.

---

## Related Documents

- [Product Vision](PRODUCT_VISION.md)
- [Project State](PROJECT_STATE.md)
- [Architecture Atlas](ARCHITECTURE_ATLAS.md)
- [Engineering Handbook](engineering/README.md)
- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Package Structure](../PACKAGE_STRUCTURE.md)
- [Roadmap](ROADMAP.md)
- [Architecture Decision Records](adr/README.md)
- [ADR-017: Scalable Collection UX Contract](adr/ADR-017-scalable-collection-ux-contract.md)
- [Architecture Review Index](../review/INDEX.md)
- [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md)
- [SSH Status](state/SSH_STATUS.md)
