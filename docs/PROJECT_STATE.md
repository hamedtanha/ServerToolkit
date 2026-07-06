# Project State

**Project:** Server Toolkit  
**Version:** 0.4.0-alpha  
**Status:** Active Implementation  
**Last Updated:** 2026-07-06

---

## Purpose

This document is the single source of truth for the current implementation state of the Server Toolkit project.

It summarizes what is implemented, what is intentionally not implemented yet, and what should happen next.

This document must stay short, current, and factual.

Engineering task selection and delivery rules are defined in [Engineering Strategy](ENGINEERING_STRATEGY.md).

---

## Current Phase

The Server Inventory 0.3.0 baseline is accepted.

ADR-009 is accepted as the SSH host trust and authentication input strategy.

Android backup and data extraction are disabled for the alpha release to avoid backing up infrastructure inventory or future SSH trust material before a reviewed restore model exists.

The SSH command/channel lifecycle boundary is accepted, and the user-facing non-interactive SSH command execution workflow is now implemented behind owned SSH sessions while keeping terminal UI, saved command workflows, background monitoring, and persistent credentials out of scope.

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

## Implemented

The following application-level items are implemented:

- Single Activity application entry point.
- Hilt-enabled application setup.
- App-level Navigation Compose infrastructure.
- Dashboard navigation destination.
- Dashboard ViewModel.
- Dashboard UI state.
- Dashboard empty-state screen.
- Dashboard navigation action to Server Inventory.
- Server Inventory navigation destination.
- Server Inventory domain model.
- Server Inventory environment model.
- Server Inventory filter state.
- Server Inventory UI state.
- Server Inventory ViewModel.
- Server Inventory empty screen.
- Server Inventory empty-state action.
- Add Server navigation destination.
- Add Server placeholder screen.
- Server Form UI state shared by Add Server and Edit Server.
- Add Server ViewModel.
- Shared Server Form screen.
- Add Server form fields.
- Add Server validation state.
- Add Server validation-only save action.
- Add Server repository-backed save flow.
- Add Server automatic return after successful save.
- Edit Server navigation destination.
- Edit Server route.
- Edit Server ViewModel.
- Edit Server form reuse through the shared Server Form screen.
- Edit Server repository-backed save flow preserving the existing server id.
- Manual Edit Server verification after app restart.
- Server repository contract.
- In-memory Server repository implementation for development and testing support.
- Room dependency setup with KSP.
- Room schema export location configuration.
- Initial Room schema export.
- Server Toolkit Room database class.
- Server entity.
- Server DAO.
- Server entity/domain mapper.
- Room-backed Server repository implementation.
- Hilt database and DAO providers.
- Server Inventory repository dependency injection binding.
- Server Inventory ViewModel repository observation.
- Basic Server Inventory list rendering.
- Delete server UI action with confirmation dialog.
- Server Inventory ViewModel delete action.
- Manual delete flow verification after app restart.
- Server Inventory search by name, host, username, category, and tags.
- Server Inventory environment filtering.
- Server Inventory favorites-only filtering.
- Server Inventory filter clearing.
- Manual search and filtering verification.
- Automated search and filtering verification through unit tests, instrumented tests, and debug build.
- Automated Edit Server verification through unit tests, instrumented tests, and debug build.
- Automated shared Server Form naming verification through unit tests, instrumented tests, and debug build.
- Server Inventory stabilization checklist.
- Accepted Server Inventory 0.3.0 baseline.
- SSH workflow architecture decision.
- Secure storage strategy decision.
- SSH client library selection decision.
- Accepted SSH host trust and authentication input strategy decision.
- SSHJ dependency declaration through the Gradle version catalog.
- SSH data-layer adapter shell.
- SSHJ-backed connection service shell without real network behavior.
- SSH connection service dependency injection binding.
- Core connection target-resolution contract.
- Server Inventory-backed connection target resolver.
- SSH connection attempt use case.
- SSH connection attempt timeout boundary.
- SSH connection attempt exception mapping and cancellation preservation.
- SSH duplicate-attempt prevention at the ViewModel boundary.
- SSH host key fingerprint model.
- SSH host endpoint trust lookup model.
- SSH observed and trusted host key models.
- SSH host trust status model for unknown, trusted, and changed host keys.
- SSH trusted host storage contract.
- SSH trusted host Room entity and DAO.
- SSH trusted host entity/domain mapper.
- SSH trusted host Room-backed repository implementation.
- SSH trusted host repository dependency injection binding.
- Server Toolkit database version 2 with trusted-host migration.
- SSH host trust evaluator.
- SSH host trust decision model.
- SSH host trust decision use case.
- SSH explicit unknown-host trust confirmation use case.
- SSH changed-host-key blocking decision flow.
- SSH host-key review presentation state.
- SSH host-key review UI mapper.
- SSH host-key review confirmation and cancellation ViewModel events.
- SSH host-key review screen actions.
- SSH authentication method model.
- SSH authentication input UI-safe presentation state.
- SSH ephemeral authentication input ViewModel events.
- SSH authentication input clearing behavior.
- SSH credential-bearing connection request boundary.
- SSH authentication input redaction and clearing at the connection attempt boundary.
- SSH host-key observation service boundary.
- SSH connection attempt outcome model for connection results and host-trust decisions.
- SSH connection attempt host-trust gating before connection service execution.
- SSHJ host-key observation adapter mapping.
- SSH host-key fingerprints use SHA256 values for observation and trusted verification.
- SSH project-owned session handle model.
- SSH session close result model.
- SSH session lifecycle service contract.
- SSHJ session lifecycle shell.
- SSHJ session owner registry boundary.
- SSH session close cancellation preservation.
- SSHJ authentication adapter mapping.
- SSHJ authentication executor boundary.
- SSH password authentication cancellation preservation.
- SSHJ trusted host-key verifier boundary.
- SSHJ trusted connection execution shell.
- SSHJ password authentication execution shell.
- SSHJ session ownership execution shell.
- SSH command channel execution strategy decision.
- SSH credential ownership and secure storage strategy decision.
- SSH command execution planning boundary.
- SSH command execution result model.
- SSHJ command channel planning shell.
- SSH command execution routed through the data-layer session owner registry.
- SSHJ command channel lifecycle executor.
- SSH command execution service contract.
- SSHJ-backed command execution service.
- SSH command execution service dependency injection binding.
- SSH command execution use case.
- SSH command execution presentation state and UI mapper.
- SSH ViewModel command execution wiring through an active project-owned session handle.
- SSH command input and Run command UI controls.
- SSH command output rendering for stdout, stderr, and exit status.
- SSH command output clearing when the active session becomes unavailable.
- SSH stale command result suppression after session invalidation.
- SSH stale command cancellation suppression after session invalidation.
- SSH command channel cancellation preservation with command-channel cleanup.
- SSH ViewModel dependency injection for the connection attempt use case.
- SSH user-triggered connect event shell.
- SSH placeholder Connect button.
- SSH connect event shell ViewModel tests.
- SSHJ adapter shell unit test.
- SSH domain connection request model.
- SSH domain connection result model.
- SSH domain connection error model.
- SSH domain connection service contract.
- SSH domain model unit tests.
- Test-only fake SSH connection service.
- SSH ViewModel result handling seam for tests.
- SSH ViewModel fake result unit tests.
- SSH UI connection status model.
- SSH UI state alignment with domain connection result.
- SSH connection result UI mapper.
- SSH presentation state unit tests.
- SSH navigation destination.
- SSH placeholder screen.
- SSH placeholder ViewModel and UI state.
- Server Inventory Connect action to open the SSH placeholder.
- Manual SSH placeholder route verification.
- Automated SSH placeholder verification through unit tests and debug build.
- Server Inventory filter matcher unit tests.
- DAO instrumentation tests for insert, replace, and delete behavior.
- Room-backed repository instrumentation tests for save, replace, and delete behavior.
- Server entity/domain mapper unit tests.
- Local persistence with Room architecture decision.
- Package structure cleanup from `feature/servers` to `feature/serverinventory`.
- Shared Add/Edit server form naming cleanup.
- App-level package structure alignment with the current source tree.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Interactive terminal workflow for owned sessions.
- Full SSH host key verification hardening beyond the current trusted verifier shell.
- Persistent credential storage implementation.
- Monitoring workflow.
- Saved command workflow.
- Xray or x-ui management workflow.
- Room migrations beyond database version 2.
- Migration tests beyond the trusted-host v1-to-v2 migration.

---

## In Progress

The current implementation area is:

- SSH command execution workflow verification and stabilization.
- SSH connection, session lifecycle, and command execution timeout, cleanup, cancellation, and failure-mapping behavior hardening.

---

## Next Planned Work

The next safe development steps are:

1. Continue hardening timeout, cleanup, cancellation, and failure-mapping behavior around SSH connection, session lifecycle, and command execution.
2. Keep terminal UI, saved command history, background monitoring, and persistent credentials out of scope.
3. Consider saved command workflows only after a separate reviewed design and documentation update.

---

## Current Git Workflow Context

Main branch must remain releasable.

Implementation and documentation changes must happen on short-lived GitHub Flow branches and be merged through pull requests.

---

## Current Engineering Rule

Do not expand SSH command execution into terminal UI, saved commands, background monitoring, or persistent credentials without a separate reviewed design and documentation update.
