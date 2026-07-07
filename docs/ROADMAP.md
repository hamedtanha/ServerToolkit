# Roadmap

**Project:** Server Toolkit
**Version:** 0.4.0-alpha
**Status:** Active Implementation
**Last Updated:** 2026-07-06

---

## Purpose

This roadmap describes the planned evolution of the Server Toolkit project.

It provides a high-level view of development milestones without specifying low-level implementation details.

Roadmap items may evolve over time, but each milestone should have a clear engineering objective.

---

## Guiding Principles

Development follows these principles:

- Build incrementally.
- Complete one major feature area at a time.
- Preserve architectural consistency.
- Avoid unnecessary scope expansion.
- Keep the application releasable.
- Keep documentation synchronized with implementation.

---

## Version 0.1.0 — Foundation ✅

Objective:

Establish the engineering foundation.

Completed:

- Repository initialization.
- Development workflow.
- Documentation structure.
- ADR process.
- Project vision.
- Engineering guidelines.
- Initial architecture documentation.
- Initial Android application skeleton.

Status:

Completed.

---

## Version 0.2.0 — Android Architecture and Navigation ✅

Objective:

Establish the Android application architecture and the first navigable application flow.

Completed:

- Single Activity structure.
- Hilt application setup.
- App-level Navigation Compose infrastructure.
- Dashboard route.
- Server Inventory route.
- Add Server route.
- Edit Server route.
- Dashboard-to-Server-Inventory navigation action.
- Add Server navigation.
- Edit Server navigation.
- Package structure cleanup.
- Removal of obsolete package placeholders.

Status:

Completed.

---

## Version 0.3.0 — Server Inventory Foundation ✅

Objective:

Implement the foundation for local server inventory management.

Completed:

- Server domain model.
- Server environment model.
- Server Inventory filter state.
- Server Inventory UI state.
- Server Inventory ViewModel.
- Server Inventory empty screen.
- Server Inventory screen structure refinement.
- Server Inventory UI state semantic clarification.
- Server Inventory empty-state action.
- Add Server placeholder screen.
- Shared Server Form UI state.
- Add Server ViewModel.
- Shared Server Form screen.
- Real Add Server form.
- Add Server validation.
- Add Server validation-only save behavior.
- Add Server repository-backed in-memory save behavior.
- Add Server automatic return after successful save.
- Edit Server ViewModel.
- Edit Server form reuse.
- Edit Server repository-backed save behavior preserving the existing server id.
- Manual Edit Server verification.
- Automated Edit Server verification.
- Server repository contract.
- In-memory Server repository implementation.
- Server Inventory repository dependency injection binding.
- Server Inventory ViewModel repository observation.
- Basic Server Inventory list rendering.
- Delete server UI action.
- Manual delete flow verification.
- Search and filtering behavior.
- Manual search and filtering verification.
- Automated search and filtering verification.
- Server Inventory stabilization checklist.
- Accepted Server Inventory 0.3.0 baseline.
- Local persistence with Room architecture decision.
- Room dependency and KSP compiler setup.
- Room schema export configuration.
- Initial Room schema export.
- Server Toolkit Room database class.
- Server entity.
- Server DAO.
- Server entity/domain mapper.
- Room-backed Server repository implementation.
- Hilt database and DAO providers.
- Server DAO instrumentation tests.
- Room-backed repository instrumentation tests.
- Server entity/domain mapper unit tests.
- Server Inventory filter matcher unit tests.

Follow-up:

- Server Inventory UI density review.
- One-character search behavior review.
- Migration tests when database version changes.

Deliverable:

Working local server inventory baseline.

Status:

Accepted baseline.

---

## Version 0.4.0 — SSH

Objective:

Introduce secure SSH connectivity.

Entry condition:

The Server Inventory 0.3.0 baseline has been accepted. Credential storage still requires a separate reviewed implementation before persistent credential storage is added.

In progress:

- SSH architecture and security ADRs.
- SSHJ integration through data-layer boundaries.
- Authentication boundary definition without persistent credential storage.
- Ephemeral password input for single-attempt SSH authentication.
- Host key verification and trust decision flow.
- Project-owned session management boundary.
- Non-interactive command execution workflow.
- Command execution session-invalidation and cancellation hardening.

Planned:

- Additional SSH host key verification hardening, if future runtime testing identifies gaps.
- Persistent credential storage only after reviewed secure storage implementation.
- Connection history only after a separate reviewed design.

Deliverable:

Reliable SSH connections to managed servers.

Status:

Active implementation.

---

## Version 0.5.0 — Operations

Objective:

Improve operational efficiency.

Planned:

- Saved commands.
- Command categories.
- Favorites.
- Execution history.
- Quick actions.

Deliverable:

Repeatable operational workflows.

Status:

Planned.

---

## Version 0.6.0 — Dashboard Evolution

Objective:

Evolve the Dashboard from a simple entry screen into an operational overview.

Planned:

- Recent servers.
- Favorite servers.
- Connection status.
- Quick statistics.
- Server Inventory summary.

Deliverable:

Operational home screen.

Status:

Planned.

---

## Version 0.7.0 — Monitoring

Objective:

Provide lightweight server monitoring.

Planned:

- Basic availability checks.
- Resource indicators.
- Service status.
- Refresh support.

Deliverable:

Basic monitoring capabilities.

Status:

Planned.

---

## Version 0.8.0 — Infrastructure Helpers

Objective:

Support common administration workflows.

Planned:

- Xray helper.
- x-ui helper.
- Certificate helper.
- Maintenance utilities.

Deliverable:

Integrated infrastructure tools.

Status:

Planned.

---

## Version 0.9.0 — Stabilization

Objective:

Prepare for production release.

Planned:

- End-to-end flow review.
- Error-state handling review.
- UI consistency review.
- Documentation review.
- Release readiness checklist.

Deliverable:

Stable pre-1.0 application baseline.

Status:

Planned.

---

## Version 1.0.0 — Initial Production Release

Objective:

Deliver the first production-ready version of Server Toolkit.

Expected scope:

- Stable local server inventory.
- Stable SSH connection workflow.
- Basic operational workflows.
- Synchronized documentation.
- Reviewed security boundaries.

Status:

Planned.
