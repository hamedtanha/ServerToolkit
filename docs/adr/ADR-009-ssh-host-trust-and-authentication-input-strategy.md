# ADR-009: SSH Host Trust and Authentication Input Strategy

**Status:** Accepted

**Date:** 2026-07-04

---

# Context

The Server Toolkit project has introduced an SSH workflow shell, an SSHJ adapter shell, a connection service contract, dependency injection wiring, and a user-triggered connect event shell.

The application still intentionally does not implement real SSH connection behavior, credential input, authentication handling, host key verification, command execution, or SSH session lifecycle management.

Before any real SSH connection behavior is added, the project must define how SSH host trust and authentication input are handled.

This decision is necessary because SSH security cannot be treated as a UI detail or adapter implementation detail. Host trust, credential handling, and session boundaries affect the domain model, presentation flow, data layer, secure storage strategy, and user experience.

The project must avoid unsafe shortcuts such as silently accepting host keys, storing credentials in plain Room tables, logging sensitive values, or allowing ViewModels to become long-lived SSH session managers.

---

# Decision

The project will introduce real SSH behavior only after explicit host trust and authentication input boundaries are implemented.

For the first real SSH connection milestone, the project will follow these rules.

## Host Trust

SSH host trust must be explicit.

Unknown host keys must not be silently accepted.

The initial host trust model will use a Trust On First Use flow with explicit user confirmation.

When the app encounters an unknown SSH host key, it must surface the key fingerprint to the user and require confirmation before the host is trusted.

Trusted host key material must be stored separately from generic server inventory metadata.

A changed host key must be treated as a blocking security warning by default.

The app must not overwrite an existing trusted host key silently.

A future explicit replacement flow may be added, but it must require clear user confirmation and must be documented before implementation.

## Authentication Input

Authentication input must be modeled separately from server inventory metadata.

The server inventory may contain non-sensitive connection metadata such as host, port, username, name, category, environment, favorite state, and tags.

Passwords, private keys, private key passphrases, access tokens, and other secrets must not be stored in the Room server inventory table.

The first real connection implementation may use ephemeral per-connection authentication input.

Credential values must be held only for the duration required to attempt authentication.

Credential values must not be logged, exposed in UI state, stored in saved state, or included in crash-safe diagnostic messages.

The first implementation should prefer a small authentication input abstraction instead of passing raw strings through the application stack.

## Secure Storage Boundary

Persistent credential storage is outside the scope of the first real SSH connection milestone.

Persistent credential storage requires implementation against the accepted secure storage strategy and must be covered by tests.

Private key import, private key generation, biometric unlock, and credential synchronization are future capabilities and must not be introduced as incidental side effects of the first real connection implementation.

## Session Lifecycle Boundary

The SSH screen and ViewModel may trigger connection attempts and render connection state.

The ViewModel must not own a long-lived SSH session directly.

SSHJ implementation details must remain inside the data layer adapter.

Presentation state must expose domain-oriented connection status and error information, not SSHJ-specific objects or exceptions.

Command execution, terminal interaction, background monitoring, and long-lived session reuse remain outside the scope of this decision.

---

# Alternatives Considered

## Silently Accept All Host Keys

Accept every SSH host key automatically during connection.

### Pros

- Fast implementation.
- Minimal user interaction.
- Simple happy-path testing.

### Cons

- Unsafe default behavior.
- Vulnerable to man-in-the-middle attacks.
- Trains users to ignore SSH trust.
- Creates technical debt in the security model.
- Difficult to fix later without breaking existing behavior.

Rejected.

---

## Trust On First Use with Explicit Confirmation

Show the host key fingerprint on first connection and require the user to confirm trust.

### Pros

- Practical for infrastructure administration workflows.
- Familiar SSH security model.
- Safer than silent acceptance.
- Testable as an application workflow.
- Fits incremental development.

### Cons

- Requires additional UI state and user interaction.
- Requires trusted host key persistence.
- Requires a changed-host-key warning flow later.

Accepted for the first real SSH connection milestone.

---

## Certificate Authority Based Host Trust

Trust hosts through a configured SSH certificate authority or enterprise trust anchor.

### Pros

- Strong centralized trust model.
- Good fit for managed enterprise infrastructure.
- Reduces per-host trust prompts.

### Cons

- Too advanced for the current project stage.
- Requires additional configuration model.
- Requires certificate authority management workflows.
- Not necessary for the first real connection milestone.

Deferred.

---

## Store Credentials Immediately

Persist passwords or private keys as soon as authentication input is introduced.

### Pros

- Convenient user experience.
- Avoids repeated credential input.

### Cons

- Expands security scope too early.
- Requires secure storage implementation and threat modeling.
- Increases risk of accidental secret leakage.
- Encourages coupling between inventory and credentials.

Rejected for the first real connection milestone.

---

## Ephemeral Per-Connection Authentication Input

Ask for authentication input when a connection is attempted and keep it only for the active attempt.

### Pros

- Minimal persistent security surface.
- Keeps inventory metadata separate from secrets.
- Enables real connection behavior without committing to credential persistence.
- Compatible with a future secure credential store.

### Cons

- Less convenient for users.
- Requires authentication input UI before real connection behavior.
- Requires careful state handling to avoid leaking secrets.

Accepted for the first real SSH connection milestone.

---

# Implementation Gates

Before real SSH connection behavior is implemented, the project must:

- Define a host trust repository or equivalent project-owned persistence boundary.
- Store trusted host key material separately from generic server inventory metadata.
- Define how Android backup and data-extraction rules apply to inventory, credentials, and trusted host keys.
- Define ephemeral authentication input models that do not expose secrets through UI state, saved state, logs, or crash diagnostics.
- Add failure containment for connection attempts, including exception mapping, timeout behavior, cancellation preservation, and state recovery.
- Prevent duplicate concurrent connection attempts from the UI and ViewModel boundary.
- Ensure blocking SSHJ operations run on an I/O dispatcher, not the main dispatcher.
- Add tests for unknown host key, accepted host key, changed host key, authentication failure, transport failure, timeout, cancellation, and duplicate connect attempts.
- Add the `INTERNET` permission only in the same reviewed implementation slice that introduces real network behavior.

---

# Consequences

## Positive

- Real SSH behavior will be introduced behind explicit security boundaries.
- Host trust becomes a first-class workflow instead of an adapter detail.
- Credential input remains separate from server inventory metadata.
- The project avoids storing secrets in plain Room tables.
- The implementation remains aligned with MVVM, repository boundaries, and dependency inversion.
- SSHJ-specific details remain isolated in the data layer.
- Future secure storage and private key support can be added without redesigning the initial SSH workflow.

## Negative

- Real SSH connection behavior will take longer to implement.
- The first connection flow requires additional UI and state handling.
- Users may need to confirm host trust before connecting.
- Persistent credentials remain unavailable until secure storage implementation is added.
- Changed host key handling requires a separate explicit warning flow before it can be considered complete.

---

# References

- ADR-006: Remote Connection Workflow Boundaries
- ADR-007: Secure Storage Strategy
- ADR-008: SSH Client Library Selection
- ARCHITECTURE.md
- PROJECT_STATE.md
- ROADMAP.md

---

# Notes

This ADR intentionally does not implement real SSH behavior.

It defines the security and workflow boundaries required before real SSH connection behavior can be safely added.

Real SSH implementation must proceed only through the implementation gates defined above.
