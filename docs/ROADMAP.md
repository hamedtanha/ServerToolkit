# Roadmap

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Active
**Last Updated:** 2026-08-12

---

## Purpose

This roadmap describes the planned evolution of the Server Toolkit project.

It provides a high-level view of development milestones without prescribing speculative implementation details.

Roadmap items may evolve, but each milestone must have a clear product objective, architecture fit, validation boundary, and factual support claim.

---

## Guiding Principles

Development follows these principles:

- Build incrementally.
- Complete one major feature area at a time.
- Preserve architectural consistency.
- Keep the product Core platform-neutral.
- Select broadly useful capabilities before named-service integrations.
- Distinguish architectural extensibility from implemented and verified support.
- Introduce Capability Gateways only for concrete translation, routing, discovery, normalization, orchestration, or policy needs.
- Avoid unnecessary scope expansion and premature abstraction.
- Keep the application releasable.
- Keep documentation synchronized with implementation.

---

## Feature Classification

Roadmap candidates are classified as:

### Core Product Capabilities

Broadly useful product workflows such as inventory, connection handling, saved commands, explicit command execution, history, favorites, and capability-aware status presentation.

### Platform Capabilities

General operational concepts whose implementation varies by target environment and may require Core contracts, a Capability Gateway, and Providers or Adapters.

### Optional Integrations

Vendor-, product-, or service-specific workflows. Optional integrations are not committed roadmap scope until separately accepted.

---

## Cross-Milestone Engineering Maintenance

Security, platform support, build compatibility, CI reliability, dependency maintenance, documentation correction, and release-toolchain work may be performed during any milestone when a concrete engineering trigger exists.

This work is not automatically part of the functional scope of the active milestone.

Rules:

- Keep maintenance changes independently reviewable from product features.
- Do not update merely because a newer version exists.
- Record current versions in [Build Toolchain Status](state/BUILD_TOOLCHAIN_STATUS.md).
- Follow [Build Toolchain and Dependency Policy](BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md).
- Track specific maintenance work in a focused Issue.
- Prioritize urgent security and supportability work when delaying it creates greater risk.

---

## Version 0.1.0 — Foundation ✅

Objective:

Establish the engineering foundation.

Completed:

- Repository initialization.
- Development workflow.
- Documentation structure.
- ADR process.
- Original project vision.
- Engineering guidelines.
- Initial architecture documentation.
- Initial Android application skeleton.

Status:

Completed.

---

## Version 0.2.0 — Android Architecture and Navigation ✅

Objective:

Establish the Android application architecture and first navigable flow.

Completed:

- Single Activity structure.
- Hilt application setup.
- App-level Navigation Compose infrastructure.
- Dashboard route.
- Server Inventory route.
- Add Server route.
- Edit Server route.
- Dashboard-to-Server-Inventory navigation.
- Add and Edit Server navigation.
- Package structure cleanup.

Status:

Completed.

---

## Version 0.3.0 — Server Inventory Foundation ✅

Objective:

Implement the foundation for local server inventory management.

Completed:

- Server domain and environment models.
- Add, edit, delete, search, and filtering workflows.
- Shared Server Form state and screen.
- Server repository contract.
- In-memory and Room-backed repository implementations.
- Room entity, DAO, mapper, Hilt wiring, and schema export.
- Unit and Android instrumentation coverage.
- Accepted Server Inventory baseline.

Deliverable:

Working local server inventory baseline.

Status:

Accepted baseline.

---

## Version 0.4.0 — SSH ✅

Objective:

Introduce secure and reliable SSH connectivity as the first verified remote-access capability.

Completed:

- SSH architecture and security ADRs.
- SSHJ integration behind project-owned data-layer boundaries.
- Host-key observation, review, confirmation, and persistence.
- Ephemeral password authentication.
- Ephemeral Android document-picker private-key workflow.
- Verified encrypted and unencrypted OpenSSH v1 Ed25519 and RSA support.
- Project-owned session lifecycle and deterministic cleanup.
- Explicit disconnect and reconnection.
- Non-interactive explicit command execution.
- SSH connection history persistence and presentation.
- Runtime and automated verification.
- Signed and published version `0.4.0` release.

Deliverable:

Reliable SSH connections to managed server records.

Status:

Completed.

---

## Version 0.5.0 — Operations

Objective:

Improve repeatable operational workflows without automatic execution.

Accepted first increment:

Saved Command Foundation.

Implemented foundation:

- Global Saved Command ownership with server-specific assignment deferred.
- Explicit execution safety: selection must never execute automatically.
- Project-owned Saved Command domain model and repository contract.
- Room entity, DAO, mapper, repository implementation, and Hilt bindings.
- Database migration `4 → 5`.
- Exported Room schema version `5`.
- Domain, mapper, DAO, repository, and migration coverage.

Implemented management slice:

- Feature-owned navigation destination and Dashboard entry point.
- Loading, empty, content, blocking-failure, and non-blocking observation-failure states.
- Create workflow with field-level validation and exact command-text preservation.
- Duplicate create-submission prevention.
- Stable-identifier delete selection and explicit confirmation.
- Duplicate delete-confirmation prevention.
- Retryable mutation failures that preserve loaded content.
- Focused UI-state and ViewModel coverage.
- Physical-device persistence verification after application restart.
- No SSH connection or command-execution path is introduced by the management slice.

Implemented editing slice:

- Explicit per-command edit workflow.
- Stable identifier and original creation-timestamp preservation.
- Shared create/edit validation and name normalization.
- Exact command-text preservation without parsing or execution.
- Duplicate-save prevention and retryable mutation failures that preserve edited input.
- Focused ViewModel and Compose instrumentation coverage.

Implemented SSH Input Integration:

- Inline Saved Command selection from the existing SSH workflow.
- Lazy repository observation with loading, empty, failure, retry, cancellation, and later-failure preservation.
- Stable-identifier presentation in repository order.
- Exact replacement of the existing multiline command input without trimming, normalization, parsing, interpolation, or appending.
- Continued manual command editing before execution.
- Separate explicit Run action as the only execution trigger.
- Preservation of execution-state blocking, session lifecycle, cleanup, and stale-result guardrails.
- Focused ViewModel coverage and five passing targeted Compose tests on the Pixel 9 Android Virtual Device.

Deferred from the first increment:

- Categories and favorites.
- Variables, templates, placeholders, or secret substitution.
- Server-specific assignment.
- Automatic or background execution.
- Import, export, synchronization, or backup.
- Persistent credential storage.

Deliverable:

Repeatable explicit operational commands.

Status:

In progress.

---

## Version 0.6.0 — Dashboard Evolution

Objective:

Evolve the Dashboard from an entry screen into a useful operational overview using only implemented and supported data.

Planned direction:

- Recent servers.
- Favorite servers after a focused feature decision.
- Connection-history summary.
- Server Inventory summary.
- Saved Commands entry and summary after the management workflow exists.
- Capability-aware status cards only for implemented and verified capabilities.

Guardrails:

- Dashboard does not own feature data.
- Dashboard consumes stable project-owned summaries.
- No platform capability is presented as supported without evidence.

Deliverable:

Operational home screen based on implemented capabilities.

Status:

Planned.

---

## Version 0.7.0 — Remote Capability Foundation

Objective:

Implement the first concrete gateway-backed remote capability using ADR-015 and ADR-016.

Entry conditions:

- The selected capability has clear platform-neutral user value.
- A focused Issue defines Core semantics, support states, Gateway responsibility, first Provider or Adapter, transport boundary, security rules, and validation evidence.
- The capability is selected by product priority rather than by attachment to one named service.

Planned architecture outcomes:

- First project-owned remote capability contract.
- Explicit supported, unsupported, unknown, and unavailable states.
- First narrowly scoped Capability Gateway.
- First concrete Provider or Adapter.
- Provider-level parsing and normalized Core results.
- Automated routing, mapping, and support-state coverage.
- Runtime verification for the first documented target environment.

The exact first capability is intentionally not selected by this roadmap revision. It requires a separate planning decision after current Operations work or an explicit priority change.

Deliverable:

Verified first use of the three-level remote capability architecture.

Status:

Planned.

---

## Version 0.8.0 — Operational Insights

Objective:

Provide lightweight operational insight through capabilities implemented and verified on the Remote Capability Foundation.

Potential direction, subject to focused acceptance:

- Availability checks.
- Latency observations.
- Basic resource indicators.
- Capability-aware refresh behavior.
- Clear unsupported and unavailable states.

Rules:

- No universal monitoring claim.
- No raw platform-output parsing in presentation.
- No guessed commands for unknown targets.
- Each added provider expands the verified support matrix explicitly.

Deliverable:

Lightweight evidence-backed operational insight.

Status:

Planned.

---

## Optional Integrations — Unscheduled

Vendor-, product-, and service-specific integrations are not committed version milestones.

Examples may include:

- Container platforms.
- VPN technologies.
- Cloud providers.
- Certificate-authority workflows.
- Service-specific management tools.

An integration enters the roadmap only after a focused decision defines:

- Product value.
- Isolation from Core models.
- Gateway and Provider boundaries.
- Security and lifecycle requirements.
- Dependency impact.
- Support policy and verification.
- Maintenance ownership.

Naming an example does not accept it for implementation.

---

## Version 0.9.0 — Stabilization

Objective:

Prepare for production release.

Planned:

- End-to-end workflow review.
- Error- and support-state handling review.
- UI consistency and accessibility review.
- Security-boundary review.
- Architecture-boundary review.
- Documentation and support-claim review.
- Release readiness checklist.

Deliverable:

Stable pre-1.0 application baseline.

Status:

Planned.

---

## Version 1.0.0 — Initial Production Release

Objective:

Publish a reliable, secure, maintainable remote-systems operations application with a clearly documented verified support boundary.

Release scope is finalized only from implemented, tested, runtime-verified, and documented capabilities.

The release does not require universal platform support or named-service integrations.

Deliverable:

Initial production-quality Server Toolkit release.

Status:

Planned.
