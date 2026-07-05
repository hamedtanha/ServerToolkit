# Project State

**Project:** Server Toolkit  
**Version:** 0.4.0-alpha  
**Status:** Active Implementation  
**Last Updated:** 2026-07-05

---

## Purpose

This document is the single source of truth for the current implementation state of the Server Toolkit project.

It summarizes what is implemented, what is intentionally not implemented yet, and what should happen next.

This document must stay short, current, and factual.

---

## Current Phase

The Server Inventory 0.3.0 baseline is accepted.

ADR-009 is accepted as the SSH host trust and authentication input strategy.

Android backup and data extraction are disabled for the alpha release to avoid backing up infrastructure inventory or future SSH trust material before a reviewed restore model exists.

The current focus is defining the remaining security and integration gates before any real SSH connection behavior.

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
- Accepted SSH host trust and authentication input strategy decision.
- SSHJ dependency declaration through the Gradle version catalog.
- SSH data-layer adapter shell.
- SSHJ-backed connection service shell without real network behavior.
- SSH connection service dependency injection binding.
- Core connection target-resolution contract.
- Server Inventory-backed connection target resolver.
- SSH connection attempt use case.
- SSH connection attempt timeout boundary.
- SSH connection attempt exception mapping and cancellation preservation.
- SSH duplicate-attempt prevention at the ViewModel boundary.
- SSH host key fingerprint model.
- SSH host endpoint trust lookup model.
- SSH observed and trusted host key models.
- SSH host trust status model for unknown, trusted, and changed host keys.
- SSH trusted host storage contract.
- SSH trusted host Room entity and DAO.
- SSH trusted host entity/domain mapper.
- SSH trusted host Room-backed repository implementation.
- SSH trusted host repository dependency injection binding.
- Server Toolkit database version 2 with trusted-host migration.
- SSH host trust evaluator.
- SSH host trust decision model.
- SSH host trust decision use case.
- SSH explicit unknown-host trust confirmation use case.
- SSH changed-host-key blocking decision flow.
- SSH host-key review presentation state.
- SSH host-key review UI mapper.
- SSH host-key review confirmation and cancellation ViewModel events.
- SSH host-key review screen actions.
- SSH authentication method model.
- SSH authentication input UI-safe presentation state.
- SSH ephemeral authentication input ViewModel events.
- SSH authentication input clearing behavior.
- SSH credential-bearing connection request boundary.
- SSH authentication input redaction and clearing at the connection attempt boundary.
- SSH ViewModel dependency injection for the connection attempt use case.
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
- Persistent credential storage.
- Monitoring workflow.
- Command execution workflow.
- Xray or x-ui management workflow.
- Room migrations beyond database version 2.
- Migration tests beyond the trusted-host v1-to-v2 migration.

---

## In Progress

The current implementation area is:

- SSH host-key review integration planning.
- SSHJ authentication adapter planning.

---

## Next Planned Work

The next safe development steps are:

1. Integrate observed host-key review with the future SSH connection boundary.
2. Add SSHJ authentication adapter mapping behind the existing host-trust and authentication boundaries.
3. Keep real SSH behavior disabled until all remaining ADR-009 implementation gates are satisfied.

---

## Current Git Workflow Context

Main branch must remain releasable.

Implementation and documentation changes must happen on short-lived GitHub Flow branches and be merged through pull requests.

---

## Current Engineering Rule

Do not add real SSH behavior before the remaining ADR-009 implementation gates, host trust, ephemeral authentication input, and tests are in place.
