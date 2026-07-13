# Project State

**Project:** Server Toolkit
**Version:** 0.4.0-alpha
**Status:** Active Implementation
**Last Updated:** 2026-07-12

---

## Purpose

This document is the primary entry point for the current implementation state of the Server Toolkit project.

It summarizes the current phase, implemented capability areas, active guardrails, intentionally excluded scope, and next planned work.

Detailed feature status is maintained in the linked state documents.

Engineering task selection and delivery rules are defined in [Engineering Strategy](ENGINEERING_STRATEGY.md).

---

## Current Phase

The Server Inventory 0.3.0 baseline is accepted.

Version 0.4.0-alpha is focused on SSH.

The current SSH implementation supports real ephemeral password-based and private-key SSH connections, together with user-facing non-interactive command execution behind project-owned SSH session handles.

The ADR-013 ephemeral private-key workflow is implemented end to end. Private-key documents are selected through the Android system picker, converted immediately into project-owned one-shot sources, read within the accepted size boundary, parsed in memory inside the SSH data layer, and consumed for one authentication attempt without temporary private-key files or persistent credential storage. Encrypted OpenSSH v1 metadata is preflight-validated before SSHJ parsing, with bcrypt KDF work limited to `64` rounds.

Automated JVM coverage, Android runtime verification, and Android benchmark evidence confirm support for encrypted and unencrypted OpenSSH v1 Ed25519 and RSA keys. Tested PKCS#8 RSA keys map to a stable unsupported-format outcome. Parser, passphrase, source-lifecycle, cleanup, and server-rejection failures map to project-owned errors, while coroutine cancellation is preserved.

Persistent credentials, terminal UI, saved command workflows, background monitoring, and Xray or x-ui management remain intentionally out of scope.

---

## Current Capability Summary

| Area | Status | Detail |
|---|---|---|
| Foundation | Implemented | Single Activity, Hilt setup, Navigation Compose, Dashboard, Room setup, and baseline Android architecture are in place. |
| Server Inventory | Accepted baseline | See [Server Inventory Status](state/SERVER_INVENTORY_STATUS.md). |
| SSH | Active implementation | See [SSH Status](state/SSH_STATUS.md). |
| Documentation Governance | Active | Source-of-truth ordering, version metadata rules, changelog usage, and ADR documentation boundaries are documented. |
| Android Version Metadata | Current | Android `versionName` is synchronized with the current project milestone. |
| Continuous Integration | Implemented | GitHub Actions validates Kotlin compilation, Android test compilation, unit tests, lint, and debug builds for pull requests and `main`. |

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
- Terminal UI, saved commands, background monitoring, and persistent credentials remain out of scope.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Interactive terminal workflow for owned sessions.
- Additional SSH host key verification hardening, if future runtime testing identifies gaps.
- Persistent credential storage implementation.
- Monitoring workflow.
- Saved command workflow.
- Xray or x-ui management workflow.
- Room migrations beyond database version 4.
- Migration tests beyond the trusted-host v1-to-v2, trusted-host v2-to-v3, and connection-history v3-to-v4 migrations.

---

## In Progress

The current implementation area is:

- SSH 0.4.0-alpha ephemeral private-key authentication is implemented and runtime-verified against ADR-013, including the supported OpenSSH format matrix and stable unsupported PKCS#8 outcomes.
- Connection history domain, Room persistence, automatic recording, and per-server presentation are complete and runtime-verified.
- Active SSH sessions are closed deterministically before permanent workflow exit, with navigation deferred until cleanup completes.
- Workflow-exit cleanup is covered by focused lifecycle and presentation tests and has been manually runtime-verified.
- Engineering review finding P8 is partially resolved; only the explicit user-facing disconnect action remains open.

---

## Next Planned Work

The next safe development steps are:

1. Implement an explicit user-facing disconnect action for connected SSH sessions.
2. Runtime-verify explicit disconnect and reassess the remaining version 0.4.0 SSH milestone.
3. Keep terminal UI, saved command workflows, background monitoring, persistent credentials, and Xray or x-ui management out of scope.

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

---

## Related Documents

- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Architecture](ARCHITECTURE.md)
- [Roadmap](ROADMAP.md)
- [Changelog](CHANGELOG.md)
- [Documentation Governance](DOCUMENTATION.md)

---
