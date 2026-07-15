# Build Toolchain and Dependency Policy

**Project:** Server Toolkit  
**Document Baseline:** 0.5.0-alpha  
**Status:** Foundational  
**Last Updated:** 2026-07-15

---

## Purpose

This document defines how Server Toolkit evaluates, plans, implements, validates, and records updates to its build toolchain and software dependencies.

The objective is to keep the project secure, supported, reproducible, and maintainable without introducing uncontrolled compatibility risk or allowing maintenance work to obscure product milestones.

This policy is the source of truth for update strategy. The currently implemented versions are recorded separately in [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md).

---

## Scope

This policy applies to:

- Java and the project JVM toolchain.
- Gradle and the Gradle Wrapper.
- Android Gradle Plugin.
- Kotlin and Kotlin compiler plugins.
- Kotlin Symbol Processing.
- Android compile SDK, target SDK, minimum SDK, and Build Tools.
- Android NDK and native build or release tooling.
- Jetpack Compose and AndroidX libraries.
- Persistence, dependency injection, serialization, concurrency, networking, cryptography, testing, and other third-party libraries.
- GitHub Actions and other repository-controlled engineering automation.
- Release scripts and reproducible-build requirements affected by toolchain changes.

This policy does not define product feature priority. Product scope remains governed by the roadmap and engineering strategy.

---

## Policy Principles

Server Toolkit follows these principles:

- Update with a concrete engineering reason, not merely because a newer version exists.
- Keep security and supportability ahead of convenience.
- Treat compatibility as an evidence requirement, not an assumption.
- Change one coherent compatibility cluster at a time.
- Keep maintenance changes independently reviewable from product features.
- Prefer small, reversible updates over broad upgrade batches.
- Keep `main` releasable throughout the update process.
- Preserve build and release reproducibility.
- Record the implemented baseline after every accepted update.
- Use ADRs only when an update changes a significant architectural, compatibility, security, or engineering-policy decision.

---

## Sources of Truth

The update model is divided into three distinct concerns:

| Concern | Source of truth |
|---|---|
| How updates are evaluated and performed | `BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md` |
| Which versions are currently implemented | `state/BUILD_TOOLCHAIN_STATUS.md` |
| Which specific future upgrade is planned | GitHub Issue, milestone plan, or `ROADMAP.md` when roadmap-significant |

Current version declarations in Gradle, CI, and release configuration remain authoritative implementation evidence. The status document summarizes those declarations and must be synchronized with them.

---

## Valid Update Triggers

An update should be proposed when at least one of the following conditions exists:

- A security advisory affects the current version.
- A component is unsupported, deprecated, or approaching end of support.
- Android platform requirements require a newer version.
- A required product capability cannot be implemented safely on the current baseline.
- A build, test, CI, packaging, signing, or release incompatibility must be resolved.
- A current version causes a confirmed reliability, performance, or correctness problem.
- A supported update removes a meaningful maintenance burden or deprecated API.
- Dependency compatibility requires coordinated movement within a toolchain cluster.
- Reproducibility or developer-environment consistency requires a pinned version.

The existence of a newer version alone is not sufficient justification.

---

## Update Categories

### Security Update

A change required to remediate a known vulnerability or unsafe behavior.

Security updates receive priority over normal milestone sequencing. They still require compatibility review and validation proportional to their risk.

### Compatibility Update

A change required to keep the project compatible with Android platform requirements, the build system, CI, release tooling, or another accepted dependency.

### Routine Maintenance Update

A low-risk patch or minor update with a clear maintenance benefit and no intended architectural change.

### Major Toolchain Update

A major-version or baseline change that can affect source compatibility, generated code, build behavior, CI, release artifacts, or contributor environments.

Examples include:

- Java 17 to Java 21.
- Gradle 9 to Gradle 10.
- Android Gradle Plugin 9 to Android Gradle Plugin 10.
- A Kotlin compiler baseline change requiring coordinated KSP or Compose changes.
- An Android NDK baseline change affecting release tooling or packaged native libraries.

### Removal or Simplification

Removal of an obsolete dependency, plugin, explicit version pin, workaround, or build configuration.

Removal requires the same evidence standard as addition. A configuration must not be removed merely because its purpose is not immediately visible.

---

## Risk Classification

### Low Risk

Typical characteristics:

- Patch-level dependency update.
- No migration requirement.
- No compiler, plugin, schema, native, security-boundary, or release-workflow change.
- Existing automated validation provides direct coverage.

Minimum validation:

- Dependency resolution.
- Project compilation.
- Unit tests.
- Lint.
- Debug assembly.

### Medium Risk

Typical characteristics:

- Minor-version library update.
- New deprecations or behavior changes.
- Generated-code changes.
- Compose, AndroidX, Room, Hilt, serialization, networking, or test-framework behavior changes.

Minimum validation:

- All low-risk validation.
- Release-note and migration-guide review.
- Relevant instrumentation or integration tests.
- Focused Android runtime verification.
- Documentation impact review.

### High Risk

Typical characteristics:

- Major toolchain update.
- Java, Gradle, Android Gradle Plugin, Kotlin, KSP, SDK, Build Tools, or NDK baseline change.
- Database schema or generated-code compatibility impact.
- Cryptography, SSH, trust, credential, or release-signing impact.
- Build artifact, native-library, signing, or reproducibility impact.

Minimum validation:

- All medium-risk validation.
- Dedicated GitHub Issue and branch.
- Explicit compatibility matrix.
- CI validation.
- Clean-build verification.
- Relevant emulator or device verification.
- Release workflow validation when affected.
- Rollback plan.
- ADR review.

---

## Compatibility Clusters

Dependencies that share compiler, generated-code, build, or runtime constraints must be evaluated together.

### Core Build Cluster

- Java/JDK.
- Gradle Wrapper.
- Android Gradle Plugin.
- Kotlin.
- Kotlin Compose plugin.
- KSP.

### Android Platform and Release Cluster

- Compile SDK.
- Target SDK.
- Android Build Tools.
- Android NDK.
- Packaging behavior.
- Release signing and verification scripts.

### UI Cluster

- Jetpack Compose BOM.
- Material 3.
- Activity Compose.
- Navigation Compose.
- Lifecycle libraries.
- Compose testing libraries.

### Persistence and Code Generation Cluster

- Room runtime and compiler.
- KSP.
- Exported Room schemas.
- Migration and instrumentation tests.

### Security and Connectivity Cluster

- SSHJ.
- Bouncy Castle.
- Authentication, host-trust, key-parsing, and cryptographic behavior.

A compatibility cluster may be updated in one pull request only when the versions are technically coupled and separating them would leave the branch unbuildable or untestable. The pull request must explain that coupling.

---

## Update Cadence

Updates are reviewed at these points:

- At the beginning of a new milestone, before feature implementation expands.
- Before release-candidate preparation.
- When platform, security, support, build, CI, or release evidence requires action.
- When a dependency blocks an accepted implementation.

Routine updates are not required on a fixed calendar schedule. Avoiding arbitrary calendar-driven churn is intentional for the current single-maintainer project.

Urgent security updates are handled outside the normal milestone cadence.

---

## Required Assessment

Before implementation, the responsible change must record:

- Current version.
- Proposed version.
- Update trigger and engineering justification.
- Release notes or migration guidance reviewed.
- Compatibility constraints.
- Affected source, tests, generated output, CI, documentation, and release tooling.
- Risk classification.
- Validation plan.
- Rollback plan for medium- and high-risk changes.
- Whether an ADR is required.

For high-risk changes, this assessment belongs in a dedicated GitHub Issue or reviewed planning document before code changes begin.

---

## Implementation Workflow

Every accepted update follows this sequence:

```text
Read current repository state
↓
Confirm current baseline
↓
Document update trigger and risk
↓
Review compatibility and migration guidance
↓
Create a dedicated GitHub Flow branch
↓
Update one coherent compatibility cluster
↓
Run automated validation
↓
Run focused runtime or release validation when required
↓
Update current-state and affected governance documentation
↓
Review the complete diff
↓
Merge through a pull request
```

Broad commands that update all dependencies without review are prohibited.

---

## Branch, Commit, and Pull Request Rules

Preferred branch forms:

```text
chore/update-<component>
chore/upgrade-android-toolchain
chore/update-security-dependencies
```

Preferred Conventional Commit forms:

```text
chore(deps): update Room dependencies
chore(build): upgrade Gradle wrapper
chore(build): upgrade JVM toolchain
fix(security): update vulnerable dependency
```

Rules:

- Do not mix unrelated dependency updates.
- Do not hide toolchain upgrades inside feature pull requests.
- Keep generated files, schemas, and lock or verification metadata in the same pull request when they are direct consequences of the update.
- Explain compatibility coupling when multiple components move together.
- Include validation evidence in the pull request description.
- Keep rollback feasible by preserving a focused diff.

---

## Validation Requirements

The validation scope must match the affected layer.

### Baseline Validation

- Gradle dependency resolution succeeds.
- Kotlin compilation succeeds.
- Android test compilation succeeds.
- Unit tests pass.
- Lint passes.
- Debug APK assembly succeeds.
- Debug Android test APK assembly succeeds.

### Additional Validation by Area

| Area | Additional evidence |
|---|---|
| Java, Gradle, AGP, Kotlin, or KSP | Clean build, CI parity, generated-code review, compiler-warning review |
| Compile SDK or target SDK | Manifest and behavior review, affected Android runtime verification |
| Build Tools or NDK | Release-script validation, packaging review, signing and artifact verification |
| Room | Schema review, migration review, DAO and repository instrumentation tests |
| Compose or AndroidX | Focused UI/runtime verification and relevant UI tests |
| SSHJ or Bouncy Castle | Authentication, host-key, key-format, cancellation, cleanup, and failure-mapping regression tests |
| GitHub Actions | Pull-request run proving the changed workflow operates successfully |

A green build is necessary but not sufficient for high-risk updates.

---

## Java and JVM Toolchain Rules

The Java source level, target level, Kotlin JVM toolchain, local build environment, and CI JDK must remain aligned unless a documented technical reason requires otherwise.

A Java baseline change is high risk and requires:

- Core build-cluster compatibility review.
- CI update in the same change.
- Clean-build and test evidence.
- Developer-environment migration guidance.
- ADR review because the accepted project baseline changes.

---

## Android SDK and Build Tools Rules

Compile SDK and target SDK changes must be evaluated separately from ordinary library updates.

The change must review:

- Android platform behavior changes.
- Manifest and permission implications.
- Deprecations and compatibility behavior.
- Required runtime verification.
- Release tooling compatibility.

Build Tools versions used by release scripts must remain synchronized with repository-controlled release configuration and release documentation.

---

## Android NDK Rules

An explicit NDK version must have a documented repository-owned purpose.

The current NDK baseline supports the Android release workflow and provides the verified `llvm-strip` used for packaged native libraries. Therefore, the current pin must not be removed or changed without reviewing:

- Packaged native dependencies and supported ABIs.
- `llvm-strip` availability and behavior.
- Release signing and verification scripts.
- Release evidence generation.
- Clean release-build validation.
- The relevant release-process documentation.

If future implementation introduces project-owned C or C++ code, the NDK becomes an application architecture concern and requires a separate architectural review.

---

## Dependency Security Rules

Security-sensitive dependencies include, but are not limited to:

- SSH libraries.
- Cryptographic providers.
- Serialization libraries handling untrusted input.
- Persistence libraries affecting stored infrastructure data.
- Build plugins capable of executing code during the build.

A security update must record the affected component, exposure, chosen remediation, compatibility impact, and validation evidence without copying sensitive exploit details into operational documentation unnecessarily.

Security fixes may be prioritized ahead of roadmap work.

---

## Release Interaction

A build toolchain update after release-candidate verification invalidates the existing candidate evidence when it can affect compilation, packaging, signing, native-library processing, application metadata, or artifact bytes.

When affected, the release candidate must be rebuilt and the complete verification sequence rerun from the exact intended source commit.

Stable release tags and published artifacts remain immutable historical baselines. A later toolchain update does not alter prior release evidence.

---

## ADR Requirements

An ADR is normally required when an update:

- Changes the accepted Java or JVM baseline.
- Replaces the build system or dependency-management strategy.
- Introduces automated dependency-update governance with material workflow consequences.
- Introduces project-owned native code.
- Changes a major security, trust, credential, backup, compatibility, or release-signing boundary.
- Changes supported platform policy.
- Supersedes an existing accepted architectural decision.

An ADR is normally not required for:

- A compatible patch update.
- A routine minor dependency update with no architectural effect.
- A security patch that preserves the accepted design and boundaries.
- Removal of an unused dependency when evidence confirms no architectural role.

ADR need is determined by decision impact, not by version-number size alone.

---

## Automation Policy

Dependency automation may be introduced only after its operating rules are reviewed.

Any future automation must:

- Create reviewable pull requests rather than merge directly.
- Respect compatibility clusters.
- Avoid uncontrolled update batches.
- Run the required validation.
- Preserve maintainer approval.
- Never change release or security configuration silently.

Introducing or materially changing automated dependency governance requires ADR review.

---

## Current-State Documentation Rules

After an accepted update:

- Update `state/BUILD_TOOLCHAIN_STATUS.md` with the implemented versions and evidence paths.
- Update `PROJECT_STATE.md` when the current project baseline or operational guardrails change.
- Update `CHANGELOG.md` when the change is notable.
- Update `RELEASES.md` when release preparation, signing, verification, or reproducibility changes.
- Update `DEVELOPMENT.md` when the engineering workflow or mandatory validation changes.
- Update ADRs and the ADR index when a significant decision is accepted.

Do not record a proposed version as current before it is implemented on the source-of-truth branch.

---

## Exceptions

An exception is allowed only when delaying an update creates greater security, platform, or release risk than following the normal workflow.

The exception must still:

- Use a branch and pull request.
- State the urgency and reduced validation scope.
- Run the strongest validation feasible.
- Record follow-up work for any deferred evidence.
- Keep `main` releasable.

Urgency is not permission for an unreviewable update batch.

---

## Related Documents

- [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md)
- [Development Process](DEVELOPMENT.md)
- [Engineering Strategy](ENGINEERING_STRATEGY.md)
- [Documentation Governance](DOCUMENTATION.md)
- [Project State](PROJECT_STATE.md)
- [Release Process](RELEASES.md)
- [Roadmap](ROADMAP.md)
- [Changelog](CHANGELOG.md)
- [Architecture Decision Records](adr/README.md)
