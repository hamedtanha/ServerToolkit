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

Date: 2026-07-08

Resolved pull request:

- PR: `#79`
- Branch: `fix/cascade-delete-ssh-trusted-host-keys`
- Commit: `49876a9 fix: cascade delete ssh trusted host keys`

Local validation completed:

    ./gradlew connectedDebugAndroidTest
    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

GitHub status:

- No GitHub CI checks were reported for PR `#79`.
- Validation was performed locally.

---

## Resolved Findings

### P1 — SSH trusted host keys were not lifecycle-bound to deleted servers

Status: Resolved.

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

## Open Review Backlog

### P2 — Duplicate SSH host key confirmation can still be submitted

Status: Resolved by PR #81.

Risk:

A user may trigger SSH host key confirmation more than once before the first confirmation operation finishes.

Recommended direction:

Add a ViewModel-level guard that prevents duplicate confirmation while confirmation is already in progress.

Suggested branch:

    fix/prevent-duplicate-ssh-host-key-confirmation

Expected validation:

- Add a ViewModel test for duplicate confirmation attempts.
- Verify that a second confirmation action is ignored or blocked while the first one is running.
- Keep the fix focused on interaction safety, not SSH transport behavior.

---

### P3 — Trusted-host accepted message contains stale placeholder wording

Status: Resolved by PR #82.

Problem:

The accepted trusted-host UI message still references remaining implementation gates. That wording is stale and no longer matches the current SSH workflow.

Expected location:

    SshHostTrustDecisionUiMapper.kt

Recommended replacement text:

    Start the SSH connection again to use the trusted server identity.

Suggested branch:

    fix/update-trusted-host-accepted-message

Expected validation:

- Run unit tests.
- Check affected UI mapper tests if present.
- Keep the PR small and focused.

---

### P4 — Documentation is behind the current SSH implementation state

Status: Addressed by the SSH current-state documentation synchronization.

Known risk:

Some documentation may still describe placeholder screens, shell behavior, or planned SSH behavior as if it were current.

Recommended direction:

Create a dedicated documentation synchronization PR after the focused fix PRs are merged.

Suggested branch:

    docs/sync-ssh-current-state

Documents to review:

- Architecture documentation.
- SSH status documentation.
- Server inventory status documentation.
- Project state documentation.
- Roadmap documentation.
- Changelog or release notes, if applicable.

Documentation rule:

Only document behavior that exists in the current codebase and has been validated.

---

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

That was the correct scope.

Do not mix the following into the same PR unless they are directly required:

- unrelated documentation synchronization,
- unrelated UI text fixes,
- unrelated concurrency guards,
- broad refactors,
- future security cleanup.

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
9. Keep documentation in professional English.
10. Keep code comments in English.
11. Use Conventional Commits.
12. Keep `main` releasable.

---

## Next Suggested Checkpoint

After PR `#79` is merged into `main`, the next focused engineering item is P2.

Suggested next branch:

    fix/prevent-duplicate-ssh-host-key-confirmation

Expected checkpoint for P2:

- Add duplicate-confirmation guard.
- Add ViewModel-level tests.
- Run local validation.
- Open a focused PR.
- Do not combine P2 with broad documentation synchronization.

Recommended validation:

    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

If instrumentation coverage is affected:

    ./gradlew connectedDebugAndroidTest
