# Product Vision

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Frozen  
**Last Updated:** 2026-07-02

---

# Purpose

Server Toolkit is a modern Android application for Linux system administrators, DevOps engineers, infrastructure engineers, network engineers, and advanced users who manage one or more remote servers.

The primary goal is to simplify common infrastructure administration workflows from a mobile device.

Unlike traditional SSH clients, Server Toolkit is focused on operational workflows rather than terminal access alone.

---

# Problem Statement

Most Android SSH applications provide a terminal emulator and leave operational workflows to the user.

Administrators often need multiple tools to complete routine tasks such as:

- Managing server inventory
- Checking server availability
- Viewing basic system status
- Executing predefined maintenance commands
- Managing Xray and x-ui services
- Renewing certificates
- Separating production and test environments

Server Toolkit aims to combine these common workflows into a single Android application with a clean and secure user experience.

---

# Vision

Server Toolkit should become a reliable mobile companion for Linux server administration.

The application should help administrators perform frequent maintenance and inspection tasks quickly, securely, and with less friction than using a generic terminal-only SSH client.

---

# Target Audience

- Linux System Administrators
- DevOps Engineers
- Network Engineers
- Infrastructure Engineers
- Cloud Engineers
- Homelab Enthusiasts
- Security Professionals

---

# Product Principles

- Simplicity
- Reliability
- Security
- Performance
- Maintainability
- Scalability

---

# Planned Capability Areas

The following capability areas define the long-term product direction. They do not imply that all capabilities currently exist.

- Server inventory management
- Dashboard and server overview
- SSH-based server operations
- Ping and latency monitoring
- Server health overview
- Favorite servers
- Command library
- Xray management
- x-ui management
- Certificate management
- Backup and restore
- Encrypted export and import
- Optional cloud synchronization

---

# Explicit Non-Goals

Server Toolkit is not intended to replace:

- Desktop SSH clients
- Full monitoring platforms
- Configuration management systems
- Kubernetes dashboards
- Enterprise SIEM or observability platforms

Server Toolkit complements existing infrastructure tools instead of replacing them.

---

# Success Criteria

The project is successful when administrators can complete their most common mobile maintenance and inspection tasks in less than one minute while keeping credentials and sensitive server data secure.

---

# Current Product Stage

The product foundation has been established in version `0.1.0`.

Current development focus:

```text
v0.2.0 — Navigation
```

The product is still in early development and is not ready for production use.

---

# Document Governance

This document is a foundational product document.

Status:

```text
Frozen
```

Changes are allowed only when one of the following applies:

- The product vision changes materially.
- The target audience changes materially.
- A major scope decision is accepted.
- An Architecture Decision Record requires an update.

Minor wording changes should be avoided unless they improve clarity without changing meaning.

---

# Related Documents

- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md
- PROJECT_STATE.md
- docs/adr/ADR-001-project-vision.md
