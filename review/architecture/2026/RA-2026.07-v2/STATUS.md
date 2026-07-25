# RA-2026.07-v2 Status

- **Review:** Server Domain and Operational UX Architecture
- **Status:** In Progress
- **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
- **Governing Issue:** `#138`
- **Review branch:** `docs/server-domain-operational-ux-review`

## Current Phase

Server domain evidence assessment.

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

## Current Evidence Document

- `01-Server-Domain-Assessment.md`

Current provisional direction:

- preserve opaque `Server.id`;
- do not derive Server identity from endpoint or host-key data;
- keep SSH trust separate and endpoint-bound;
- require explicit endpoint lifecycle rules;
- defer multiple endpoints without a concrete user need;
- prove `REPLACE` plus cascading-child behavior before persistence decisions.

## Pending Evidence Work

- [x] Collect current Server domain and persistence evidence.
- [ ] Complete endpoint, trust, authentication, history, and session ownership mapping.
- [x] Compare stable Server identity alternatives.
- [ ] Assess profile-layer ownership, persistence, freshness, and invalidation.
- [ ] Assess platform-identification and capability-discovery boundaries.
- [ ] Map current SSH workflow and lifecycle invariants.
- [ ] Compare focused-screen and Server-workspace UX alternatives.
- [ ] Assess compact and expanded-window behavior.
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
