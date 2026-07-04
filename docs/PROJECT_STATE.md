# Project State

**Project:** Server Toolkit  
**Version:** 0.2.0-alpha  
**Status:** Active Implementation  
**Last Updated:** 2026-07-04

---

## Purpose

This document is the single source of truth for the current implementation state of the Server Toolkit project.

It summarizes what is implemented, what is intentionally not implemented yet, and what should happen next.

This document must stay short, current, and factual.

---

## Current Phase

The project is in early Android feature implementation.

The current focus is preparing the next Server Inventory behavior after validating the first Room-backed persistence boundary.

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
- Add Server form fields.
- Add Server validation state.
- Add Server validation-only save action.
- Add Server repository-backed save flow.
- Add Server automatic return after successful save.
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
- DAO instrumentation tests for insert, replace, and delete behavior.
- Room-backed repository instrumentation tests for save, replace, and delete behavior.
- Server entity/domain mapper unit tests.
- Local persistence with Room architecture decision.
- Package structure cleanup from `feature/servers` to `feature/serverinventory`.
- App-level package structure alignment with the current source tree.

---

## Not Implemented Yet

The following items are intentionally not implemented yet:

- Edit server screen.
- Delete server UI action.
- Search and filtering behavior.
- Room migration beyond database version 1.
- Migration tests.
- SSH connection workflow.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Secure credential storage.

---

## In Progress

The current implementation area is:

- Server Inventory behavior expansion planning.
- Delete server action design.
- Preservation of persistence test coverage before adding new inventory operations.

---

## Next Planned Work

The next safe development steps are:

1. Run mapper unit tests and persistence instrumentation tests locally.
2. Add a delete server UI action behind the existing `ServerRepository.deleteServer` contract.
3. Keep edit, search, SSH, and credential handling out of scope until delete behavior is implemented and reviewed.

---

## Current Git Branch

```text
feature/android-project
```

---

## Current Engineering Rule

No SSH or credential handling should be introduced until Room-backed server inventory persistence and basic inventory modification flows are tested and reviewed.
