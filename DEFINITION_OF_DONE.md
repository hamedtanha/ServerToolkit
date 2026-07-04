# Definition of Done

**Project:** ServerToolkit  
**Document Version:** 1.0  
**Status:** Active  
**Last Updated:** 2026-07-03  
**Owner:** Maintainer

---

# Purpose

This document defines when work is considered complete in the ServerToolkit project.

The Definition of Done applies to source code changes, documentation changes, architecture changes, and feature implementation.

Work that does not meet this definition must not be considered complete.

---

# General Definition of Done

A change is considered done only when:

- The scope of the change is clear.
- The implementation is complete for the intended scope.
- The project builds successfully.
- The Git diff has been reviewed.
- The working tree is clean after commit.
- The commit message follows Conventional Commits.
- No unrelated changes are included.
- No empty or meaningless files are committed.
- No temporary debug code is left behind.
- No secrets, credentials, tokens, private keys, or private infrastructure details are committed.

---

# Code Changes

Code changes are done only when:

- The code is readable and maintainable.
- The implementation follows the existing architecture.
- The change avoids unnecessary abstraction.
- The change does not introduce avoidable technical debt.
- Kotlin code uses clear naming and idiomatic style.
- Compose UI code remains simple and testable.
- ViewModels expose UI state clearly.
- Dependencies are injected when dependency injection provides real value.
- The project builds successfully.

---

# Feature Changes

A feature is done only when:

- The feature scope is implemented.
- The feature package structure is consistent with the project architecture.
- UI state exists when the feature has screen state.
- A ViewModel exists when the feature requires presentation logic.
- Navigation is updated when the feature is user-facing.
- Repository or data source layers are added only when needed.
- Related documentation is updated.
- The feature does not introduce unrelated architecture changes.

---

# Documentation Changes

Documentation changes are done only when:

- The document has a clear responsibility.
- The document follows Documentation Governance.
- Related documents are reviewed for consistency.
- The document does not duplicate another document's responsibility.
- The document does not describe functionality that does not exist.
- Relative links are valid and useful.
- The document uses professional English.
- The project builds successfully before the commit.

---

# Architecture Changes

Architecture changes are done only when:

- The engineering reason is clear.
- The change is consistent with existing accepted ADRs.
- A new ADR is created when the decision is significant.
- Existing documentation is updated.
- The change improves maintainability, scalability, security, or clarity.
- The change does not redesign existing architecture without strong justification.

---

# Build Verification

Before a change is committed:

- Run the project build.
- Confirm there are no unexpected build failures.
- Review warnings when they indicate compatibility, maintainability, or security risks.
- Confirm the working tree contains only intended changes.

---

# Not Done

A change is not done if:

- It only works locally by accident.
- It requires undocumented manual steps.
- It breaks the build.
- It introduces unexplained architecture changes.
- It leaves broken documentation links.
- It commits empty files.
- It mixes unrelated work in one commit.
- It documents behavior that is not implemented.

---

# Related Documents

- [Documentation Governance](DOCUMENTATION_GOVERNANCE.md)
- [Engineering Workflow](ENGINEERING_WORKFLOW.md)
- [Code Review Checklist](CODE_REVIEW_CHECKLIST.md)
- [AI Rules](AI_RULES.md)
