# Project State

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Released
**Last Updated:** 2026-07-16

---

## Purpose

This document is the primary entry point for the current implementation state of the Server Toolkit project.

It summarizes the current phase, implemented capability areas, active guardrails, intentionally excluded scope, accepted architecture direction, and next planned work.

Detailed feature and engineering baseline status is maintained in the linked state documents.

Engineering task selection and delivery rules are defined in [Engineering Strategy](ENGINEERING_STRATEGY.md).

---

## Current Phase

Version `0.4.0` is released and establishes the accepted SSH baseline.

The first version `0.5.0-alpha — Operations` increment is accepted as the Saved Command Foundation.

Slice 1 is merged and implements:

- Global Saved Command domain model.
- Project-owned repository contract.
- Room persistence.
- Hilt bindings.
- Database migration `4 → 5`.
- Exported schema `5`.
- Domain, mapper, DAO, repository, and migration coverage.

No user-facing Saved Commands management UI or SSH Saved Command selection is implemented yet.

The project has now accepted a platform-neutral product direction and a three-level remote capability architecture through ADR-015 and ADR-016.

These decisions change product and architecture guidance only. They do not implement support for additional operating systems, transports, service managers, or infrastructure integrations.

---

## Product Direction

Server Toolkit is a platform-neutral remote systems operations application.

The product core must not depend on one operating system, distribution, shell, service manager, package manager, transport, vendor, cloud provider, or infrastructure service.

Support claims distinguish:

1. Architecturally permitted.
2. Implemented.
3. Verified.

The current verified remote-access capability is SSH through the documented SSH workflows and tested OpenSSH-compatible environments.

The repository does not claim universal Linux, Windows, BSD, appliance, cloud-provider, or service-manager support.

---

## Current Capability Summary

| Area | Status | Detail |
|---|---|---|
| Foundation | Implemented | Single Activity, Hilt, Navigation Compose, Dashboard, Room, CI, and baseline Android architecture are in place. |
| Product Direction | Accepted | ADR-015 defines platform-neutral product direction and evidence-based support claims. |
| Remote Capability Architecture | Accepted | ADR-016 defines Core, Capability Gateway, and Provider/Adapter responsibilities without adding production abstractions. |
| Server Inventory | Accepted baseline | See [Server Inventory Status](state/SERVER_INVENTORY_STATUS.md). |
| SSH | Completed milestone | See [SSH Status](state/SSH_STATUS.md). |
| Saved Commands | Persistence foundation | Global domain and Room persistence are implemented; management UI and SSH integration remain planned. See [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md). |
| Local Persistence | Current | Room database version `5`, explicit migrations through `4 → 5`, and exported schema `5`. |
| Documentation Governance | Active | Source-of-truth, version metadata, ADR, changelog, and factual documentation boundaries are defined. |
| Build Toolchain Governance | Active | Update triggers, risk classification, compatibility clusters, validation, release interaction, and ADR boundaries are defined. |
| Continuous Integration | Implemented | Pull requests and `main` validate Kotlin compilation, Android test compilation, unit tests, lint, and debug builds. |
| Android Release Signing | Implemented and published | The version `0.4.0` release artifacts and signing evidence are published and verified. |

---

## Current Architecture Model

Server Toolkit preserves the accepted feature-first Android application architecture:

```text
UI
 ↓
Presentation
 ↓
Domain contracts and models
 ↑
Data implementations
```

Gateway-backed remote capabilities use the accepted three-level model only when translation, routing, discovery, normalization, orchestration, policy enforcement, or external access requires it:

```text
Core capability contract
 ↑
Capability Gateway
 ↑
Provider / Adapter
 ↑
Transport or external system
```

Purely local features do not receive unnecessary Gateway abstractions.

Saved Commands management remains a direct feature-owned presentation, domain, repository, and Room workflow.

---

## Current SSH Guardrails

The current SSH implementation must continue from the accepted architecture on `main`.

- SSH username ownership belongs to Server Inventory and inventory-backed target resolution.
- SSH authentication presentation state does not own a separate username value.
- Secret values remain transient and outside observable UI state.
- Persistent credential metadata and secret storage are not implemented.
- Private-key documents, loaded key material, and passphrases remain one-attempt and non-persistent.
- Credential persistence requires a separate reviewed secure-storage implementation.
- SSH command execution remains explicit, non-interactive, and project-owned.
- Active session cleanup completes before permanent workflow exit.
- Automatic or background command execution remains out of scope.

---

## Current Saved Command Guardrails

The current implementation is documented in [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md).

- Saved Commands are global and are not assigned to individual servers.
- Saved Commands contain operational text, not credentials or secure secret storage.
- Command text is preserved exactly and is not parsed, rewritten, or executed by persistence code.
- Duplicate identifiers fail closed instead of replacing an existing command silently.
- Selection must never execute a command automatically.
- Management UI and SSH integration remain independent later slices.
- Saved Commands management does not require a Capability Gateway.

---

## Remote Capability Guardrails

Future gateway-backed remote capabilities must follow ADR-015 and ADR-016.

- Core models and contracts remain platform-neutral.
- Presentation consumes project-owned states and results.
- Raw provider output, SDK exceptions, and platform enums do not leak into presentation.
- Gateways are introduced only for concrete routing, translation, discovery, normalization, orchestration, or policy needs.
- Providers and Adapters own transports, platform behavior, command construction, output parsing, and third-party clients.
- Capability support states remain explicit: supported, unsupported, unknown, or unavailable.
- Unknown and unsupported states must not trigger guessed commands or unsafe fallback behavior.
- No generic provider registry, plugin framework, base gateway, or empty package hierarchy is accepted yet.

---

## Current Build Toolchain Guardrails

The implemented baseline is documented in [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md), and all updates follow [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md).

- Java source compatibility, Java target compatibility, Kotlin JVM toolchain, and CI JDK remain aligned on Java `17`.
- Gradle, Android Gradle Plugin, Kotlin, and KSP are reviewed as a compatibility cluster.
- Android Build Tools and NDK declarations used by release workflows remain synchronized with scripts and documentation.
- The pinned NDK remains required while release verification depends on its matching `llvm-strip`.
- Toolchain maintenance remains independently reviewable from product feature work.
- Proposed versions are not documented as current before implementation and merge.

---

## Implemented Persistence Baseline

Room database version:

```text
5
```

Implemented migration sequence:

```text
1 -> 2
2 -> 3
3 -> 4
4 -> 5
```

Current persisted areas:

- Server inventory.
- Trusted SSH host keys.
- SSH connection history.
- Saved Commands.

Room tables must not be used for persistent credentials, private keys, passphrases, access tokens, or other secret material without an accepted secure-storage boundary.

---

## Not Implemented Yet

The following items are intentionally not implemented:

- Saved Commands management UI and navigation.
- Saved Command editing, categories, favorites, templates, variables, server assignment, import, export, synchronization, or backup.
- SSH Saved Command selection and command-input population.
- Interactive terminal UI.
- Persistent credential storage.
- Automatic or background command execution.
- Operating-system or service-manager discovery.
- Production Capability Gateway abstractions.
- WinRM or another remote transport.
- Service management, resource monitoring, system-log, or package-management providers.
- Xray, x-ui, Docker, Kubernetes, certificate-authority, cloud-provider, or another named integration.
- A public plugin framework.
- Room migrations beyond database version `5`.
- Accepted toolchain upgrades beyond the current recorded baseline.

---

## Completed Milestone

Version `0.4.0` includes:

- Accepted SSH architecture and security decisions.
- Host trust and trusted-host persistence.
- Ephemeral password and private-key authentication.
- Verified OpenSSH v1 Ed25519 and RSA key support with optional passphrases.
- Project-owned session lifecycle and deterministic cleanup.
- Explicit disconnect and reconnection.
- Non-interactive explicit command execution.
- SSH connection history persistence and presentation.
- Release-toolchain hardening and published signed APK evidence.

The immutable release tag, artifacts, checksums, and release evidence remain unchanged by later development.

---

## Active Operations Increment

The Saved Command Foundation includes:

- Planning Issue `#122`.
- Completed persistence Issue `#123` and merged PR `#126`.
- Global Saved Command ownership.
- Explicit execution safety.
- Project-owned domain and repository boundaries.
- Room entity, DAO, mapper, repository implementation, and Hilt bindings.
- Database version `5`, migration `4 → 5`, and exported schema `5`.
- Focused automated and Android instrumentation coverage.

The next product slice is Saved Commands Management.

---

## Active Architecture Decision Work

Issue `#127` records the accepted platform-neutral direction and three-level remote capability architecture.

The documentation-only implementation includes:

- ADR-015.
- ADR-016.
- Product Vision alignment.
- Architecture and Engineering Strategy alignment.
- Roadmap reframing.
- ADR index updates.
- Correction of stale architecture claims about Room version `4` and unimplemented Saved Commands persistence.

No Android production code or package structure is introduced by this decision work.

---

## Next Planned Work

The next safe development steps are:

1. Review, validate, and merge the documentation-only platform-neutral architecture decision.
2. Keep Android version metadata unchanged at the released `0.4.0` baseline.
3. Create a focused Issue and branch for Saved Commands Management from the updated `main`.
4. Implement Saved Commands navigation, list states, create workflow, delete confirmation, and restart persistence verification.
5. Implement SSH Saved Command input selection as a later independent slice with no automatic execution.
6. Select the first gateway-backed capability only through a separate focused planning decision after the Saved Commands increment or when roadmap priority explicitly changes.
7. Keep named integrations outside the committed core direction until individually accepted.

---

## Current Git Workflow Context

- `main` must remain releasable.
- Implementation and documentation changes use short-lived GitHub Flow branches.
- Changes merge through pull requests.
- Android validation runs for pull requests targeting `main` and pushes to `main`.
- Failed validation must be resolved before merge.

---

## Current Engineering Rule

Before starting implementation work:

1. Verify the current repository state.
2. Read the relevant current documents and ADRs.
3. Classify the feature as Core, platform capability, or optional integration.
4. Determine whether a Capability Gateway is actually required.
5. Define validation and factual support claims.

Repository documentation is the source of truth over older uploaded snapshots, previous chat context, assistant memory, or earlier recommendations.

---

## Detailed State Documents

- [Server Inventory Status](state/SERVER_INVENTORY_STATUS.md)
- [SSH Status](state/SSH_STATUS.md)
- [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md)
- [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md)

---

## Related Documents

- [Product Vision](PRODUCT_VISION.md)
- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Architecture](ARCHITECTURE.md)
- [Roadmap](ROADMAP.md)
- [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Documentation Governance](DOCUMENTATION.md)
- [Architecture Decision Records](adr/README.md)
