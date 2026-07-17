# Architecture

**Project:** Server Toolkit  
**Version:** 0.4.0
**Status:** Active  
**Last Updated:** 2026-07-17

---

## Purpose

This document defines the practical application architecture for the Server Toolkit Android application.

It translates accepted Architecture Decision Records into implementation rules for project structure, layer responsibilities, dependency direction, data flow, navigation, dependency injection, persistence, remote-capability boundaries, and support claims.

This document describes the current implementation and accepted implementation direction. It must not advertise planned functionality as completed functionality.

---

## Product and Architecture Baseline

Server Toolkit is a platform-neutral remote systems operations application.

The current verified remote-access capability is SSH. The architecture may support additional platforms, transports, and providers over time, but architectural extensibility is not a support claim.

The application follows:

- Kotlin.
- Jetpack Compose.
- Single Activity architecture.
- MVVM.
- Repository Pattern.
- Unidirectional Data Flow.
- Hilt for dependency injection.
- Jetpack Navigation Compose.
- Room for local structured persistence.
- Feature-first ownership where practical.
- Platform-neutral Core concepts.
- Incremental remote-capability gateways and providers only when justified by concrete features.

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

Accepted ADRs are the source of truth for architectural decisions. This document explains how those decisions apply to the current codebase and accepted future implementation direction.

---

## Two Complementary Architecture Views

Server Toolkit uses two complementary architectural views.

### Android Application Architecture

This view governs UI, presentation state, domain contracts, repositories, persistence, navigation, and Android integration.

```text
UI
 ↓
Presentation
 ↓
Domain contracts and models
 ↑
Data implementations
```

### Remote Capability Architecture

This view governs platform-neutral remote operations that require discovery, translation, routing, normalization, orchestration, or provider isolation.

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

The remote-capability view complements the Android application layers. It does not require every feature to use a Gateway.

---

## Current Implementation Status

The current implementation includes:

### Foundation

- Single Activity application entry point.
- Hilt-enabled application setup.
- App-level Navigation Compose infrastructure.
- Dashboard route, screen, ViewModel, UI state, and navigation action.
- Repository-defined build-toolchain and dependency policy.
- GitHub Actions Android validation.
- Fail-closed Android release signing workflow.

### Server Inventory

- Server Inventory route, screen, ViewModel, UI state, and filter state.
- Add Server and Edit Server workflows using the shared Server Form screen.
- Delete confirmation, search, and filtering behavior.
- Server domain and environment models.
- Project-owned Server repository contract.
- Room-backed Server persistence.
- In-memory repository retained for development and testing support.
- DAO, repository, mapper, validation, filtering, and presentation coverage.

### SSH

- Inventory-backed connection target resolution.
- Host-key observation, review, confirmation, blocking, and trusted-host persistence.
- Ephemeral password authentication.
- Android document-picker private-key selection.
- Project-owned one-shot private-key source and bounded in-memory parsing.
- Verified encrypted and unencrypted OpenSSH v1 Ed25519 and RSA authentication.
- Project-owned session handles and lifecycle contracts.
- Deterministic cleanup before permanent workflow exit.
- Explicit disconnect and reconnection support.
- Non-interactive command execution through explicit user action.
- Inline Saved Command selector backed by direct `SavedCommandRepository` domain-contract observation.
- Exact replacement of the editable multiline command input without automatic execution.
- stdout, stderr, and exit-status presentation.
- Room-backed SSH connection history and per-server presentation.
- Focused domain, presentation, adapter, lifecycle, persistence, migration, and runtime verification.

### Saved Commands

- Global `SavedCommand` domain model.
- Project-owned `SavedCommandRepository` contract.
- Minimal feature-owned `SavedCommandFactory` contract with concrete UUID and timestamp generation in the data layer.
- Room entity, DAO, mapper, repository implementation, and Hilt bindings.
- Feature-owned navigation destination registered through the app-level Navigation Compose boundary.
- Dashboard entry that preserves Server Inventory navigation.
- Repository-observed loading, empty, content, and failure states.
- Validated create workflow with exact command-text preservation and duplicate-submission prevention.
- Stable-identifier deletion with explicit confirmation, retryable failure handling, and duplicate-confirmation prevention.
- Domain, mapper, DAO, repository, migration, UI-state, and ViewModel coverage.
- Physical-device verification of persistence across restart and confirmed deletion across a second restart.
- SSH presentation integration through the project-owned `SavedCommandRepository` domain contract.
- Repository-order selector presentation with loading, empty, failure, retry, cancellation, and later-failure preservation.
- Stable-identifier selection and exact command-input replacement.
- No automatic execution from Saved Commands; the existing explicit Run action remains the only execution trigger.
- No dependency from SSH presentation on Saved Commands DAO, entity, concrete repository, screen, or ViewModel types.

### Local Persistence

- Room database version `5`.
- Explicit migrations from version `1` through version `5`.
- Exported Room schemas through version `5`.
- Persisted server inventory, trusted SSH host keys, SSH connection history, and Saved Commands.

---

## Support Claims

Architecture documentation and user-facing copy must distinguish:

- **Architecturally permitted:** the design can accommodate the platform or capability.
- **Implemented:** a concrete workflow, provider, adapter, or transport exists.
- **Verified:** automated or runtime evidence confirms documented behavior.

The current architecture is platform-neutral, but the repository does not claim universal operating-system, service-manager, transport, or vendor support.

---

## Application Layers

### UI Layer

Contains:

- Compose screens.
- Reusable Compose components when reuse is real.
- Theme definitions.
- Navigation host integration.
- UI-specific rendering.

Rules:

- Composables remain declarative.
- Composables receive state and emit events.
- Composables must not access repositories, DAOs, providers, transports, or third-party clients directly.
- UI must not parse raw command output or provider payloads to infer domain meaning.
- Navigation actions should be passed as narrow callbacks where practical.

### Presentation Layer

Contains:

- ViewModels.
- Immutable UI state.
- UI events.
- Screen-specific mappers and coordination.

Rules:

- ViewModels call project-owned repositories, use cases, or capability contracts.
- ViewModels must not depend on Room DAOs or concrete providers.
- ViewModels must not expose credentials, private keys, passphrases, raw SDK errors, or provider-specific models in observable state.
- Simple presentation validation may remain local.
- Reusable business rules belong in domain-owned components when justified.

### Domain Contracts and Models

Contains:

- Feature-owned domain models.
- Repository contracts.
- Capability contracts.
- Use cases when logic is complex, reusable, security-sensitive, or orchestration-heavy.
- Project-owned results and errors.

Rules:

- Domain types must not depend on Android, Compose, Room, SSHJ, provider SDKs, or concrete data implementations.
- Do not create use cases only for theoretical purity.
- Do not introduce broad models such as `Device`, `Resource`, or generic `Operation` until implemented behavior requires them.
- Domain language must represent application meaning rather than command syntax or external payload structure.

### Data Layer

Contains:

- Repository implementations.
- Local and remote data sources.
- Room DAOs and entities.
- Entity/domain mapping.
- SSHJ and Android platform adapters.
- Concrete persistence and integration code.

Rules:

- Data implementations depend on project-owned domain contracts.
- Room entities and SDK models must not leak into presentation.
- Mapping across persistence or external integration boundaries remains explicit.
- Repositories coordinate owned data access; they are not generic orchestration containers for unrelated capabilities.

---

## Three-Level Remote Capability Architecture

The following levels apply only when a concrete remote capability requires translation, provider routing, discovery, normalization, orchestration, or external integration.

### Level 1 — Core

Core owns platform-neutral meaning:

- Capability contracts.
- Domain models.
- Normalized results and errors.
- Support states.
- Security and lifecycle policy independent from a provider.
- Use cases when justified.

Core must not import:

- Android or Compose types.
- Room types.
- SSHJ, WinRM, HTTP client, cloud SDK, or vendor SDK types.
- Shell commands and output formats.
- Platform-specific enums or response models.

### Level 2 — Capability Gateway

A Gateway may:

- Resolve target context.
- Determine or consume capability support information.
- Select an appropriate provider.
- Translate a Core request.
- Orchestrate required provider operations.
- Apply safety and lifecycle policy.
- Normalize provider results and errors.

A Gateway is introduced only when a concrete capability requires it.

The following do not currently require a Gateway:

- Saved Commands management.
- Local favorites or tags.
- Local search and filtering.
- Room-backed local history.

### Level 3 — Providers and Adapters

Providers and Adapters own:

- Transport implementations.
- Operating-system and service-manager behavior.
- Service- or vendor-specific APIs.
- Command construction.
- Output and payload parsing.
- Third-party clients and SDKs.
- Mapping raw failures to provider-level results.

Examples such as systemd, OpenRC, Windows Service Control Manager, Docker, Kubernetes, or cloud providers describe possible boundaries only. They are not accepted implementation scope.

---

## Capability Support States

Gateway-backed features use explicit project-owned support states:

- **Supported:** an accepted implementation is available and support has been confirmed.
- **Unsupported:** the target has been evaluated and lacks the capability through an accepted implementation.
- **Unknown:** support has not been determined or evidence is insufficient.
- **Unavailable:** the capability may exist but cannot currently be used due to permissions, connectivity, configuration, or dependency availability.

Rules:

- Unknown must not be treated as supported.
- Unsupported must not trigger guessed commands or unsafe fallbacks.
- UI consumes project-owned support state.
- Provider output parsing remains inside the provider boundary.

---

## Dependency Direction

Allowed directions:

```text
UI -> Presentation
Presentation -> Domain contracts / Use cases
Data -> Domain contracts
Gateway -> Core contracts
Provider / Adapter -> narrow provider contracts and external libraries
Feature DI -> contracts and concrete implementations
App navigation -> feature navigation entry points
```

Narrow Room aggregation exception:

```text
core/database -> feature-owned Room entities and DAOs
```

This exception exists because Room requires central schema aggregation. It does not transfer business ownership to `core/database`.

Forbidden directions:

```text
Domain -> Data
Domain -> Android framework
Core capability contract -> Gateway
Core capability contract -> Provider
Presentation -> DAO
Presentation -> concrete provider
Presentation -> raw external payload
Feature A data -> Feature B data
Feature A presentation -> Feature B presentation
```

---

## Feature-First Ownership

The project remains feature-first where practical.

Current feature boundaries:

- `feature/dashboard`
- `feature/serverinventory`
- `feature/ssh`
- `feature/savedcommands`

Rules:

- A feature owns its domain, data, navigation, and presentation responsibilities.
- Packages exist only when implemented classes require them.
- Do not create speculative `gateway`, `provider`, `platform`, or `operations` packages.
- Shared contracts move to `core` only after genuine cross-feature reuse or application-wide ownership is demonstrated.
- Cross-feature interaction uses explicit contracts or app-level navigation.

---

## Naming Scope

`Server` and `ServerInventory` remain the current implemented inventory concepts.

The platform-neutral product direction does not justify renaming the model to `Device`, `RemoteSystem`, `ManagedTarget`, or another broader abstraction before non-server asset behavior exists.

Broader naming requires a concrete implementation need and a focused architecture review.

---

## Local Persistence

Room is the accepted local structured-persistence technology.

Current persisted areas:

- Server inventory.
- Trusted SSH host keys.
- SSH connection history.
- Saved Commands.

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
- Credentials, private keys, passphrases, access tokens, and other secrets must not be stored in ordinary Room tables.
- Persistent credential storage requires a separate accepted secure-storage implementation.

---

## Navigation

Navigation Compose is the accepted screen-navigation mechanism.

Rules:

- Feature destinations own route arguments and entry contracts.
- Screens receive navigation callbacks rather than navigating through global state where practical.
- Navigation must not occur until security- or lifecycle-critical cleanup completes.
- Permanent exit from an active SSH workflow continues to wait for session cleanup.

---

## Dependency Injection

Hilt is the accepted dependency-injection framework.

Rules:

- Domain models are not injected.
- Feature-specific bindings remain in the owning feature.
- Application-wide Room aggregation remains in `core/di` and `core/database` as required.
- Gateways and providers are bound only after concrete implementations exist.
- Dependency injection must not become a service locator or hide provider selection policy.

---

## Security Boundaries

- Secret values remain transient and outside observable UI state.
- Private-key material remains one-attempt, bounded, and non-persistent.
- Host trust remains explicit and fail-closed.
- Command execution remains explicit and non-interactive.
- Selecting a Saved Command must never execute it automatically.
- Unsupported or unknown capabilities must not fall back to guessed commands.
- Automatic or background execution requires a separate accepted decision.
- Optional integrations require focused security and lifecycle review.

---

## Testing and Verification

Every behavior-changing slice must use the smallest relevant validation set.

Expected coverage may include:

- Domain and validation unit tests.
- ViewModel and UI-state tests.
- Mapper tests.
- DAO and repository instrumentation tests.
- Room migration tests.
- Provider parser and mapping tests.
- Gateway routing and support-state tests.
- Runtime verification for newly claimed platform support.
- Kotlin compilation, Android test compilation, unit tests, lint, and debug builds.

A new provider must not create a support claim until its documented environment has verification evidence.

---

## Evolution Rules

Before adding a gateway-backed capability:

1. Define the platform-neutral user operation.
2. Define Core models, results, and support states.
3. Explain why a Gateway is required.
4. Identify the first Provider or Adapter and transport.
5. Define security and lifecycle boundaries.
6. Define unsupported, unknown, and unavailable behavior.
7. Define automated and runtime verification.
8. Update ADR or architecture documentation when the decision is significant.

Before adding a purely local feature, use the existing feature-owned domain/repository/presentation structure and avoid unnecessary capability abstractions.

---

## Current Non-Goals

The current implementation does not include:

- Universal multi-platform support.
- Interactive terminal UI.
- Persistent credential storage.
- Background monitoring or execution.
- Operating-system discovery.
- Capability Gateway production abstractions.
- WinRM or additional transports.
- Service management or monitoring providers.
- Xray, x-ui, Docker, Kubernetes, certificate-authority, cloud-provider, or other named integrations.
- A public plugin system.

---

## Related Documents

- [Product Vision](PRODUCT_VISION.md)
- [Project State](PROJECT_STATE.md)
- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Package Structure](../PACKAGE_STRUCTURE.md)
- [Roadmap](ROADMAP.md)
- [Architecture Decision Records](adr/README.md)
- [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md)
- [SSH Status](state/SSH_STATUS.md)
