# Roadmap

**Project:** Server Toolkit  
**Version:** 0.2.0-alpha  
**Status:** Active  
**Last Updated:** 2026-07-03

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

## Version 0.2.0 — Android Architecture and Navigation

Objective:

Establish the Android application architecture and the first navigable application flow.

Completed:

- Single Activity structure.
- Hilt application setup.
- App-level Navigation Compose infrastructure.
- Dashboard route.
- Server Inventory route.
- Add Server route.
- Dashboard-to-Server-Inventory navigation action.
- Add Server placeholder navigation.
- Package structure cleanup.
- Removal of obsolete package placeholders.

Remaining:

- Documentation alignment.
- Navigation flow review.
- Basic UI stabilization.

Status:

In Progress.

---

## Version 0.3.0 — Server Inventory Foundation

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
- Add Server UI state.
- Add Server ViewModel.
- Real Add Server form.
- Add Server validation.
- Add Server validation-only save behavior.
- Server repository contract.
- In-memory Server repository implementation.
- Server Inventory repository dependency injection binding.
- Server Inventory ViewModel repository observation.

Planned:

- Server list rendering.
- Add Server persistence-backed save workflow.
- Edit server workflow.
- Local persistence with Room.
- Server entity.
- Server DAO.
- Search and filtering behavior.

Deliverable:

Working local server inventory.

Status:

In Progress.

---

## Version 0.4.0 — SSH

Objective:

Introduce secure SSH connectivity.

Planned:

- SSH client integration.
- Authentication.
- Host key verification.
- Session management.
- Connection history.

Deliverable:

Reliable SSH connections to managed servers.

Status:

Planned.

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

- UI refinement.
- Performance improvements.
- Bug fixing.
- Documentation review.
- Test coverage improvements.

Deliverable:

Release candidate.

Status:

Planned.

---

## Version 1.0.0 — Initial Release

Objective:

Deliver the first stable public release.

Planned:

- Stable Server Inventory.
- Stable SSH workflow.
- Stable operational workflows.
- Updated documentation.
- Release-ready build.

Status:

Planned.
