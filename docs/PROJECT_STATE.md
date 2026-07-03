# Project State

**Project:** Server Toolkit  
**Version:** 0.2.0-alpha  
**Status:** Active Implementation  
**Last Updated:** 2026-07-03

---

## Purpose

This document is the single source of truth for the current implementation state of the Server Toolkit project.

It summarizes what is implemented, what is intentionally not implemented yet, and what should happen next.

This document must stay short, current, and factual.

---

## Current Phase

The project is in early Android feature implementation.

The current focus is building the initial Dashboard and Server Inventory scaffolding while preserving architectural consistency and documentation accuracy.

---

## Implemented

The following application-level items are implemented:

- Single Activity application entry point.
- Hilt-enabled application setup.
- App-level Navigation Compose infrastructure.
- Dashboard navigation destination.
- Dashboard ViewModel.
- Dashboard UI state.
- Dashboard empty-state screen.
- Dashboard navigation action to Server Inventory.
- Server Inventory navigation destination.
- Server Inventory domain model.
- Server Inventory environment model.
- Server Inventory filter state.
- Server Inventory UI state.
- Server Inventory ViewModel.
- Server Inventory empty screen.
- Server Inventory empty-state action.
- Add Server navigation destination.
- Add Server placeholder screen.
- Add Server UI state.
- Add Server ViewModel.
- Package structure cleanup from `feature/servers` to `feature/serverinventory`.
- App-level package structure alignment with the current source tree.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Server repository contract.
- Server repository implementation.
- Room database integration.
- Server entity.
- Server DAO.
- Real Add Server form.
- Add Server validation.
- Add Server save action.
- Edit server screen.
- Server list rendering.
- SSH connection workflow.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Secure credential storage.

---

## In Progress

The current implementation area is:

- Server Inventory feature scaffolding.
- Add Server placeholder documentation alignment.
- Preparation for real Add Server form design.

---

## Next Planned Work

The next safe development steps are:

1. Keep documentation synchronized with the current implementation.
2. Review whether an implementation note is needed for the current Server Inventory scaffold.
3. Stabilize the Server Inventory UI foundation.
4. Defer persistence, repository, add/edit server workflows, SSH, and credential handling until the current scaffold is reviewed.

---

## Current Git Branch

```text
feature/android-project
```

---

## Current Engineering Rule

No database, repository, SSH, credential handling, or add/edit server workflow should be introduced until the Server Inventory scaffold is reviewed and documented.
