# Architecture Decision Records (ADR)

**Project:** Server Toolkit

**Version:** 0.4.0-alpha

**Status:** Active

**Last Updated:** 2026-07-14

---

# Purpose

This directory contains all Architecture Decision Records for the Server Toolkit project.

---

# Current ADRs

| ADR | Title | Status |
|------|-------|--------|
| ADR-001 | Project Vision | Accepted |
| ADR-002 | Application Architecture | Accepted |
| ADR-003 | Local Persistence with Room | Accepted |
| ADR-004 | Navigation Strategy | Accepted |
| ADR-005 | Dependency Injection Strategy | Accepted |
| ADR-006 | SSH Workflow and Security Boundaries | Accepted |
| ADR-007 | Secure Storage Strategy | Accepted |
| ADR-008 | SSH Client Library Selection | Accepted |
| ADR-009 | SSH Host Trust and Authentication Input Strategy | Accepted |
| ADR-010 | SSH Command Channel Execution Strategy | Accepted |
| ADR-011 | SSH Credential Ownership and Secure Storage Strategy | Accepted |
| ADR-012 | Android Backup and Data Extraction Policy | Accepted |
| ADR-013 | Ephemeral SSH Private-Key Authentication Boundary | Accepted |
| ADR-014 | Android Release Signing Strategy | Accepted |

---

# Decision Boundary Map

This map clarifies the primary owner, decision role, and dependency context of each accepted ADR. It is navigational guidance only; the complete text of each ADR remains authoritative.

Decision roles:

- **Foundational:** establishes a top-level product or architecture decision.
- **Extension:** adds a distinct capability while preserving accepted parent decisions.
- **Refinement:** narrows or specifies accepted decisions for a particular context.
- **Cross-cutting:** establishes a policy that applies across multiple decision boundaries.

The decision role describes what an ADR contributes. The dependency column identifies earlier accepted decisions that constrain or inform it; dependency does not transfer ownership of the decision.

| ADR | Primary decision owner | Decision role | Depends on |
|---|---|---|---|
| ADR-001 | Product direction and infrastructure-management identity | Foundational | None |
| ADR-002 | Application architecture and dependency direction | Foundational | ADR-001 |
| ADR-003 | Structured local persistence technology and Room boundaries | Extension | ADR-002 |
| ADR-004 | Application navigation strategy and route ownership | Extension | ADR-002 |
| ADR-005 | Dependency injection framework and dependency-construction rules | Extension | ADR-002 |
| ADR-006 | SSH feature, workflow, lifecycle, and initial security boundary | Extension | ADR-001, ADR-002 |
| ADR-007 | Secure-storage foundation for runtime connection secrets | Cross-cutting | ADR-003, ADR-006 |
| ADR-008 | SSH client library selection and adapter isolation | Refinement | ADR-006 |
| ADR-009 | SSH host trust, authentication input, and initial session boundaries | Refinement | ADR-006, ADR-007, ADR-008 |
| ADR-010 | Non-interactive SSH command-channel execution | Extension | ADR-006, ADR-008, ADR-009 |
| ADR-011 | SSH credential ownership and future persistent credential model | Refinement | ADR-007, ADR-009 |
| ADR-012 | Android backup, data extraction, and device-transfer policy | Cross-cutting | ADR-003, ADR-007, ADR-009, ADR-011 |
| ADR-013 | Ephemeral SSH private-key authentication and one-shot key-source lifecycle | Refinement | ADR-006, ADR-008, ADR-009, ADR-011, ADR-012 |
| ADR-014 | Android application signing identity and release artifact trust | Cross-cutting | ADR-001 |

No current ADR supersedes another ADR. A future decision that replaces an accepted decision must declare the superseded ADR explicitly; document date or ADR number alone does not establish precedence.

---

# Related Documents

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- PROJECT_STATE.md
- ROADMAP.md
