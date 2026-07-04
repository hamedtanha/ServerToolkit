# Code Review Checklist

**Project:** ServerToolkit  
**Document Version:** 1.0  
**Status:** Active  
**Last Updated:** 2026-07-03  
**Owner:** Maintainer

---

# Purpose

This document defines the review checklist used for code, documentation, architecture, and build changes in the ServerToolkit project.

The goal is to ensure that every change improves or preserves maintainability, readability, security, testability, and architectural consistency.

---

# General Review

Before approving a change, verify:

- The change has a clear purpose.
- The scope is small and reviewable.
- The commit message follows Conventional Commits.
- The diff does not include unrelated changes.
- The project builds successfully.
- The working tree is clean after commit.
- No temporary files, IDE noise, debug code, or empty files are included.
- No secrets, credentials, tokens, private keys, or private infrastructure details are committed.

---

# Architecture Review

Verify that:

- The change follows the existing architecture.
- Package structure remains consistent.
- Dependency direction remains clean.
- Presentation, domain, and data responsibilities are not mixed.
- New abstractions have a clear reason.
- No architecture redesign is introduced without justification.
- Significant decisions are documented in ADRs.
- Existing accepted ADRs are respected.

---

# Android and Kotlin Review

Verify that:

- Kotlin code is readable and idiomatic.
- Class, function, and variable names are clear.
- Code avoids unnecessary cleverness.
- Compose UI remains simple and state-driven.
- ViewModels expose UI state clearly.
- Business logic is not placed directly inside Composables.
- Lifecycle-aware APIs are used when needed.
- Dependencies are injected only when dependency injection provides real value.

---

# Feature Review

Verify that:

- The feature scope is clear.
- The feature follows the expected package structure.
- UI state exists when screen state exists.
- A ViewModel exists when presentation logic exists.
- Navigation changes are explicit and minimal.
- Data and repository layers are added only when needed.
- Feature implementation does not introduce unrelated changes.
- Related documentation is updated.

---

# Documentation Review

Verify that:

- Documentation follows Documentation Governance.
- The document has a clear responsibility.
- The change does not duplicate another document's responsibility.
- Related documents remain consistent.
- Links are relative and useful.
- The documentation describes current or intentionally planned work accurately.
- The documentation does not advertise features that do not exist.
- Professional English is used.

---

# Build and Test Review

Verify that:

- The project build succeeds.
- Build warnings are reviewed.
- Tests are added when logic, data transformation, or failure handling exists.
- Existing tests are not weakened to make implementation easier.
- No manual-only verification is accepted when automated verification is practical.

---

# Security Review

Verify that:

- No secrets or private infrastructure details are committed.
- Sensitive configuration is not hardcoded.
- External connectivity is introduced intentionally.
- Authentication and credential handling are not implemented casually.
- Direct mobile access to external databases is avoided unless a strong, documented technical reason exists.
- Backend/API boundaries are respected when remote data access is introduced.

---

# Rejection Criteria

A change should be rejected if:

- It breaks the build.
- It mixes unrelated responsibilities.
- It introduces unexplained architecture changes.
- It adds empty files or unused abstractions without immediate purpose.
- It duplicates documentation ownership.
- It creates broken documentation links.
- It introduces security risks.
- It reduces maintainability for short-term convenience.

---

# Related Documents

- [Documentation Governance](DOCUMENTATION_GOVERNANCE.md)
- [Engineering Workflow](ENGINEERING_WORKFLOW.md)
- [Definition of Done](DEFINITION_OF_DONE.md)
- [AI Rules](AI_RULES.md)
- [ADR Template](ADR-TEMPLATE.md)
