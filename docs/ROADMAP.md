# Roadmap

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Active  
**Last Updated:** 2026-07-01

---

# Purpose

This roadmap describes the planned evolution of the Server Toolkit project.

It provides a high-level view of development milestones without specifying implementation details.

Roadmap items may evolve over time, but each milestone should have a clear engineering objective.

---

# Guiding Principles

Development follows these principles:

- Build incrementally.
- Complete one major feature at a time.
- Preserve architectural consistency.
- Avoid unnecessary scope expansion.
- Keep the application releasable.

---

# Version 0.1.0 — Foundation ✅

Objective:

Establish the engineering foundation.

Completed:

- Repository initialization
- Development workflow
- Documentation structure
- ADR process
- Project vision
- Engineering guidelines
- Initial architecture documentation

Status:

Completed

---

# Version 0.2.0 — Architecture

Objective:

Define the technical architecture before feature implementation.

Planned:

- Navigation strategy
- Project structure
- Package organization
- Data layer definition
- Dependency strategy
- Persistence strategy
- Security strategy

Deliverable:

A stable Android project skeleton with documented architectural decisions.

---

# Version 0.3.0 — Server Management

Objective:

Implement the foundation for server administration.

Planned:

- Server model
- Local server storage
- Server CRUD
- Server grouping
- Server details
- Search and filtering

Deliverable:

Working server inventory.

---

# Version 0.4.0 — SSH

Objective:

Introduce secure SSH connectivity.

Planned:

- SSH client integration
- Authentication
- Host key verification
- Session management
- Connection history

Deliverable:

Reliable SSH connections to managed servers.

---

# Version 0.5.0 — Operations

Objective:

Improve operational efficiency.

Planned:

- Saved commands
- Command categories
- Favorites
- Execution history
- Quick actions

Deliverable:

Repeatable operational workflows.

---

# Version 0.6.0 — Dashboard

Objective:

Provide an operational overview.

Planned:

- Dashboard
- Recent servers
- Favorite servers
- Connection status
- Quick statistics

Deliverable:

Operational home screen.

---

# Version 0.7.0 — Monitoring

Objective:

Provide lightweight server monitoring.

Planned:

- Basic availability checks
- Resource indicators
- Service status
- Refresh support

Deliverable:

Basic monitoring capabilities.

---

# Version 0.8.0 — Infrastructure Helpers

Objective:

Support common administration workflows.

Planned:

- Xray helper
- x-ui helper
- Certificate helper
- Maintenance utilities

Deliverable:

Integrated infrastructure tools.

---

# Version 0.9.0 — Stabilization

Objective:

Prepare for production release.

Planned:

- UI refinement
- Performance improvements
- Bug fixing
- Documentation review
- Test coverage improvements

Deliverable:

Release candidate.

---

# Version 1.0.0 — Initial Release

Objective:

Deliver the first stable public release.

Requirements:

- Stable architecture
- Complete documentation
- Reliable SSH support
- Server inventory
- Dashboard
- Command execution
- Infrastructure helper workflows

Deliverable:

Production-ready release.

---

# Future

Possible future directions include:

- Docker management
- WireGuard
- Tailscale
- Kubernetes
- Cloud integrations
- Notifications
- Synchronization
- Plugin architecture

These items are intentionally outside the current roadmap.

---

# Roadmap Maintenance

The roadmap should evolve gradually.

Completed milestones should never be rewritten.

New milestones should preserve the project's long-term architectural vision.