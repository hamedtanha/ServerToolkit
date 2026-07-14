# Android Release Signing

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Implemented — Publication Pending
**Last Updated:** 2026-07-14

---

## Purpose

This document defines the repeatable local workflow for producing, signing, verifying, and recording evidence for an official Server Toolkit APK.

The workflow implements ADR-014 while keeping all private signing material outside the Git repository.

---

## Trust Boundary

The Android release signing identity is a long-lived application and supply-chain identity.

The following material must remain outside the repository:

- Release keystore.
- Private signing key.
- Keystore password.
- Key password.
- Recovery copy.
- Recovery location.
- Password-manager details.
- Private operational recovery notes.

The repository contains only:

- Non-secret release metadata.
- The accepted public signing-certificate SHA-256 fingerprint.
- Fail-closed signing and verification logic.
- Non-secret operator instructions.

---

## Implemented Files

```text
scripts/release/sign-android-apk.sh
config/release/android-release.properties
config/release/android-signing-certificate.sha256
```

The Gradle release build remains unsigned and usable for technical validation without signing secrets.

---

## Required Toolchain

The workflow requires:

- Java 17.
- The committed Gradle Wrapper.
- Android SDK.
- Android Build Tools `36.1.0`.
- `zipalign`.
- `apksigner`.
- `aapt2`.
- `shasum`.
- Git.

The required Build Tools version is repository-controlled in:

```text
config/release/android-release.properties
```

---

## Signing Inputs

Default non-secret identifiers:

```text
Keystore convention: $HOME/.servertoolkit/release-signing/servertoolkit-release.p12
Key alias: servertoolkit-release
```

Optional environment overrides:

```text
SERVERTOOLKIT_RELEASE_KEYSTORE
SERVERTOOLKIT_RELEASE_KEY_ALIAS
SERVERTOOLKIT_RELEASE_STORE_PASSWORD
SERVERTOOLKIT_RELEASE_KEY_PASSWORD
```

Passwords should normally be entered interactively. They must not be written into shell history, committed files, documentation, build logs, issues, pull requests, or release notes.

---

## Validation Mode

Run from a clean feature branch:

```bash
scripts/release/sign-android-apk.sh --validation
```

Validation mode performs the complete workflow:

1. Verify repository cleanliness.
2. Resolve repository-controlled release metadata.
3. Resolve Android SDK Build Tools.
4. Verify that the keystore remains outside the repository.
5. Build the unsigned release APK with Gradle.
6. Reject an unexpectedly signed Gradle output.
7. Align the APK with `zipalign`.
8. Sign the aligned APK with `apksigner`.
9. Verify the APK signature with warnings treated as errors.
10. Confirm that exactly one signer exists.
11. Reject the Android debug certificate.
12. Match the signing certificate against the accepted public SHA-256 fingerprint.
13. Verify application identifier, version code, and version name.
14. Calculate the signed APK SHA-256 checksum.
15. Delete the temporary signed validation artifact.

A validation artifact is not an official release artifact and must not be distributed.

---

## Official Mode

Official signing must run only after the signing workflow has been merged to `main`.

From a clean `main` branch aligned with `origin/main`:

```bash
SERVERTOOLKIT_RELEASE_RECOVERY_VERIFIED=YES \
  scripts/release/sign-android-apk.sh
```

The recovery variable is a maintainer attestation. It is not a secret and does not replace actual recovery verification.

Official mode produces:

```text
build/release/ServerToolkit-v<versionName>.apk
build/release/ServerToolkit-v<versionName>.apk.sha256
build/release/ServerToolkit-v<versionName>-release-evidence.txt
```

The script refuses to overwrite existing official outputs.

---

## Verification Evidence

Official release evidence records:

- UTC generation time.
- Source branch.
- Exact source commit.
- Application identifier.
- Version code.
- Version name.
- Android Build Tools version.
- Release certificate SHA-256 fingerprint.
- Signed APK SHA-256 checksum.
- Artifact filename.
- Maintainer recovery-readiness attestation.

The published APK must remain byte-for-byte identical to the verified artifact identified by this evidence.

Any repository change after verification invalidates the existing evidence and requires a complete rebuild, re-sign, and re-verification.

---

## Recovery Requirement

Before first distribution:

- The primary keystore must open successfully.
- At least one independently protected recovery copy must exist.
- The recovery copy must open successfully.
- Its key alias and public certificate must match the primary identity.
- Its keystore checksum must match the intended recovery source.
- The recovery location must remain outside the repository and project documentation.

The primary and recovery signing identities were verified during version 0.4.0 release preparation.

---

## Failure Behavior

The workflow fails closed when:

- The working tree is dirty.
- Official mode is run outside `main`.
- Local `main` does not match `origin/main`.
- Recovery readiness is not explicitly attested in official mode.
- Required Android tools are unavailable.
- Signing configuration is absent or invalid.
- The keystore is missing, unreadable, or inside the repository.
- Signing credentials are absent or rejected.
- Gradle output is unexpectedly signed.
- APK alignment fails.
- Signature verification fails or reports warnings.
- More than one signer is detected.
- The debug certificate is detected.
- The release certificate fingerprint does not match.
- Application metadata does not match repository-controlled values.
- Checksum generation fails.
- An official output file already exists.

Temporary signing files and validation artifacts are removed during cleanup.

---

## Current Scope

Implemented:

- Local project-distributed APK signing.
- GitHub Release or controlled internal distribution preparation.
- Public certificate fingerprint verification.
- Signed artifact checksum and release evidence.

Not implemented:

- Signing in GitHub Actions.
- Signing material in GitHub Secrets.
- Automatic GitHub Release publication.
- Android App Bundle publication.
- Google Play distribution.
- Play App Signing.
- Key rotation or certificate lineage.
- Hardware-backed signing.
- Delegated multi-maintainer signing.

These capabilities require separate review before implementation.

---

## Related Documents

- `../adr/ADR-014-android-release-signing-strategy.md`
- `../SECURITY.md`
- `../RELEASES.md`
- `../PROJECT_STATE.md`
