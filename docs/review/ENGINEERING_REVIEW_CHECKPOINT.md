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
- PR `#87`: `docs: close engineering review checkpoint`
- PR `#88`: `fix: improve ssh failure state messages`
- PR `#89`: `docs: record ssh runtime failure mapping review`
- PR `#90`: `fix: serialize ssh session close and command execution`
- PR `#91`: `docs: update engineering review checkpoint after ssh hardening`
- PR `#92`: `docs: add AI change impact workflow rules`
- PR `#93`: `test: cover ssh command timeout stream suppression`
- PR `#94`: `test: cover ssh command cancellation cleanup failure`
- PR `#95`: `test: cover trusted ssh connection cancellation cleanup`
- PR `#96`: `test: cover ssh failure ui mapper states`

Current `main` state after PR `#96`:

- Trusted SSH host keys are lifecycle-bound to their owning server inventory entries.
- Duplicate SSH host key confirmation attempts are guarded at the ViewModel boundary.
- The trusted-host accepted message no longer references obsolete implementation gates.
- Current-state SSH documentation is synchronized with the implemented ephemeral password connection and non-interactive command execution workflow.
- SSH connection and command execution failure states use specific non-interactive SSH guidance.
- SSH runtime failure mapping was reviewed; no additional domain error category is required without runtime evidence.
- SSH session close cleanup and command execution are serialized at the SSHJ session-owner boundary.
- SSH command timeout stream suppression is covered by regression tests.
- SSH command cancellation remains preserved when command-channel cleanup fails.
- Trusted SSH connection cancellation remains preserved when client cleanup fails.
- SSH connection and command failure UI mapper states are covered by regression tests.
- The current SSH timeout, cleanup, cancellation, and failure-mapping hardening coverage pass is complete.
- No generic SSH hardening backlog remains open without new runtime evidence or a new focused review finding.

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

For PR `#88`:

    ./gradlew testDebugUnitTest
    git diff --check
    ./gradlew :app:assembleDebug

For PR `#89`:

    git diff --check HEAD~1..HEAD

For PR `#90`:

    ./gradlew testDebugUnitTest
    git diff --check
    ./gradlew :app:assembleDebug

For PR `#92`:

    git diff --check

For PR `#93` through PR `#96`:

    ./gradlew testDebugUnitTest
    ./gradlew :app:assembleDebug
    git diff --check

Each regression-coverage pull request also ran the relevant targeted unit test for the changed boundary before full validation.

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

### P5 — SSH session close-vs-execute concurrency needed review

Status: Resolved by PR `#90`.

Risk:

Command execution and session close cleanup could race on the same SSHJ-backed session owner if close and execute were invoked concurrently.

Resolution:

The SSHJ session owner now serializes command execution and close cleanup through a shared owner-level lifecycle lock.

Implemented changes:

- Added owner-level serialization between `SshjSessionOwner.execute()` and `SshjSessionOwner.close()`.
- Kept SSHJ client/session ownership inside the data-layer boundary.
- Added focused regression coverage for close requests while command execution is running.
- Updated SSH status and changelog documentation.

Engineering rationale:

The SSHJ client is a concrete data-layer resource owned by `SshjSessionOwner`. Serializing close cleanup and command execution at that owner boundary keeps concurrency control close to the resource without leaking SSHJ lifecycle concerns into domain, presentation, or ViewModel code.

---

### P7 — SSH hardening coverage pass required closure

Status: Reviewed and closed after PRs `#93` through `#96`.

Risk reviewed:

After the original SSH engineering checkpoint was closed, current-state documents still pointed to broad timeout, cleanup, cancellation, and failure-mapping hardening. That could keep the project in an open-ended hardening loop instead of moving to evidence-driven work selection.

Review outcome:

The current hardening coverage pass is complete for the reviewed SSH boundaries.

Confirmed coverage:

- SSH command timeout stream suppression.
- SSH command cancellation preservation when command-channel cleanup fails.
- Trusted SSH connection cancellation preservation when client cleanup fails.
- SSH connection and command failure UI mapper states.

Decision:

No additional generic SSH hardening work is currently tracked.

Future SSH hardening must come from concrete runtime evidence, current repository inspection, or a newly recorded focused review finding.

---

## Open Review Backlog

### P8 — Active SSH sessions are not released when the SSH workflow exits

Status: Partially resolved.

Recorded: 2026-07-13.

Updated: 2026-07-13.

Implemented resolution:

- `SshViewModel.onWorkflowExit()` closes the active project-owned session through `SshSessionLifecycleService`.
- Back navigation, system back, and connection-history navigation wait for cleanup before leaving the SSH route.
- `Closed` and `NotFound` clear the active session state and permit navigation.
- `Failed` restores the active session state and keeps the route open so cleanup can be retried.
- Duplicate workflow-exit requests do not start duplicate close operations.
- Workflow exit is blocked while connection or command execution remains in progress.
- Successful cleanup clears stale command output and resets presentation state to not connected.
- SSHJ cleanup runs on the IO dispatcher inside a non-cancellable cleanup context.
- Route navigation checks coroutine cancellation after cleanup and before invoking navigation.
- Focused lifecycle and ViewModel regression coverage has been added.
- Manual Android runtime verification completed without an observed application crash or SSH lifecycle error.

Remaining resolution:

- Add an explicit user-facing disconnect action for connected sessions.
- Runtime-verify explicit disconnect behavior.
- Reassess the remaining version `0.4.0` milestone afterward.

Residual limitation:

- A connected user cannot disconnect while remaining on the SSH screen.
- Permanent workflow exit is currently the supported user-triggered session cleanup path.

Scope boundary:

This finding does not authorize:

- interactive terminal implementation;
- saved command workflows;
- background session retention;
- persistent credentials;
- new SSHJ types outside the data layer;
- unrelated SSH hardening.

Decision:

Automatic active-session release on permanent SSH workflow exit is implemented and runtime-verified.

P8 remains partially open only for the explicit user-facing disconnect action. Version `0.4.0` should not be closed until that behavior is implemented and runtime-verified.

Future SSH hardening must continue to be driven by concrete runtime evidence, current repository inspection, or a newly recorded focused review finding.

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
PR `#88` stayed focused on presentation-layer SSH failure-state wording.
PR `#89` stayed focused on recording runtime failure-mapping review findings without speculative implementation.
PR `#90` stayed focused on SSHJ session-owner close-vs-execute serialization and its regression coverage.
PR `#92` stayed focused on AI change-impact workflow rules.
PR `#93` stayed focused on SSH command timeout stream-suppression regression coverage.
PR `#94` stayed focused on SSH command cancellation cleanup-failure regression coverage.
PR `#95` stayed focused on trusted SSH connection cancellation cleanup regression coverage.
PR `#96` stayed focused on SSH failure UI mapper state coverage.

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
- P5 was resolved by PR `#90`.
- P6 was reviewed by PR `#85` and finalized by PR `#86`.
- SSH failure-state wording was improved by PR `#88`.
- SSH runtime failure mapping was reviewed by PR `#89`.
- SSH session close and command execution serialization was resolved by PR `#90`.

Post-closure hardening coverage pass:

- AI change-impact workflow rules were added by PR `#92`.
- SSH command timeout stream suppression was covered by PR `#93`.
- SSH command cancellation cleanup-failure behavior was covered by PR `#94`.
- Trusted SSH connection cancellation cleanup behavior was covered by PR `#95`.
- SSH failure UI mapper states were covered by PR `#96`.

Remaining backlog:

- No open review findings are currently tracked for this checkpoint.
- No generic SSH timeout, cleanup, cancellation, or failure-mapping hardening backlog remains open without new runtime evidence.

Next planning rule:

Select the next implementation slice from the project roadmap or current product priorities. Do not continue this checkpoint unless a new review finding is discovered from current repository inspection or runtime verification.

