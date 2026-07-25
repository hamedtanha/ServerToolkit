# RA-2026.07-v2 Status

- **Review:** Server Domain and Operational UX Architecture
- **Status:** In Progress
- **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
- **Governing Issue:** `#138`
- **Review branch:** `docs/server-domain-operational-ux-review`

## Current Phase

Operational UX evidence assessment.

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

## Current Evidence Documents

- `01-Server-Domain-Assessment.md`
- `02-Operational-UX-Assessment.md`

Current provisional direction:

- preserve opaque `Server.id`;
- do not derive Server identity from endpoint or host-key data;
- keep SSH trust separate and endpoint-bound;
- require explicit endpoint lifecycle rules;
- defer multiple endpoints without a concrete user need;
- prove `REPLACE` plus cascading-child behavior before persistence decisions.
- preserve focused compact navigation until a concrete workspace need exists;
- prefer a Server workspace shell with capability-owned content as the scalable target candidate;
- keep SSH lifecycle and secrets inside the SSH capability boundary;
- reject generic workspace and application-wide operational-state owners;
- require an explicit decision before session continuity across sections.

## Pending Evidence Work

- [x] Collect current Server domain and persistence evidence.
- [x] Complete endpoint, trust, authentication, history, and session ownership mapping.
- [x] Compare stable Server identity alternatives.
- [ ] Assess profile-layer ownership, persistence, freshness, and invalidation.
- [ ] Assess platform-identification and capability-discovery boundaries.
- [x] Map current SSH workflow and lifecycle invariants.
- [x] Compare focused-screen and Server-workspace UX alternatives.
- [x] Assess compact and expanded-window behavior.
- [ ] Complete persistence, migration, security, and support-claim implications.
- [ ] Produce evidence-backed decision recommendations.
- [ ] Identify ADR and bounded follow-up Issue requirements.
- [ ] Review living documentation for consistency.
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
