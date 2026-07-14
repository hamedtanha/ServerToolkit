# Release Process

**Project:** Server Toolkit
**Document Baseline:** 0.2.0-alpha
**Status:** Foundational
**Last Updated:** 2026-07-14

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

## Android Application Version Metadata

Android application version metadata is defined in `app/build.gradle.kts`.

Rules:

- `versionName` must match the current project milestone version without the leading `v`.
- `versionName` may include a pre-release suffix during alpha development.
- `versionCode` is the Android monotonic install and upgrade ordering value.
- `versionCode` must not be derived mechanically from the project milestone number.
- `versionCode` should be incremented only when preparing a distributed Android artifact that must upgrade over a previous installed artifact.

Release candidate metadata:

- `versionName`: `0.4.0`
- `versionCode`: `2`

---

## Implemented Android APK Signing Workflow

Server Toolkit uses a local post-build APK signing workflow for project-distributed Android releases.

Implemented sequence:

```text
Gradle assembleRelease
↓
Unsigned technical APK
↓
zipalign
↓
apksigner sign
↓
apksigner verify
↓
Signing-certificate fingerprint verification
↓
Application metadata verification
↓
SHA-256 checksum and release evidence
```

Repository-controlled, non-secret configuration:

```text
config/release/android-release.properties
config/release/android-signing-certificate.sha256
```

Executable workflow:

```text
scripts/release/sign-android-apk.sh
```

Validation mode:

```bash
scripts/release/sign-android-apk.sh --validation
```

Validation mode performs the complete signing and verification sequence from a clean feature branch. The signed validation artifact is temporary and is deleted after verification.

Official mode:

```bash
SERVERTOOLKIT_RELEASE_RECOVERY_VERIFIED=YES \
  scripts/release/sign-android-apk.sh
```

Official mode:

- runs only from a clean `main` branch;
- requires the local `main` commit to match `origin/main` when that remote reference is available;
- requires explicit maintainer confirmation that signing-key recovery readiness has been verified;
- refuses missing, unreadable, repository-contained, or invalid signing material;
- verifies the accepted release certificate and rejects the Android debug certificate;
- verifies the application identifier, version code, and version name;
- generates the final APK SHA-256 checksum and release evidence;
- refuses to overwrite an existing official release artifact.

The release keystore, signing passwords, and recovery locations remain outside the repository. Automated signing in GitHub Actions is not part of the current distribution model.

Detailed operator guidance is maintained in `release/ANDROID_RELEASE_SIGNING.md`.

---

## Release Workflow

Every release follows a two-stage validation and publication process.

### Release Candidate Validation

```text
Feature Complete
↓
Testing
↓
Documentation Review
↓
Update CHANGELOG and PROJECT_STATE for Release Preparation
↓
Update Version Metadata
↓
Merge Approved Release Candidate State to main
↓
Build Candidate APK from the Exact main Commit
↓
Sign and Verify the Candidate APK
↓
Verify Signing Recovery Readiness and Record Candidate Evidence
```

A successfully verified candidate proves that the release workflow is operational, but it is not the official published artifact. `CHANGELOG.md` remains unreleased and the project status remains `Release Preparation` until finalization.

### Final Release Publication

```text
Finalize CHANGELOG Date and Release State
↓
Merge Approved Final Release State to main
↓
Build Official APK from the Exact Final main Commit
↓
Sign APK
↓
Verify Signing Certificate and Application Metadata
↓
Generate SHA-256 Checksum and Final Release Evidence
↓
Create Git Tag for the Exact Final Commit
↓
Create GitHub Release
↓
Distribution
```

The final tag, source archive, release notes, checksum evidence, and APK must identify the same final `main` commit. Any repository change after final artifact verification invalidates the existing release evidence and requires the APK to be rebuilt, signed, and verified again.

---

## Release Checklist

Before creating the Git tag and GitHub Release, verify:

- All milestone tasks are complete.
- Release candidate signing and verification completed successfully.
- The official APK was rebuilt from the exact final `main` commit that will be tagged.
- Project builds successfully.
- No critical compiler warnings remain.
- Relevant tests are completed.
- Documentation is updated.
- `CHANGELOG.md` is updated.
- `PROJECT_STATE.md` is updated.
- Version metadata is updated where applicable.
- The official signing workflow is available and fails closed when required signing material is unavailable or invalid.
- The primary signing keystore and at least one recovery copy have been verified before first distribution.
- The APK is signed with the accepted release identity and not with the Android debug certificate.
- The signing certificate, application identifier, and version metadata are verified.
- The signed APK SHA-256 checksum and required release evidence are recorded.
- Sensitive data is absent from Git history, build output, logs, documentation, and release assets.
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

- Signed and verified APK.
- APK SHA-256 checksum.
- Release notes.
- Source code archive.

---

## Current Release State

Current stable checkpoint:

```text
v0.1.0 — Foundation
```

Current release candidate:

```text
v0.4.0 — SSH
```

Next development milestone:

```text
v0.5.0-alpha — Operations
```

Project status:

```text
Release Preparation
```

Signing implementation status:

```text
Implemented and locally validation-tested.
Candidate signing from the exact merged main commit remains pending.
Official publication remains pending.
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
