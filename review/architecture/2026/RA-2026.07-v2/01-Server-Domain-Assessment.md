# Server Domain Assessment

> **Review ID:** `RA-2026.07-v2`
>
> **Status:** In Progress
>
> **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
>
> **Governing Issue:** `#138`
>
> **Assessment scope:** Current Server identity, endpoint, persistence, trust, history, and profile-layer boundaries

## Purpose

This assessment records the implemented Server-domain baseline and compares identity and ownership alternatives before any Server Profile, endpoint, persistence, migration, or operational-UX implementation is authorized.

The document distinguishes verified implementation evidence from provisional architecture direction. It does not make a final architecture decision and does not authorize production changes.

## Evidence Basis

The assessment inspected the following repository evidence at the recorded baseline:

- `feature/serverinventory/domain/model/Server.kt`;
- `feature/serverinventory/domain/repository/ServerRepository.kt`;
- `feature/serverinventory/data/local/entity/ServerEntity.kt`;
- `feature/serverinventory/data/local/dao/ServerDao.kt`;
- `feature/serverinventory/data/mapper/ServerEntityMapper.kt`;
- `feature/serverinventory/data/connection/ServerInventoryConnectionTargetResolver.kt`;
- `feature/ssh/domain/model/SshHostEndpoint.kt`;
- `feature/ssh/domain/model/SshTrustedHostKey.kt`;
- `feature/ssh/domain/repository/SshHostTrustRepository.kt`;
- `feature/ssh/data/local/entity/SshTrustedHostKeyEntity.kt`;
- `feature/ssh/data/repository/RoomSshHostTrustRepository.kt`;
- `feature/ssh/data/local/entity/SshConnectionHistoryEntity.kt`;
- `core/database/ServerToolkitDatabase.kt`;
- `core/database/ServerToolkitDatabaseMigrations.kt`;
- `ServerDaoTest.kt`;
- `docs/state/SERVER_INVENTORY_STATUS.md`;
- ADR-015;
- ADR-016.

## Executive Assessment

The current implementation uses an opaque `Server.id` as the stable inventory-record identity.

That record currently combines:

- user-defined inventory metadata;
- one SSH connection endpoint;
- one optional SSH username.

The endpoint fields are mutable attributes of the Server record rather than the primary key. SSH trust is stored separately and is addressed by the compound endpoint identity `serverId + host + port`. SSH connection history is also stored separately, retains an endpoint snapshot, and remains lifecycle-dependent on the parent Server record through a cascading foreign key.

The current implementation therefore already expresses three different identity concepts:

1. **Inventory identity** — opaque `Server.id`;
2. **SSH trust lookup identity** — `serverId + host + port`;
3. **Connection-attempt identity** — independent history-record id plus a snapshot of `serverId`, host, port, and username.

These concepts are related but are not equivalent.

Current evidence supports preserving an opaque Server identifier. Current evidence does not support deriving Server identity from host, port, username, or trusted host key. Current evidence is insufficient to accept either a dedicated one-to-one endpoint entity or one-to-many endpoints.

## Implemented Server Model

The current domain model contains:

```text
Server
├── id
├── name
├── host
├── sshPort
├── sshUsername
├── environment
├── category
├── tags
├── isFavorite
└── description
```

### Verified Characteristics

- `id` is a project-owned opaque string.
- `host`, `sshPort`, and `sshUsername` describe one SSH connection target.
- `name`, `environment`, `category`, `tags`, `isFavorite`, and `description` are user-defined inventory metadata.
- No credentials are stored in the Server model.
- No authentication-reference type is stored in the Server model.
- No trusted-host-key material is stored in the Server model.
- No platform facts or capability evidence are stored in the Server model.
- No profile freshness, observation source, or invalidation metadata exists.
- No multiple-endpoint representation exists.

### Domain and Persistence Shape

`ServerEntity` mirrors the domain model closely. The mapper performs a direct field-by-field transformation, except for:

- enum-name persistence for `environment`;
- delimiter-based serialization for `tags`.

The current persistence shape does not provide separate ownership or lifecycle boundaries for:

- endpoint data;
- platform observations;
- capability observations;
- operational preferences;
- authentication references.

This is a description of the current baseline, not evidence that all future concepts belong in the Server row.

## Current Identity Semantics

### Inventory Record Identity — Verified

`Server.id` is the primary key of the `servers` table.

The repository:

- loads by `serverId`;
- saves a complete `Server`;
- deletes by `serverId`.

The DAO conflict path also uses the same id when replacing an existing row.

Therefore, the implemented inventory identity is the opaque id, not host, port, username, display name, category, environment, or trusted host key.

### Endpoint Mutability — Verified

The connection-target resolver receives `serverId`, reloads the current Server record, and resolves the current:

```text
host + sshPort + sshUsername
```

This means the endpoint can change while `Server.id` remains stable.

The implementation does not define whether such a change represents:

- the same remote system at a new endpoint;
- a replacement system;
- a corrected inventory entry;
- a rebuilt system;
- a different SSH service on the same system.

That semantic decision remains unresolved.

### Endpoint-Derived Server Identity — Not Supported

Deriving Server identity from host, port, or username is not supported by current evidence:

- hostnames and addresses may change;
- SSH ports may change;
- usernames represent access context rather than host identity;
- DNS names may point to replaced systems;
- multiple logical Servers could theoretically share network infrastructure;
- trusted host keys are endpoint trust evidence, not complete product identity;
- the current repository and foreign-key model already use an independent id.

Endpoint-derived identity should not be introduced without a new accepted decision that disproves these constraints.

## Trust Ownership

### Verified Boundary

SSH trust is intentionally stored outside generic Server Inventory metadata.

The domain trust lookup key is:

```text
SshHostEndpoint
├── serverId
├── host
└── port
```

The trusted host key contains:

```text
SshTrustedHostKey
├── endpoint
└── fingerprint
```

The Room entity uses the compound primary key:

```text
server_id + host + port
```

and a foreign key from `server_id` to `servers.id` with delete cascade.

### Consequences

- Trust belongs to the SSH feature, not generic Server metadata.
- Trust is scoped to both the Server record and the concrete endpoint.
- Changing host or port changes the trust lookup identity.
- Username is not part of host trust identity.
- Deleting the parent Server deletes its stored SSH trust records.
- A host-key change requires an explicit replacement flow; silent replacement is prohibited by the repository contract.

### Unresolved Endpoint-Change Behavior

The implementation can retain an old trusted-key row for an old endpoint under the same Server id unless a separate path removes it.

The current assessment has not found an accepted rule defining whether endpoint editing must:

- retain old trust evidence;
- remove old trust evidence;
- archive it;
- prompt the user;
- classify the endpoint as a different remote system.

This behavior requires an explicit product and security decision before endpoint/profile redesign.

## Connection-History Ownership

### Verified Boundary

SSH connection history is stored in a dedicated table.

Each history record contains:

- an independent history id;
- parent `serverId`;
- host snapshot;
- port snapshot;
- username snapshot;
- status;
- attempt and completion timestamps;
- normalized connection error.

The history table has a foreign key to `servers.id` with delete cascade.

### Consequences

- Historical endpoint values are preserved within each retained history row.
- Current Server endpoint edits do not rewrite the endpoint snapshot already stored in a history row.
- History remains owned by the SSH feature.
- Server deletion removes its connection history through the current database relationship.
- The current model is operational history associated with an inventory record, not an immutable audit log independent from that record.

Whether history should survive Server deletion is a product-retention decision and is not answered by current implementation alone.

## Current Relationship Model

```text
ServerEntity
  id (primary key)
  user-defined metadata
  one embedded SSH endpoint
          │
          ├── SshTrustedHostKeyEntity
          │     key: serverId + host + port
          │     on Server delete: cascade
          │
          └── SshConnectionHistoryEntity
                key: independent history id
                endpoint snapshot: host + port + username
                on Server delete: cascade
```

Saved Commands are global and do not currently participate in Server identity or ownership.

## Current Profile-Layer Assessment

| Proposed layer | Current representation | Current owner | Assessment |
|---|---|---|---|
| User-defined metadata | Fields inside `Server` | Server Inventory | Verified |
| Connection endpoint | `host`, `sshPort`, `sshUsername` inside `Server` | Server Inventory, resolved for SSH | Verified but not independently modeled |
| Trust evidence | Separate trusted-host-key domain and Room entity | SSH | Verified and endpoint-bound |
| Authentication references | None in Server persistence | Not implemented as durable Server data | Verified absence |
| Credentials | Not stored in Server inventory | Ephemeral SSH workflows | Verified exclusion from Server model |
| Observed platform facts | None | Not implemented | Verified absence |
| Observed capabilities | None | Not implemented | Verified absence |
| Operational preferences | Environment/category/tags/favorite are inventory metadata; no capability preferences exist | Server Inventory | Partially represented |
| Historical observations | SSH connection history only | SSH | Verified |
| Transient session state | Not represented in Server persistence | SSH runtime/presentation boundaries | Verified separation at persistence level |

## Persistence Risk Requiring Evidence

### Server Save Strategy

`ServerDao.upsertServer()` uses:

```text
INSERT with OnConflictStrategy.REPLACE
```

The trusted-key and connection-history tables use cascading foreign keys to `servers.id`.

The current DAO test verifies that the Server row itself is replaced, but it does not verify preservation or deletion of existing child trust/history records during replacement.

### Classification

**Needs More Evidence**

This review must not claim that editing a Server either preserves or deletes child records until an instrumentation test proves the behavior of the actual Room and SQLite configuration.

Required focused evidence:

1. insert a Server;
2. insert one trusted host key and one connection-history record;
3. save an updated Server with the same id;
4. verify the resulting Server, trusted-key, and history rows;
5. repeat with endpoint-only and metadata-only changes;
6. record the observed behavior before persistence recommendations are accepted.

If child rows are removed during replacement, that is a current correctness and retention defect requiring a focused Issue independent from broader Server Profile design.

## Documentation Consistency Finding

`docs/state/SERVER_INVENTORY_STATUS.md` lists connection history as not implemented.

At the evidence baseline, Room database version `5` includes `SshConnectionHistoryEntity`, migration `3 → 4` creates the history table, and current SSH documentation records implemented history behavior.

Therefore, the Server Inventory status statement is stale.

Classification:

```text
Verified current-state documentation inconsistency
```

Correction should occur through a focused documentation-synchronization change in this review PR or a separately bounded documentation PR. Historical release evidence must not be rewritten.

## Identity Alternatives

### Alternative A — Preserve Opaque Server ID and Embedded Single Endpoint

Description:

- keep `Server.id` as stable identity;
- retain one endpoint directly in `Server`;
- add no endpoint entity.

Advantages:

- smallest change;
- preserves current repository and navigation assumptions;
- avoids speculative multi-endpoint support;
- keeps current foreign-key parent stable.

Limitations:

- user metadata and connection data remain structurally coupled;
- endpoint lifecycle rules remain implicit;
- future profile layers could overload the Server row;
- trust/history behavior on endpoint change remains difficult to express.

Assessment:

```text
Current implemented baseline; not yet accepted as the long-term target
```

### Alternative B — Preserve Opaque Server ID and Extract One Endpoint Concept

Description:

- keep `Server.id` as stable identity;
- model a distinct endpoint value or persistence relationship;
- retain one active endpoint per Server initially.

Advantages:

- separates inventory identity from access coordinates;
- makes endpoint replacement semantics explicit;
- supports clearer trust and authentication-reference relationships;
- can evolve incrementally without accepting multiple endpoints immediately.

Limitations:

- requires migration and relationship design;
- risks unnecessary complexity if the product remains permanently one-endpoint-only;
- requires ownership decisions between Server Inventory and connection capabilities.

Assessment:

```text
Plausible target candidate; requires persistence and UX evidence
```

### Alternative C — Preserve Opaque Server ID with Multiple Endpoints

Description:

- one Server owns multiple connection endpoints;
- endpoints may represent transports, addresses, or access contexts.

Advantages:

- supports multiple interfaces, bastions, transports, and environments;
- separates logical remote-system identity from access paths.

Limitations:

- no current implemented user workflow requires multiple endpoints;
- endpoint selection, priority, trust, credentials, history, and failure semantics become substantially more complex;
- accepting this now would be speculative.

Assessment:

```text
Deferred unless a concrete multi-endpoint user requirement is established
```

### Alternative D — Derive Server Identity from Endpoint or Host Key

Description:

- use host, host-plus-port, username, or trusted host key as Server identity.

Advantages:

- superficially reduces identifiers;
- may simplify isolated endpoint lookup.

Limitations:

- endpoint data is mutable;
- username is not machine identity;
- host keys rotate and may differ by service;
- rebuilt or cloned infrastructure is ambiguous;
- conflicts with the existing opaque-id repository and foreign-key baseline;
- couples product identity to SSH-specific evidence.

Assessment:

```text
Not recommended by current evidence
```

## Provisional Architecture Direction

The following direction is provisional and must be confirmed through the remaining review:

1. Preserve the opaque Server id as the stable inventory identity.
2. Do not derive Server identity from host, port, username, or trusted host key.
3. Treat endpoint data as a concept with an explicit lifecycle, even if the first accepted target remains one endpoint per Server.
4. Keep trust evidence outside generic inventory metadata and bound to a concrete endpoint.
5. Keep credentials outside Server persistence.
6. Model any future authentication reference separately from credential material.
7. Do not place observed platform or capability facts directly into the current flat Server row without evidence source, freshness, and invalidation semantics.
8. Do not accept multiple endpoints until a concrete workflow requires them.
9. Do not introduce a Capability Gateway for identity or local persistence modelling alone.
10. Do not retrofit existing SSH boundaries cosmetically; restructure only for a concrete accepted requirement or verified defect.

## Required Next Evidence

Before final Server-domain recommendations:

- prove child-record behavior during Server replacement;
- inspect Add/Edit Server validation and mutation behavior;
- inspect Server deletion UX and retention expectations;
- map ephemeral password and private-key authentication ownership;
- determine whether any durable authentication-reference use case exists;
- determine whether multiple endpoint use cases exist;
- map operational UX ownership in the separate UX assessment;
- define endpoint-change and host-key-rotation user flows;
- assess whether connection history should survive Server deletion;
- review current-state documents for additional inconsistencies.

## Assessment Boundary

This assessment does not authorize:

- changes to `Server`;
- a new endpoint model or table;
- Room migration `5 → 6`;
- changes to cascade behavior;
- authentication-reference persistence;
- platform or capability profile fields;
- Server workspace implementation;
- Gateway, Provider, Adapter, registry, or plugin implementation.

Final decisions remain pending the operational-UX assessment, focused persistence evidence, and the decision-recommendation document.
