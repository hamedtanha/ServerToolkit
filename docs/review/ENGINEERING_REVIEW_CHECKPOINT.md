# Engineering Review Checkpoint

This document records engineering review findings, completed fixes, open risks, validation notes, and the next technical checkpoints for Server Toolkit.

It is an engineering review document, not product documentation and not an Architecture Decision Record. It should help maintain review continuity across pull requests without mixing review notes into user-facing documentation.

---

## Purpose

This document exists to prevent review findings from being lost between small focused pull requests.

Use it to track:

- completed engineering findings,
- open review items,
- validation lessons,
- follow-up branches,
- documentation synchronization needs,
- technical risks that are not urgent enough for the current pull request.

Do not use this document to describe planned functionality as implemented behavior.

---

## Current Review Checkpoint

Date: 2026-07-09

Recent resolved pull requests:

- PR `#79`: `fix: cascade delete ssh trusted host keys`
- PR `#80`: `docs: add engineering review checkpoint`
- PR `#81`: `fix: prevent duplicate ssh host key confirmation`
- PR `#82`: `fix: update trusted host accepted message`
- PR `#83`: `docs: sync ssh current state`
- PR `#84`: `docs: update engineering review checkpoint`
- PR `#85`: `docs: record ssh secret cleanup review`
- PR `#86`: `docs: finalize engineering review checkpoint`

Current `main` state after PR `#86`:

- Trusted SSH host keys are lifecycle-bound to their owning server inventory entries.
- Duplicate SSH host key confirmation attempts are guarded at the ViewModel boundary.
- The trusted-host accepted message no longer references obsolete implementation gates.
- Current-state SSH documentation is synchronized with the implemented ephemeral password connection and non-interactive command execution workflow.
- The open review backlog contains only the future SSH session close-vs-execute concurrency review item.

Recent validation completed:

For PR `#79`:

    ./gradlew connectedDebugAndroidTest
    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

For PR `#81`:

    ./gradlew testDebugUnitTest --tests "de.hamedtanha.servertoolkit.feature.ssh.presentation.viewmodel.SshViewModelHostTrustConfirmationTest"
    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

For PR `#82`:

    ./gradlew testDebugUnitTest --tests "de.hamedtanha.servertoolkit.feature.ssh.presentation.state.SshHostTrustDecisionUiMapperTest"
    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

For PR `#83`:

    git diff --check
    ./gradlew :app:assembleDebug

GitHub status:

- No GitHub CI checks were reported for these pull requests.
- Validation was performed locally.

---

## Resolved Findings

### P1 — SSH trusted host keys were not lifecycle-bound to deleted servers

Status: Resolved by PR `#79`.

Problem:

Deleting a server inventory entry could leave related SSH trusted host key records behind. That created stale trust data and weakened consistency between server inventory state and SSH trust state.

Resolution:

The trusted host key lifecycle is now enforced at the database layer.

Implemented changes:

- Increased the Room database version from `2` to `3`.
- Added a foreign key from `ssh_trusted_host_keys.server_id` to `servers.id`.
- Added cascade delete behavior for trusted host keys when the owning server is deleted.
- Added migration `2 -> 3`.
- Rebuilt the trusted host key table during migration to add the foreign key constraint.
- Removed orphaned trusted host key rows during migration.
- Generated and committed Room schema version `3`.
- Updated migration tests.
- Updated DAO and repository tests that depend on trusted host key persistence.
- Added repository-level validation that deleting a server removes its trusted host key.

Engineering rationale:

This is a persistence-layer invariant. The cleanup belongs in Room / SQLite referential integrity rather than in a ViewModel or a manual application-layer cleanup path.

This keeps responsibilities clean:

- ViewModels coordinate UI state and user actions.
- Repositories expose data operations.
- The database enforces data integrity.

---

### P2 — Duplicate SSH host key confirmation could be submitted

Status: Resolved by PR `#81`.

Problem:

A user could trigger SSH host key confirmation more than once before the first confirmation operation finished.

Resolution:

The SSH ViewModel now guards host-key confirmation while confirmation is already running.

Implemented changes:

- Added a ViewModel-level host-key confirmation in-progress guard.
- Ignored duplicate confirmation attempts while the first confirmation is running.
- Reset the guard with `finally` so failed or cancelled confirmation paths do not permanently block later attempts.
- Added focused ViewModel regression coverage for duplicate confirmation attempts.

Engineering rationale:

This is an interaction-safety issue at the presentation coordination boundary. The ViewModel owns the user action sequencing, while the domain use case remains responsible for the trust decision itself.

---

### P3 — Trusted-host accepted message contained stale placeholder wording

Status: Resolved by PR `#82`.

Problem:

The trusted-host accepted UI message still referenced remaining implementation gates. That wording no longer matched the current SSH workflow.

Resolution:

The accepted trusted-host detail message now tells the user to start the SSH connection again to use the trusted server identity.

Implemented change:

- Replaced the stale detail text in `SshHostTrustDecisionUiMapper.kt`.

Final user-facing detail text:

    Start the SSH connection again to use the trusted server identity.

Engineering rationale:

The message is a UI copy correction only. It should not change SSH trust, connection, or retry behavior.

---

### P4 — Documentation was behind the current SSH implementation state

Status: Addressed by PR `#83`.

Problem:

Some current-state documentation still described placeholder screens, shell behavior, planned SSH behavior, or older migration status as if they were current.

Resolution:

Current-state documentation was synchronized with the implemented SSH workflow.

Documents updated:

- `docs/ARCHITECTURE.md`
- `docs/state/SSH_STATUS.md`
- `docs/state/SERVER_INVENTORY_STATUS.md`
- `docs/PROJECT_STATE.md`
- `docs/ROADMAP.md`
- `docs/CHANGELOG.md`
- `docs/review/ENGINEERING_REVIEW_CHECKPOINT.md`

Implemented documentation corrections:

- Replaced stale SSH placeholder route and screen wording in current-state documents.
- Replaced current-state `shell` wording with `boundary` wording where the behavior has moved beyond placeholder shells.
- Documented database version `3` and the trusted-host cascade-delete migration.
- Documented duplicate host-key confirmation protection.
- Documented the trusted-host accepted message cleanup.
- Kept ADR history unchanged.

Documentation rationale:

Current-state documentation must describe behavior that exists in the current codebase. ADRs may retain historical decision context and should not be rewritten merely to remove older implementation-step wording.

---

## Reviewed Findings

### P6 — SSH authentication secret cleanup review

Status: Reviewed. No implementation change required.

Risk reviewed:

Sensitive SSH authentication values could remain in UI state, ViewModel memory, domain request objects, or service-layer mappings longer than necessary.

Review outcome:

The current SSH authentication flow already keeps secret handling bounded and ephemeral.

Confirmed behavior:

- `SshAuthenticationInputUiState` exposes only authentication method selection and presence flags.
- Passwords and passphrases are not stored in UI state snapshots.
- `SshScreen` clears its local password input after the Connect action.
- `SshViewModel` clears pending authentication secrets in the connection attempt `finally` block.
- `SshConnectionAttemptUseCase` clears authentication input in its `finally` block.
- Host-trust review and changed-host-key paths clear authentication input before returning a host-trust decision outcome.
- Authentication input models redact sensitive values from string representations.

Decision:

No Kotlin implementation change is required for the current reviewed flow.

Follow-up trigger:

Reopen this review only if the project adds private-key material, persistent credentials, saved authentication profiles, background reconnect, terminal sessions with credential prompts, or another workflow that stores or reuses authentication secrets beyond one connection attempt.

---

## Open Review Backlog

### P5 — SSH session close-vs-execute concurrency needs future review

Status: Future review item.

Risk:

As SSH execution behavior grows, race conditions may appear between command execution, session closing, and disconnect handling.

Current recommendation:

Do not add speculative concurrency infrastructure yet.

Revisit this when:

- multiple commands can overlap,
- command cancellation is introduced,
- session reuse expands,
- disconnect behavior becomes user-controllable,
- a concrete failing case appears.

---

## Lessons Learned

### Room migration schema assets must be available to instrumentation tests

Room migration tests require schema JSON files to be available as Android test assets.

The project now configures Android test assets so migration tests can load exported Room schemas.

Important distinction:

- Exporting schemas with KSP is not enough.
- Migration tests must also be able to read those schemas at runtime.

---

### Room migration tests can expose dependency mismatches before migration logic runs

During PR `#79`, migration tests initially failed before reaching migration logic because Room schema parsing hit a Kotlinx Serialization runtime mismatch.

Observed mismatch:

- `kotlinx-serialization-json` resolved to `1.8.1`.
- `kotlinx-serialization-core` resolved to `1.7.3`.

Resolution:

A Kotlinx Serialization BOM was added so serialization modules resolve consistently.

Useful commands:

    ./gradlew :app:dependencyInsight \
      --configuration debugAndroidTestRuntimeClasspath \
      --dependency kotlinx-serialization-core

    ./gradlew :app:dependencyInsight \
      --configuration debugAndroidTestRuntimeClasspath \
      --dependency kotlinx-serialization-json

Review rule:

When migration tests fail with runtime linkage errors, inspect dependency resolution before changing migration logic.

---

### SQLite cascade behavior requires foreign-key enforcement

SQLite cascade delete behavior only applies when foreign-key enforcement is enabled.

For raw migration-test database access, tests that directly verify cascade behavior may need:

    setForeignKeyConstraintsEnabled(true)

This is a test-environment requirement, not a production workaround.

---

### Keep feature PRs focused

PR `#79` stayed focused on database referential integrity and related validation.

PR `#81` stayed focused on duplicate host-key confirmation interaction safety.

PR `#82` stayed focused on a single user-facing trusted-host accepted message.

PR `#83` stayed focused on documentation synchronization after focused implementation fixes were merged.

That was the correct scope.

Do not mix the following into the same PR unless they are directly required:

- unrelated documentation synchronization,
- unrelated UI text fixes,
- unrelated concurrency guards,
- broad refactors,
- future security cleanup.

---

### Interaction guards belong at the user-action coordination boundary

The duplicate host-key confirmation fix belongs in the ViewModel because the ViewModel coordinates user actions and UI state transitions.

The domain use case should remain focused on evaluating and saving the trust decision.

Avoid moving user-action deduplication into persistence or domain logic unless the duplicate action creates a true domain invariant.

---

### Documentation synchronization should happen after focused fixes

PR `#83` synchronized current-state documentation only after PR `#81` and PR `#82` were merged.

This avoided mixing broad documentation edits with focused behavior and copy fixes.

Review rule:

When multiple small implementation issues are already identified, fix them in focused PRs first, then synchronize broad current-state documentation in a dedicated documentation PR.

---

## Review Rules Going Forward

Before recommending implementation or architecture changes:

1. Inspect current repository files.
2. Prefer current repository state over older uploaded snapshots or previous chat context.
3. Keep each PR focused.
4. Use dedicated documentation PRs for broad documentation synchronization.
5. Commit Room schema files for database version changes.
6. Validate migrations with instrumentation tests.
7. Keep data-integrity rules in the database when the invariant is relational.
8. Avoid solving persistence invariants in ViewModels.
9. Keep interaction-safety guards at the ViewModel boundary when the issue is user-action coordination.
10. Keep documentation in professional English.
11. Keep code comments in English.
12. Use Conventional Commits.
13. Keep `main` releasable.

---

## Checkpoint Closure

This engineering review checkpoint is closed.

Closed scope:

- P1 was resolved by PR `#79`.
- P2 was resolved by PR `#81`.
- P3 was resolved by PR `#82`.
- P4 was addressed by PR `#83`.
- P6 was reviewed by PR `#85` and finalized by PR `#86`.

Remaining backlog:

- P5 remains recorded as a future review item only.

P5 must not be implemented speculatively. Revisit it only when a concrete trigger appears, such as overlapping command execution, command cancellation, expanded session reuse, user-controllable disconnect behavior, or a failing concurrency case.

Next planning rule:

Select the next implementation slice from the project roadmap or current product priorities. Do not continue this checkpoint unless a new review finding is discovered from current repository inspection.

