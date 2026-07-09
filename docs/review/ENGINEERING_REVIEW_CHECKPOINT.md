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

Current `main` state after PR `#83`:

- Trusted SSH host keys are lifecycle-bound to their owning server inventory entries.
- Duplicate SSH host key confirmation attempts are guarded at the ViewModel boundary.
- The trusted-host accepted message no longer references obsolete implementation gates.
- Current-state SSH documentation is synchronized with the implemented ephemeral password connection and non-interactive command execution workflow.
- The open review backlog has been reduced to future concurrency and security-hygiene review items.

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

### P6 — Secret cleanup should be reviewed

Status: Future security hygiene item.

Risk:

Sensitive values may remain in UI state or ViewModel memory longer than necessary.

Current recommendation:

Review lifecycle cleanup for screens that handle secrets, credentials, passphrases, private keys, or SSH authentication inputs.

Do not add broad cleanup logic before confirming which values are actually retained.

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

## Next Suggested Checkpoint

The next focused engineering checkpoint is P6.

Suggested next branch:

    review/ssh-secret-cleanup

Expected checkpoint for P6:

- Inspect current SSH authentication input handling.
- Confirm which sensitive values are retained in UI state, ViewModel fields, domain request objects, and service boundaries.
- Verify whether existing cleanup paths clear password and passphrase values after connection attempts.
- Do not add broad cleanup logic before identifying a concrete retained value or lifecycle gap.
- Keep terminal UI, saved commands, persistent credentials, and private-key authentication out of scope.

Recommended inspection commands:

    grep -RIn "password\|passphrase\|privateKey\|authenticationInput\|AuthenticationInput" app/src/main/java app/src/test/java

Recommended validation if code changes become necessary:

    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

If no code change is needed, record the review outcome in documentation only.

