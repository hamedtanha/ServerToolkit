# ADR-014: Android Release Signing Strategy

**Status:** Accepted

**Date:** 2026-07-14

**Related Milestone:** Version 0.4.0 — Release Preparation

---

# Context

Server Toolkit is preparing its first distributed Android milestone beyond the initial foundation checkpoint.

Android requires installable and updateable APK artifacts to be digitally signed. The current project can assemble a release variant, but the generated release APK is unsigned because no release signing configuration or project-owned application signing identity has been established.

Application signing is a long-lived release and supply-chain security boundary:

- Android uses the signing certificate to determine whether a new APK is an authorized update to an installed application.
- Possession of the private signing key can allow an unauthorized party to publish artifacts under the project's application identity.
- Loss of a self-managed signing key can prevent future versions from updating previously distributed installations.
- Release signing credentials must not become part of source control, build logs, documentation, or normal debug development.

The Android application signing key is not an SSH credential and is not runtime application data. ADR-007, ADR-011, and ADR-013 govern runtime connection secrets and SSH authentication material. This ADR governs the build and release identity of the Android application itself.

A stable, explicit signing strategy is required before Server Toolkit distributes an official APK.

---

# Decision

Server Toolkit will establish one stable Android application signing identity for official project-distributed release artifacts.

The following rules apply.

## Official Release Identity

Official Server Toolkit APK releases must be signed with the accepted project release signing identity.

The debug signing identity must remain separate from the release signing identity.

An APK that is unsigned, signed with the Android debug key, or signed with an unapproved key must not be published as an official Server Toolkit release artifact.

## Key and Credential Boundary

The release signing private key, keystore, keystore password, key password, and equivalent secret material must remain outside the Git repository.

Signing secrets must not be embedded in:

- Gradle build files;
- committed properties files;
- source code;
- documentation;
- shell scripts committed to the repository;
- GitHub issues or pull requests;
- release notes;
- build logs;
- generated reports.

The official release workflow must receive signing key material and credentials through an external, non-version-controlled secret boundary during authorized release preparation.

The signing mechanism may use Gradle-managed signing or post-build APK signing with supported Android SDK tools. The implementation must preserve the same secret boundary, fail-closed behavior, and artifact-verification requirements regardless of the selected mechanism.

The repository may contain non-secret signing policy, validation logic, public certificate fingerprints, and operator instructions. Documentation may use placeholder or conventional local paths, but it must not disclose actual recovery locations, secret values, or private operational details that weaken the signing boundary.

## Release Build Behavior

Normal debug development, unit testing, lint, Android test compilation, and validation CI must remain usable without access to release signing secrets.

The project must distinguish between:

- release-variant output used for technical compilation, lint, packaging, or optimization validation; and
- an official distributable release artifact.

A locally assembled unsigned release variant may exist as technical build output. It must be clearly treated as non-distributable and must not be attached to a GitHub Release.

The dedicated workflow that produces an official distributable artifact must fail closed when the approved signing configuration is absent, incomplete, invalid, or references unavailable key material.

## Artifact Verification

Every official APK must be verified after signing and before distribution.

Verification must confirm at least:

- the APK has a valid signing certificate;
- the artifact is not signed with the Android debug certificate;
- the signing certificate matches the accepted Server Toolkit release certificate;
- the application identifier and version metadata match the intended release;
- the artifact checksum is calculated after signing and verification.

The verified artifact must be published unchanged. The release evidence must record its SHA-256 checksum so the attached file can be matched to the verified local artifact.

The public signing-certificate SHA-256 fingerprint must be recorded as release evidence because it identifies the accepted public certificate without exposing the private key.

## Key Lifetime, Backup, and Recovery

The release signing key must have a validity period of at least 25 years from generation and must otherwise be suitable for the expected lifetime of the application and future updates.

Before the first official signed APK is distributed, the maintainer must create and verify at least one secure recovery copy of the keystore and the information required to use it.

The recovery copy must:

- remain outside the repository;
- remain outside normal project documentation;
- be protected independently from the primary working copy;
- be verified before the first release;
- be accessible only to authorized maintainers.

Release evidence must include a maintainer attestation that the primary keystore and at least one recovery copy were successfully verified before first distribution. The repository must not document actual storage locations, passwords, recovery secrets, or private operational details that would weaken the signing boundary.

## Current Distribution Scope

The initial signing implementation authorizes local signing of project-distributed APK artifacts for GitHub Releases or controlled internal distribution.

The following remain outside the current scope:

- automated release signing in GitHub Actions;
- storage of signing material in GitHub Secrets;
- automatic GitHub Release publication;
- Google Play distribution;
- Play App Signing enrollment;
- Android App Bundle publication;
- key rotation;
- signing-certificate lineage;
- hardware security module integration;
- delegated multi-maintainer signing.

Each of these capabilities requires a separately reviewed implementation or a new ADR when it changes the accepted trust or operational model.

---

# Alternatives Considered

## Distribute the Unsigned Release APK

Publish the current unsigned release output as the official artifact.

### Pros

- No signing setup is required.
- Release preparation remains simple.

### Cons

- The artifact is not a valid official Android release package.
- It cannot establish a stable update identity.
- It creates ambiguity between technical build output and distributable release output.
- It violates Android release requirements.

Rejected.

---

## Use the Android Debug Signing Key

Publish an APK signed with the automatically managed Android debug key.

### Pros

- Requires minimal setup.
- Works automatically on development machines.

### Cons

- The debug identity is not an appropriate project release identity.
- Debug keystores may differ between machines or be regenerated.
- It weakens provenance and future update continuity.
- It can cause an artifact intended only for development to be mistaken for an official release.

Rejected.

---

## Commit the Keystore or Signing Credentials

Store the release keystore or credential-bearing configuration in the repository.

### Pros

- Simple local and CI builds.
- No external configuration is required.

### Cons

- Exposes long-lived signing material to repository history and clones.
- Deleting the file later does not remove it from existing history or copies.
- Enables unauthorized signing and application impersonation.
- Violates the project's security policy.

Rejected.

---

## Use a Repository-External Self-Managed Signing Identity

Keep the release keystore and credentials outside the repository and provide them only during authorized local release preparation.

### Pros

- Establishes a stable application identity.
- Keeps signing secrets outside source control.
- Supports controlled GitHub Release APK distribution.
- Leaves debug development and validation CI independent from release secrets.
- Provides a clear migration path toward future protected automation or Play App Signing.

### Cons

- Requires explicit local setup and operational discipline.
- Secure backup and recovery become maintainer responsibilities.
- Release production is not fully automated.
- Loss or compromise of the self-managed key requires incident handling.

Accepted for the initial distribution model.

---

## Adopt Play App Signing Immediately

Use Google Play App Signing and separate upload-key management before the first GitHub-distributed APK.

### Pros

- Provides managed protection and recovery capabilities for the app signing key.
- Supports modern Google Play distribution and Android App Bundles.
- Allows separation of app signing and upload keys.

### Cons

- Introduces a distribution channel that is not part of the current milestone.
- Requires Play Console enrollment and operational policy decisions.
- Does not remove the need to define signing behavior for non-Play APK distribution.
- Expands the release scope before the initial GitHub distribution process is stable.

Deferred.

---

# Consequences

## Positive

- Official APKs have a stable and reviewable project identity.
- Future updates can preserve Android signing continuity.
- Release secrets remain outside source control.
- Debug development remains independent from production signing credentials.
- Unsigned and debug-signed artifacts cannot be treated as official releases.
- Release verification becomes an explicit quality gate.
- Future CI signing or Play App Signing can build on a documented trust model.

## Negative

- Release preparation requires protected external key material and credentials.
- The maintainer must manage backup, recovery, and access control.
- Fully automated official release builds remain unavailable.
- Key loss or compromise can have long-term release consequences.
- The release process gains additional validation and documentation steps.

---

# Implementation Requirements

Before the first official signed APK is distributed, the implementation must:

- implement an official release signing workflow using supported Android tooling without embedding secret values in committed files;
- keep the signing keystore outside the repository;
- add defensive ignore rules for common keystore and signing-secret files;
- keep normal debug and validation workflows functional without signing secrets;
- distinguish unsigned technical build output from official distributable artifacts;
- verify the signed APK with the Android SDK signing tools;
- record the accepted public signing-certificate SHA-256 fingerprint as release evidence;
- calculate and record the signed APK SHA-256 checksum after verification;
- update `SECURITY.md`, `RELEASES.md`, and relevant release documentation;
- verify the primary keystore and at least one recovery copy before distribution;
- confirm that no signing secret appears in Git history, build output, logs, or documentation.

Implementation-specific secret names, actual private or recovery paths, command sequences, and machine configuration belong in protected local configuration or operational documentation, not in this ADR. Repository documentation may contain non-secret placeholders and repeatable verification procedures.

---

# References

- ADR-001: Project Vision
- ADR-002: Application Architecture
- SECURITY.md
- RELEASES.md
- DEVELOPMENT.md
- [Android Developers: Sign your app](https://developer.android.com/studio/publish/app-signing)
- [Android Developers: Build your app from the command line](https://developer.android.com/build/building-cmdline)
- [Android Developers: apksigner](https://developer.android.com/tools/apksigner)
- [Android Developers: zipalign](https://developer.android.com/tools/zipalign)
- [Android Developers: Configure build variants](https://developer.android.com/build/build-variants)

---

# Notes

This ADR governs Android application release signing and artifact trust.

It does not govern SSH client private keys, SSH authentication credentials, trusted SSH host keys, or runtime secure storage.

This ADR was accepted after review confirmed the signing identity, secret boundary, recovery requirement, artifact-verification gate, and current distribution scope.
