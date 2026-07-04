# ADR-007: Secure Storage Strategy

**Status:** Accepted  
**Date:** 2026-07-04  
**Related Milestone:** Version 0.4.0 — SSH

---

## Context

Server Toolkit is moving toward SSH-related workflows after accepting the Server Inventory 0.3.0 baseline and ADR-006.

The project needs a professional storage strategy before it stores any sensitive connection-related material.

Room is already used for local server metadata. That database is appropriate for non-secret inventory data, but it must not become the storage location for sensitive connection material.

Android provides platform security primitives that can protect encryption keys and support safer local storage design.

---

## Decision

Server Toolkit will separate metadata storage from secure material storage.

Room remains the storage layer for non-secret metadata only.

Sensitive connection material must not be stored directly in Room.

The secure storage design will use Android platform-protected keys as the foundation for encrypting sensitive values when persistence is explicitly approved.

Persistent storage of sensitive connection material remains disabled until an implementation plan and tests are added.

The first SSH implementation should still use non-persistent session input.

---

## Storage Model

The intended model is:

```text
Room
    stores metadata only

Secure storage component
    stores encrypted sensitive values only after explicit implementation approval

Android platform key protection
    protects encryption keys used by the secure storage component
```

Room may store references such as:

- `serverId`
- `secureEntryId`
- `type`
- `label`
- `createdAt`
- `updatedAt`

Room must not store raw sensitive values.

---

## Security Rules

The secure storage implementation must follow these rules:

- No raw sensitive value is stored in Room.
- No raw sensitive value is written to logs.
- No raw sensitive value is exposed through UI state longer than necessary.
- No raw sensitive value is included in crash reports.
- Secure entries must be deletable by the user.
- Backup behavior must be reviewed before enabling persistence.
- The app must tolerate missing or invalid secure entries.
- The app must expose clear user-facing states for unavailable stored entries.

---

## User Consent

Persistent secure storage must require an explicit user action.

The application must not silently remember sensitive connection material.

A future UI must clearly communicate whether a value is used for the current session only or stored for later reuse.

---

## Biometric and Device Lock

Biometric or device-lock confirmation may be added as an optional protection layer.

The initial secure storage implementation should not require biometric support on all devices.

The design must support devices with different hardware capabilities.

---

## Alternatives Considered

### Store everything in Room

Rejected.

Room is appropriate for inventory metadata, not raw sensitive connection material.

### Store only in memory forever

Rejected as the long-term strategy.

In-memory-only input is safe for the first SSH slice, but users may later need a reviewed persistence option.

### Require biometric support from the beginning

Rejected.

Not all devices provide the same biometric or hardware capabilities. The design should support stronger protection where available without making it mandatory for the first implementation.

---

## Consequences

### Positive

- Server metadata and sensitive material have separate responsibilities.
- Room remains simple and auditable.
- Future persistence can be added behind a clear boundary.
- The design supports gradual hardening.

### Negative

- The first SSH implementation will still not remember sensitive input.
- Secure storage requires additional implementation and tests.
- Some behavior may vary by device capability.

---

## Follow-up Work

Before implementing persistent secure storage, the project must define:

- Secure storage component API.
- Encryption approach.
- Backup behavior.
- Deletion behavior.
- Error handling behavior.
- Test strategy.
- UI consent flow.

---

## Implementation Boundary

The next implementation slice remains unchanged:

1. Add SSH destination and placeholder route.
2. Add SSH screen and ViewModel with static UI state.
3. Add navigation from a server item to the SSH placeholder.
4. Keep real SSH connection logic and persistent secure storage out of scope for the first slice.
