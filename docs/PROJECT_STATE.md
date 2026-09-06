# Project State

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Released
**Last Updated:** 2026-09-06

---

## Purpose

This document is the primary entry point for the current implementation state of the Server Toolkit project.

It summarizes the current phase, implemented capability areas, active guardrails, intentionally excluded scope, accepted architecture direction, and next planned work.

Detailed feature and engineering baseline status is maintained in the linked state documents.

Engineering task selection and delivery rules are defined in [Engineering Strategy](ENGINEERING_STRATEGY.md).

---

## Current Phase

Version `0.4.0` is released and establishes the accepted SSH baseline.

The first version `0.5.0-alpha — Operations` increment is the Saved Command Foundation.

Implemented slices now include:

- Global Saved Command domain model.
- Project-owned repository contract.
- Room persistence and Hilt bindings.
- Database migration `4 → 5` and exported schema `5`.
- Saved Commands navigation from the Dashboard.
- Loading, empty, content, and failure presentation states.
- Validated create workflow with exact command-text preservation.
- Explicit Saved Command editing with stable identity, creation-time preservation, exact command-text handling, and retryable failure containment.
- Explicit delete confirmation with retryable failure handling.
- Focused domain, persistence, UI-state, and ViewModel coverage.
- Manual persistence verification after application restart on a physical Android device.
- SSH Saved Command selector backed by direct repository-domain observation.
- Exact replacement of the existing editable command input without automatic execution.
- Loading, empty, failure, retry, cancellation, later-failure preservation, and repository-order presentation.
- Focused ViewModel and Compose instrumentation coverage, including five passing targeted emulator tests.

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
| Saved Commands | Management and SSH input workflows implemented | Global domain, Room persistence, Dashboard navigation, list states, validated creation and editing, explicit deletion, restart persistence verification, and exact SSH command-input selection without automatic execution are implemented. See [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md). |
| Local Persistence | Current | Room database version `5`, explicit migrations through `4 → 5`, and exported schema `5`. |
| Documentation Governance | Active | Source-of-truth, Architecture Atlas, engineering-handbook navigation, immutable review history, version metadata, ADR, changelog, and factual documentation boundaries are defined. |
| Build Toolchain Governance | Active | Update triggers, risk classification, compatibility clusters, validation, release interaction, and ADR boundaries are defined. |
| Continuous Integration | Implemented | Pull requests and `main` run build/unit validation plus the complete debug instrumentation suite on managed Android API `36`; the required aggregate `Validate Android project` check fails unless both validation layers succeed. |
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
- Saved Command selection may replace the editable command input exactly but must never trigger execution.
- SSH presentation may depend on the project-owned `SavedCommandRepository` domain contract but not Saved Commands DAO, entity, concrete repository, screen, or ViewModel types.
- Active session cleanup completes before permanent workflow exit.
- Automatic or background command execution remains out of scope.

---

## Current Saved Command Guardrails

The current implementation is documented in [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md).

- Saved Commands are global and are not assigned to individual servers.
- Saved Commands contain operational text, not credentials or secure secret storage.
- Command text is preserved exactly and is not parsed, rewritten, or executed by persistence code.
- Duplicate identifiers fail closed instead of replacing an existing command silently.
- Creating, editing, viewing, and deleting saved commands never execute command text.
- The management workflow uses feature-owned navigation, presentation, domain, repository, and Room boundaries.
- Presentation depends on `SavedCommandRepository`, not DAO, entity, or concrete Room types.
- SSH input integration uses the same project-owned repository contract, preserves exact text, and does not execute automatically.
- Saved Commands management and SSH input integration do not require a Capability Gateway.

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

- Java source compatibility, Java target compatibility, and the Kotlin JVM toolchain remain aligned on Java `17`.
- CI uses Temurin `17` as the launcher JDK; repository-controlled Gradle daemon JVM criteria remain on Java `21`.
- The implemented compatibility cluster is Gradle `9.6.1`, Android Gradle Plugin `9.4.0`, Kotlin `2.4.10`, and KSP `2.3.10`.
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

Existing Server saves use non-destructive Room upsert semantics. Metadata-only and username-only updates preserve SSH trust and connection history; endpoint updates preserve history and do not transfer old-endpoint trust to the new endpoint; explicit Server deletion retains the existing child cascade behavior.

Room tables must not be used for persistent credentials, private keys, passphrases, access tokens, or other secret material without an accepted secure-storage boundary.

---

## Not Implemented Yet

The following items are intentionally not implemented:

- Saved Command categories, favorites, templates, variables, server assignment, import, export, synchronization, or backup.
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
- Toolchain upgrades beyond the currently recorded Gradle `9.6.1`, Android Gradle Plugin `9.4.0`, Kotlin `2.4.10`, and KSP `2.3.10` baseline.

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
- Management workflow tracked by Issue `#129` and PR `#132`.
- Global Saved Command ownership.
- Explicit execution safety.
- Project-owned domain and repository boundaries.
- Room entity, DAO, mapper, repository implementation, and Hilt bindings.
- Database version `5`, migration `4 → 5`, and exported schema `5`.
- Feature-owned navigation and Dashboard entry.
- Repository-observed loading, empty, content, and failure states.
- Validated create workflow with exact command-text preservation.
- Saved Command editing tracked by Issue `#150`, preserving stable identifiers, original creation timestamps, and exact command text with retryable mutation failure handling.
- Explicit delete confirmation with retryable mutation failure handling.
- Focused automated coverage and physical-device restart verification.
- Completed SSH Saved Command Input Integration tracked by Issue `#133` and merged through PR `#134`.
- Inline selector with repository-order observation and stable-identifier selection.
- Exact command-input replacement with continued manual editing.
- Explicit Run-only execution with no connection, authentication, session, history, or automatic-execution side effects.
- Focused ViewModel coverage and five passing targeted Compose tests on the Pixel 9 Android Virtual Device.

The next Operations slice after Saved Command editing has not yet been selected.

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

Issue `#135` establishes the Architecture Atlas, engineering-handbook entry point, and immutable architecture-review structure against evidence baseline `0135faf89b1035fd91c75b37a25ec51bc7c71074`.

The Server Domain and Operational UX Architecture Review `RA-2026.07-v2` is published. Acceptance PR `#139` was squash-merged into `main` as `8070830dfae14f908b9dd128846f66112b36423e`, publication PR `#142` was squash-merged as `0a159394e228bb2847bb3600b59058bff1be1c96`, and governing Issue `#138` is closed as completed.

Current review evidence has:

- assessed stable Server identity, endpoint ownership, trust, history, authentication, session, and operational UX boundaries;
- verified that the former Room `REPLACE` path deleted SSH trust and connection-history children during existing-Server saves;
- handed the bounded persistence defect to Issue `#140`, which now implements the focused non-destructive save correction without a schema migration;
- completed profile-layer, platform-versus-capability, freshness, invalidation, persistence, migration, security, retention, and support-claim assessment;
- produced evidence-backed decision recommendations;
- identified Server identity, endpoint, and evidence-lifecycle topics that remain subject to the current ADR admission gate before any future architecture change;
- identified a bounded platform-neutral Add Server copy correction, now completed in production copy;
- preserved the current roadmap milestone names and sequencing.

The review is `Published` through this metadata-only follow-up. Final substantive head `0af71c70133e8fd27277ef50cf4b801fd0c3a618` passed documentation-integrity validation and GitHub Android Validation run `#88`; accepted head `f569e96245e2a552028d1ec22fb560762adb2e1c` passed run `#89`; the acceptance merge `8070830dfae14f908b9dd128846f66112b36423e` passed main run `#90`.

Its recommendations do not authorize production code, a Room schema change, Server Profile implementation, multiple endpoints, Server Workspace implementation, platform or capability persistence, credential persistence, session-continuity changes, or new support claims.

The published review records that a future visual/UX quality initiative does not require speculative Server Profile, multi-endpoint, workspace, or capability infrastructure before visual work begins. This is an architecture recommendation, not production implementation authorization.


---

## September 2026 Architecture Review Remediation Closure

Published architecture review `RA-2026.09-v1` remains immutable historical evidence.

All findings `F01` through `F10` have been resolved through focused follow-up work.
The final remaining finding was completed through Issue `#161` and PR `#190`.

Final remediation baseline:

`main@aa8f7cd8c39c94fafd5b07585931e57bfbebffc7`

Post-merge Android Validation run `#198` (`34031686707`) completed successfully
on that exact main commit.

This closure records completion of the architecture-review remediation sequence
only. It does not create a release, change Android version metadata, complete
version `0.5.0 - Operations`, or authorize the next product slice.

---

## Next Planned Work

The next safe development steps are:

1. Keep Android version metadata unchanged at the released `0.4.0` baseline.
2. Apply the ADR admission gate to any future Server identity, endpoint, or evidence-lifecycle architecture change; ordinary implementation work does not require a new ADR.
3. Preserve the explicit Run action, editable command input, exact-text replacement, session lifecycle, cleanup, and stale-result guardrails.
4. Select the next Operations slice and the first gateway-backed capability only through separate focused planning decisions.
5. Keep the existing `0.6.0 — Dashboard Evolution` roadmap objective unchanged unless a separate product-planning decision explicitly revises it.
6. Keep named integrations outside the committed core direction until individually accepted.

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
