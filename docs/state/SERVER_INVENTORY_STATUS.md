# Server Inventory Status

**Project:** Server Toolkit
**Feature Area:** Server Inventory
**Status:** Accepted Baseline
**Related Milestone:** Version 0.3.0 — Server Inventory Foundation
**Last Updated:** 2026-08-11

---

## Purpose

This document records the detailed implementation status of the Server Inventory feature area.

The high-level project state remains documented in [Project State](../PROJECT_STATE.md).

---

## Current Status

The Server Inventory 0.3.0 baseline remains the accepted historical foundation.

The feature provides local server inventory management backed by Room persistence.

The previously verified existing-Server persistence defect tracked by Issue `#140` has been corrected. Existing Server saves now use Room upsert semantics that update the existing parent row instead of replacement semantics, preserving SSH trust and connection-history children according to their accepted lifecycle boundaries.

---

## Implemented

### Navigation and Presentation

- Server Inventory navigation destination.
- Server Inventory ViewModel.
- Server Inventory UI state.
- Server Inventory empty screen.
- Server Inventory empty-state action.
- Stable inventory-empty rendering after deleting the final server while a filter remains active.
- Basic Server Inventory list rendering.
- Dashboard navigation action to Server Inventory.

### Server Form Workflows

- Add Server navigation destination.
- Add Server route backed by the shared Server Form screen.
- Add Server ViewModel.
- Add Server form fields.
- Add Server validation state.
- Add Server validation-only save action.
- Add Server repository-backed save flow.
- Add Server automatic return after successful save.
- Edit Server navigation destination.
- Edit Server route.
- Edit Server ViewModel.
- Shared Server Form screen.
- Server Form UI state shared by Add Server and Edit Server.
- Edit Server form reuse through the shared Server Form screen.
- Edit Server repository-backed save flow preserving the existing server id.

### Domain and Filtering

- Server Inventory domain model.
- Server Inventory environment model.
- Server Inventory filter state.
- Server Inventory search by name, host, username, category, and tags.
- Server Inventory environment filtering.
- Server Inventory favorites-only filtering.
- Server Inventory filter clearing.

### Persistence

- Server repository contract.
- In-memory Server repository implementation for development and testing support.
- Room dependency setup with KSP.
- Room schema export location configuration.
- Initial Room schema export.
- Server Toolkit Room database class.
- Server entity.
- Server DAO.
- Server entity/domain mapper.
- Room-backed Server repository implementation.
- Hilt database and DAO providers.
- Server Inventory repository dependency injection binding.
- Server Inventory ViewModel repository observation.
- Existing-Server saves use Room `@Upsert` semantics so existing rows are updated without destructive parent replacement.

### Delete Workflow

- Delete server UI action with confirmation dialog.
- Server Inventory ViewModel delete action.
- Manual delete flow verification after app restart.

### Verification

- Manual Edit Server verification after app restart.
- Manual search and filtering verification.
- Automated search and filtering verification through unit tests, instrumented tests, and debug build.
- Automated Edit Server verification through unit tests, instrumented tests, and debug build.
- Automated shared Server Form naming verification through unit tests, instrumented tests, and debug build.
- Server Inventory filter matcher unit tests.
- Server Inventory UI-state regression tests for inventory-empty and filter-result-empty semantics.
- DAO instrumentation tests for insert, non-destructive update, repeated update, and explicit delete behavior.
- Room-backed repository instrumentation tests for save and non-destructive update behavior.
- Permanent Room regression coverage verifies that metadata-only and username-only updates preserve trusted-host-key and connection-history children.
- Permanent Room regression coverage verifies that endpoint updates preserve connection-history snapshots, do not cascade-delete old-endpoint trust, and do not authorize that trust for the new endpoint.
- Explicit Server deletion regression coverage verifies that the existing trusted-host-key and connection-history cascade behavior remains intact.
- Server entity/domain mapper unit tests.
- Historical focused Room instrumentation evidence with foreign keys enabled verified the destructive replacement behavior that Issue `#140` corrects.

---

## Resolved Persistence Defect

Issue `#140` corrects the destructive existing-Server save path by replacing `OnConflictStrategy.REPLACE` with Room `@Upsert`.

For an existing `Server.id`, Room now generates an update path instead of deleting and recreating the parent row.

Current verified behavior:

- Metadata-only edits preserve trusted SSH host keys and SSH connection history.
- Username-only edits preserve trusted SSH host keys and SSH connection history.
- Host or SSH-port edits preserve the stable Server id and existing connection-history snapshots.
- Endpoint edits no longer delete old-endpoint trust through incidental parent-row cascade behavior.
- Trust remains keyed by `serverId + host + port`, so trust for an old endpoint does not authorize a new endpoint.
- Explicit Server deletion retains the existing trusted-host-key and connection-history cascade behavior.

The inactive-old-endpoint trust retention, removal, archival, or cleanup lifecycle remains unresolved and is not selected by this correction.

The Room database remains at version `5`; exported schema `5` is unchanged and no migration is required.

Implementation tracking: Issue `#140`.

## Accepted Baseline

The Server Inventory stabilization checklist is complete.

The Server Inventory 0.3.0 baseline is accepted as stable enough to support SSH workflow development.

---

## Naming Scope

The current inventory-related implementation is intentionally named `serverinventory` because it manages one concrete asset type: `Server`.

A broader `inventory` package or model should be introduced only after the application implements additional non-server asset types or shared inventory behavior that is no longer server-specific.

Current naming rules:

- Use `Server` for the implemented domain model.
- Use `ServerInventory` for the implemented feature scope.
- Use `ServerForm` for UI and state shared by Add Server and Edit Server.
- Do not introduce `Device`, `ServerDevice`, `InventoryItem`, or `feature/inventory` naming until the broader concept is implemented.

---

## Not Implemented

The following items are intentionally not implemented in the Server Inventory baseline:

- Non-server inventory asset types.
- Generic inventory package or model.
- Credential storage inside the server inventory table.
- Server Inventory-owned connection history. Per-server SSH connection history is implemented and owned by the SSH feature.
- Monitoring data.
- Server grouping beyond the currently implemented metadata fields.

---

## Related Documents

- [Project State](../PROJECT_STATE.md)
- [Architecture](../ARCHITECTURE.md)
- [Roadmap](../ROADMAP.md)
- [Changelog](../CHANGELOG.md)

---
