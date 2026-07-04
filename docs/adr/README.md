# Architecture Decision Records (ADR)

**Project:** Server Toolkit

**Version:** 0.2.0-alpha

**Status:** Active

**Last Updated:** 2026-07-04

---

# Purpose

This directory contains all Architecture Decision Records (ADRs) for the Server Toolkit project.

An ADR documents a significant architectural or technical decision together with its context, alternatives, and consequences.

The objective is to preserve engineering knowledge and explain *why* important decisions were made.

---

# What is an ADR?

An Architecture Decision Record is a lightweight document that captures:

- The problem being solved
- The available alternatives
- The selected solution
- The reasons for the decision
- The expected consequences

ADRs provide historical context and improve long-term maintainability.

---

# When to Create an ADR

Create a new ADR only when a significant technical decision has been made.

Examples include:

- Choosing an architectural pattern
- Selecting a database technology
- Selecting an SSH library
- Choosing a networking framework
- Defining a security strategy
- Introducing dependency injection
- Changing application architecture

Do not create ADRs for minor implementation details.

---

# ADR Lifecycle

Every ADR follows this lifecycle.

```text
Draft

↓

Discussion

↓

Accepted

↓

Implemented

↓

Superseded (optional)

↓

Deprecated (optional)
```

---

# Status Values

## Draft

The proposal is under discussion.

## Accepted

The decision has been approved.

## Implemented

The decision has been implemented.

## Superseded

A newer ADR replaces this decision.

## Deprecated

The decision is no longer recommended.

---

# ADR Naming Convention

Each ADR uses a sequential identifier.

Examples:

```text
ADR-001-project-vision.md
ADR-002-application-architecture.md
ADR-003-local-persistence-with-room.md
```

Numbers are never reused.

---

# ADR Structure

Every ADR must contain the following sections.

- Context
- Decision
- Alternatives Considered
- Consequences

Additional sections may be added if necessary.

---

# Modification Policy

Accepted ADRs should not be rewritten.

If a decision changes, create a new ADR and reference the previous one.

The history of architectural decisions should always remain traceable.

---

# Current ADRs

| ADR | Title | Status |
|------|-------|--------|
| ADR-001 | Project Vision | Accepted |
| ADR-002 | Application Architecture | Accepted |
| ADR-003 | Local Persistence with Room | Accepted |
| ADR-004 | Navigation Strategy | Accepted |
| ADR-005 | Dependency Injection Strategy | Accepted |

---

# Related Documents

- PRODUCT_VISION.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- PROJECT_STATE.md
- ROADMAP.md
