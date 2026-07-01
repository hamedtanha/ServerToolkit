# Release Process

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Active

**Last Updated:** 2026-07-01

---

# Purpose

This document defines how application releases are prepared, reviewed, versioned, and published.

The objective is to make every release reproducible, traceable, and reliable.

---

# Release Strategy

Server Toolkit follows Semantic Versioning.

```
MAJOR.MINOR.PATCH
```

Example

```
1.0.0
1.1.0
1.1.1
2.0.0
```

---

# Release Types

## Major Release

Breaking changes.

Examples

- Architecture redesign
- Database migration
- Major UI redesign

Example

```
2.0.0
```

---

## Minor Release

New features without breaking compatibility.

Examples

- SSH support
- Dashboard improvements
- Monitoring

Example

```
1.3.0
```

---

## Patch Release

Bug fixes.

Examples

- Crash fixes
- UI corrections
- Security fixes

Example

```
1.3.2
```

---

# Release Workflow

Every release follows the same process.

```
Feature Complete

↓

Testing

↓

Documentation Review

↓

Update CHANGELOG

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

- All features completed
- Project builds successfully
- No compiler warnings
- Documentation updated
- CHANGELOG updated
- Version updated
- Tests completed
- Sensitive data removed
- Release notes prepared

---

# Git Tags

Every release must be tagged.

Examples

```
v0.1.0

v0.2.0

v1.0.0
```

---

# Release Notes

Every release should include:

- New features
- Improvements
- Bug fixes
- Breaking changes
- Known limitations

---

# APK Naming

Recommended naming format

```
ServerToolkit-v1.0.0.apk
```

Debug builds

```
ServerToolkit-debug.apk
```

---

# GitHub Releases

Each stable release should create a GitHub Release.

Assets

- APK
- Release Notes
- Source Code

---

# Version History

Current Version

```
0.1.0
```

Status

Development

---

# Future Distribution

Planned distribution channels

- GitHub Releases
- Google Play Store
- Internal Testing
- Closed Beta

---

# Related Documents

- CHANGELOG.md
- DEVELOPMENT.md
- ROADMAP.md