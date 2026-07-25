# Current Repository Atlas Assessment

> **Review ID:** `RA-2026.07-v1`
>
> **Status:** In Progress
>
> **Evidence baseline:** `0135faf89b1035fd91c75b37a25ec51bc7c71074`
>
> **Governing Issue:** `#135`

## Purpose

This document records the time-bound evidence and findings used to construct `docs/ARCHITECTURE_ATLAS.md`.

The living Atlas may change as the repository changes. This review record becomes immutable after publication.

## Evidence Set

### Implementation and Configuration

| Evidence | Verified concern |
|---|---|
| `feature/serverinventory/domain/model/Server.kt` | Current Server domain fields |
| `feature/serverinventory/data/local/entity/ServerEntity.kt` | Current flat Room representation |
| `core/database/ServerToolkitDatabase.kt` | Database version and registered entities |
| `core/database/ServerToolkitDatabaseMigrations.kt` | Explicit migration sequence and relationships |
| `navigation/AppNavHost.kt` | Destination registration and navigation topology |
| `PACKAGE_STRUCTURE.md` | Canonical feature and package ownership |
| `.github/workflows/android-validation.yml` | Current CI triggers, environment, and Gradle tasks |
| `scripts/release/sign-android-apk.sh` and release documentation | Local release-signing boundary |

### Current-State and Policy Documents

| Evidence | Verified concern |
|---|---|
| `docs/PROJECT_STATE.md` | Primary implementation summary |
| `docs/ARCHITECTURE.md` | Practical architecture and dependency rules |
| `docs/ENGINEERING_STRATEGY.md` | Task selection and delivery rules |
| `docs/DOCUMENTATION.md` | Source-of-truth and synchronization rules |
| `docs/state/SERVER_INVENTORY_STATUS.md` | Inventory baseline |
| `docs/state/SSH_STATUS.md` | SSH lifecycle and verification baseline |
| `docs/state/SAVED_COMMANDS_STATUS.md` | Saved Commands ownership and SSH integration |
| `docs/ROADMAP.md` | Current milestone direction |
| `docs/RELEASES.md` | Release process and signing workflow |

### Accepted Decisions

| ADR | Governing concern |
|---|---|
| ADR-002 | Android application architecture |
| ADR-003 | Room persistence |
| ADR-004 | Navigation strategy |
| ADR-005 | Hilt dependency injection |
| ADR-006 through ADR-013 | SSH trust, authentication, session, command, secret, and backup boundaries |
| ADR-014 | Android release signing |
| ADR-015 | Platform-neutral product direction |
| ADR-016 | Three-level remote capability architecture |

### GitHub State

At evidence collection:

- `main` resolved to `0135faf89b1035fd91c75b37a25ec51bc7c71074`;
- PR `#134` was merged;
- Issue `#133` was closed;
- no pull request was open before the review branch was created;
- Issue `#122` remained open;
- Issue `#135` governs this review.

## Verified Architecture Findings

### F-001 — Feature Ownership Is Explicit

**Classification:** Verified strength

Dashboard, Server Inventory, SSH, and Saved Commands have distinct feature ownership. Cross-feature Saved Command use occurs through `SavedCommandRepository`, not through data or presentation implementation imports.

### F-002 — Room Ownership Is Centralized Only for Schema Aggregation

**Classification:** Verified accepted trade-off

Feature entities and DAOs remain feature-owned. `core/database` references them because Room requires one aggregated application database.

### F-003 — Server Is Currently a Flat Inventory Record

**Classification:** Verified current constraint

The current Server model contains identity, one SSH endpoint, and user-managed inventory metadata. It has no layered endpoint, trust, authentication-reference, platform, capability, operational, or freshness model.

This is not recorded as an implementation defect. It is a trigger for a separate domain review before expanding the product around Server profiles.

### F-004 — SSH Has Mature Lifecycle Guardrails

**Classification:** Verified strength

The SSH feature documents and verifies explicit host trust, ephemeral authentication, project-owned sessions, command-channel execution, deterministic cleanup, duplicate suppression, cancellation preservation, and stale-result protection.

### F-005 — Saved Commands Preserves Execution Separation

**Classification:** Verified strength

Saved Commands owns persistence and observation. SSH owns mutable input and execution. Selection replaces exact text and never invokes execution.

### F-006 — No Production Capability Gateway Exists

**Classification:** Verified current state

ADR-016 defines future responsibility boundaries. The repository intentionally has no speculative Gateway, Provider registry, plugin framework, platform discovery, or provider package hierarchy.

### F-007 — Architecture Knowledge Was Fragmented

**Classification:** Confirmed documentation gap

Before Issue `#135`, no integrated Architecture Atlas, engineering-handbook entry point, or immutable review structure existed.

### F-008 — Current-State Documentation Contained Stale Claims

**Classification:** Confirmed consistency defect

The evidence baseline contained stale statements in Project State, SSH Status, and Release Process. These statements are corrected in the same focused documentation change.

### F-009 — Saved Command Planning Issue Remains Open

**Classification:** Administrative follow-up

Issue `#122` has all planning acceptance criteria checked and its three accepted slices are implemented, but the issue remains open. Closing it should be a separate explicit GitHub action after final verification.

## Dependency Assessment

Verified allowed directions:

```text
presentation -> domain
data -> domain
feature DI -> domain and data
app navigation -> feature destinations
core/database -> feature Room entities and DAOs
SSH presentation -> SavedCommandRepository
```

No evidence in the reviewed sources requires a package or Gradle-module restructure for Phase 1.

## Runtime Assessment

The connected SSH lifecycle has one coherent owner:

```text
Server Inventory target
-> SSH target resolution
-> trust decision
-> ephemeral authentication
-> session handle
-> explicit command execution
-> output
-> disconnect or workflow-exit cleanup
```

A future screen decomposition must not create independent session owners or bypass cleanup sequencing.

## Persistence Assessment

Current schema relationships are appropriate for implemented behavior:

- trusted host keys belong to a Server and cascade on Server deletion;
- connection history belongs to a Server and cascades on Server deletion;
- Saved Commands are global;
- credential secrets are excluded from ordinary Room persistence.

A multi-level Server Profile would require a new persistence assessment. No version `5 -> 6` migration is accepted by this review.

## Documentation Assessment

The accepted hierarchy after this review is:

```text
repository evidence
-> focused current-state authority
-> practical architecture
-> integrated Architecture Atlas
-> immutable time-bound review evidence
```

The Atlas is a navigation and integration surface. It is not a higher authority than implementation, focused state documents, or accepted ADRs.

## Review Conclusion

The current architecture is suitable for continued incremental development. The project should not jump directly from the current flat Server record to a speculative universal profile schema.

The next architecture activity should be a separate Server Domain and UX Architecture Review that defines ownership, invariants, lifecycle, persistence implications, and decision triggers before production implementation.
