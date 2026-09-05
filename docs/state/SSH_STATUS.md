# SSH Status

**Project:** Server Toolkit
**Feature Area:** SSH
**Status:** Milestone Complete with Operations Integration
**Related Milestone:** Version 0.4.0 — SSH
**Last Updated:** 2026-09-05

---

## Purpose

This document records the detailed implementation status of the SSH feature area.

The high-level project state remains documented in [Project State](../PROJECT_STATE.md).

---

## Current Status

The SSH milestone implementation for version 0.4.0 is complete. The signed and verified version 0.4.0 APK, tag, checksums, and required release evidence are published.

The current implementation supports real ephemeral password-based and private-key SSH authentication, user-facing non-interactive command execution behind project-owned SSH session handles, deterministic workflow-exit cleanup, explicit disconnection while remaining on the SSH screen, and exact Saved Command selection into the existing editable command input without automatic execution.

The ADR-013 ephemeral private-key workflow is implemented end to end. The implementation includes the one-shot source, bounded key-document reading, Android system-picker integration, private ViewModel pending-source ownership, in-memory SSHJ key-provider creation, stable project-owned failure mapping, and authentication without temporary private-key files.

Automated JVM coverage, Android runtime verification, full unit tests, lint, and debug-build validation are complete. Verified private-key support is intentionally restricted to the documented format matrix below.

The current connection/session, command-execution, and host-trust hardening state incorporates the focused F01/F02/F03 corrections from architecture review `RA-2026.09-v1`: cancellation-safe connected-session handoff, concurrent bounded command-output draining under a complete-operation deadline, and OpenSSH-compatible SHA-256 host-key fingerprints for new observation and trust. Historical SSHJ MD5 and Java-encoded SHA-256 trusted-host records remain verifiable through explicit scheme-aware compatibility without silent rewrite or schema migration.

Persistent credentials, terminal UI, Saved Command automation, background monitoring, and Xray or x-ui management remain intentionally out of scope.

---

## Accepted Private-Key Authentication Design

ADR-013 defines the following private-key authentication boundaries:

- User-selected private-key documents through the Android system document picker.
- Immediate conversion of the selected Android document reference into a project-owned one-shot source.
- No persistent URI permission, key import, key copy, credential profile, or secret persistence.
- Bounded key-document reading with an initial `256 KiB` limit.
- Preflight validation of encrypted OpenSSH v1 KDF metadata before SSHJ parsing, with a maximum accepted bcrypt work factor of `64` rounds.
- Private-key parsing and SSHJ authentication inside the SSH data layer.
- Optional passphrase handling outside observable UI state and saved state.
- Secret invalidation when an attempt enters host-key review.
- Stable project-owned failure mapping, cancellation preservation, and best-effort cleanup.

These boundaries are implemented for the current ephemeral private-key authentication workflow. Persistent key import, persistent URI access, credential profiles, and secret storage remain outside the accepted scope.

---

## Verified Private-Key Format Matrix

The initial private-key format matrix is based on automated SSHJ adapter tests and manual Android runtime verification through the system document picker.

| Container | Algorithm | Passphrase | Result |
|---|---|---|---|
| OpenSSH v1 | Ed25519 | None | Supported |
| OpenSSH v1 | Ed25519 | Required | Supported |
| OpenSSH v1 | RSA | None | Supported |
| OpenSSH v1 | RSA | Required | Supported |
| PKCS#8 | RSA | None | Unsupported format |
| PKCS#8 | RSA | Required | Unsupported format |

Algorithms and containers outside this verified matrix are unsupported.

The Android runtime verification also confirmed:

- successful authentication with all four supported OpenSSH combinations;
- successful non-interactive `whoami` execution after authentication;
- stable rejection for an incorrect private-key passphrase;
- stable rejection for a valid but unauthorized key;
- stable unsupported-format outcomes for both tested PKCS#8 variants;
- key reselection after host-key review, consistent with the one-attempt source contract.

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
- SSH screen with ephemeral password input, host-key review actions, connection status, explicit disconnect control, non-interactive command controls, inline Saved Command selection, and connection history navigation.
- SSH ViewModel and UI state for connection attempts, host-key review, authentication input, and command execution.
- Per-server SSH connection history destination and read-only screen.
- Connection history ViewModel and UI state backed by repository observation.
- Connection history loading, error, empty, and newest-first entry presentation.
- Server Inventory Connect action to open the SSH route.
- SSH UI connection status model.
- SSH UI state alignment with domain connection result.
- SSH connection result UI mapper.
- SSH presentation state unit tests.
- SSH user-triggered connect event handling.
- SSH Connect button with ephemeral password input.
- SSH authentication-method selection for password and ephemeral private-key workflows.
- SSH system content-picker action with no retained Android document reference.
- SSH private-key selection and optional passphrase presentation with non-sensitive presence state only.
- Authentication UI disposal clears password and passphrase text while retaining only the pending project-owned source across configuration changes.

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
- SSH connection history domain model.
- SSH connection history repository contract.
- SSH connection history Room persistence.
- Per-server connection history observation through the repository boundary without presentation-layer DAO access.
- Automatic SSH connection attempt history recording for resolved targets.
- SSH connection history persistence failure containment that preserves primary connection outcomes and cancellation.
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
- Newly observed and newly trusted host-key fingerprints use canonical OpenSSH-compatible SHA-256 over the SSH public-key wire representation and remain displayed as `SHA256:<value>`.
- Historical trusted-host rows using SSHJ MD5 or Java `PublicKey.encoded` SHA-256 remain scheme-aware and verifiable without silent relabeling, rewrite, or Room schema migration; unknown persisted schemes fail closed.
- SSH host key observation verifier clarity hardening.
- SSH trusted-host cascade delete behavior when the owning server is deleted.
- Existing-Server updates no longer delete trusted-host rows through incidental parent replacement.
- SSH trust remains endpoint-keyed by Server id, host, and port, so old-endpoint trust does not authorize a different active endpoint.
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
- Project-owned one-shot private-key source contract.
- Atomic available-to-consumed and available-to-invalidated source transitions.
- Bounded private-key material reading with the accepted `256 KiB` limit.
- Stable source lifecycle and content-read failure categories.
- Callback-scoped key-material access with redacted string representations and best-effort buffer clearing.
- Android private-key source factory backed by cancellable descriptor opening on an I/O dispatcher.
- Joint descriptor-stream ownership with prompt-cancellation cleanup.
- Immediate conversion of each picker result into a project-owned one-shot source.
- Private ViewModel ownership of at most one pending source outside observable and saved state.
- Pending-source invalidation on replacement, picker cancellation, method change, workflow exit, and host-key review.
- One-attempt source transfer into connection-attempt orchestration.
- Authentication-time source consumption exactly once inside the key-material lifetime boundary.
- Best-effort clearing of application-owned key-material buffers after success, failure, or cancellation.

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
- SSHJ private-key provider factory and authentication execution boundary.
- In-memory OpenSSH v1 private-key parsing without temporary private-key files.
- Project-owned rejection of zero or excessive OpenSSH bcrypt KDF rounds before SSHJ parsing and decryption begin.
- Explicit Ed25519 and RSA key-type restriction for the verified OpenSSH matrix.
- Stable mapping for unavailable, empty, oversized, unsupported, malformed, passphrase, server-rejection, cancellation, and unexpected outcomes.
- Best-effort clearing of application-owned passphrase character arrays.
- SSHJ session ownership execution boundary.
- Real ephemeral password-based and private-key SSH connection workflows.

### Session Lifecycle

- SSH project-owned session handle model.
- SSH session close result model.
- SSH session lifecycle service contract.
- SSHJ session lifecycle boundary.
- SSHJ session owner registry boundary.
- SSH session owner serialization between command execution and close cleanup.
- SSH session close cancellation preservation.
- Deterministic active-session cleanup before permanent SSH workflow exit.
- Back, system-back, and connection-history navigation deferred until cleanup completes.
- Dedicated disconnecting presentation state during session cleanup.
- Shared active-session close orchestration for workflow exit and explicit user-requested disconnect.
- Duplicate session-close suppression across workflow-exit and explicit disconnect requests.
- Workflow exit and explicit disconnect blocking during connection and command execution.
- Explicit disconnect presentation with reconnect support after successful cleanup.
- Deterministic `Closed`, `NotFound`, `Failed`, and cancellation handling.
- Active-session restoration and cleanup retry after close failure.
- Stale command output clearing after successful cleanup.
- IO-dispatched SSHJ cleanup inside a non-cancellable cleanup context.

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
- Concurrent stdout and stderr draining while the command channel is active so remote receive-window backpressure cannot depend on post-completion reads.
- Bounded retained command output of up to `256 KiB` per stdout and stderr stream while excess bytes continue to be drained and discarded.
- Explicit stdout and stderr truncation metadata propagated through the project-owned command result and surfaced in completion detail.
- One complete-operation command timeout spanning channel completion and stream draining; executor waits consume only the remaining deadline.
- Interruptible blocking command execution on the I/O dispatcher so coroutine timeout or cancellation can release blocking waits and reach local command-channel cleanup.
- SSH command execution presentation state and UI mapper.
- SSH ViewModel command execution wiring through an active project-owned session handle.
- SSH multiline command input and Run command UI controls.
- Inline Saved Command selector adjacent to the command input.
- Lazy `SavedCommandRepository` observation with loading, empty, content, failure, later-failure preservation, retry, and cancellation states.
- Stable-identifier selection that replaces the current command input with exact persisted text.
- Manual command editing remains available while Saved Commands load or fail.
- Saved Command selection never invokes execution; Run remains the only execution trigger.
- Selector closure on execution start and relevant SSH lifecycle transitions.
- Back closes the visible selector before permanent workflow-exit cleanup is requested.
- SSH command output rendering for retained stdout, retained stderr, exit status, and explicit truncation state.
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
- SSH connection history domain model tests.
- SSH connection history mapper, DAO, repository, and migration tests.
- SSH connection history recording tests for connected, failed, timed-out, cancelled, unrecorded, and persistence-failure paths.
- SSH connection history UI mapper and ViewModel unit tests.
- Manual runtime verification of history navigation, newest-first presentation, entry details, and back navigation using recorded SSH attempts.
- Test-only fake SSH connection service.
- SSH ViewModel result handling seam for tests.
- SSH ViewModel fake result unit tests.
- SSH failure UI mapper state coverage.
- SSH host trust domain and connection attempt unit tests.
- SSH host-key observation and trusted verifier unit tests.
- OpenSSH-compatible host-key fingerprint known-answer coverage for fixed RSA and Ed25519 public-key fixtures, plus scheme-specific compatibility coverage for historical SSHJ MD5 and Java-encoded SHA-256 trust records.
- Server-update Room regression coverage verifies preservation of trusted-host and connection-history rows across metadata, username, endpoint, and repeated updates while retaining explicit Server-delete cascades.
- SSH trusted connection cancellation cleanup-failure regression test.
- SSH command execution service tests.
- SSH command channel lifecycle tests.
- SSH command timeout stream-suppression regression test.
- SSH command cancellation cleanup-failure regression test.
- SSH command cancellation tests.
- Deterministic stdout-heavy and stderr-heavy command tests whose completion depends on concurrent stream draining.
- Mixed stdout/stderr retention-limit coverage that verifies excess bytes are still drained while retained output remains bounded.
- Missing/late EOF coverage that verifies stream draining cannot outlive the complete command-operation deadline.
- Actual blocking-read and blocking-command cancellation coverage that verifies interruption reaches command cleanup.
- Explicit retained-output truncation presentation coverage.
- SSH duplicate host-key confirmation regression test.
- Saved Command selector ViewModel coverage for lazy observation, ordering, exact replacement, cancellation, retry idempotency, later-failure preservation, manual input, execution separation, and lifecycle closure.
- Saved Command selector Compose instrumentation coverage for availability, disabled state, loading-time manual input, selection without execution, retry, and cancellation.
- Five passing targeted Compose tests on the Pixel 9 Android Virtual Device.
- Manual SSH route verification.
- Automated SSH route verification through unit tests and debug build.
- One-shot private-key source lifecycle, concurrency, size-limit, failure-mapping, resource-closing, redaction, cancellation, and buffer-clearing unit tests.
- Android private-key content factory instrumentation tests for successful reads, unavailable content, descriptor cleanup, and cancellation bridging.
- Private-key picker ownership tests for presence-only state, replacement, cancellation, workflow exit, host review, one-attempt transfer, and connection cancellation.
- Manual Android verification of private-key picker cancellation, selection, replacement, configuration-change behavior, navigation cleanup, process-death reset, and host-key review cleanup.
- Android benchmark verification of complete SSHJ parsing at `16` and `64` OpenSSH bcrypt KDF rounds, measuring approximately `204.11 ms` and `724.18 ms` respectively on a Pixel 9 Android Virtual Device.
- Manual runtime test against macOS Remote Login through the Android emulator host address.
- Verified successful SSH connection and non-interactive `whoami` command execution.
- Dedicated test-only OpenSSH Ed25519 and RSA fixtures for encrypted and unencrypted authentication.
- Dedicated test-only PKCS#8 RSA fixtures for stable unsupported-format verification.
- Malformed-key, missing-passphrase, incorrect-passphrase, unauthorized-key, source-failure, cancellation, and buffer-clearing coverage.
- Android runtime verification of the complete supported and unsupported private-key format matrix.
- Full unit-test, lint, and debug-assembly quality gate after private-key authentication implementation.
- Focused SSH session-close coverage for workflow exit and explicit disconnect, including no-session, successful close, missing owner, close failure, retry, duplicate requests, active connection, active command execution, stale output cleanup, cancellation, and reconnect eligibility.
- SSHJ lifecycle regression coverage for caller cancellation during started cleanup.
- Manual Android runtime verification of workflow-exit cleanup, explicit disconnect, reconnection, and crash-free execution.
- Full compilation, Android-test compilation, unit-test, lint, and debug-assembly quality gate after explicit disconnect implementation.
- Full compilation, Android-test compilation, unit-test, lint, debug-app assembly, and debug-test assembly quality gate after F02 command-I/O hardening.

---

## Current Guardrails

- SSH username ownership belongs to Server Inventory and inventory-backed connection target resolution.
- SSH authentication input state must not own a separate username value.
- SSH authentication input state may expose only the selected authentication method and secret presence flags.
- Persistent credential metadata and persistent secret storage are not implemented.
- Private-key documents, loaded key material, and passphrases remain one-attempt and non-persistent.
- Credential persistence requires a separate reviewed implementation slice with a secure storage boundary.
- SSH command execution remains non-interactive and must continue to use project-owned session handles.
- Non-interactive command output remains ephemeral and must not be persisted or logged as ordinary application telemetry.
- Retained command output remains bounded to `256 KiB` per stdout/stderr stream, and any truncation must remain explicit to the user.
- Command timeout and cancellation may close the local SSH command channel to release blocking I/O; this must not be represented as a guarantee that every remote process has terminated.
- SSH connection history must contain non-sensitive resolved target metadata and result classification only.
- Existing connection-history snapshot rows must not be rewritten or deleted as a side effect of Server metadata, username, host, or port updates.
- Target-resolution failures and host-trust decision outcomes must not create incomplete connection history entries.
- Connection history persistence failures must not replace the primary SSH outcome or cancellation.
- New host-key trust persists canonical OpenSSH-compatible SHA-256 fingerprints under an explicit persisted scheme; historical trusted-host fingerprint rows must not be silently rewritten or reinterpreted across schemes.
- Terminal UI, Saved Command automation, background monitoring, and persistent credentials remain out of scope.

---

## Not Implemented

The following items are intentionally not implemented yet:

- Interactive terminal workflow for owned sessions.
- Persistent credential storage implementation.
- Monitoring workflow.
- Saved Command categories, favorites, templates, variables, Server assignment, synchronization, or automatic execution.
- Operating-system or service-manager discovery.
- Xray or x-ui management workflow.
- Room migrations beyond database version 5.
- Migration tests beyond the trusted-host `1 -> 2`, trusted-host `2 -> 3`, connection-history `3 -> 4`, and Saved Commands `4 -> 5` migrations.

---

## Next Safe Work

The next safe development steps are:

1. Preserve version 0.4.0 SSH as the accepted baseline while retaining the version 0.5.0 Saved Command input integration as an additive Operations workflow.
2. Preserve exact input replacement, manual editing, explicit Run-only execution, execution-state blocking, session lifecycle, cleanup, bounded output retention, and stale-result guardrails.
3. Reopen SSH hardening only from concrete runtime evidence, current repository inspection, or a newly recorded focused review finding.
4. Keep terminal UI, background monitoring, persistent credentials, Saved Command automation, and Xray or x-ui management outside the accepted scope.

---

## Related Documents

- [Project State](../PROJECT_STATE.md)
- [Architecture](../ARCHITECTURE.md)
- [Roadmap](../ROADMAP.md)
- [Changelog](../CHANGELOG.md)
- [ADR Index](../adr/README.md)
- [ADR-013: Ephemeral SSH Private-Key Authentication Boundary](../adr/ADR-013-ephemeral-ssh-private-key-authentication-strategy.md)

---