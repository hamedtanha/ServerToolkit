# Architecture Review Charter

> **Review ID:** `RA-2026.07-v1`
>
> **Status:** Accepted
>
> **Evidence baseline:** `0135faf89b1035fd91c75b37a25ec51bc7c71074`
>
> **Governing Issue:** `#135`
>
> **Repository:** `hamedtanha/ServerToolkit`

## Purpose

This review establishes the architecture knowledge and evidence framework required to understand ServerToolkit's current implementation before selecting the next major product, Server-domain, capability, navigation, or UI increment.

The review must describe the repository that exists. It must not redesign the application implicitly or present target-state proposals as implemented behavior.

## Review Questions

1. What are the verified repository, feature, package, persistence, navigation, and runtime boundaries?
2. Which component owns each important model, lifecycle, state transition, and external integration?
3. Which dependency directions are implemented, documented, accepted, or violated?
4. Which current-state documents remain accurate against the evidence baseline?
5. Which claims are stale, incomplete, unsupported, or expressed at the wrong authority level?
6. Which accepted trade-offs and technical risks materially affect the next architecture decisions?
7. What evidence and governance are required before reviewing the central `Server` domain and a modern operational UI?

## Phase 1 Scope

- repository and package topology;
- feature ownership;
- dependency direction;
- app navigation topology;
- Room database topology and migration baseline;
- Server Inventory model and persistence baseline;
- SSH trust, authentication, session, command execution, history, and cleanup boundaries;
- Saved Commands ownership and SSH command-input integration;
- documentation, ADR, CI, release, and review topology;
- current invariants, accepted trade-offs, stale claims, and known gaps;
- Architecture Atlas publication and maintenance contract.

## Follow-up Scope

A later review may assess:

- `Server` as the central domain concept;
- stable identity and endpoint ownership;
- multi-level server profiles;
- trust and authentication-reference ownership;
- platform and capability evidence;
- profile freshness and invalidation;
- server inspection;
- curated command catalogs;
- SSH workspace and navigation design;
- compact and expanded-window UI architecture;
- persistence and migration implications.

Follow-up topics are not accepted implementation scope in this review.

## Explicit Non-Goals

- no Android production-code change;
- no Room entity, schema, or migration change;
- no `Server` domain-model change;
- no package or Gradle-module restructuring;
- no Compose or navigation implementation;
- no Capability Gateway, Provider, registry, or plugin implementation;
- no operating-system discovery;
- no command catalog;
- no automatic or background command execution;
- no new platform, transport, service-manager, or vendor support claim;
- no wholesale copy of Leannect architecture or documentation.

## Evidence Hierarchy

| Evidence class | Role | Authority |
|---|---|---|
| Implementation and repository configuration | Executable behavior, ownership, dependency, schema, build, and workflow evidence | Primary |
| `docs/PROJECT_STATE.md` and `docs/state/` | Accepted current implementation summary | Current-state authority |
| Accepted ADRs | Durable decision and rationale | Governing intent |
| Architecture and engineering policy documents | Accepted implementation and delivery rules | Policy authority |
| Roadmap and changelog | Planned direction and notable change history | Supporting authority |
| `review/` | Time-bound findings, assessments, and proposals | Historical evidence |

When a current-state statement conflicts with executable repository evidence, executable evidence governs and the current-state document must be corrected.

## Claim Labels

- **Verified** — directly corroborated by implementation, build, test, workflow, schema, or repository configuration.
- **Documented** — established by an authoritative current-state document but not exhaustively retraced in the current review pass.
- **Constrained** — implemented behavior whose validity depends on runtime, platform, release, environment, or local evidence.
- **Target** — accepted or proposed direction that is not implemented.
- **Deferred** — intentionally postponed with a defined reason or trigger.
- **Rejected** — reviewed and intentionally not selected.

## Initial Verified Baseline

At the evidence baseline:

- `main` points to `0135faf89b1035fd91c75b37a25ec51bc7c71074`;
- no pull request is open;
- PR `#134` is merged and Issue `#133` is closed;
- Issue `#122` remains open;
- Room database version is `5`;
- persisted areas are Server Inventory, trusted SSH host keys, SSH connection history, and Saved Commands;
- Server Inventory owns the current flat `Server` record and Room persistence;
- SSH owns trust, authentication, session lifecycle, command input, explicit execution, output, history, and cleanup;
- Saved Commands owns global reusable command definitions and exposes only its domain repository contract to SSH presentation;
- no Architecture Atlas, engineering-handbook index, or immutable review structure existed before this review branch.

## Expected Review Outputs

The complete Phase 1 review is expected to publish:

```text
docs/ARCHITECTURE_ATLAS.md
docs/engineering/README.md
review/README.md
review/INDEX.md
review/architecture/2026/RA-2026.07-v1/00-Review-Charter.md
review/architecture/2026/RA-2026.07-v1/01-Executive-Summary.md
review/architecture/2026/RA-2026.07-v1/02-Current-Repository-Atlas.md
review/architecture/2026/RA-2026.07-v1/STATUS.md
```

Existing authoritative documents may change only when required to register the new documentation roles, correct verified stale claims, or maintain navigation and consistency.

## Decision Model

Review recommendations use one of:

- Accepted;
- Accepted with Modification;
- Deferred;
- Rejected;
- Needs More Evidence.

Only accepted decisions may update governing ADRs or authorize focused implementation planning.

## Completion Criteria

- exact evidence baseline recorded;
- scope and non-goals remain explicit;
- current and target state remain distinguishable;
- repository and major lifecycle maps are evidence-backed;
- documentation authority and conflict resolution are explicit;
- living documentation and immutable review evidence remain separate;
- stale current-state claims found in scope are corrected;
- no production code, schema, module, or UI behavior changes;
- related documentation is reviewed for consistency;
- `git diff --check` passes;
- repository Android Validation passes;
- review status is changed to `Accepted` only after merge-ready evidence is complete;
- `Published` is reserved for a metadata-only publication change after the accepted review package is merged into `main`.
