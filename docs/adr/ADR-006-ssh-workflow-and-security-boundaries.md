# ADR-006: SSH Workflow and Security Boundaries

**Status:** Accepted  
**Date:** 2026-07-04  
**Related Milestone:** Version 0.4.0 — SSH

---

## Context

Server Toolkit has an accepted Server Inventory 0.3.0 baseline.

The next milestone is SSH connectivity to managed servers.

SSH is a security-sensitive feature because it introduces network sessions, authentication input, server trust decisions, and connection lifecycle management.

The project must introduce SSH incrementally without weakening the existing architecture or mixing connection behavior into Server Inventory.

---

## Decision

Server Toolkit will introduce SSH as a separate feature boundary.

The SSH feature will use existing `Server` records as connection targets.

Server Inventory remains responsible for local server metadata.

SSH remains responsible for connection flow, session state, authentication input, and host trust behavior.

The initial SSH implementation must not persist authentication material.

Persistent authentication storage requires a separate security ADR before implementation.

The recommended package boundary is:

```text
feature/ssh/
```

The existing dependency direction remains unchanged:

```text
presentation -> domain -> data/infrastructure
```

---

## Initial Scope

The initial SSH scope may include:

- SSH navigation destination.
- SSH placeholder or skeleton screen.
- SSH ViewModel and UI state.
- Selection of an existing server as a connection target.
- Non-persistent authentication input for a session.
- Host trust review flow.
- Connection state reporting.
- Clear error states.

The initial SSH scope must not include:

- Persistent authentication storage.
- Saved command execution.
- Monitoring.
- Automated remote changes.
- x-ui or Xray management.

---

## Security Boundaries

Authentication input may be used only for the active SSH flow until a later ADR approves persistence.

The SSH workflow must not silently trust unknown or changed server identities.

Host trust behavior must be visible to the user.

Command execution is outside the initial SSH architecture milestone.

Credential persistence, known-host persistence, and command execution each require explicit follow-up decisions before implementation.

---

## Package Boundary

The SSH feature should be introduced incrementally.

Recommended structure when implementation begins:

```text
feature/ssh/
    domain/
        model/
        repository/
    data/
        repository/
    di/
    presentation/
        screen/
        state/
        viewmodel/
```

Empty packages should not be added unless they are part of an approved implementation step.

The SSH feature must not access Server Inventory DAOs or Room entities from presentation code.

If SSH needs server metadata, use a stable contract or navigation argument instead of depending on persistence internals.

---

## Alternatives Considered

### Implement SSH inside Server Inventory

Rejected.

Server Inventory owns metadata. SSH owns connection behavior. Combining both would broaden Server Inventory too much.

### Add persistent authentication storage immediately

Rejected.

Authentication storage needs a separate security decision before implementation.

### Create a broad infrastructure feature first

Rejected.

SSH is the concrete next milestone. A broader feature boundary would be premature.

---

## Consequences

### Positive

- Server Inventory remains focused.
- SSH work can start behind a clear boundary.
- Security-sensitive persistence is blocked until reviewed separately.
- Host trust behavior is treated as a first-class concern.

### Negative

- The first SSH implementation will not remember authentication input.
- Additional decisions are required before persistent authentication or command execution.

---

## Next Implementation Slice

The next safe implementation slice is:

1. Add SSH destination and placeholder route.
2. Add SSH screen and ViewModel with static UI state.
3. Add navigation from a server item to the SSH placeholder.
4. Keep real SSH connection logic out of scope for the first slice.

This keeps the application releasable while preparing the architecture for SSH behavior.
