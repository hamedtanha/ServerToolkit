# ADR-010: SSH Command Channel Execution Strategy

**Status:** Accepted

**Date:** 2026-07-05

---

# Context

Server Toolkit now has the foundations required for real SSH connection ownership:

- SSH host trust is explicit.
- Authentication input is ephemeral.
- SSHJ connection behavior is isolated in the data layer.
- Authenticated SSHJ clients are owned by the data-layer session owner registry.
- Domain and presentation layers only receive project-owned session handles.

At the time of this decision, the next SSH milestone was command and channel execution planning behind owned SSH sessions.

This decision is necessary because command execution introduces a new resource lifecycle. SSH command channels can hold streams, exit status, remote process state, timeout behavior, and cleanup requirements. If command channels leak into ViewModels, UI state, or domain models, the project will quickly become a fragile terminal client instead of a maintainable infrastructure management application.

The project must support future operational workflows such as predefined maintenance commands and quick actions without introducing terminal UI, persistent credentials, or SSHJ-specific objects outside the data layer.

---

# Decision

Server Toolkit introduces SSH command execution through project-owned command execution contracts and data-layer SSHJ channel ownership.

Real command execution may only be enabled through the accepted planning boundary, lifecycle rules, timeout behavior, result model, and tests. The command workflow remains non-interactive and must not expose SSHJ channels, streams, sessions, sockets, or terminal behavior outside the data layer.

## Implementation Status

This decision has been implemented for the current non-interactive SSH command execution workflow.

The implementation includes project-owned command request, result, error, planning, and execution contracts; session-handle based execution; SSHJ command channel ownership inside the data layer; timeout, cleanup, cancellation, and failure-mapping boundaries; and presentation-layer stabilization for command input and output state.

The original gates remain active guardrails for future command execution changes.

## Command Execution Scope

The initial command execution model is non-interactive.

It supports the architectural path toward commands such as:

- Run a predefined command.
- Capture stdout.
- Capture stderr.
- Capture exit status.
- Map failures into project-owned error categories.
- Close command channels after execution.

It does not support:

- Terminal emulation.
- Interactive shell sessions.
- User-driven terminal input.
- Persistent command history.
- Persistent credential storage.
- Private-key authentication.
- Background monitoring.
- Streaming terminal UI output.

## Layer Boundaries

Domain and presentation code must only use project-owned command models and session handles.

The following SSHJ objects must remain inside the data layer:

- SSHJ client objects.
- SSHJ session objects.
- SSHJ command/channel objects.
- Sockets.
- Input streams.
- Output streams.
- SSHJ exceptions.

ViewModels must not own SSHJ clients, sessions, sockets, command channels, streams, or credential values.

## Session Ownership

Command execution must run only against an existing project-owned SSH session handle.

The data layer may resolve the session handle through the SSHJ session owner registry, but the registry must remain a data-layer implementation detail.

If the session handle is unknown, closed, expired, or otherwise unavailable, command execution must fail with a stable project-owned error instead of exposing SSHJ or registry details.

## Command Channel Lifecycle

Each command execution request must own a short-lived command channel.

A command channel must be closed after the command completes, fails, times out, or is cancelled.

The SSH connection may remain owned by the session owner registry after command channel cleanup.

Command channel cleanup failure must be contained and mapped to a project-owned result.

## Timeout and Cancellation

Command execution must have explicit timeout behavior.

Blocking SSHJ command execution must run on an I/O dispatcher and must not block the main thread.

Coroutine cancellation must be preserved where applicable. Cancellation handling must still attempt command channel cleanup.

## Credentials

Command execution must not introduce persistent credential storage.

Command execution must not require credentials to be stored after the SSH session has already been established.

Credential values must not be logged, persisted, exposed in UI state, or included in diagnostic messages.

## Terminal UI

Terminal UI remains out of scope.

A future interactive terminal workflow requires a separate architectural decision because it has different lifecycle, input, rendering, buffering, security, and UX constraints.

---

# Alternatives Considered

## Execute Commands Directly from the ViewModel

Let the SSH screen ViewModel access the active SSHJ client or session and execute commands directly.

### Pros

- Fast implementation.
- Fewer intermediate contracts.
- Easy to prototype.

### Cons

- Violates MVVM boundaries.
- Leaks SSHJ objects into presentation code.
- Makes lifecycle cleanup fragile.
- Makes testing harder.
- Encourages terminal-client architecture.
- Creates long-term technical debt.

Rejected.

---

## Expose SSHJ Sessions Through Domain Models

Wrap or expose SSHJ session objects through domain-level models so command execution can be added quickly.

### Pros

- Simple adapter implementation.
- Reduces mapping work.
- Allows advanced SSHJ features to be reached quickly.

### Cons

- Couples the domain layer to SSHJ.
- Makes future SSH library replacement difficult.
- Violates the accepted SSH client library boundary.
- Risks leaking streams, sockets, and command channels into higher layers.

Rejected.

---

## Add Interactive Terminal First

Implement a terminal-like screen and build command execution from terminal interaction.

### Pros

- Familiar SSH user experience.
- Useful for advanced administrators.
- Could expose many capabilities quickly.

### Cons

- Large UI and lifecycle scope.
- Requires terminal emulation, buffering, keyboard handling, stream management, and process lifecycle rules.
- Conflicts with the product vision of operational workflows over traditional SSH client behavior.
- Delays predefined command and quick-action workflows.

Rejected for the current milestone.

---

## Add Non-Interactive Command Execution Boundary First

Introduce command execution through project-owned request, result, error, and service contracts while keeping SSHJ channel ownership inside the data layer.

### Pros

- Preserves architecture.
- Supports future operational workflows.
- Keeps SSHJ details isolated.
- Keeps terminal UI out of scope.
- Enabled focused tests before real execution and continues to preserve a clear planning boundary.
- Aligns with owned SSH session boundaries.

### Cons

- Slower than directly executing commands.
- Requires additional models and tests.
- Required additional implementation slices before real execution could be enabled.

Accepted.

---

# Implementation Gates

Real SSH command execution is enabled only while the implementation continues to satisfy these gates:

- Define project-owned command request, result, error, and execution service contracts.
- Define command/channel lifecycle ownership inside the SSHJ data layer.
- Ensure command execution resolves only through project-owned SSH session handles.
- Keep SSHJ clients, sessions, channels, streams, sockets, and exceptions inside the data layer.
- Add unknown-session and closed-session failure handling.
- Add timeout behavior for command execution.
- Preserve coroutine cancellation where applicable.
- Ensure blocking SSHJ operations run on an I/O dispatcher.
- Ensure command channels are closed after success, failure, timeout, or cancellation.
- Add focused unit tests for planning, result mapping, unknown session handling, timeout behavior, cancellation behavior, and cleanup behavior.
- Keep terminal UI out of scope.
- Keep persistent credential storage out of scope.

---

# Consequences

## Positive

- Command execution can evolve without turning the project into a terminal client.
- SSHJ implementation details remain isolated in the data layer.
- ViewModels remain responsible for presentation state, not SSH resource ownership.
- Future saved commands and quick actions can use stable domain contracts.
- Persistent credentials remain outside the current security surface.
- Command/channel lifecycle remains testable independently of UI interaction.

## Negative

- More implementation steps were required before users could run commands.
- Non-interactive command execution does not satisfy full terminal use cases.
- Additional lifecycle tests remain required as the feature evolves.
- Future terminal support will require a separate decision and implementation path.

---

# References

- ADR-001: Project Vision
- ADR-006: Remote Connection Workflow Boundaries
- ADR-007: Secure Storage Strategy
- ADR-008: SSH Client Library Selection
- ADR-009: SSH Host Trust and Authentication Input Strategy
- ARCHITECTURE.md
- PROJECT_STATE.md
- ROADMAP.md

---

# Notes

This ADR intentionally separates command execution from terminal interaction.

The planning boundary has since been implemented and is now used by the non-interactive command execution workflow. Future work must continue to keep terminal interaction, saved commands, background monitoring, and persistent credentials outside this ADR unless a new reviewed decision expands the scope.
