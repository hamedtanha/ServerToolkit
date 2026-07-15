# Saved Commands Status

**Project:** Server Toolkit  
**Milestone:** 0.5.0-alpha — Operations  
**Status:** Persistence Foundation Implemented  
**Last Updated:** 2026-07-15

---

## Purpose

This document records the current implemented state of the Saved Commands capability.

It is a living current-state document. It must describe implemented behavior only and must not present planned management UI or SSH integration as complete.

---

## Accepted Increment

The accepted first Operations increment is the Saved Command Foundation defined in GitHub Issue `#122`.

The complete increment will eventually allow users to create, manage, select, and explicitly execute reusable operational commands. Selection must never execute a command automatically.

Slice 1 establishes only the domain and persistence foundation.

---

## Implemented Domain Boundary

The project now owns a global `SavedCommand` domain model with:

- a stable identifier;
- a user-facing name;
- exact command text;
- a creation timestamp.

Current domain guardrails:

- identifiers must not be blank;
- names must not be blank;
- names must not contain leading or trailing whitespace;
- names are limited to `100` characters;
- command text must not be blank;
- command text is limited to `16,384` characters;
- creation timestamps must be positive;
- command text is preserved exactly and is not trimmed, parsed, rewritten, or executed by the domain model.

Saved commands are operational text. They are not credentials and do not provide secure secret storage.

---

## Implemented Persistence Boundary

The project now includes:

- `SavedCommandRepository` as the project-owned domain contract;
- `SavedCommandEntity` as the Room representation;
- `SavedCommandDao` for observation, lookup, insert, and deletion;
- `SavedCommandEntityMapper` for domain/entity conversion;
- `RoomSavedCommandRepository` as the Room-backed implementation;
- Hilt provisioning for the DAO and repository binding.

Persistence behavior:

- saved commands are global and are not assigned to individual servers;
- commands are observed newest-first;
- the identifier provides a stable tie-breaker when timestamps match;
- duplicate identifiers fail with a database constraint instead of replacing an existing command silently;
- persistence stores command text exactly and never interprets or executes it.

---

## Database State

The Server Toolkit Room database is now version `5`.

Migration `4 → 5` creates:

```text
saved_commands
```

Columns:

```text
id
name
command_text
created_at_epoch_millis
```

The migration also creates an index for `created_at_epoch_millis`.

Room schema version `5` is exported under `app/schemas/`.

No destructive migration fallback is used.

---

## Verification Coverage

Implemented coverage includes:

- saved-command domain invariant tests;
- exact command-text mapper round-trip tests;
- DAO insert, lookup, observation ordering, duplicate rejection, and delete tests;
- Room-backed repository mapping and persistence tests;
- migration `4 → 5` validation using the exported Room schemas;
- exact command-text preservation verification after migration.

The repository-wide Android Validation workflow remains required before merge.

---

## Explicitly Not Implemented Yet

The following behavior is not part of Slice 1:

- Saved Commands navigation destination.
- Management screen.
- Create form UI.
- Delete confirmation UI.
- Saved-command editing.
- SSH command-input selection.
- Automatic execution.
- Command categories.
- Favorites.
- Quick actions.
- Variables, templates, placeholders, or secret substitution.
- Server-specific saved-command assignment.
- Import, export, synchronization, or backup behavior.
- Persistent credential storage.

No user-visible Saved Commands workflow should be claimed until the management slice is implemented and verified.

---

## Architecture Decision Review

No new ADR is required for Slice 1.

Reason:

- the existing Room persistence decision remains applicable;
- the implementation follows the existing project-owned repository pattern;
- the `savedcommands` feature boundary is concrete and limited to one accepted capability;
- no new security, credential, release, platform, or external-library decision is introduced.

A new ADR must be reviewed if later work changes ownership, secure-storage boundaries, execution safety, server assignment, synchronization, or another significant architectural decision.

---

## Next Safe Slice

The next accepted slice is Saved Commands Management:

- navigation destination and entry point;
- loading, empty, content, and failure states;
- create workflow;
- delete confirmation workflow;
- persistence verification after application restart.

SSH input integration remains a later independent slice.

---

## Related Documents

- [Project State](../PROJECT_STATE.md)
- [Roadmap](../ROADMAP.md)
- [Architecture](../ARCHITECTURE.md)
- [Changelog](../CHANGELOG.md)
- [Server Inventory Status](SERVER_INVENTORY_STATUS.md)
- [SSH Status](SSH_STATUS.md)
- [Build Toolchain Status](BUILD_TOOLCHAIN_STATUS.md)
- [ADR-003: Local Persistence with Room](../adr/ADR-003-local-persistence-with-room.md)
