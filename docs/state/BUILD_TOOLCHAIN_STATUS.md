# Build Toolchain Status

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-15

---

## Purpose

This document records the currently implemented build toolchain, Android platform baseline, repository-controlled dependency versions, CI environment, and release-toolchain constraints for Server Toolkit.

It is a living current-state document. It describes what is implemented now and must not be used to record unapproved future versions.

Update strategy is defined separately in [Build Toolchain and Dependency Policy](../BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md).

---

## Baseline Scope

This status was reconstructed from the current repository declarations on `main`, including:

- `app/build.gradle.kts`.
- `gradle/libs.versions.toml`.
- `gradle/wrapper/gradle-wrapper.properties`.
- `.github/workflows/android-validation.yml`.
- `config/release/android-release.properties`.
- The implemented Android release process documented in `RELEASES.md`.

Repository declarations are authoritative when this summary conflicts with implementation.

---

## Current Project and Android Metadata

| Item | Current value | Source |
|---|---:|---|
| Stable project release | `v0.4.0` | `PROJECT_STATE.md` and `RELEASES.md` |
| Android application ID | `de.hamedtanha.servertoolkit` | `app/build.gradle.kts` and release configuration |
| Android `versionName` | `0.4.0` | `app/build.gradle.kts` |
| Android `versionCode` | `2` | `app/build.gradle.kts` |
| Minimum SDK | `26` | `app/build.gradle.kts` |
| Target SDK | `36` | `app/build.gradle.kts` |
| Compile SDK | Android API `36`, minor API level `1` | `app/build.gradle.kts` |

Android version metadata is release metadata, not a dependency version. It is included here because build and release verification depend on it.

---

## Core Build Toolchain

| Component | Current version or setting | Repository declaration |
|---|---:|---|
| Java source compatibility | `17` | `app/build.gradle.kts` |
| Java target compatibility | `17` | `app/build.gradle.kts` |
| Kotlin JVM toolchain | `17` | `app/build.gradle.kts` |
| CI JDK | Temurin `17` | `.github/workflows/android-validation.yml` |
| Gradle Wrapper | `9.4.1` | `gradle/wrapper/gradle-wrapper.properties` |
| Android Gradle Plugin | `9.2.1` | `gradle/libs.versions.toml` |
| Kotlin | `2.2.10` | `gradle/libs.versions.toml` |
| KSP | `2.2.10-2.0.2` | `gradle/libs.versions.toml` |

### Current Alignment

The Java source level, Java target level, Kotlin JVM toolchain, and CI JDK are aligned on Java 17.

Android Gradle Plugin, Kotlin, and KSP form a compatibility cluster. They must not be updated independently without checking their supported version relationship.

No Java, Gradle, Android Gradle Plugin, Kotlin, or KSP upgrade is currently accepted merely by the existence of a newer release.

---

## Android Platform and Release Toolchain

| Component | Current version or setting | Repository declaration |
|---|---:|---|
| Android compile SDK | API `36`, minor API level `1` | `app/build.gradle.kts` |
| Android target SDK | `36` | `app/build.gradle.kts` |
| Android minimum SDK | `26` | `app/build.gradle.kts` |
| Android Build Tools used by release workflow | `36.1.0` | `config/release/android-release.properties` |
| Android NDK | `28.2.13676358` | `app/build.gradle.kts` and release configuration |
| Required NDK tool | `llvm-strip` from the pinned NDK | Release signing workflow |

### NDK Baseline

The project does not currently document project-owned C or C++ source as an application capability.

However, the NDK version is operationally required by the implemented release workflow. The signing and verification process resolves the pinned NDK, verifies the matching `llvm-strip`, processes packaged native libraries, and records the NDK version in release evidence.

Therefore, the explicit NDK pin is not currently removable as unused configuration. Removing or changing it requires coordinated release-toolchain validation and documentation updates.

### Build Tools Baseline

Android Build Tools `36.1.0` is repository-controlled for the local signing and verification workflow. A Build Tools change can alter release tooling behavior and requires release-script validation.

---

## Primary Application Dependencies

| Dependency or platform | Current version |
|---|---:|
| Jetpack Compose BOM | `2026.02.01` |
| AndroidX Core KTX | `1.10.1` |
| AndroidX Activity Compose | `1.8.0` |
| AndroidX Lifecycle Runtime KTX | `2.6.1` |
| Navigation Compose | `2.9.6` |
| Hilt | `2.60` |
| Hilt Navigation Compose | `1.3.0` |
| Room | `2.8.4` |
| Kotlin Coroutines test library | `1.9.0` |
| Kotlinx Serialization BOM | `1.8.1` |
| SSHJ | `0.38.0` |
| Bouncy Castle provider | `1.75` |

Compose UI, graphics, tooling, testing, and Material 3 artifacts derive their versions from the Compose BOM.

Kotlinx Serialization core and JSON artifacts derive their versions from the Kotlinx Serialization BOM.

---

## Test Dependencies

| Dependency | Current version |
|---|---:|
| JUnit 4 | `4.13.2` |
| AndroidX Test JUnit | `1.1.5` |
| Espresso Core | `3.5.1` |
| Room Testing | `2.8.4` through the Room version catalog entry |
| Compose UI test libraries | Compose BOM `2026.02.01` |

Test dependencies must be reviewed together with the production APIs and platform behavior they validate. A test-library update is not useful when it silently reduces compatibility with the current application stack.

---

## Continuous Integration Baseline

The Android validation workflow currently uses:

| Item | Current value |
|---|---:|
| Runner | `ubuntu-24.04` |
| Checkout action | `actions/checkout@v7` |
| Java setup action | `actions/setup-java@v5` |
| Java distribution | Temurin |
| Java version | `17` |
| Gradle setup action | `gradle/actions/setup-gradle@v6` |
| Job timeout | `30` minutes |

The workflow validates:

```text
:app:compileDebugKotlin
:app:compileDebugAndroidTestKotlin
:app:testDebugUnitTest
:app:lintDebug
:app:assembleDebug
:app:assembleDebugAndroidTest
```

A core toolchain update must keep local and CI behavior aligned.

---

## Release Baseline Constraints

The released `v0.4.0` artifact established the following immutable historical release baseline:

| Item | Released value |
|---|---:|
| Android Build Tools | `36.1.0` |
| Android NDK | `28.2.13676358` |
| Application version name | `0.4.0` |
| Application version code | `2` |
| Source tag | `v0.4.0` |

Future updates do not modify this historical baseline. They define a new build baseline for later artifacts.

Any accepted change to Java, Gradle, Android Gradle Plugin, Kotlin, KSP, compile SDK, Build Tools, NDK, packaging, or release scripts must be assessed for release-evidence impact before the next distributed artifact is prepared.

---

## Local Development Environment Boundary

The repository currently pins or declares the versions required for Gradle, compilation, CI, Android platform targeting, and release processing.

The repository does not currently define one mandatory Android Studio release as part of the reproducible baseline.

Android Studio is a developer tool and may vary locally as long as it supports the repository-controlled Gradle, Android Gradle Plugin, Kotlin, SDK, and JDK baseline. If a specific Android Studio version becomes mandatory, that requirement must be documented explicitly and justified.

Local SDK installation paths, workstation-specific environment variables, credentials, signing material, and private recovery locations are not part of this status document and must not be committed.

---

## Current Upgrade Decisions

No toolchain or dependency version upgrade is accepted by this document.

The next upgrade must begin with an explicit engineering trigger and follow [Build Toolchain and Dependency Policy](../BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md).

Potential versions discovered by IDE suggestions, dependency reports, or automated tools remain proposals until reviewed, implemented, validated, and merged.

---

## Current Technical Guardrails

- Keep Java source, target, Kotlin JVM toolchain, and CI JDK aligned.
- Treat Gradle, Android Gradle Plugin, Kotlin, and KSP as a compatibility cluster.
- Keep the release Build Tools declaration synchronized with release scripts and documentation.
- Keep the NDK declaration synchronized between the Android build and release configuration.
- Do not remove the NDK pin while the verified release workflow depends on its `llvm-strip`.
- Review Room updates together with KSP, generated schemas, and persistence tests.
- Review SSHJ and Bouncy Castle updates as security-sensitive changes.
- Do not mix unrelated dependency upgrades with Operations feature implementation.
- Do not record proposed versions as current implementation.

---

## Update Procedure

Whenever an accepted update is merged:

1. Read the actual version declarations from the source-of-truth branch.
2. Update the affected tables in this document.
3. Update the `Last Updated` date.
4. Record material changes in `CHANGELOG.md`.
5. Update `PROJECT_STATE.md` if the baseline or a project guardrail changes.
6. Update `RELEASES.md` when release tooling or evidence changes.
7. Update or create an ADR when the accepted decision boundary changes.
8. Verify that no old version is incorrectly presented as current outside historical release evidence.

---

## Related Documents

- [Build Toolchain and Dependency Policy](../BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Project State](../PROJECT_STATE.md)
- [Development Process](../DEVELOPMENT.md)
- [Release Process](../RELEASES.md)
- [Documentation Governance](../DOCUMENTATION.md)
- [Changelog](../CHANGELOG.md)
- [Architecture Decision Records](../adr/README.md)
