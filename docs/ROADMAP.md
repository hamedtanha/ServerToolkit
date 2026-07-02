# Roadmap

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Active  
**Last Updated:** 2026-07-02

---

# Overview

This roadmap describes the planned evolution of Server Toolkit.

The roadmap provides direction and milestone order. It does not define fixed delivery dates.

---

# Roadmap Policy

The roadmap is a planning document.

It may change when product priorities, technical constraints, or implementation realities change.

Major roadmap changes must be documented through an Architecture Decision Record when they affect architecture, security, release strategy, or product scope.

---

# Current Project Status

Current stable checkpoint:

```text
v0.1.0 — Project Foundation
```

Current development milestone:

```text
v0.2.0 — Navigation
```

Progress:

```text
Navigation: Not Started
```

---

# Milestone Roadmap

| Version | Milestone | Goal | Status |
|---|---|---|---|
| v0.1.0 | Project Foundation | Establish project structure, documentation, architecture baseline, and development workflow. | Completed |
| v0.2.0 | Navigation | Add application navigation foundation. | Not Started |
| v0.3.0 | Dashboard | Add initial home dashboard and basic UI structure. | Planned |
| v0.4.0 | Server Inventory | Manage server information locally in memory or temporary state. | Planned |
| v0.5.0 | Local Storage | Persist server data using a local database. | Planned |
| v0.6.0 | SSH Connectivity | Establish secure SSH connectivity and connection validation. | Planned |
| v0.7.0 | Monitoring | Add basic server availability and latency monitoring. | Planned |
| v0.8.0 | Xray Integration | Add Xray and x-ui operational workflows. | Planned |
| v0.9.0 | Beta Stabilization | Improve reliability, testing, security, and user experience before stable release. | Future |
| v1.0.0 | First Stable Release | Prepare the application for public distribution. | Future |

---

# v0.1.0 — Project Foundation

Goal:

Build a stable, maintainable, and scalable project foundation.

Completed work:

- Android Studio project initialized
- Git repository configured
- GitHub repository created
- Documentation foundation created
- MVVM package structure established
- Initial Server model created
- Development workflow defined
- Architecture baseline documented
- ADR process created
- Initial release tag created

Status:

```text
Completed
```

---

# v0.2.0 — Navigation

Goal:

Create the application navigation foundation.

Scope:

- Define screen routes
- Add navigation graph
- Add initial app navigation host
- Prepare navigation for Home, Add Server, Server Details, and Settings screens

Expected implementation targets:

```text
navigation/Screen.kt
navigation/AppNavigation.kt
```

Status:

```text
Not Started
```

---

# v0.3.0 — Dashboard

Goal:

Create the first useful landing experience for the application.

Planned features:

- Home screen
- Dashboard layout
- Basic server summary placeholders
- Empty state
- Navigation entry points

Status:

```text
Planned
```

---

# v0.4.0 — Server Inventory

Goal:

Manage server information.

Planned features:

- Add server
- Edit server
- Delete server
- Favorite server
- Search servers
- Categories
- Input validation

Status:

```text
Planned
```

---

# v0.5.0 — Local Storage

Goal:

Persist application data securely and reliably.

Planned features:

- Room database
- Repository implementation
- Data migration strategy
- Import
- Export

Status:

```text
Planned
```

---

# v0.6.0 — SSH Connectivity

Goal:

Communicate with remote servers securely.

Planned features:

- SSH connection
- Connection validation
- Saved connections
- Host fingerprint verification
- Multiple authentication methods

Status:

```text
Planned
```

---

# v0.7.0 — Monitoring

Goal:

Provide basic operational visibility.

Planned features:

- Ping
- Latency
- Online status
- Last seen
- Basic server information
- Dashboard statistics

Status:

```text
Planned
```

---

# v0.8.0 — Xray Integration

Goal:

Manage Xray-related operational workflows.

Planned features:

- Xray status
- Restart service
- View logs
- Configuration overview
- x-ui integration

Status:

```text
Planned
```

---

# v0.9.0 — Beta Stabilization

Goal:

Prepare the project for stable release quality.

Planned work:

- Reliability improvements
- Security review
- UI polish
- Performance review
- Accessibility review
- Testing improvements
- Documentation review

Status:

```text
Future
```

---

# v1.0.0 — First Stable Release

Goal:

Prepare the application for public distribution.

Planned work:

- Release build
- GitHub Release
- APK distribution
- Release notes
- Final documentation review
- Optional Google Play preparation

Status:

```text
Future
```

---

# Long-Term Candidate Modules

The following modules are intentionally outside the current 1.0 roadmap unless explicitly promoted later:

- Docker
- Docker Compose
- Podman
- Kubernetes
- WireGuard
- Tailscale
- Proxmox
- VMware
- Hyper-V
- Cloud provider integrations
- Notifications
- Background monitoring
- Cloud synchronization

---

# Related Documents

- PRODUCT_VISION.md
- PROJECT_STATE.md
- DEVELOPMENT.md
- ARCHITECTURE.md
- CHANGELOG.md
