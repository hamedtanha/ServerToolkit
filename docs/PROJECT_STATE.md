# Project State

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Released
**Last Updated:** 2026-07-15

---

## Purpose

This document is the primary entry point for the current implementation state of the Server Toolkit project.

It summarizes the current phase, implemented capability areas, active guardrails, intentionally excluded scope, and next planned work.

Detailed feature and engineering baseline status is maintained in the linked state documents.

Engineering task selection and delivery rules are defined in [Engineering Strategy](ENGINEERING_STRATEGY.md).

---

## Current Phase

The Server Inventory 0.3.0 baseline is accepted.

Version 0.4.0 is released and establishes the accepted reliable SSH connection baseline.

The current SSH implementation supports real ephemeral password-based and private-key SSH connections, user-facing non-interactive command execution behind project-owned SSH session handles, deterministic workflow-exit cleanup, and explicit user-requested disconnection with reconnection support.

The ADR-013 ephemeral private-key workflow is implemented end to end. Private-key documents are selected through the Android system picker, converted immediately into project-owned one-shot sources, read within the accepted size boundary, parsed in memory inside the SSH data layer, and consumed for one authentication attempt without temporary private-key files or persistent credential storage. Encrypted OpenSSH v1 metadata is preflight-validated before SSHJ parsing, with bcrypt KDF work limited to `64` rounds.

Automated JVM coverage, Android runtime verification, and Android benchmark evidence confirm support for encrypted and unencrypted OpenSSH v1 Ed25519 and RSA keys. Tested PKCS#8 RSA keys map to a stable unsupported-format outcome. Parser, passphrase, source-lifecycle, cleanup, and server-rejection failures map to project-owned errors, while coroutine cancellation is preserved.

The repository includes a fail-closed local post-build Android APK signing workflow. Version 0.4.0 was officially built, signed, and independently verified from exact tagged commit `7be930f27cc19ea849d6ce720fb05fce074f3320`. The published APK SHA-256 is `5BA5187999AA93677802DCCC2D318A9AE098D67C551399FBBA9CD9F563151273`, and the accepted release certificate SHA-256 is `8EECDB2A84052ABCA92848B8E717A136C33F4A3D1CB85EE2AA77C4F3ED9424FC`. Android Validation run `29358724293` completed successfully for the same commit. Tag `v0.4.0`, the GitHub Release, and all three published assets were verified byte-for-byte against the local official outputs. Android NDK `28.2.13676358` remains pinned, native-library stripping succeeds with the matching `llvm-strip`, and signing validation fails closed when the NDK toolchain is missing or inconsistent.

The project defines a foundational build-toolchain and dependency update policy. The currently implemented Java, Gradle, Android, Kotlin, dependency, CI, and release-toolchain baseline is recorded in a focused living status document under `docs/state/`.

The first version 0.5.0-alpha Operations increment is accepted as the Saved Command Foundation. Slice 1 implements the global saved-command domain model, project-owned repository contract, Room persistence, Hilt bindings, database migration from version `4` to version `5`, exported schema `5`, and focused domain, mapper, DAO, repository, and migration coverage. No user-facing management UI or SSH saved-command selection is implemented yet.

Persistent credentials, terminal UI, background monitoring, saved-command automatic execution, and Xray or x-ui management remain intentionally out of scope.

---

## Current Capability Summary

| Area | Status | Detail |
|---|---|---|
| Foundation | Implemented | Single Activity, Hilt setup, Navigation Compose, Dashboard, Room setup, and baseline Android architecture are in place. |
| Server Inventory | Accepted baseline | See [Server Inventory Status](state/SERVER_INVENTORY_STATUS.md). |
| SSH | Completed milestone | See [SSH Status](state/SSH_STATUS.md). |
| Saved Commands | Persistence foundation | Global domain and Room persistence are implemented; management UI and SSH integration remain planned. See [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md). |
| Documentation Governance | Active | Source-of-truth ordering, version metadata rules, changelog usage, and ADR documentation boundaries are documented. |
| Build Toolchain Governance | Active | Update triggers, risk classification, compatibility clusters, validation, release interaction, and ADR boundaries are defined. |
| Build Toolchain Baseline | Current | See [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md). |
| Android Version Metadata | Current | Android `versionName` remains synchronized with the released project milestone. |
| Continuous Integration | Implemented | GitHub Actions validates Kotlin compilation, Android test compilation, unit tests, lint, and debug builds for pull requests and `main`. |
| Android Release Signing | Implemented and published | The version 0.4.0 APK, checksum, and release evidence were signed, independently verified, published, and confirmed byte-for-byte against the official local outputs. |

---

## Current SSH Guardrails

The current SSH implementation must continue from the accepted architecture on `main`.

- SSH username ownership belongs to Server Inventory and inventory-backed connection target resolution.
- SSH authentication input state must not own a separate username value.
- SSH authentication input state may expose only the selected authentication method and secret presence flags.
- Persistent credential metadata and persistent secret storage are not implemented.
- Private-key documents, loaded key material, and passphrases remain one-attempt and non-persistent.
- Credential persistence requires a separate reviewed implementation slice with a secure storage boundary.
- SSH command execution remains non-interactive and must continue to use project-owned session handles.
- Terminal UI, saved-command selection, automatic command execution, background monitoring, and persistent credentials remain outside the current SSH implementation.

---

## Current Saved Command Guardrails

The current implementation is documented in [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md).

- Saved commands are global and are not assigned to individual servers.
- Saved commands contain operational text, not credentials or secure secret storage.
- Command text must be preserved exactly and must not be parsed, rewritten, or executed by persistence code.
- Duplicate identifiers fail closed instead of replacing an existing command silently.
- User-facing selection must never execute a command automatically.
- Management UI and SSH integration must be implemented as later independent slices.

---

## Current Build Toolchain Guardrails

The current implemented baseline is documented in [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md), and all updates must follow [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md).

- Java source compatibility, Java target compatibility, Kotlin JVM toolchain, and CI JDK remain aligned on Java 17.
- Gradle, Android Gradle Plugin, Kotlin, and KSP must be reviewed as a compatibility cluster.
- Android Build Tools and NDK declarations used by the release workflow must remain synchronized with release scripts and documentation.
- The pinned NDK must not be removed while release verification depends on its matching `llvm-strip`.
- Toolchain and dependency updates must remain independently reviewable from Operations feature implementation.
- Proposed versions must not be documented as current before they are implemented and merged.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Interactive terminal workflow for owned sessions.
- Additional SSH host key verification hardening, if future runtime testing identifies gaps.
- Persistent credential storage implementation.
- Monitoring workflow.
- Saved Commands management UI and navigation.
- Saved-command editing, categories, favorites, quick actions, templates, server assignment, import, export, synchronization, and backup behavior.
- SSH saved-command selection and command-input population.
- Automatic or background command execution.
- Xray or x-ui management workflow.
- Room migrations beyond database version 5.
- Migration tests beyond the trusted-host v1-to-v2, trusted-host v2-to-v3, connection-history v3-to-v4, and saved-command v4-to-v5 migrations.
- Automated dependency-update governance.
- An accepted Java, Gradle, Android Gradle Plugin, Kotlin, SDK, Build Tools, or NDK upgrade beyond the current baseline.

---

## Completed Milestone

The completed version 0.4.0 SSH milestone includes:

- SSH 0.4.0 ephemeral private-key authentication is implemented and runtime-verified against ADR-013, including the supported OpenSSH format matrix and stable unsupported PKCS#8 outcomes.
- Connection history domain, Room persistence, automatic recording, and per-server presentation are complete and runtime-verified.
- Active SSH sessions are closed deterministically before permanent workflow exit, with navigation deferred until cleanup completes.
- Connected users can explicitly disconnect while remaining on the SSH screen and reconnect after successful cleanup.
- Shared session-close behavior is covered by focused lifecycle and presentation tests and has been manually runtime-verified.
- Engineering review finding P8 is resolved.
- Version 0.4.0 candidate verification completed from exact `main` commit `250d46649834ef0f88dd6c3c330aacf137d44ab5` with APK SHA-256 `906A87D7645F7E4035F6A96AD2DB395A4A7600D8B487BC39CF80D30C92C7EB04`.
- Release-toolchain hardening pins Android NDK `28.2.13676358`, verifies `llvm-strip`, and removes the deprecated Android test-assets source-set API.
- Version 0.4.0 was published from tagged commit `7be930f27cc19ea849d6ce720fb05fce074f3320` with APK SHA-256 `5BA5187999AA93677802DCCC2D318A9AE098D67C551399FBBA9CD9F563151273`.
- The published APK, checksum, and release-evidence assets were downloaded and verified byte-for-byte against the official local outputs.

---

## Active Operations Increment

The accepted Saved Command Foundation currently includes:

- GitHub planning Issue `#122` and focused Slice 1 Issue `#123`.
- Global saved-command ownership with server-specific assignment deferred.
- Explicit execution safety: selection may populate input later but must never execute automatically.
- Project-owned domain and repository boundaries.
- Room entity, DAO, mapper, repository implementation, and Hilt bindings.
- Database version `5`, migration `4 → 5`, and exported schema `5`.
- Focused automated coverage for domain boundaries, exact text preservation, persistence behavior, and migration validation.

---

## Next Planned Work

The next safe development steps are:

1. Preserve tag `v0.4.0` and its published assets as the immutable released SSH baseline.
2. Complete review and merge of the Saved Command persistence foundation while keeping Android version metadata unchanged.
3. Implement the Saved Commands Management slice with navigation, list states, create workflow, delete confirmation, and restart persistence verification.
4. Implement SSH command-input selection as a separate slice with no automatic execution.
5. Evaluate any proposed build-toolchain or dependency maintenance separately under the accepted update policy and current baseline.
6. Keep terminal UI, background monitoring, persistent credentials, and Xray or x-ui management outside the current increment unless a future milestone explicitly accepts them.

---

## Current Git Workflow Context

Main branch must remain releasable.

Implementation and documentation changes must happen on short-lived GitHub Flow branches and be merged through pull requests.

The Android validation workflow runs for pull requests targeting `main` and for pushes to `main`. Failed validation must be resolved before merge.

---

## Current Engineering Rule

Before starting implementation work, verify the current repository state and read the relevant project documents.

Repository documentation is the source of truth over older uploaded snapshots, previous chat context, assistant memory, or earlier recommendations.

---

## Detailed State Documents

- [Server Inventory Status](state/SERVER_INVENTORY_STATUS.md)
- [SSH Status](state/SSH_STATUS.md)
- [Saved Commands Status](state/SAVED_COMMANDS_STATUS.md)
- [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md)

---

## Related Documents

- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Architecture](ARCHITECTURE.md)
- [Roadmap](ROADMAP.md)
- [Changelog](CHANGELOG.md)
- [Documentation Governance](DOCUMENTATION.md)

---
