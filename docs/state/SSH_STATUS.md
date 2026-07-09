# SSH Status

**Project:** Server Toolkit
**Feature Area:** SSH
**Status:** Active Implementation
**Related Milestone:** Version 0.4.0 — SSH
**Last Updated:** 2026-07-09

---

## Purpose

This document records the detailed implementation status of the SSH feature area.

The high-level project state remains documented in [Project State](../PROJECT_STATE.md).

---

## Current Status

SSH is in active implementation for version 0.4.0-alpha.

The current implementation supports real ephemeral password-based SSH connections and user-facing non-interactive command execution behind project-owned SSH session handles.

Persistent credentials, terminal UI, saved command workflows, background monitoring, and Xray or x-ui management remain intentionally out of scope.

---

## Implemented

### Architecture and Decisions

- SSH workflow architecture decision.
- Secure storage strategy decision.
- SSH client library selection decision.
- Accepted SSH host trust and authentication input strategy decision.
- SSH command channel execution strategy decision.
- SSH credential ownership and secure storage strategy decision.
- Android backup and data extraction policy decision.

### Navigation and Presentation

- SSH navigation destination.
- SSH screen with ephemeral password input, host-key review actions, connection status, and non-interactive command controls.
- SSH ViewModel and UI state for connection attempts, host-key review, authentication input, and command execution.
- Server Inventory Connect action to open the SSH route.
- SSH UI connection status model.
- SSH UI state alignment with domain connection result.
- SSH connection result UI mapper.
- SSH presentation state unit tests.
- SSH user-triggered connect event handling.
- SSH Connect button with ephemeral password input.

### Target Resolution and Connection Boundary

- Core connection target-resolution contract.
- Server Inventory-backed connection target resolver.
- SSH domain connection request model.
- SSH domain connection result model.
- SSH domain connection error model.
- SSH domain connection service contract.
- SSH connection service dependency injection binding.
- SSH connection attempt use case.
- SSH connection attempt timeout boundary.
- SSH connection attempt exception mapping and cancellation preservation.
- SSH duplicate-attempt prevention at the ViewModel boundary.
- SSH ViewModel dependency injection for the connection attempt use case.

### Host Trust

- SSH host key fingerprint model.
- SSH host endpoint trust lookup model.
- SSH observed and trusted host key models.
- SSH host trust status model for unknown, trusted, and changed host keys.
- SSH trusted host storage contract.
- SSH trusted host Room entity and DAO.
- SSH trusted host entity/domain mapper.
- SSH trusted host Room-backed repository implementation.
- SSH trusted host repository dependency injection binding.
- Server Toolkit database version 2 trusted-host migration and version 3 trusted-host cascade-delete migration.
- SSH host trust evaluator.
- SSH host trust decision model.
- SSH host trust decision use case.
- SSH explicit unknown-host trust confirmation use case.
- SSH changed-host-key blocking decision flow.
- SSH host-key review presentation state.
- SSH host-key review UI mapper.
- SSH host-key review confirmation and cancellation ViewModel events.
- SSH host-key review screen actions.
- SSH host-key fingerprints use SHA256 values for observation and trusted verification.
- SSH host key observation verifier clarity hardening.
- SSH trusted-host cascade delete behavior when the owning server is deleted.
- SSH duplicate host-key confirmation guard at the ViewModel boundary.
- SSH trusted-host accepted message aligned with the current connection workflow.

### Authentication Input

- SSH authentication method model.
- SSH authentication input UI-safe presentation state.
- SSH ephemeral authentication input ViewModel events.
- SSH authentication input clearing behavior.
- SSH ephemeral password input on the SSH screen.
- SSH credential-bearing connection request boundary.
- SSH authentication input redaction and clearing at the connection attempt boundary.
- SSH password authentication cancellation preservation.

### SSHJ Integration

- SSHJ dependency declaration through the Gradle version catalog.
- SSH data-layer adapter shell.
- SSHJ-backed connection service boundary.
- SSHJ Android-compatible client factory with explicit Bouncy Castle provider registration and constrained key exchange defaults.
- SSHJ adapter shell unit test.
- SSHJ host-key observation adapter mapping.
- SSHJ authentication adapter mapping.
- SSHJ authentication executor boundary.
- SSHJ trusted host-key verifier boundary.
- SSHJ trusted connection execution boundary.
- SSHJ password authentication execution boundary.
- SSHJ session ownership execution boundary.
- Real ephemeral password-based SSH connection workflow.

### Session Lifecycle

- SSH project-owned session handle model.
- SSH session close result model.
- SSH session lifecycle service contract.
- SSHJ session lifecycle boundary.
- SSHJ session owner registry boundary.
- SSH session owner serialization between command execution and close cleanup.
- SSH session close cancellation preservation.

### Command Execution

- SSH command execution planning boundary.
- SSH command execution result model.
- SSHJ command channel planning boundary.
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
- SSH blank command idle-state handling and execution guard.
- SSH command text edit suppression while command execution is running.
- SSH command input disabling while command execution is running.
- SSH command output clearing when the active session becomes unavailable.
- SSH stale command result suppression after session invalidation.
- SSH stale command cancellation suppression after session invalidation.
- SSH command channel cancellation preservation with command-channel cleanup.

### Verification

- SSH connect event shell ViewModel tests.
- SSH domain model unit tests.
- Test-only fake SSH connection service.
- SSH ViewModel result handling seam for tests.
- SSH ViewModel fake result unit tests.
- SSH host trust domain and connection attempt unit tests.
- SSH host-key observation and trusted verifier unit tests.
- SSH command execution service tests.
- SSH command channel lifecycle tests.
- SSH command timeout stream-suppression regression test.
- SSH command cancellation tests.
- SSH duplicate host-key confirmation regression test.
- Manual SSH route verification.
- Automated SSH route verification through unit tests and debug build.
- Manual runtime test against macOS Remote Login through the Android emulator host address.
- Verified successful SSH connection and non-interactive `whoami` command execution.

---

## Current Guardrails

- SSH username ownership belongs to Server Inventory and inventory-backed connection target resolution.
- SSH authentication input state must not own a separate username value.
- SSH authentication input state may expose only the selected authentication method and secret presence flags.
- Persistent credential metadata and persistent secret storage are not implemented.
- Credential persistence requires a separate reviewed implementation slice with a secure storage boundary.
- SSH command execution remains non-interactive and must continue to use project-owned session handles.
- Terminal UI, saved commands, background monitoring, and persistent credentials remain out of scope.

---

## Not Implemented

The following items are intentionally not implemented yet:

- Interactive terminal workflow for owned sessions.
- Persistent credential storage implementation.
- Private-key authentication.
- Monitoring workflow.
- Saved command workflow.
- Xray or x-ui management workflow.
- Connection history.
- Room migrations beyond database version 3.
- Migration tests beyond the trusted-host v1-to-v2 and v2-to-v3 migrations.

---

## Next Safe Work

The next safe development steps are:

1. Continue runtime verification and hardening around SSH connection, session lifecycle, command execution timeout, cleanup, cancellation, and failure mapping.
2. Improve user-facing failure states for authentication failure, connection failure, host trust review, changed host key, and command failure.
3. Keep terminal UI, saved command history, background monitoring, persistent credentials, and Xray or x-ui management out of scope.

---

## Related Documents

- [Project State](../PROJECT_STATE.md)
- [Architecture](../ARCHITECTURE.md)
- [Roadmap](../ROADMAP.md)
- [Changelog](../CHANGELOG.md)
- [ADR Index](../adr/README.md)

---
