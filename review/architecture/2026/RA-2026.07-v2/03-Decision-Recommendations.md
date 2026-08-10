# Decision Recommendations

> **Review ID:** `RA-2026.07-v2`
>
> **Status:** Accepted
>
> **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
>
> **Governing Issue:** `#138`
>
> **Recommendation scope:** Server identity, endpoint lifecycle, profile-layer ownership, platform and capability evidence, persistence, security, retention, and operational UX

## Purpose

This document synthesizes the evidence recorded in the Server Domain Assessment
and Operational UX Assessment into explicit recommendation classifications.

It does not authorize production implementation.

Recommendations classified as Accepted or Accepted with Modification must still
be translated into the required ADRs, current-state documentation, focused
Issues, implementation changes, tests, and validation before they become
production behavior.

## Evidence Basis

The recommendations are based on:

- the exact review baseline recorded by the charter;
- the current `Server` domain and Room persistence model;
- SSH trusted-host-key ownership and endpoint identity;
- SSH connection-history persistence and endpoint snapshots;
- current Add/Edit Server mutation behavior;
- focused Room runtime evidence for `REPLACE`-driven child deletion;
- current SSH authentication, trust, session, command, history, cleanup, and
  navigation ownership;
- ADR-003, ADR-006, ADR-009, ADR-011, ADR-012, ADR-015, and ADR-016;
- current Security Policy;
- current Project State and Roadmap;
- the completed Server-domain and operational-UX assessment sections.

## Cross-Cutting Persistence and Migration Implications

### Current Persistence Baseline

Room database version `5` currently persists:

- Server Inventory records;
- SSH trusted host keys;
- SSH connection history;
- Saved Commands.

The current `Server` row remains a flat inventory record containing user-managed
metadata and one active SSH connection target.

No current table stores:

- persistent credential secrets;
- durable authentication references;
- observed platform facts;
- observed capability evidence;
- multiple Server endpoints;
- a Server Workspace model;
- detached or background session state.

### Existing-Server Update Correction

The verified `REPLACE` defect does not, by itself, justify a schema migration.

The accepted semantic requirement is:

```text
Existing Server updates must modify the existing parent row without
delete-and-reinsert semantics.
```

Issue `#140` owns the production correction.

The selected implementation must prove through permanent Room instrumentation
and repository tests that:

- metadata-only updates preserve SSH trust;
- metadata-only updates preserve SSH connection history;
- username-only changes preserve SSH host trust;
- host or port changes preserve history snapshots;
- explicit Server deletion retains the current cascade behavior;
- the new active endpoint never inherits trust from a different endpoint.

If implementation evidence demonstrates that a schema change is genuinely
required, the migration decision must be reopened rather than silently adding
Room version `6`.

### Future Endpoint Modelling

The review does not accept a separate endpoint table or multiple endpoints.

The current single active endpoint may remain embedded in `Server` while its
lifecycle semantics are made explicit.

A future endpoint extraction requires a separate persistence design when a
concrete user workflow demonstrates value that cannot be represented cleanly by
the current single-endpoint model.

### Future Platform and Capability Evidence

Observed platform and capability data must not be added to the current flat
Server row merely because a future screen wants to display it.

Any persisted observed evidence requires an accepted design for:

- owner;
- normalized project-owned value;
- evidence source;
- observation timestamp;
- freshness policy;
- invalidation policy;
- support-state semantics where applicable;
- migration and backward-compatibility behavior.

No capability-evidence table, platform-profile table, provider registry, or
generic evidence schema is authorized by this review.

### History Retention

Current SSH connection history is operational history associated with the
Server record.

Each history entry already preserves an endpoint and username snapshot.

The current explicit Server-deletion cascade remains the accepted baseline.

Independent audit-log retention, export, recovery, or compliance retention is
deferred until a concrete product requirement exists.

## Security Implications

### Credential Boundary

Password values, private-key sources, private-key material, and passphrases
remain transient and non-persistent.

No credential secret may be moved into:

- `Server`;
- `ServerEntity`;
- generic Server Profile state;
- navigation arguments;
- observable shared workspace state;
- ordinary Room persistence.

Durable authentication references remain deferred until a concrete
credential-selection workflow exists and its secure-storage, deletion,
backup, recovery, and lifecycle rules are separately reviewed.

### Host Trust Boundary

SSH trust remains owned by the SSH feature and bound to:

```text
serverId + host + port
```

The Server display name and SSH username are not host-trust identity.

Therefore:

- metadata-only edits do not invalidate host trust;
- username-only edits do not invalidate host trust;
- changing host or port changes the active trust endpoint;
- trust for an old endpoint must never authorize the new endpoint;
- a changed host key for the same endpoint remains blocked until an explicit
  reviewed replacement action occurs;
- silent trusted-host-key replacement remains prohibited.

The physical retention or removal of an old endpoint trust record must never be
an accidental consequence of replacing the Server parent row.

This review does not select retention, removal, archival, or automatic cleanup
for trust associated with an endpoint that is no longer active.

Issue `#140` must preserve the security invariants established here without
silently selecting that unresolved lifecycle policy:

- trust for an old endpoint must never authorize a different active endpoint;
- metadata-only and username-only edits must not destroy applicable trust;
- host or port changes must not gain trust through incidental parent-row
  replacement behavior.

Any later retention, removal, cleanup, or archival policy for inactive endpoint
trust requires explicit ownership, user-flow semantics where applicable, and
focused test coverage.

### Backup and Transfer

This review does not change the accepted backup and data-extraction policy.

Server inventory metadata, trusted SSH host keys, and future credential-related
data must not gain backup, restore, synchronization, or device-transfer
behavior through this work.

### Session and Secret Lifetime

The current SSH lifecycle remains the security baseline:

- attempt-scoped secrets;
- capability-owned active session state;
- explicit execution;
- deterministic cleanup before permanent workflow exit;
- no detached session;
- no background command execution;
- no generic application-wide operations state.

## Support-Claim Implications

ADR-015 remains authoritative.

This review does not add verified support for any operating system, shell,
package manager, service manager, remote transport, provider, or vendor.

Platform identification and capability support remain separate concepts.

Future capability evidence must preserve the ADR-016 support states:

```text
Supported
Unsupported
Unknown
Unavailable
```

A platform label must not be converted into an inferred capability claim.

### Platform-Neutral UI Copy

The current Add Server presentation contains the user-facing text:

```text
Enter the connection details for a Linux server.
```

That wording conflicts with the accepted platform-neutral product direction.

The inconsistency requires a bounded production-copy correction after review
acceptance.

The correction must describe the implemented Server/connection workflow without
claiming platform support that has not been verified.

## Recommendation Matrix

| ID | Recommendation | Classification | Required follow-up |
|---|---|---|---|
| R1 | Preserve opaque `Server.id` as stable inventory identity. | Accepted | New Server identity/evidence lifecycle ADR |
| R2 | Do not derive Server identity from host, port, username, or host key. | Accepted | New Server identity/evidence lifecycle ADR |
| R3 | Preserve one active endpoint in the current model while making endpoint lifecycle explicit. | Accepted with Modification | New ADR; no endpoint table yet |
| R4 | Introduce multiple Server endpoints now. | Deferred | New concrete user requirement required |
| R5 | Keep SSH trust separate from generic Server metadata and endpoint-bound. | Accepted | Preserve ADR-009; cross-reference new ADR |
| R6 | Treat username changes as trust-neutral and host/port changes as active-endpoint changes. | Accepted | New ADR; Issue `#140` regression evidence |
| R7 | Allow old-endpoint trust to authorize a new endpoint. | Rejected | None |
| R8 | Preserve current explicit Server-deletion cascade for SSH trust and connection history. | Accepted with Modification | New ADR; independent retention deferred |
| R9 | Convert connection history into an immutable audit log independent from Server deletion. | Deferred | Concrete audit/compliance requirement required |
| R10 | Persist authentication secrets in Server or generic profile storage. | Rejected | Existing security ADRs remain authoritative |
| R11 | Introduce durable authentication references now. | Deferred | Concrete credential-selection workflow required |
| R12 | Add observed platform or capability fields directly to the current flat Server row. | Rejected | Dedicated evidence model required first |
| R13 | Require source, timestamp, freshness, and invalidation semantics for future observed platform/capability evidence. | Accepted | New ADR; implementation deferred |
| R14 | Introduce generic Gateway, Provider registry, platform registry, or capability table without a concrete capability. | Rejected | ADR-016 remains authoritative |
| R15 | Preserve focused destinations and capability-owned SSH state for the current product. | Accepted | No new navigation ADR required |
| R16 | Introduce a Server Workspace now. | Deferred | Concrete additional Server-scoped capability required |
| R17 | Preserve SSH session continuity across arbitrary section or destination changes now. | Deferred | Separate lifecycle/security ADR if later required |
| R18 | Introduce application-wide persistent operational state or detached sessions now. | Rejected | Separate product/security decision required |
| R19 | Select adaptive navigation or pane infrastructure in this review. | Deferred | Visual/adaptive evidence task required |
| R20 | Correct the destructive existing-Server save semantics without a Room migration when evidence permits. | Accepted | Existing Issue `#140` |
| R21 | Expand platform or capability support claims through this review. | Rejected | Runtime evidence required by ADR-015/016 |
| R22 | Correct the Linux-specific Add Server copy. | Accepted | Bounded UI-copy follow-up Issue |

## ADR Impact

### New ADR Required

A new architecture decision is required after this review is accepted.

Proposed decision scope:

```text
Server Identity, Endpoint, and Evidence Lifecycle
```

The ADR should record:

- opaque Server identity;
- independence of Server identity from endpoint coordinates;
- current one-active-endpoint baseline;
- explicit endpoint lifecycle semantics;
- SSH trust applicability on metadata, username, host, and port changes;
- current Server-deletion retention baseline;
- separation of user-defined metadata from observed evidence;
- source, freshness, and invalidation requirements for future observed evidence;
- prohibition on turning the flat Server row into a generic catch-all profile;
- deferral of multiple endpoints and durable authentication references.

The ADR should reference, rather than rewrite:

- ADR-003 for Room persistence;
- ADR-009 for SSH host trust and authentication input;
- ADR-011 for credential ownership;
- ADR-012 for backup and data extraction;
- ADR-015 for platform-neutral product direction;
- ADR-016 for remote capability architecture.

### Existing ADRs Do Not Require Modification Now

No current recommendation changes:

- the accepted SSH host-key replacement rule;
- ephemeral authentication ownership;
- cleanup-before-navigation;
- explicit command execution;
- platform-neutral product direction;
- three-level remote capability architecture.

Therefore, ADR-009, ADR-011, ADR-015, and ADR-016 do not require substantive
modification merely to complete this review.

### ADR Required Only If a Deferred Direction Is Later Accepted

A new or amended ADR is required before:

- SSH session continuity across workspace sections or destinations;
- detached or background SSH sessions;
- a parent workspace owner that changes SSH state ownership;
- persistent authentication references or credentials;
- a multi-endpoint Server model;
- platform/capability evidence persistence that changes durable ownership or
  schema boundaries;
- backup, restore, synchronization, or transfer of infrastructure data.

### No ADR Required

No ADR is required for:

- the focused DAO/repository correction in Issue `#140` if it implements these
  accepted semantics without changing schema or ownership;
- the platform-neutral Add Server copy correction;
- internal Compose decomposition that preserves ownership and behavior;
- visual styling, typography, spacing, iconography, or theming that does not
  change architecture or lifecycle semantics.

## Bounded Follow-Up Requirements

### Existing Required Follow-Up

Issue `#140` remains the bounded implementation owner for the verified
Server-update data-loss defect.

It must not absorb:

- Server Profile redesign;
- multiple endpoints;
- capability discovery;
- credential persistence;
- workspace navigation;
- visual redesign.

### Required After Review Acceptance

Create a focused ADR task for the accepted Server identity, endpoint, and
evidence-lifecycle decision.

Create a focused production Issue for the platform-neutral Add Server copy
correction.

### Deferred Follow-Up

A future visual/UX foundation task should collect evidence for:

- application visual identity and launcher icon;
- Material theme and component consistency;
- typography and spacing hierarchy;
- command-output readability;
- action reachability;
- accessibility;
- keyboard and landscape behavior;
- narrow, medium, and expanded windows;
- adaptive presentation only when runtime evidence justifies it.

This visual/UX work must preserve the accepted ownership and lifecycle
boundaries unless a separate architecture decision changes them.

A Server Workspace Issue must not be created until a concrete additional
Server-scoped capability establishes the need.

A platform/capability persistence Issue must not be created until a concrete
capability establishes the required evidence model.

## Roadmap Boundary

This review does not rename or renumber roadmap milestones.

The current roadmap identifies:

```text
0.5.0 — Operations
0.6.0 — Dashboard Evolution
0.7.0 — Remote Capability Foundation
```

A visual/UX foundation initiative may be scheduled through a separate product
planning decision.

It must not silently replace the accepted `0.6.0 — Dashboard Evolution`
objective.

## Visual UX Readiness Boundary

The architecture does not require a Server Workspace, multi-endpoint model,
platform profile, capability profile, or persistent credentials before a
visual/UX quality pass may begin.

A future visual/UX initiative may safely improve:

- product visual identity;
- launcher icon;
- Material theme;
- typography;
- spacing;
- component consistency;
- information hierarchy;
- Server context presentation using already implemented data;
- empty, loading, error, and confirmation presentation;
- accessibility and adaptive evidence;
- platform-neutral copy.

It must not silently change:

- Server identity;
- endpoint ownership;
- trust semantics;
- credential lifetime;
- SSH session ownership;
- cleanup-before-navigation;
- explicit Run-only execution;
- support claims.

## Decision Synthesis Conclusion

The review evidence, living-document consistency work, documentation-integrity
validation, and repository Android Validation are complete.

The accepted recommendations preserve the existing restrained architecture
while making Server identity, endpoint, trust, retention, and future evidence
lifecycles explicit.

The review rejects speculative profile, workspace, multi-endpoint, capability
registry, credential-persistence, and background-session infrastructure.

Acceptance freezes the substantive review content for merge through PR `#139`.
It does not authorize production implementation.

The next architecture-governance work is:

1. merge the accepted review package;
2. publish the merged review through a metadata-only follow-up;
3. translate the accepted Server identity, endpoint, and evidence-lifecycle
   recommendations into a focused ADR;
4. keep Issue `#140` as a separately bounded defect implementation;
5. create the bounded platform-neutral Add Server copy follow-up.

## Authorization Boundary

This accepted recommendation document does not authorize production
implementation.

Review acceptance freezes the recommendation set; production work still
requires the identified ADRs, bounded Issues, implementation evidence, tests,
and validation. This review does not authorize:

- production code changes;
- Room schema changes;
- Server Profile implementation;
- endpoint extraction;
- multiple endpoints;
- platform or capability persistence;
- Server Workspace implementation;
- session continuity changes;
- credential persistence;
- new support claims.

Issue `#140` remains a separately bounded future implementation change.

## References

- `00-Review-Charter.md`
- `01-Server-Domain-Assessment.md`
- `02-Operational-UX-Assessment.md`
- `docs/ARCHITECTURE_ATLAS.md`
- `docs/PROJECT_STATE.md`
- `docs/ROADMAP.md`
- `docs/SECURITY.md`
- `docs/adr/ADR-003-local-persistence-with-room.md`
- `docs/adr/ADR-006-ssh-workflow-and-security-boundaries.md`
- `docs/adr/ADR-009-ssh-host-trust-and-authentication-input-strategy.md`
- `docs/adr/ADR-011-ssh-credential-ownership-and-secure-storage-strategy.md`
- `docs/adr/ADR-012-android-backup-and-data-extraction-policy.md`
- `docs/adr/ADR-015-platform-neutral-remote-systems-product-direction.md`
- `docs/adr/ADR-016-three-level-remote-capability-architecture.md`
