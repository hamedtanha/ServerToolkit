# RA-2026.07-v2 Status

- **Review:** Server Domain and Operational UX Architecture
- **Status:** In Progress
- **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
- **Governing Issue:** `#138`
- **Review branch:** `docs/server-domain-operational-ux-review`

## Current Phase

Local documentation-integrity checks and repository Android Validation.

## Completed

- [x] Publish architecture baseline review `RA-2026.07-v1`.
- [x] Close stale Saved Command Foundation planning Issue `#122`.
- [x] Establish governing Issue `#138`.
- [x] Record exact evidence baseline.
- [x] Define review scope, non-goals, evidence hierarchy, claim labels, decision model, and completion boundary.
- [x] Register the active review in review navigation.
- [x] Record the current Server domain, Room, endpoint, trust, and history baseline.
- [x] Record identity alternatives and provisional constraints without authorizing implementation.
- [x] Identify the Server replacement and cascade-preservation evidence gap.
- [x] Identify the stale Server Inventory connection-history status statement.
- [x] Record the current navigation graph and Server-to-SSH user journey.
- [x] Record SSH route, ViewModel, UI-state, secret, session, and history ownership.
- [x] Record cleanup-before-navigation as a current lifecycle invariant.
- [x] Compare focused destination, capability-owned workspace, generic workspace, and application-wide shell alternatives.
- [x] Record compact and expanded-layout implications without selecting an implementation framework.
- [x] Execute focused Room instrumentation evidence with foreign keys enabled.
- [x] Verify metadata-only Server replacement deletes both trust and history children.
- [x] Verify endpoint Server replacement deletes both trust and history children.
- [x] Classify generic Server replacement as a current correctness, security, and retention defect.
- [x] Synchronize Server Inventory current-state documentation with implemented SSH history and the verified defect.
- [x] Create focused defect Issue `#140` with correction acceptance criteria.
- [x] Separate defect implementation ownership from review PR `#139`.
- [x] Assess Server Profile layer ownership and durable/transient boundaries.
- [x] Define platform-identification versus capability-discovery boundaries.
- [x] Define freshness and invalidation semantics for future observed evidence.
- [x] Compare safe existing-Server update mechanisms without selecting implementation.
- [x] Record the platform-neutral Add Server UI-copy inconsistency.
- [x] Complete persistence, migration, security, retention, and support-claim implications.
- [x] Produce evidence-backed decision recommendations.
- [x] Identify ADR and bounded follow-up Issue requirements.
- [x] Preserve the current roadmap boundary while defining a future visual/UX readiness boundary.
- [x] Review living documentation for consistency and synchronize Project State and Changelog without promoting review recommendations to accepted implementation state.

## Current Evidence Documents

- `01-Server-Domain-Assessment.md`
- `02-Operational-UX-Assessment.md`
- `03-Decision-Recommendations.md`

Current provisional direction:

- preserve opaque `Server.id`;
- do not derive Server identity from endpoint or host-key data;
- keep SSH trust separate and endpoint-bound;
- require explicit endpoint lifecycle rules;
- defer multiple endpoints without a concrete user need;
- treat `REPLACE`-driven trust and history deletion as a verified defect;
- require an explicit correction path and permanent regression coverage;
- preserve focused compact navigation until a concrete workspace need exists;
- retain a capability-owned Server workspace as a deferred scalable candidate rather than an implementation commitment;
- keep SSH lifecycle and secrets inside the SSH capability boundary;
- reject generic workspace and application-wide operational-state owners;
- preserve cleanup-before-navigation and defer session continuity across sections until a concrete workflow requires it;
- keep future platform and capability observations source-aware, freshness-aware, and explicitly invalidated;
- defer durable authentication references until a concrete credential-selection workflow exists.

## Pending Evidence Work

- [x] Collect current Server domain and persistence evidence.
- [x] Complete endpoint, trust, authentication, history, and session ownership mapping.
- [x] Compare stable Server identity alternatives.
- [x] Assess profile-layer ownership, persistence, freshness, and invalidation.
- [x] Assess platform-identification and capability-discovery boundaries.
- [x] Map current SSH workflow and lifecycle invariants.
- [x] Compare focused-screen and Server-workspace UX alternatives.
- [x] Assess compact and expanded-window behavior.
- [x] Define focused defect Issue `#140` and correction acceptance criteria.
- [x] Compare safe Room update mechanisms while preserving explicit-delete semantics.
- [x] Complete persistence, migration, security, and support-claim implications.
- [x] Produce evidence-backed decision recommendations.
- [x] Identify ADR and bounded follow-up Issue requirements.
- [x] Review living documentation for consistency.
- [ ] Run local documentation-integrity checks.
- [ ] Run repository Android Validation.
- [ ] Complete review and acceptance.
- [ ] Publish the accepted review version.

## Current Authorization Boundary

This review authorizes evidence collection and architecture assessment only.

It does not authorize:

- production-code changes;
- `Server` model changes;
- Room schema or migration changes;
- navigation or Compose behavior changes;
- Server Profile implementation;
- platform detection or capability discovery;
- credential persistence;
- Capability Gateway, Provider, Adapter, registry, or plugin implementation;
- new support claims.

## Review Lifecycle

```text
In Progress -> Accepted -> Published
```

`Accepted` freezes substantive review content for merge.

`Published` is reserved for a metadata-only publication change after the accepted review package reaches `main`. A published review version is immutable.
