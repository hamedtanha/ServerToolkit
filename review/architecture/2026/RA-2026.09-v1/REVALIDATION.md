# RA-2026.09-v1 Current-HEAD Revalidation

**Date:** 2026-09-05  
**Original report:** `STK-ARCH-2026-09-05`  
**Original evidence baseline:** `2800f3a250e9b2733dc040a69a9a1f851538d84e`  
**Revalidated main:** `e526b6d6f73713ce23e419ee275ad8ba3d4745a6`  
**Governing Issue:** `#166`

## Purpose

This document records the repository state after the external review baseline without modifying or correcting the original review artifact.

The original report remains evidence against its exact reviewed commit. This revalidation records later repository drift and the current disposition of stable finding IDs F01–F10.

## Baseline Drift

`main` is two commits ahead of the original review baseline:

1. `e518a17502e821e1fcfc69300b81ed8310a4778b` — `docs: verify main branch governance`
2. `e526b6d6f73713ce23e419ee275ad8ba3d4745a6` — `docs: consolidate documentation governance`

The drift contains repository/documentation-governance changes only. No Android production or test source changed between the original review baseline and the revalidation baseline.

## Current Repository Checkpoint

At revalidation:

- `main` is protected by the active `ServerToolkit main governance` ruleset.
- Pull requests are required for normal changes to `main`.
- `Validate Android project` is a required status check.
- Issue `#160` is closed as completed.
- Issue `#159` is closed as completed after root documentation-governance consolidation.
- Issue `#161` remains open for constrained collection layout.
- There are no open pull requests before this registration work begins.
- The only remote branch before this registration work begins is `main`.

## Finding Revalidation

| ID | Current disposition | Revalidation note |
|---|---|---|
| F01 | **CONFIRMED** | No production-code drift affected the cancellation-sensitive connected-session ownership handoff. |
| F02 | **CONFIRMED** | No production-code drift affected command completion-before-drain ordering or unbounded `readBytes()` retention. |
| F03 | **CONFIRMED** | No production-code drift affected host-key fingerprint generation from `PublicKey.encoded`. |
| F04 | **NEEDS_RUNTIME_REPRODUCTION** | Static lifecycle risk remains; device/lifecycle reproduction is still required before treating all terminal exit paths as demonstrated defects. |
| F05 | **CONFIRMED** | No production-code drift changed Inventory/Add/Edit/History failure and cancellation semantics identified by the review. |
| F06 | **CONFIRMED** | CI still compiles/assembles Android tests but does not execute connected or managed-device instrumentation tests. |
| F07 | **CONFIRMED** | The repository remains a single `:app` module without an executable architecture dependency rule check. |
| F08 | **FIXED** | Issue `#160` / PR `#164` established and verified the active `main` governance ruleset. |
| F09 | **PARTIALLY_FIXED** | Issue `#159` / PR `#165` removed competing root governance/process authorities. Residual stale agent-bootstrap/package/ADR-index facts remain separately visible and require focused synchronization. |
| F10 | **CONFIRMED** | The constrained Server Inventory collection layout remains tracked by Issue `#161` and ADR-017. |

## Residual F09 Evidence

The governance conflict portion of F09 is resolved, but current repository documentation still contains narrower factual drift:

- `PACKAGE_STRUCTURE.md` still presents `ui/theme` and omits implemented `core/connection`, while production source contains `ui/designsystem/theme` and `core/connection/...`.
- `docs/ai/INTRO.md` still describes the product primarily as a Linux administration application and lists named integrations as project focus, while `docs/PROJECT_STATE.md` defines the current platform-neutral remote-systems direction.
- `docs/ARCHITECTURE.md` still has a local accepted-ADR table ending at ADR-016, while `docs/adr/README.md` records accepted ADR-017.

These are documentation synchronization issues. They do not change the accepted feature-first architecture or authorize unrelated cleanup.

## Revalidated Priority

The smallest justified correction sequence remains:

1. F01 — cancellation-safe connected-session ownership.
2. F02 — concurrent bounded SSH command output draining under one operation deadline.
3. F03 — OpenSSH-compatible fingerprinting only after an explicit legacy-trust compatibility decision.

F04 remains reproduction-gated. F06 is a confirmed assurance gap and should be addressed after the immediate P1 runtime corrections unless new evidence changes sequencing.

## Registration Boundary

This revalidation is part of review registration only.

It does not modify Android production code, tests, Room schemas, trust records, SSH runtime behavior, design-system tokens, roadmap scope, repository rulesets, or accepted ADR text.
