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

The current focus is aligning the SSH presentation state with the project-owned domain result model.

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
- SSH client library selection decision.
- SSHJ dependency declaration through the Gradle version catalog.
- SSH domain connection request model.
- SSH domain connection result model.
- SSH domain connection error model.
- SSH domain connection service contract.
- SSH domain model unit tests.
- SSH UI connection status model.
- SSH UI state alignment with domain connection result.
- SSH connection result UI mapper.
- SSH presentation state unit tests.
- SSH navigation destination.
- SSH placeholder screen.
- SSH placeholder ViewModel and UI state.
- Server Inventory Connect action to open the SSH placeholder.
- Manual SSH placeholder route verification.
- Automated SSH placeholder verification through unit tests and debug build.
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
- SSHJ adapter implementation.
- SSH session lifecycle model.
- Fake SSH connection service.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Room migration beyond database version 1.
- Migration tests.

---

## In Progress

The current implementation area is:

- SSH presentation state verification.
- SSH ViewModel test planning.
- Future fake SSH connection service planning.

---

## Next Planned Work

The next safe development steps are:

1. Verify the SSH UI state alignment with unit tests and debug build.
2. Add a fake SSH connection service for ViewModel tests.
3. Connect the SSH ViewModel to the fake service without real network behavior.

---

## Current Git Branch

```text
feature/ssh-ui-state-alignment
```

---

## Current Engineering Rule

Do not add real SSH behavior before the corresponding architecture decisions and tests are in place.
