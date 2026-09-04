# Architecture Decision Records (ADR)

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Active
**Last Updated:** 2026-09-04

---

# Purpose

This directory contains all Architecture Decision Records for the Server Toolkit project.

Accepted ADRs are immutable decision records. A later ADR may refine, extend, or supersede an earlier decision, but historical ADR text must not be rewritten to hide the original context.

## ADR Admission Gate

The default for ordinary implementation work is **no new ADR**.

Create an ADR only when a durable architectural decision is genuinely required and existing accepted ADRs do not already govern the decision.

An ADR records the decision, meaningful alternatives, rationale, and durable consequences. It is not the home for implementation plans, delivery slices, acceptance criteria, test matrices, runtime evidence, benchmark results, or implementation status.

After an ADR is accepted, implementation and verification evidence must be recorded in the appropriate Issue, pull request, current-state documentation, or Changelog without rewriting the accepted decision record.

---

# Current ADRs

| ADR | Title | Status |
|---|---|---|
| ADR-001 | Project Vision | Accepted; partially superseded by ADR-015 |
| ADR-002 | Application Architecture | Accepted; refined by ADR-016 |
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
| ADR-015 | Platform-Neutral Remote Systems Product Direction | Accepted |
| ADR-016 | Three-Level Remote Capability Architecture | Accepted |
| ADR-017 | Scalable Collection UX Contract | Accepted |

---

# Decision Boundary Map

This map clarifies the primary owner, decision role, dependency context, and relationship of each accepted ADR. It is navigational guidance only; the complete ADR text remains authoritative.

Decision roles:

- **Foundational:** establishes a top-level product or architecture decision.
- **Extension:** adds a distinct capability while preserving accepted parent decisions.
- **Refinement:** narrows or specifies accepted decisions for a particular context.
- **Cross-cutting:** establishes a policy that applies across multiple decision boundaries.
- **Superseding refinement:** replaces a defined part of an earlier decision while preserving the remaining accepted decisions.

| ADR | Primary decision owner | Decision role | Depends on | Relationship |
|---|---|---|---|---|
| ADR-001 | Original product direction and infrastructure-management identity | Foundational | None | Linux-specific product assumptions superseded by ADR-015; infrastructure-management identity preserved |
| ADR-002 | Android application architecture and dependency direction | Foundational | ADR-001 | Refined by ADR-016 for remote capabilities |
| ADR-003 | Structured local persistence technology and Room boundaries | Extension | ADR-002 | Active |
| ADR-004 | Application navigation strategy and route ownership | Extension | ADR-002 | Active |
| ADR-005 | Dependency injection framework and dependency-construction rules | Extension | ADR-002 | Active |
| ADR-006 | SSH feature, workflow, lifecycle, and initial security boundary | Extension | ADR-001, ADR-002 | Active under the platform-neutral product direction |
| ADR-007 | Secure-storage foundation for runtime connection secrets | Cross-cutting | ADR-003, ADR-006 | Active |
| ADR-008 | SSH client library selection and adapter isolation | Refinement | ADR-006 | Active |
| ADR-009 | SSH host trust, authentication input, and initial session boundaries | Refinement | ADR-006, ADR-007, ADR-008 | Active |
| ADR-010 | Non-interactive SSH command-channel execution | Extension | ADR-006, ADR-008, ADR-009 | Active |
| ADR-011 | SSH credential ownership and future persistent credential model | Refinement | ADR-007, ADR-009 | Active |
| ADR-012 | Android backup, data extraction, and device-transfer policy | Cross-cutting | ADR-003, ADR-007, ADR-009, ADR-011 | Active |
| ADR-013 | Ephemeral SSH private-key authentication and one-shot key-source lifecycle | Refinement | ADR-006, ADR-008, ADR-009, ADR-011, ADR-012 | Active |
| ADR-014 | Android application signing identity and release artifact trust | Cross-cutting | ADR-001 | Active |
| ADR-015 | Platform-neutral product direction and evidence-based support claims | Superseding refinement | ADR-001 | Supersedes Linux-specific product-scope assumptions in ADR-001 |
| ADR-016 | Core, Capability Gateway, and Provider/Adapter responsibilities | Refinement | ADR-002, ADR-015 | Refines ADR-002 without replacing feature-first MVVM |
| ADR-017 | Scalable collection presentation and growth boundaries | Cross-cutting | ADR-002, ADR-015 | Establishes collection UX invariants without mandating feature-specific layouts or premature pagination |

---

# Current Supersession Rules

ADR-015 supersedes only the Linux-specific product-scope assumptions of ADR-001.

The following ADR-001 decisions remain active:

- Server Toolkit is an infrastructure-management application rather than a traditional SSH client.
- SSH is one capability, not the whole product.
- Operational efficiency, maintainability, and production-quality Android engineering remain product priorities.

ADR-016 refines ADR-002 for remote capabilities. It does not supersede the accepted Android architecture, feature-first ownership, MVVM, repository pattern, Hilt, Navigation Compose, Room, or unidirectional UI state.

No other accepted ADR currently supersedes another ADR.

---

# Related Documents

- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `ENGINEERING_STRATEGY.md`
- `DEVELOPMENT.md`
- `PROJECT_STATE.md`
- `ROADMAP.md`
