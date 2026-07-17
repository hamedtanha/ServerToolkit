# Saved Commands Status

**Project:** Server Toolkit  
**Milestone:** 0.5.0-alpha — Operations  
**Status:** Management and SSH Input Workflows Implemented and Verified
**Last Updated:** 2026-07-17

---

## Purpose

This document records the current implemented state of the Saved Commands capability.

It is a living current-state document. It must describe implemented behavior only and must not present deferred editing, automation, templating, assignment, or synchronization behavior as complete.

---

## Accepted Increment

The accepted first Operations increment is the Saved Command Foundation defined in GitHub Issue `#122`.

Slice 1 established the domain and persistence foundation.

Slice 2, tracked by GitHub Issue `#129`, implements the user-visible Saved Commands management workflow. Users can navigate to Saved Commands, observe persisted commands, create validated commands with exact command-text preservation, and delete commands through explicit confirmation.

Slice 3, tracked by GitHub Issue `#133`, integrates persisted Saved Commands with the existing SSH command input. Users can open an inline selector, inspect commands in repository order, replace the current input with the exact selected command text, continue editing manually, and execute only through the existing explicit Run action.

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

## Implemented SSH Input Integration

The existing SSH workflow now includes:

- a selector action adjacent to the existing command input;
- lazy observation through the project-owned `SavedCommandRepository` contract;
- repository-order preservation without presentation-layer sorting;
- stable identifier selection from the currently visible command list;
- visible Saved Command names and exact command text;
- loading, empty, blocking-failure, retry, and non-blocking later-failure presentation;
- preservation of already loaded commands when a later observation fails;
- cancellation that leaves the current command input unchanged;
- exact input replacement without trimming, normalization, parsing, interpolation, or appending;
- continued manual input editing while the selector loads or reports an error;
- multiline command-input presentation for exact multiline command text;
- selector closure when execution begins or the SSH workflow changes lifecycle state;
- Back behavior that closes the selector before requesting permanent SSH workflow exit;
- execution only through the existing explicit Run action.

SSH owns mutable command input, execution, session lifecycle, and output. Saved Commands continues to own persistence and observation. SSH presentation depends directly on the project-owned `SavedCommandRepository` domain contract and does not depend on Saved Commands DAO, entity, concrete repository, screen, or ViewModel types.

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

SSH input integration verification now includes:

- selector UI-state invariants and editability rules;
- lazy repository observation, retry idempotency, ordering, exact selection, cancellation, and later-failure preservation;
- proof that selection does not invoke command execution or mutate SSH connection, authentication, session, or history behavior;
- Compose instrumentation coverage for selector availability, disabled state, loading-time manual input, stable-identifier selection, retry, cancellation, and execution separation;
- five passing targeted Compose tests on the Pixel 9 Android Virtual Device.

Manual verification on a physical Android device confirmed:

- exact persisted command text after application force-stop and relaunch;
- persistence of an undeleted saved command after restart;
- confirmed deletion remaining effective after a second restart.

Repository Android Validation run `29514594720` completed successfully for commit `390abcae3ac27764744ef7fae776542ab54a5ad3`.

---

## Explicitly Not Implemented Yet

The following behavior remains outside the implemented management slice:

- Saved-command editing.
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
- SSH presentation consumes the existing project-owned `SavedCommandRepository` domain contract without depending on Saved Commands data or presentation implementations;
- no Capability Gateway, Provider, Adapter, secure-storage boundary, or new production library decision is introduced.

A new ADR must be reviewed if later work changes ownership, secure-storage boundaries, execution safety, server assignment, synchronization, or another significant architectural decision.

---

## Next Safe Work

The accepted SSH Saved Command Input Integration slice is implemented.

The next Operations slice must be selected through a separate focused issue. Editing, categories, favorites, templates, variables, server assignment, synchronization, backup, credential storage, and automatic or background execution remain deferred.

Saved Commands management and persistence remain independent from SSH data-layer implementations.

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
