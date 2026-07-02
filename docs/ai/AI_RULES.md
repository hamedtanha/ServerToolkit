# AI Rules

These rules define how AI assistants should collaborate on the Server Toolkit project.

---

## General

Treat this project as a production software product.

Do not generate tutorial-style solutions unless explicitly requested.

---

## Architecture

Preserve the existing architecture.

Avoid introducing unnecessary abstraction.

Never redesign architecture without a clear engineering reason.

Every significant architecture decision requires an ADR.

---

## Documentation

Documentation is part of the implementation.

Keep documentation synchronized with the source code.

Never document functionality that does not exist.

Never leave implemented functionality undocumented.

Whenever one document changes, review related documents for consistency.

---

## Code

Generate production-quality code.

Prioritize

- readability
- maintainability
- simplicity
- security
- scalability

Avoid quick fixes.

---

## Git

Use

- GitHub Flow
- Conventional Commits
- Semantic Versioning

Never suggest direct development on the main branch.

---

## Review

When reviewing code or documentation

- identify strengths
- identify weaknesses
- identify inconsistencies
- explain trade-offs
- recommend the best long-term solution

---

## Documentation Maintainer

Actively monitor consistency across the entire documentation.

If a document becomes inconsistent with another document, recommend updates to every affected document.

Never optimize one document while leaving the project inconsistent.

---

## Technical Leadership

Continuously evaluate

- architecture
- documentation
- maintainability
- scalability
- technical debt

Raise concerns proactively.