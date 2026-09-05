# Repository Governance Status

**Status:** Active
**Last Verified:** 2026-09-05

## Purpose

This document records the enforced repository-governance baseline for `hamedtanha/ServerToolkit`.

It describes repository settings that are not fully represented by source-controlled files and therefore require explicit verification evidence.

## Main Branch Governance

The `main` branch is protected by the active repository ruleset:

- **Ruleset name:** `ServerToolkit main governance`
- **Ruleset ID:** `22328882`
- **Target:** `refs/heads/main`
- **Enforcement:** `active`

The ruleset enforces the following normal development path:

```text
feature branch
-> pull request
-> required Android validation
-> merge to main
```

### Enforced Rules

- Changes to `main` require a pull request.
- The `Validate Android project` status check is required before merge.
- Required approving review count is `0`, which is intentional for the current single-maintainer repository model.
- Non-fast-forward updates are blocked.
- Branch deletion is blocked.
- The ruleset does not require strict branch freshness before merge.
- Server-side signed-commit enforcement is not enabled.

## Maintainer Recovery Boundary

The repository owner is configured as a bypass actor with:

```text
bypass_mode = pull_request
```

This preserves an emergency recovery path inside the pull-request workflow without making direct writes to `main` part of the normal operating model.

The bypass exists for exceptional repository recovery only. Routine development continues to follow GitHub Flow through pull requests and required validation.

## Required Validation

The required check is produced by:

```text
Workflow: Android Validation
Job:      Validate Android project
```

The workflow is defined in:

`/.github/workflows/android-validation.yml`

It runs for pull requests targeting `main` and for pushes to `main`.

## Governance Change Rule

Changes to this repository-governance baseline must be deliberate and reviewable. If the ruleset, bypass model, required status checks, merge policy, or maintainer model changes, this document and the relevant project documentation must be reviewed for consistency.

## Verification Record

The ruleset was created and verified on 2026-09-05 after Issue #160 identified that `main` was previously unprotected.

A focused pull request from `chore/verify-main-governance` is used to validate that the protected-branch workflow and required Android Validation check operate as configured.
