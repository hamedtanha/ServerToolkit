# Project State

**Project:** Server Toolkit
**Version:** 0.4.0-alpha
**Status:** Active Implementation
**Last Updated:** 2026-07-09

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

The current SSH implementation supports real ephemeral password-based SSH connections and user-facing non-interactive command execution behind project-owned SSH session handles.

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

---

## Current SSH Guardrails

The current SSH implementation must continue from the accepted architecture on `main`.

- SSH username ownership belongs to Server Inventory and inventory-backed connection target resolution.
- SSH authentication input state must not own a separate username value.
- SSH authentication input state may expose only the selected authentication method and secret presence flags.
- Persistent credential metadata and persistent secret storage are not implemented.
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
- Room migrations beyond database version 3.
- Migration tests beyond the trusted-host v1-to-v2 and v2-to-v3 migrations.

---

## In Progress

The current implementation area is:

- SSH 0.4.0-alpha runtime verification and evidence-driven stabilization.
- Reviewed planning for the next SSH implementation slice.

---

## Next Planned Work

The next safe development steps are:

1. Add more SSH hardening only when current repository inspection or runtime testing identifies a concrete gap.
2. Select the next SSH implementation slice through a separate reviewed design and documentation update.
3. Keep terminal UI, saved command history, background monitoring, and persistent credentials out of scope.
4. Consider saved command workflows only after a separate reviewed design and documentation update.

---

## Current Git Workflow Context

Main branch must remain releasable.

Implementation and documentation changes must happen on short-lived GitHub Flow branches and be merged through pull requests.

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
