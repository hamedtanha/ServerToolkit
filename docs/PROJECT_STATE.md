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

The current focus is planning SSH host trust and authentication input boundaries before any real SSH connection behavior.

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
- SSH data-layer adapter shell.
- SSHJ-backed connection service shell without real network behavior.
- SSH connection service dependency injection binding.
- SSH ViewModel dependency injection for the connection service.
- SSH user-triggered connect event shell.
- SSH placeholder Connect button.
- SSH connect event shell ViewModel tests.
- SSHJ adapter shell unit test.
- SSH domain connection request model.
- SSH domain connection result model.
- SSH domain connection error model.
- SSH domain connection service contract.
- SSH domain model unit tests.
- Test-only fake SSH connection service.
- SSH ViewModel result handling seam for tests.
- SSH ViewModel fake result unit tests.
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
- SSH authentication handling.
- SSH host key verification implementation.
- SSH session lifecycle model.
- Credential input.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Room migration beyond database version 1.
- Migration tests.

---

## In Progress

The current implementation area is:

- SSH host trust and authentication input strategy planning.
- SSH host key verification behavior planning.
- Future real connection behavior planning.

---

## Next Planned Work

The next safe development steps are:

1. Review and accept ADR-009 before any real SSH connection behavior.
2. Implement host trust and authentication input models after ADR acceptance.
3. Add explicit safeguards and tests before enabling real SSH connection behavior.

---

## Current Git Workflow Context

Main branch must remain releasable.

Implementation and documentation changes must happen on short-lived GitHub Flow branches and be merged through pull requests.

---

## Current Engineering Rule

Do not add real SSH behavior before the corresponding architecture decisions and tests are in place.
