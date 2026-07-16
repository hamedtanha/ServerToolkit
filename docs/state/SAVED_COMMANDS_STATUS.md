# Saved Commands Status

**Project:** Server Toolkit  
**Milestone:** 0.5.0-alpha — Operations  
**Status:** Management Workflow Implemented and Manually Verified
**Last Updated:** 2026-07-16

---

## Purpose

This document records the current implemented state of the Saved Commands capability.

It is a living current-state document. It must describe implemented behavior only and must not present planned management UI or SSH integration as complete.

---

## Accepted Increment

The accepted first Operations increment is the Saved Command Foundation defined in GitHub Issue `#122`.

Slice 1 established the domain and persistence foundation.

Slice 2, tracked by GitHub Issue `#129`, now implements the user-visible Saved Commands management workflow. Users can navigate to Saved Commands, observe persisted commands, create validated commands with exact command-text preservation, and delete commands through explicit confirmation.

SSH command-input integration remains a later independent slice. Selecting or managing a saved command must never execute it automatically.

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
- names must not contain control characters such as line breaks or tabs;
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
- the repository exposes `createSavedCommand` rather than an update or upsert operation;
- commands are observed newest-first;
- the identifier provides a stable tie-breaker when timestamps match;
- duplicate identifiers fail with a database constraint instead of replacing an existing command silently;
- persistence stores command text exactly and never interprets or executes it.

---

## Implemented Management Workflow

The project now includes:

- a feature-owned Saved Commands navigation destination;
- a Dashboard entry that preserves the existing Server Inventory entry;
- loading, empty, content, blocking-failure, and non-blocking observation-failure states;
- a create dialog with name and command-text fields;
- field-level validation and visible persistence failures;
- exact command-text transfer through the domain and repository boundaries;
- duplicate create-submission prevention;
- per-command delete actions identified by stable saved-command identifiers;
- explicit delete confirmation containing the selected command name;
- duplicate delete-confirmation prevention;
- visible delete failures with retry and cancellation;
- repository-observed list updates after successful creation or deletion.

The management workflow never parses, rewrites, previews, or executes command text.

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

Implemented automated coverage includes:

- saved-command domain invariant tests;
- exact command-text mapper round-trip tests;
- DAO insert, lookup, observation ordering, duplicate rejection, and delete tests;
- Room-backed repository mapping and persistence tests;
- migration `4 → 5` validation using the exported Room schemas;
- initial loading, empty, content, observation-failure, and retry presentation behavior;
- create-form visibility, validation boundaries, exact command-text preservation, failure containment, and duplicate-submission prevention;
- delete selection, cancellation, success, failure containment, retry, and duplicate-confirmation prevention;
- preservation of loaded content during observation and mutation failures.

Manual verification on a physical Android device confirmed:

- exact persisted command text after application force-stop and relaunch;
- persistence of an undeleted saved command after restart;
- confirmed deletion remaining effective after a second restart.

Repository Android Validation run `29514594720` completed successfully for commit `390abcae3ac27764744ef7fae776542ab54a5ad3`.

---

## Explicitly Not Implemented Yet

The following behavior remains outside the implemented management slice:

- Saved-command editing.
- SSH command-input selection or population.
- Any command execution from Saved Commands.
- Command categories.
- Favorites.
- Quick actions.
- Variables, templates, placeholders, or secret substitution.
- Server-specific saved-command assignment.
- Import, export, synchronization, or backup behavior.
- Search, filtering, or custom sorting.
- Copy-to-clipboard actions.
- Bulk deletion or swipe-to-delete.
- Persistent credential storage.

---

## Architecture Decision Review

No new ADR is required for the persistence or management slices.

Reason:

- the existing Room persistence decision remains applicable;
- the implementation follows the existing project-owned repository and feature-first MVVM patterns;
- navigation uses the accepted app-level Navigation Compose boundary;
- presentation depends on `SavedCommandRepository`, not Room types;
- no Capability Gateway, Provider, Adapter, SSH coupling, secure-storage boundary, or external-library decision is introduced.

A new ADR must be reviewed if later work changes ownership, secure-storage boundaries, execution safety, server assignment, synchronization, or another significant architectural decision.

---

## Next Safe Slice

The next accepted slice is SSH Saved Command Input Integration:

- select a saved command from the existing SSH workflow;
- populate the existing command input without automatic execution;
- preserve user editing before execution;
- retain the existing explicit Run action;
- retain command-execution blocking, session lifecycle, cleanup, and stale-result guardrails.

The management workflow remains independent from SSH data-layer implementations.

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
