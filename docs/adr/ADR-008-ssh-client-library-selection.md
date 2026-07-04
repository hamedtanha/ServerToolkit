# ADR-008: SSH Client Library Selection

**Status:** Accepted  
**Date:** 2026-07-04  
**Related Milestone:** Version 0.4.0 — SSH

---

## Context

Server Toolkit has introduced the SSH feature boundary and placeholder route.

The next milestone requires selecting a Java SSH client library before real connection behavior is added.

The selected library must support maintainable client-side SSH behavior while remaining isolated behind project-owned abstractions.

---

## Decision

Server Toolkit will use SSHJ as the initial SSH client library.

SSHJ must be wrapped behind project-owned abstractions before real connection behavior is implemented.

The minimum accepted SSHJ version is:

```text
0.38.0
```

A newer stable version may be used after dependency verification.

---

## Architecture Boundary

The SSH library must be isolated behind the SSH feature data layer.

The intended dependency direction is:

```text
feature/ssh/presentation
    -> feature/ssh/domain
        -> feature/ssh/data
            -> SSHJ
```

Compose screens and ViewModels must not depend directly on SSHJ classes.

Domain models must not expose SSHJ implementation types.

---

## Required Capabilities

The selected library should support:

- SSH client connection lifecycle.
- Host key verification integration.
- Password authentication.
- Public-key authentication.
- Keyboard-interactive authentication or a future path for it.
- Command channels for future work.
- Shell channels for future work.
- SFTP or SCP for future work.

The first implementation does not need to expose all capabilities immediately.

---

## Alternatives Considered

### SSHJ

Accepted.

SSHJ is focused on Java SSH client workflows and supports the expected Server Toolkit direction when hidden behind internal abstractions.

### mwiede/jsch

Rejected for the initial implementation.

The maintained JSch fork is a valid option, especially as a drop-in replacement for existing JSch users. Server Toolkit is not migrating existing JSch code, so the older JSch-style API is less attractive for a new implementation.

### Apache MINA SSHD

Rejected for the initial implementation.

Apache MINA SSHD is mature and comprehensive, but it covers more than the initial Server Toolkit SSH client slice requires.

It may be reconsidered if future requirements outgrow SSHJ.

---

## Consequences

### Positive

- The first real SSH implementation can use a focused SSH client library.
- The dependency remains replaceable behind project-owned abstractions.
- UI and ViewModel code stay independent from third-party SSH types.

### Negative

- SSHJ integration must be verified on Android.
- Library-specific behavior still needs adapter tests.
- A future migration may be required if Android runtime issues appear.

---

## Implementation Rules

Before adding real connection behavior:

1. Add SSHJ through the version catalog.
2. Introduce SSH feature domain contracts.
3. Implement SSHJ only inside the SSH data layer.
4. Keep UI state free of SSHJ types.
5. Keep persistent secure storage out of scope.
6. Add tests around UI-safe connection state mapping.

---

## Follow-up Work

The next safe implementation slice is:

1. Add the SSHJ dependency.
2. Add minimal SSH domain contracts.
3. Add a fake implementation for tests.
4. Add an SSHJ adapter shell.
5. Verify build stability before adding real network behavior.
