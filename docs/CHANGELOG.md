# Changelog

**Project:** Server Toolkit

This document records all notable changes to the project.

The format is inspired by Keep a Changelog and follows Semantic Versioning.

---

## [Unreleased]

### Changed

- Aligned project documentation status across Product Vision, Roadmap, Project State, Architecture, Development Process, Security Policy, Contribution Guide, and Release Process.
- Clarified the documentation governance model by separating frozen foundational documents from mutable planning and operational documents.
- Updated the roadmap to use versioned milestones from `v0.1.0` to `v1.0.0`.
- Clarified that `v0.1.0` represents the completed Project Foundation milestone.
- Clarified that `v0.2.0` is the current Navigation milestone.

### Fixed

- Removed the inconsistency where Project Foundation was marked as completed in PROJECT_STATE.md but still in progress in ROADMAP.md.
- Removed ambiguity between current product phase, current project state, and current development milestone.
- Removed duplicated or conflicting milestone definitions across planning documents.

---

## [0.1.0] - 2026-07-01

### Added

- Android Studio project initialized
- Git repository configured
- GitHub repository created
- Initial project documentation
- MVVM package structure
- Initial Server model
- Project roadmap
- Architecture documentation
- Development workflow
- Product vision
- ADR infrastructure

### Changed

- Documentation structure standardized

### Fixed

- Duplicate ADR files removed

---

## Versioning Policy

The project follows Semantic Versioning.

```text
MAJOR.MINOR.PATCH
```

Examples:

```text
1.0.0
1.1.0
1.1.1
2.0.0
```

---

## Changelog Rules

Every release must update this document.

Entries should describe user-visible or developer-visible changes.

Internal refactoring that does not affect functionality should generally not appear unless it significantly improves maintainability.

---

## Categories

### Added

New functionality.

### Changed

Existing functionality modified.

### Deprecated

Features scheduled for removal.

### Removed

Features removed from the project.

### Fixed

Bug fixes.

### Security

Security-related improvements.

---

## Related Documents

- ROADMAP.md
- RELEASES.md
- DEVELOPMENT.md
- PROJECT_STATE.md
