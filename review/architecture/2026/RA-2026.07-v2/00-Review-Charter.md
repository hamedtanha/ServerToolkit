# Server Domain and Operational UX Review Charter

> **Review ID:** `RA-2026.07-v2`
>
> **Status:** Published
>
> **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
>
> **Governing Issue:** `#138`
>
> **Repository:** `hamedtanha/ServerToolkit`

## Purpose

This review determines the long-term architecture boundary of the central `Server` concept and the operational user experience built around it.

The review must describe the repository that exists at the evidence baseline, compare target alternatives explicitly, and prevent implementation-first development from establishing durable product or architecture decisions accidentally.

Published review `RA-2026.07-v1` remains immutable and is an input to this review, not a document to revise.

## Review Objectives

The review will produce evidence-backed recommendations for:

1. the role of `Server` as a central domain concept or aggregate root;
2. stable Server identity and endpoint ownership;
3. separation of user-defined, observed, trusted, historical, and transient data;
4. ownership and lifecycle of platform and capability evidence;
5. SSH workspace, navigation, and operational user-experience boundaries;
6. persistence, migration, security, and support-claim implications;
7. ADR, evidence-task, and bounded implementation follow-up requirements.

Review recommendations do not authorize production implementation by themselves.

## Review Scope

### Server Identity and Endpoint Ownership

Assess:

- whether `Server` should remain the primary aggregate boundary;
- identity alternatives independent from mutable host, port, and username values;
- Server-to-endpoint cardinality and ownership;
- trusted-host-key relationship to Server identity and endpoint identity;
- endpoint replacement, host-key rotation, cloned infrastructure, and rebuilt systems;
- deletion and historical-record implications.

### Server Profile Layers

Assess whether a future Server Profile should distinguish:

- user-defined metadata;
- endpoint and connection metadata;
- trust evidence;
- authentication references;
- observed platform facts;
- observed capabilities;
- operational preferences;
- historical observations;
- transient session state.

For every proposed layer, identify:

- owning domain or feature boundary;
- source of truth;
- mutability;
- persistence requirement;
- freshness and invalidation rule;
- security classification;
- evidence classification;
- relationship to current implementation.

### Platform and Capability Evidence

Assess:

- which platform facts provide durable product value;
- which facts must remain session-scoped;
- observation source and timestamp requirements;
- evidence freshness and invalidation;
- reinstall, restore, replacement, and operating-system-change behavior;
- platform-identification versus capability-discovery boundaries;
- the concrete trigger required before ADR-016 permits a Capability Gateway or Provider-Adapter path;
- speculative abstractions that must remain rejected or deferred.

### SSH Workspace and Operational UX

Assess:

- focused SSH screen versus Server workspace alternatives;
- ownership of server context, trust, authentication input, session state, command input, results, history, and Saved Command selection;
- durable versus transient UI and domain state;
- compact and expanded-window behavior;
- destination-oriented versus workspace-oriented navigation;
- preservation of explicit execution, execution-state blocking, lifecycle cleanup, timeout and cancellation behavior, and stale-result suppression;
- evidence required before a UX direction may be accepted.

### Persistence and Migration

Assess:

- concepts that may require entities, relationships, or indices;
- incremental evolution of the current flat `Server` representation;
- migration ordering and backward-compatibility constraints;
- data that must not be persisted;
- schema decisions that require an ADR;
- proof requirements before any Room version change.

### Security and Support Claims

Assess:

- sensitive operational metadata;
- authentication-reference storage without credential persistence;
- separation of trust evidence from user labels and observed facts;
- platform, shell, package-manager, service-manager, capability, transport, and vendor support claims;
- evidence required to move a claim from target or constrained to verified.

## Explicit Non-Goals

- no Android production-code change;
- no `Server` model or Room entity change;
- no Room schema version or migration change;
- no SSH screen, Compose, or navigation implementation;
- no Server Profile implementation;
- no platform detection or capability discovery;
- no command catalog or automatic command execution;
- no credential persistence;
- no Capability Gateway, Provider, Adapter, registry, or plugin implementation;
- no new support claim;
- no modification of published review `RA-2026.07-v1`;
- no implementation branch derived from review proposals before decisions are accepted and translated into bounded Issues.

## Evidence Hierarchy

| Evidence class | Role | Authority |
|---|---|---|
| Implementation and repository configuration | Executable behavior, ownership, schema, navigation, build, and workflow evidence | Primary |
| Current-state documents under `docs/` | Accepted current implementation summary | Current-state authority |
| Accepted ADRs | Durable decisions and rationale | Governing intent |
| Architecture and engineering policies | Implementation and delivery constraints | Policy authority |
| Roadmap and changelog | Planned direction and notable history | Supporting authority |
| Published review `RA-2026.07-v1` | Verified earlier architecture baseline | Historical evidence |
| This review | Time-bound findings, alternatives, and recommendations | Review evidence |

When executable evidence conflicts with current-state documentation, executable evidence governs and the living document must be corrected through focused scope.

## Claim Labels

- **Verified** — directly corroborated by implementation, schema, build, test, workflow, or repository configuration.
- **Documented** — established by an authoritative current-state document but not exhaustively retraced in this review pass.
- **Constrained** — valid only under an explicit runtime, platform, release, environment, or evidence condition.
- **Target** — proposed or accepted future direction that is not implemented.
- **Deferred** — intentionally postponed with a defined reason or trigger.
- **Rejected** — reviewed and intentionally not selected.
- **Needs More Evidence** — no responsible decision is possible from current evidence.

## Evidence Collection Areas

The review must inspect at least:

- `docs/ARCHITECTURE_ATLAS.md`;
- `docs/PROJECT_STATE.md`;
- focused documents under `docs/state/`;
- `docs/ARCHITECTURE.md`;
- `PACKAGE_STRUCTURE.md`;
- accepted ADRs, especially ADR-015 and ADR-016;
- current `Server` domain model and Room entity;
- Server Inventory repository and presentation boundaries;
- trusted SSH host-key persistence and verification ownership;
- SSH authentication, session, command, history, cancellation, timeout, cleanup, and stale-result boundaries;
- Saved Commands ownership and SSH input integration;
- app navigation and Compose screen ownership;
- Room schema version `5` and migration history;
- CI, release, documentation, and review-governance constraints.

## Required Review Outputs

Create outputs only when their evidence-backed content is ready:

```text
review/architecture/2026/RA-2026.07-v2/00-Review-Charter.md
review/architecture/2026/RA-2026.07-v2/01-Server-Domain-Assessment.md
review/architecture/2026/RA-2026.07-v2/02-Operational-UX-Assessment.md
review/architecture/2026/RA-2026.07-v2/03-Decision-Recommendations.md
review/architecture/2026/RA-2026.07-v2/STATUS.md
```

Empty placeholder assessment or recommendation documents are not permitted.

## Decision Model

Each recommendation must be classified as one of:

- Accepted;
- Accepted with Modification;
- Deferred;
- Rejected;
- Needs More Evidence.

Every significant accepted recommendation must state whether it requires:

- a new ADR;
- an update to an existing ADR;
- a focused evidence task or proof of concept;
- a persistence or migration design Issue;
- a bounded implementation Issue;
- no implementation action.

## Change-Control Impact Matrix

| Area | Current review impact | Constraint |
|---|---|---|
| Production code | None | Review only |
| Room schema and migrations | Analysis only | No version or entity change |
| Navigation and Compose | Analysis only | No behavior change |
| ADRs | Identify requirements only | Create or update only after recommendations are accepted |
| Living current-state documentation | Review for factual consistency | Change only to correct verified current-state inaccuracies |
| `review/` | Add versioned review evidence | Keep current state and target state distinguishable |
| CI | Validate final documentation head | No workflow redesign in this review |

## Completion Criteria

The review may move from `In Progress` to `Accepted` only when:

- the exact evidence baseline remains recorded;
- current implementation and target architecture are clearly separated;
- stable Server identity alternatives and trade-offs are compared;
- endpoint, trust, authentication-reference, platform, capability, preference, history, and transient-session ownership are assessed;
- freshness and invalidation are defined or explicitly deferred;
- current SSH ownership and lifecycle invariants are mapped;
- compact and expanded-window UX implications are assessed;
- persistence and migration implications are identified without implementation;
- security and support-claim boundaries are explicit;
- speculative abstractions are rejected or deferred;
- ADR requirements are identified per accepted recommendation;
- recommendation-derived follow-up work is bounded and created only after review acceptance;
- a verified current-state defect discovered during evidence collection may be handed off before acceptance only when the defect Issue is explicitly non-authorizing and implementation remains gated on the final review state;
- related living documentation is reviewed for consistency;
- documentation integrity checks pass;
- repository Android Validation passes on the final review head;
- no production code, schema, navigation behavior, or support claim changes.

## Completion Boundary

Governing Issue `#138` completes only after review version `RA-2026.07-v2` is accepted, merged, and published with explicit decisions and bounded follow-up actions.

Completion of this review does not complete any Server Profile, capability-discovery, persistence, navigation, or operational-UI implementation.
