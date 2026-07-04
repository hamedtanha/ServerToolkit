# Project State

**Project:** Server Toolkit  
**Version:** 0.4.0-alpha  
**Status:** Active Implementation  
**Last Updated:** 2026-07-04

---

## Purpose

This document is the single source of truth for the current implementation state of the Server Toolkit project.

It summarizes what is implemented, what is intentionally not implemented yet, and what should happen next.

This document must stay short, current, and factual.

---

## Current Phase

The Server Inventory 0.3.0 baseline is accepted.

The current focus is introducing the first SSH UI boundary without real connection behavior.

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
- Server Form UI state shared by Add Server and Edit Server.
- Add Server ViewModel.
- Shared Server Form screen.
- Add Server form fields.
- Add Server validation state.
- Add Server validation-only save action.
- Add Server repository-backed save flow.
- Add Server automatic return after successful save.
- Edit Server navigation destination.
- Edit Server route.
- Edit Server ViewModel.
- Edit Server form reuse through the shared Server Form screen.
- Edit Server repository-backed save flow preserving the existing server id.
- Manual Edit Server verification after app restart.
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
- Basic Server Inventory list rendering.
- Delete server UI action with confirmation dialog.
- Server Inventory ViewModel delete action.
- Manual delete flow verification after app restart.
- Server Inventory search by name, host, username, category, and tags.
- Server Inventory environment filtering.
- Server Inventory favorites-only filtering.
- Server Inventory filter clearing.
- Manual search and filtering verification.
- Automated search and filtering verification through unit tests, instrumented tests, and debug build.
- Automated Edit Server verification through unit tests, instrumented tests, and debug build.
- Automated shared Server Form naming verification through unit tests, instrumented tests, and debug build.
- Server Inventory stabilization checklist.
- Accepted Server Inventory 0.3.0 baseline.
- SSH workflow architecture decision.
- Secure storage strategy decision.
- SSH navigation destination.
- SSH placeholder screen.
- SSH placeholder ViewModel and UI state.
- Server Inventory Connect action to open the SSH placeholder.
- Server Inventory filter matcher unit tests.
- DAO instrumentation tests for insert, replace, and delete behavior.
- Room-backed repository instrumentation tests for save, replace, and delete behavior.
- Server entity/domain mapper unit tests.
- Local persistence with Room architecture decision.
- Package structure cleanup from `feature/servers` to `feature/serverinventory`.
- Shared Add/Edit server form naming cleanup.
- App-level package structure alignment with the current source tree.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Real SSH connection behavior.
- Persistent sensitive connection storage.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Room migration beyond database version 1.
- Migration tests.

---

## In Progress

The current implementation area is:

- SSH placeholder verification.
- SSH UI boundary stabilization.
- Preparation for a future SSH client decision.

---

## Next Planned Work

The next safe development steps are:

1. Verify the SSH placeholder route manually from a server item.
2. Add automated coverage for the SSH placeholder if the UI boundary grows.
3. Keep real SSH connection behavior out of scope until the SSH client decision is accepted.

---

## Current Git Branch

```text
feature/ssh-architecture
```

---

## Current Engineering Rule

Do not add real SSH behavior or persistent sensitive connection storage before the corresponding architecture decisions and tests are in place.
