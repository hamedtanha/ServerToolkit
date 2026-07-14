# Android Release Signing

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Implemented — Published
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
- Android NDK `28.2.13676358`.
- NDK `llvm-strip`.
- `zipalign`.
- `apksigner`.
- `aapt2`.
- `shasum`.
- Git.

The required Build Tools and NDK versions are repository-controlled in:

```text
config/release/android-release.properties
```

The Android module declares the same NDK version in `app/build.gradle.kts`. The signing workflow fails closed when the two declarations differ or the required `llvm-strip` executable is unavailable.

The current AGP built-in Kotlin and KSP integration requires:

```properties
android.disallowKotlinSourceSets=false
```

Removing this compatibility setting causes project configuration to fail because current KSP-generated Kotlin and Java source directories are still registered through the Kotlin source-set integration. The setting emits an experimental-option warning, but the warning is understood, non-critical, and must remain until a separately validated KSP and built-in Kotlin migration removes the requirement.

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
3. Resolve Android SDK Build Tools and the repository-pinned Android NDK.
4. Verify that the Gradle and release-metadata NDK declarations match.
5. Verify that the required NDK `llvm-strip` executable is available.
6. Verify that the keystore remains outside the repository.
7. Build the unsigned release APK with Gradle.
8. Reject an unexpectedly signed Gradle output.
9. Align the APK with `zipalign`.
10. Sign the aligned APK with `apksigner`.
11. Verify the APK signature with warnings treated as errors.
12. Confirm that exactly one signer exists.
13. Reject the Android debug certificate.
14. Match the signing certificate against the accepted public SHA-256 fingerprint.
15. Verify application identifier, version code, and version name.
16. Calculate the signed APK SHA-256 checksum.
17. Delete the temporary signed validation artifact.

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

## Version 0.4.0 Candidate Evidence

The non-distributable version 0.4.0 candidate was built, signed, and independently verified from the exact merged `main` state.

```text
Source commit: 250d46649834ef0f88dd6c3c330aacf137d44ab5
Application ID: de.hamedtanha.servertoolkit
Version code: 2
Version name: 0.4.0
Android Build Tools: 36.1.0
Certificate SHA-256: 8EECDB2A84052ABCA92848B8E717A136C33F4A3D1CB85EE2AA77C4F3ED9424FC
Candidate APK SHA-256: 906A87D7645F7E4035F6A96AD2DB395A4A7600D8B487BC39CF80D30C92C7EB04
Android Validation run: 29351658269
Android Validation conclusion: success
```

The candidate proved the signing and verification workflow but is not a distribution artifact. During finalization, the native-stripping warning was traced to a missing local NDK installation. Android NDK `28.2.13676358` is now repository-pinned, the matching `llvm-strip` has been validated for all packaged ABIs, and the signing workflow fails closed when the required NDK toolchain is unavailable.

The candidate checksum differs from the official checksum because the official APK was rebuilt from the final tagged release commit.

---

## Version 0.4.0 Official Release Evidence

Version 0.4.0 was built, signed, independently verified, tagged, published, downloaded again, and compared byte-for-byte with the verified local outputs.

```text
Release tag: v0.4.0
Published date: 2026-07-14
Source commit: 7be930f27cc19ea849d6ce720fb05fce074f3320
Application ID: de.hamedtanha.servertoolkit
Version code: 2
Version name: 0.4.0
Android Build Tools: 36.1.0
Android NDK: 28.2.13676358
Certificate SHA-256: 8EECDB2A84052ABCA92848B8E717A136C33F4A3D1CB85EE2AA77C4F3ED9424FC
Official APK SHA-256: 5BA5187999AA93677802DCCC2D318A9AE098D67C551399FBBA9CD9F563151273
Android Validation run: 29358724293
Android Validation conclusion: success
Published asset verification: byte-for-byte match
```

Published project assets:

```text
ServerToolkit-v0.4.0.apk
ServerToolkit-v0.4.0.apk.sha256
ServerToolkit-v0.4.0-release-evidence.txt
```

The release tag and published assets identify the same exact source commit. Future repository changes do not alter the immutable version 0.4.0 release baseline.

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
- Android NDK version.
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

The primary and recovery signing identities were verified before version 0.4.0 publication.

---

## Failure Behavior

The workflow fails closed when:

- The working tree is dirty.
- Official mode is run outside `main`.
- Local `main` does not match `origin/main`.
- Recovery readiness is not explicitly attested in official mode.
- Required Android Build Tools are unavailable.
- The repository-pinned Android NDK is unavailable.
- The Gradle and release-metadata NDK declarations differ.
- The required NDK `llvm-strip` executable is unavailable.
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
- Verified GitHub Release publication using locally signed artifacts.
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
