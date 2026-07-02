# Release Process

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Frozen  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines how Server Toolkit releases are prepared, reviewed, versioned, tagged, and distributed.

The objective is to make every release reproducible, traceable, and reliable.

---

# Release Strategy

Server Toolkit follows Semantic Versioning.

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

# Release Types

## Major Release

Used for breaking changes.

Examples:

- Architecture redesign
- Database migration with breaking compatibility
- Major UI redesign

Example:

```text
2.0.0
```

---

## Minor Release

Used for new features without breaking compatibility.

Examples:

- Navigation
- SSH support
- Dashboard improvements
- Monitoring

Example:

```text
1.3.0
```

---

## Patch Release

Used for fixes and safe improvements.

Examples:

- Crash fixes
- UI corrections
- Security fixes
- Documentation corrections

Example:

```text
1.3.2
```

---

# Milestone Versioning

Current milestone model:

```text
v0.1.0  Project Foundation
v0.2.0  Navigation
v0.3.0  Dashboard
v0.4.0  Server Inventory
v0.5.0  Local Storage
v0.6.0  SSH Connectivity
v0.7.0  Monitoring
v0.8.0  Xray Integration
v0.9.0  Beta Stabilization
v1.0.0  First Stable Release
```

---

# Release Workflow

Every release follows this process:

```text
Feature Complete
↓
Testing
↓
Documentation Review
↓
Update CHANGELOG
↓
Update PROJECT_STATE
↓
Version Update
↓
Git Tag
↓
GitHub Release
↓
APK Build
↓
Distribution
```

---

# Release Checklist

Before creating a release verify:

- All milestone tasks are complete.
- Project builds successfully.
- No critical compiler warnings remain.
- Relevant tests are completed.
- Documentation is updated.
- CHANGELOG.md is updated.
- PROJECT_STATE.md is updated.
- Version is updated where applicable.
- Sensitive data is removed.
- Release notes are prepared.
- The main branch is releasable.

---

# Git Tags

Every release must be tagged.

Examples:

```text
v0.1.0
v0.2.0
v1.0.0
```

Tags should be created only from a releasable state of `main`.

---

# Release Notes

Every release should include:

- New features
- Improvements
- Bug fixes
- Security changes
- Breaking changes
- Known limitations

---

# APK Naming

Recommended stable build naming format:

```text
ServerToolkit-v1.0.0.apk
```

Recommended debug build naming format:

```text
ServerToolkit-debug.apk
```

---

# GitHub Releases

Each stable release should create a GitHub Release.

Recommended assets:

- APK
- Release notes
- Source code archive

---

# Current Release State

Current stable checkpoint:

```text
v0.1.0 — Project Foundation
```

Current development milestone:

```text
v0.2.0 — Navigation
```

Project status:

```text
Development
```

---

# Future Distribution Channels

Planned distribution channels:

- GitHub Releases
- Internal testing
- Closed beta
- Google Play Store

---

# Document Governance

This document is foundational and frozen.

Changes are allowed only when:

- The release process changes.
- The milestone versioning model changes.
- Distribution strategy changes.
- An ADR or release decision requires an update.

---

# Related Documents

- CHANGELOG.md
- DEVELOPMENT.md
- ROADMAP.md
- PROJECT_STATE.md
