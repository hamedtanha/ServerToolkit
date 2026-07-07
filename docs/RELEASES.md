# Release Process

**Project:** Server Toolkit
**Document Baseline:** 0.2.0-alpha
**Status:** Foundational
**Last Updated:** 2026-07-07

---

## Purpose

This document defines how Server Toolkit releases are prepared, reviewed, versioned, tagged, and distributed.

The objective is to make every release reproducible, traceable, and reliable.

---

## Release Strategy

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

During pre-1.0 development, minor versions represent stable development milestones.

---

## Release Types

### Major Release

Used for breaking changes.

Examples:

- Architecture redesign.
- Database migration with breaking compatibility.
- Major UI redesign.

Example:

```text
2.0.0
```

---

### Minor Release

Used for new milestone functionality without breaking compatibility.

Examples:

- Server Inventory foundation.
- SSH support.
- Dashboard evolution.
- Monitoring.

Example:

```text
1.3.0
```

---

### Patch Release

Used for fixes and safe improvements.

Examples:

- Crash fixes.
- UI corrections.
- Security fixes.
- Documentation corrections.

Example:

```text
1.3.2
```

---

## Milestone Versioning

Current milestone model:

```text
v0.1.0  Foundation
v0.2.0  Android Architecture and Navigation
v0.3.0  Server Inventory Foundation
v0.4.0  SSH
v0.5.0  Operations
v0.6.0  Dashboard Evolution
v0.7.0  Monitoring
v0.8.0  Infrastructure Helpers
v0.9.0  Stabilization
v1.0.0  Initial Release
```

This model must stay synchronized with `ROADMAP.md`.

---

## Release Workflow

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

## Release Checklist

Before creating a release verify:

- All milestone tasks are complete.
- Project builds successfully.
- No critical compiler warnings remain.
- Relevant tests are completed.
- Documentation is updated.
- `CHANGELOG.md` is updated.
- `PROJECT_STATE.md` is updated.
- Version is updated where applicable.
- Sensitive data is removed.
- Release notes are prepared.
- The `main` branch is releasable.

---

## Git Tags

Every release must be tagged.

Examples:

```text
v0.1.0
v0.2.0
v1.0.0
```

Tags should be created only from a releasable state of `main`.

---

## Release Notes

Every release should include:

- New features.
- Improvements.
- Bug fixes.
- Security changes.
- Breaking changes.
- Known limitations.

---

## APK Naming

Recommended stable build naming format:

```text
ServerToolkit-v1.0.0.apk
```

Recommended debug build naming format:

```text
ServerToolkit-debug.apk
```

---

## GitHub Releases

Each stable release should create a GitHub Release.

Recommended assets:

- APK.
- Release notes.
- Source code archive.

---

## Current Release State

Current stable checkpoint:

```text
v0.1.0 — Foundation
```

Current development milestone:

```text
v0.4.0-alpha — SSH
```

Project status:

```text
Active Development
```

---

## Future Distribution Channels

Planned distribution channels:

- GitHub Releases.
- Internal testing.
- Closed beta.
- Google Play Store.

---

## Document Governance

This document is foundational and frozen.

Changes are allowed only when:

- The release process changes.
- The milestone versioning model changes.
- Distribution strategy changes.
- An ADR or release decision requires an update.

---

## Related Documents

- [Changelog](CHANGELOG.md)
- [Development Process](DEVELOPMENT.md)
- [Roadmap](ROADMAP.md)
- [Project State](PROJECT_STATE.md)
