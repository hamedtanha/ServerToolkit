# AI Memory

This document captures engineering knowledge accumulated during the project.

It records important decisions, lessons learned, and project preferences.

---

# Lesson 001

The project was intentionally started with documentation before implementation.

Reason

Good engineering decisions should precede coding.

---

# Lesson 002

Foundation was completed before the first feature.

Version

v0.1.0

This milestone includes

- Git
- GitHub
- SSH
- Documentation
- Architecture
- ADR
- Development workflow

---

# Lesson 003

README should describe the current implementation.

Do not advertise features that do not yet exist.

The project follows Living Documentation.

---

# Lesson 004

PROJECT_STATE.md is the Single Source of Truth.

Whenever inconsistencies exist

PROJECT_STATE

↓

ROADMAP

↓

CHANGELOG

---

# Lesson 005

Architecture stability is preferred over frequent redesign.

Major architectural changes require strong technical justification.

---

# Lesson 006

The project is developed feature by feature.

Avoid implementing multiple major features simultaneously.

---

# Lesson 007

Documentation quality is considered equal to code quality.

Every feature includes documentation updates.

---

# Lesson 008

The project aims to become a portfolio-quality engineering project.

The repository should demonstrate professional engineering practices in addition to application functionality.

---

# Team Preferences

The maintainer prefers

- long-term maintainability
- complete documentation
- engineering reasoning
- explicit trade-off analysis
- incremental development

over rapid implementation.

---

# Future Notes

Add new lessons whenever important engineering knowledge is gained.

# Lesson 009

The project transitions from documentation-first planning to implementation-driven development after the architecture baseline is established.

Documentation should evolve together with implementation instead of preceding it.

New documents should only be created when they provide measurable engineering value.

---

# Lesson 010

The initial application skeleton validates the architectural baseline before feature implementation begins.

The skeleton intentionally contains only the minimum executable structure required to support future development.

Feature-specific architecture decisions should be documented only when implementation requires them.

# Lesson 011

The current Git repository state is the authoritative project context.

Previous chat context, uploaded documentation snapshots, assistant memory, and earlier recommendations may become stale because the documentation is living and changes over time.

When making architecture, implementation, or documentation decisions, always prefer the current repository files over older context.

If there is a conflict, the repository wins.

Reason

Server Toolkit documentation evolves continuously. Decisions based on stale documentation can duplicate ADRs, corrupt documentation indexes, or recommend obsolete implementation paths.

