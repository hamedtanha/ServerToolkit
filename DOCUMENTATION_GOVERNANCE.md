# Documentation Governance

**Project:** ServerToolkit  
**Document Version:** 1.0  
**Status:** Active  
**Last Updated:** 2026-07-03  
**Owner:** Maintainer

---

# Purpose

This document defines how documentation is organized, owned, linked, versioned, and reviewed in the ServerToolkit project.

The goal is to prevent duplicated responsibilities, conflicting documentation, outdated project knowledge, and unclear sources of truth.

Documentation in this project is treated as part of the implementation and must remain synchronized with the source code, architecture, and project state.

---

# Documentation Principles

The ServerToolkit documentation system follows these principles:

- Each document must have a clear responsibility.
- Each major topic must have exactly one primary source of truth.
- Documents may reference related documents but must not duplicate their responsibility.
- Documentation must describe the current project accurately.
- Documentation must not describe features that do not exist.
- Accepted Architecture Decision Records must not be rewritten.
- When a document changes, related documents must be reviewed for consistency.
- Empty documentation files must not be committed.
- Documentation changes must be small, reviewable, and meaningful.

---

# Source of Truth

The following documents own the primary responsibility for each documentation area.

| Area | Source of Truth |
|---|---|
| Project overview | README.md |
| Current project state | PROJECT_STATE.md |
| Product direction | PRODUCT_VISION.md |
| Architecture | ARCHITECTURE.md |
| Architecture decisions | adr/*.md |
| Engineering workflow | ENGINEERING_WORKFLOW.md |
| Definition of Done | DEFINITION_OF_DONE.md |
| Code review process | CODE_REVIEW_CHECKLIST.md |
| Development setup and commands | DEVELOPMENT.md |
| Security rules | SECURITY.md |
| Release process | RELEASES.md |
| Roadmap | ROADMAP.md |
| Version history | CHANGELOG.md |
| AI collaboration rules | AI_RULES.md |
| Engineering memory | AI_MEMORY.md |
| Documentation governance | DOCUMENTATION_GOVERNANCE.md |

If two documents appear to define the same rule, this table determines which document owns the topic.

---

# Document Boundaries

## README.md

README.md is the project entry point.

It may include:

- Project summary
- Current status
- Main documentation links
- Basic setup instructions
- High-level feature overview

It must not include:

- Detailed architecture decisions
- Full engineering workflow
- Complete roadmap details
- Long-form development rules

---

## PROJECT_STATE.md

PROJECT_STATE.md describes the current state of the project.

It may include:

- Current version
- Implemented features
- Active development phase
- Known current limitations
- Current technical status

It must not include:

- Future roadmap details
- Historical changelog entries
- Architecture decision rationale

---

## ARCHITECTURE.md

ARCHITECTURE.md describes the current architecture.

It may include:

- Architecture overview
- Layer responsibilities
- Package structure
- Dependency direction
- Architectural constraints

It must not include:

- Historical decision discussion
- Rejected alternatives
- Sprint tasks
- Detailed implementation logs

Significant architecture decisions must be documented in ADRs.

---

## ADR Documents

ADR documents explain why significant decisions were made.

They may include:

- Context
- Decision
- Alternatives considered
- Consequences

They must not be rewritten after acceptance.

If a decision changes, a new ADR must be created and linked to the previous one.

---

## ENGINEERING_WORKFLOW.md

ENGINEERING_WORKFLOW.md defines how development work is performed.

It may include:

- Development workflow
- Feature lifecycle
- Git workflow
- Build verification rules
- Documentation workflow
- Release readiness workflow

It must not duplicate:

- Full Definition of Done
- Full code review checklist
- Architecture decision details

---

## DEFINITION_OF_DONE.md

DEFINITION_OF_DONE.md defines when work is considered complete.

It may include:

- Feature completion criteria
- Documentation completion criteria
- Build and test requirements
- Review requirements

It must not include:

- Full development workflow
- Architecture rationale
- Release history

---

## CODE_REVIEW_CHECKLIST.md

CODE_REVIEW_CHECKLIST.md defines the review checklist for code and documentation changes.

It may include:

- Architecture checks
- Code quality checks
- Documentation checks
- Testing checks
- Security checks

It must not include:

- Project roadmap
- Product vision
- Long-form architecture documentation

---

# Linking Rules

Documents must use relative Markdown links when referencing other project documents.

Examples:

- See [Architecture](ARCHITECTURE.md).
- See [Engineering Workflow](ENGINEERING_WORKFLOW.md).
- See [ADR-001: Project Vision](adr/ADR-001-project-vision.md).

A document should use one of the following relationship labels when appropriate:

| Relationship | Meaning |
|---|---|
| Depends On | This document must remain consistent with another document. |
| Related Documents | Documents that provide additional context. |
| Supersedes | This document replaces another document or decision. |
| Implemented By | Source code or documentation that implements the decision. |
| References | Informational references that are not binding. |

---

# Versioning Rules

Main documentation files use document versions.

Example:

- Document Version: 1.0
- Status: Active
- Last Updated: YYYY-MM-DD

Document version numbers are updated when the document meaningfully changes.

Minor wording improvements do not require a document version change unless they affect interpretation.

ADR documents do not require document versions. ADRs use status values instead.

Living documents such as PROJECT_STATE.md, ROADMAP.md, and CHANGELOG.md prioritize accurate status and dates over document versioning.

---

# Review Rules

Before committing documentation changes, verify:

- The document has a clear responsibility.
- The document does not duplicate another document's responsibility.
- Related documents remain consistent.
- Links are valid and useful.
- The document describes implemented or planned work accurately.
- The document does not contain secrets, credentials, private infrastructure details, or temporary notes.
- The project builds successfully before the commit.

Documentation changes should be committed using Conventional Commits.

Example commit message:

- docs: define documentation governance

---

# Related Documents

- [Engineering Workflow](ENGINEERING_WORKFLOW.md)
- [AI Rules](AI_RULES.md)
- [AI Memory](AI_MEMORY.md)
- [ADR Template](ADR-TEMPLATE.md)
