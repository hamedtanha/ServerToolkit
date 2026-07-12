# Server Inventory Status

**Project:** Server Toolkit
**Feature Area:** Server Inventory
**Status:** Accepted Baseline
**Related Milestone:** Version 0.3.0 — Server Inventory Foundation
**Last Updated:** 2026-07-12

---

## Purpose

This document records the detailed implementation status of the Server Inventory feature area.

The high-level project state remains documented in [Project State](../PROJECT_STATE.md).

---

## Current Status

The Server Inventory 0.3.0 baseline is accepted.

The feature provides local server inventory management backed by Room persistence.

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
- DAO instrumentation tests for insert, replace, and delete behavior.
- Room-backed repository instrumentation tests for save, replace, and delete behavior.
- Server entity/domain mapper unit tests.

---

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
- Connection history.
- Monitoring data.
- Server grouping beyond the currently implemented metadata fields.

---

## Related Documents

- [Project State](../PROJECT_STATE.md)
- [Architecture](../ARCHITECTURE.md)
- [Roadmap](../ROADMAP.md)
- [Changelog](../CHANGELOG.md)

---
