# Engineering Strategy

**Project:** Server Toolkit  
**Status:** Active  
**Last Updated:** 2026-07-16

---

## Purpose

This document defines how Server Toolkit work is selected, sliced, designed, implemented, reviewed, validated, and documented.

It does not replace the project state, product vision, architecture documentation, security documentation, contribution rules, build-toolchain policy, or ADRs.

Current implementation status is defined in [Project State](PROJECT_STATE.md).

Architecture decisions are defined in the ADRs.

---

## Core Principle

Server Toolkit development follows a standards-aligned, product-roadmap-first, platform-neutral, capability-aware, and shortest-safe-job-first strategy.

Work must first pass product-direction, security, architecture, documentation, compatibility, supportability, and releasability gates.

After those gates pass, tasks are selected and sliced according to user value, modularity, dependency consistency, evidence requirements, and modern Android engineering practices.

---

## Non-Negotiable Engineering Gates

A task must not proceed if it violates any of these gates:

- Security and data-safety requirements.
- Accepted product direction.
- Accepted architecture decisions.
- Platform-neutral Core boundaries.
- Main branch releasability.
- Factual and evidence-based documentation.
- Clear ownership and lifecycle boundaries.
- Maintainability and testability expectations.
- Accepted build-toolchain compatibility and reproducibility requirements.

These gates apply before task size, convenience, novelty, or implementation speed.

---

## Task Selection Order

After the non-negotiable gates pass, normal product work selection follows this order:

1. Product and roadmap value.
2. Platform-neutral capability fit.
3. Architecture and modularity fit.
4. Security, supportability, and evidence requirements.
5. Dependency consistency.
6. Shortest safe job first.
7. Modern Android alignment.

Shortest-job-first is not a reason to implement work that is unsafe, premature, undocumented, service-biased, or disconnected from the product roadmap.

Security, platform-support, build, CI, and release-toolchain maintenance may be selected outside normal roadmap order when delaying the work creates greater project risk. Such work must still use a focused reviewable slice.

---

## Product Roadmap First

Implementation work must support the accepted product direction.

A task should not be selected only because it is technically interesting, quick to implement, convenient for one deployment, suggested by an IDE, or useful for one named service.

Before starting a task, verify:

- The task supports the current product direction or resolves an accepted engineering risk.
- The user-visible, architecture-visible, security, supportability, or reproducibility value is clear.
- The task does not introduce premature abstractions.
- The task does not create orphan models, unused dependencies, or duplicated ownership.
- The task does not conflict with the current project state.
- The task does not redefine the Core around one operating system, shell, transport, vendor, or service.

---

## Platform-Neutral Feature Selection

Every proposed remote-operation feature must be classified before implementation.

### Core Product Capability

A broadly useful application capability whose meaning is independent from one provider or platform.

Examples include inventory, saved commands, explicit command execution, local history, grouping, search, and capability-aware status presentation.

### Platform Capability

A general operational concept whose implementation differs across target environments.

Examples may include service management, resource inspection, or system-log access.

A platform capability requires explicit Core semantics and may require a Capability Gateway and one or more Providers or Adapters.

### Optional Integration

A vendor-, product-, or service-specific workflow.

An optional integration must not become a committed core feature merely because the maintainer currently uses that technology.

It requires a focused issue, clear isolation boundary, support policy, security review, and validation plan.

---

## Support Claim Gate

Documentation and UI must distinguish:

1. Architecturally permitted.
2. Implemented.
3. Verified.

Architecture designed to permit a platform is not evidence that the platform is supported.

A verified support claim requires identified automated tests, runtime evidence, or both for a documented environment.

Unsupported, unknown, and unavailable capability states must remain explicit. The application must not silently guess commands or providers.

---

## Shortest Safe Job First

Work should be divided into the smallest safe slice that can be reviewed, tested, documented, and merged.

A safe slice must:

- Have one clear purpose.
- Keep `main` releasable.
- Avoid unrelated refactoring.
- Avoid broad rewrites unless explicitly justified.
- Include tests when behavior changes.
- Include documentation updates when behavior, architecture, security posture, support claims, roadmap status, technical baseline, release process, or project scope changes.

Small pull requests are preferred because they reduce review risk and make architectural, product, or toolchain drift easier to detect.

---

## Android Application Architecture

The project preserves the accepted feature-first Android architecture unless a stronger technical reason is documented.

Preferred responsibilities:

- UI for declarative Compose rendering and event forwarding.
- Presentation for immutable UI state, ViewModels, and user-event coordination.
- Domain for project-owned models, repositories, capability contracts, results, and justified use cases.
- Data for Room, external clients, Android APIs, repository implementations, transports, and adapters.
- Hilt for dependency construction and binding.
- Documentation and ADRs for significant decisions.

Ownership must remain explicit.

A model, state value, dependency, contract, or abstraction should not be introduced unless its owner, lifecycle, consumer, and boundary are clear.

---

## Three-Level Remote Capability Strategy

A remote capability that requires translation, routing, discovery, normalization, orchestration, policy enforcement, or external access uses the accepted three-level architecture.

### Core

Owns platform-neutral contracts, models, results, support states, and policy.

### Capability Gateway

Owns target resolution, provider selection, translation, orchestration, normalization, and shared capability guardrails.

### Providers and Adapters

Own concrete transports, operating-system behavior, service-specific APIs, command construction, parsing, and third-party clients.

A Gateway is introduced only when a concrete capability needs it.

Purely local features such as Saved Commands management, local favorites, tags, search, filtering, and Room-backed history must not receive unnecessary Gateway abstractions.

No generic base gateway, base provider, provider registry, plugin framework, or empty package hierarchy is created before concrete implementation needs justify it.

---

## Capability Design Checklist

Before implementing a gateway-backed capability, confirm:

1. What is the platform-neutral user operation?
2. Which layer owns the Core contract?
3. What normalized success and failure results are required?
4. Which support states are required?
5. Why is a Gateway necessary?
6. What target context or discovery information is required?
7. What is the first Provider or Adapter?
8. Which transport and external dependency are involved?
9. What command, identifier, argument, and output boundaries require validation?
10. How do unsupported, unknown, and unavailable states behave?
11. What security and lifecycle rules apply?
12. What automated tests are required?
13. What runtime evidence is required before support is claimed?
14. Which documents and ADRs must change?

If these answers are unclear, reduce the scope or document the decision before implementation.

---

## Dependency Strategy

Dependencies must be added and updated conservatively.

Before adding a dependency, verify:

- It solves a real current problem.
- It has a clear architectural boundary.
- It does not leak into Core or presentation layers.
- It has acceptable maintenance, security, support, and compatibility characteristics.
- The project can test behavior that depends on it.
- It is documented when it affects architecture, security, support claims, or release behavior.

Before updating a dependency, verify:

- The update has a concrete engineering trigger.
- Current and proposed versions are known.
- Release notes and migration guidance were reviewed.
- Compatibility clusters were identified.
- Validation and rollback requirements match the risk.
- The change remains independently reviewable from unrelated feature work.

Modern libraries are valuable only when they strengthen the product architecture.

They are not a substitute for clear boundaries, ownership, tests, documentation, compatibility evidence, or support verification.

---

## Toolchain Maintenance Strategy

Toolchain maintenance includes Java, Gradle, Android Gradle Plugin, Kotlin, KSP, Android SDK, Build Tools, NDK, CI actions, and release tooling.

Rules:

- The existence of a newer version is not sufficient justification.
- Updates must identify security, supportability, platform, compatibility, reliability, or reproducibility value.
- Java, Gradle, Android Gradle Plugin, Kotlin, and KSP are treated as a compatibility cluster.
- Android SDK, Build Tools, NDK, packaging, and release scripts are reviewed as a release-toolchain cluster.
- High-risk updates require a dedicated Issue, explicit validation, rollback planning, and ADR review.
- Current implemented versions remain synchronized in `state/BUILD_TOOLCHAIN_STATUS.md`.
- A toolchain change that affects a release artifact invalidates earlier candidate evidence for that pending release.

---

## Android Engineering Standards

Preferred technologies and practices include:

- Kotlin.
- Jetpack Compose.
- MVVM.
- StateFlow or lifecycle-aware observable state.
- Hilt.
- Room.
- Navigation Compose.
- Coroutines and Flow.
- KSP where annotation processing is required.
- Repository pattern for owned data access.
- Clear separation of UI, presentation, domain, data, gateway, and provider responsibilities when each is justified.
- Unit tests for domain and presentation behavior.
- Instrumentation tests for Android and Room behavior.
- Lint and build verification before merge.

New Android APIs or libraries are introduced only when they fit the current architecture and solve a concrete problem.

Android platform updates additionally review manifest, permission, target-SDK behavior, runtime compatibility, and release-toolchain impact.

---

## Documentation as Implementation

Documentation is part of the implementation.

When behavior, architecture, security posture, support claims, roadmap status, technical baseline, release process, or project scope changes, documentation must be updated in the same pull request.

Documentation must remain factual:

- Do not document behavior that does not exist.
- Do not leave implemented behavior undocumented.
- Do not describe architectural extensibility as verified support.
- Do not keep obsolete placeholder wording after real behavior exists.
- Do not record proposed toolchain or dependency versions as current.
- Preserve historical ADRs and release evidence.
- Prefer complete, coherent document updates over fragmented edits.

Significant product, architecture, security, compatibility, or support-policy decisions require an ADR.

---

## Security and Data Safety

Security-sensitive work must be designed before implementation.

Credential handling, secure storage, backup behavior, SSH trust, session lifecycle, command execution, provider selection, command translation, cryptographic dependencies, and release signing remain explicit and reviewed.

No secret material may be stored unless a secure storage boundary, lifecycle, deletion behavior, tests, and documentation are already defined.

Unsupported or unknown capabilities must not trigger unsafe fallback commands.

Automatic or background execution requires a separate accepted decision.

---

## Pull Request Standard

Each pull request should:

- Have one clear purpose.
- Use a short-lived branch.
- Keep `main` releasable.
- Use Conventional Commits.
- Include tests when behavior changes.
- Include documentation updates when needed.
- Avoid unrelated cleanup.
- Avoid mixing feature work, refactoring, toolchain maintenance, and documentation unless the connection is explicit.
- Explain scope, rationale, risk, support impact, and verification.
- Include rollback reasoning for medium- and high-risk changes.

Architecture-only pull requests must not create speculative production packages or placeholder abstractions.

---

## Task Selection Checklist

Before starting a task, confirm:

1. Does it support the accepted product direction, roadmap, or engineering-risk reduction?
2. Is the value clear?
3. Is it Core, a platform capability, or an optional integration?
4. Does it preserve platform-neutral Core meaning?
5. Does it pass security and data-safety requirements?
6. Is ownership clear?
7. Does it fit the current Android architecture?
8. Does it require a Capability Gateway?
9. Is it the smallest safe slice?
10. Does it add, remove, or update a dependency or toolchain component?
11. Does it require compatibility-cluster review?
12. Does it require an ADR?
13. What automated and runtime verification is required?
14. What support claim will be valid after completion?
15. Which documents require updates?
16. Will `main` remain releasable after merge?

If an answer is unclear, reduce the scope or document the decision before implementation.

---

## Related Documents

- [Product Vision](PRODUCT_VISION.md)
- [Project State](PROJECT_STATE.md)
- [Architecture](ARCHITECTURE.md)
- [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md)
- [Development Process](DEVELOPMENT.md)
- [Documentation Governance](DOCUMENTATION.md)
- [Release Process](RELEASES.md)
- [Roadmap](ROADMAP.md)
- [Architecture Decision Records](adr/README.md)
