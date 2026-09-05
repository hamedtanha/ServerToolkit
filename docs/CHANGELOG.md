# Changelog

All notable changes to this project are documented in this file.

The project follows Conventional Commits and Semantic Versioning principles.

---

## [Unreleased]

### Added

- Added the Server Toolkit application launcher identity with a project-owned geometric mark, adaptive foreground/background resources, Android themed-icon monochrome support, and canonical vector artwork.
- Added a foundational build-toolchain and dependency policy covering update triggers, risk classification, compatibility clusters, validation, release impact, rollback, automation, and ADR boundaries.
- Added a living build-toolchain status document recording the current Java, Gradle, Android, Kotlin, dependency, CI, Build Tools, NDK, and release-toolchain baseline from repository declarations.
- Accepted the first version 0.5.0-alpha Operations increment as the global Saved Command Foundation.
- Added the project-owned `SavedCommand` domain model and repository contract with explicit name, command-text, size, and creation-time boundaries.
- Added Room-backed saved-command persistence with entity, DAO, exact domain/entity mapping, repository implementation, and Hilt bindings.
- Added database migration `4 → 5`, exported Room schema version `5`, and the indexed `saved_commands` table.
- Added domain, mapper, DAO, repository, and migration coverage for validation, exact command-text preservation, stable newest-first ordering, duplicate rejection, lookup, and deletion.
- Added a focused Saved Commands current-state document and accepted delivery boundaries for management UI and later SSH input integration.
- Added a feature-owned Saved Commands navigation destination and Dashboard entry while preserving Server Inventory navigation.
- Added repository-observed loading, empty, content, blocking-failure, and non-blocking observation-failure states.
- Added a validated Saved Command create workflow with exact command-text preservation, visible persistence failures, and duplicate-submission prevention.
- Added stable-identifier delete actions with explicit confirmation, deletion progress, retryable failure handling, cancellation, and duplicate-confirmation prevention.
- Added explicit Saved Command editing with pre-populated values, shared validation, stable identifier and creation-time preservation, exact command-text updates, duplicate-save prevention, retryable failures, and focused ViewModel and Compose coverage.
- Added focused Saved Commands UI-state and ViewModel coverage for observation, creation, deletion, failure containment, retry, and loaded-content preservation.
- Added SSH Saved Command input integration with lazy repository observation, repository-order presentation, stable-identifier selection, exact command-input replacement, cancellation, retry, later-failure preservation, and no automatic execution.
- Added an inline Saved Command selector adjacent to the multiline SSH command input while retaining manual editing and the explicit Run action.
- Added focused selector ViewModel coverage and five passing targeted Compose instrumentation tests on the Pixel 9 Android Virtual Device.
- Added ADR-015 for the platform-neutral remote systems product direction and evidence-based support claims.
- Added ADR-016 for the three-level Core, Capability Gateway, and Provider/Adapter remote capability architecture.
- Added an evidence-bound Architecture Atlas covering current ownership, dependencies, navigation, persistence, SSH lifecycles, Saved Commands integration, CI, release, and documentation topology.
- Added an engineering-handbook entry point and immutable versioned architecture-review structure governed by Issue `#135`.
- Added architecture review `RA-2026.07-v2` for Server identity, endpoint lifecycle, profile-layer ownership, operational UX, persistence, security, retention, platform/capability evidence, freshness, and invalidation under Issue `#138` and PR `#139`.
- Added evidence-backed decision recommendations for the Server domain and operational UX, including ADR and bounded follow-up requirements without authorizing production implementation.

### Changed

- Consolidated legacy root documentation governance and process files into explicit compatibility pointers while preserving the current documentation, development, architecture, security, and package-ownership sources of truth.
- Updated the core Android build compatibility cluster to Gradle `9.6.1` and Android Gradle Plugin `9.4.0` while retaining Kotlin `2.4.10` and KSP `2.3.10`.
- Generalized the Add Server helper copy so the inventory workflow no longer implies Linux-only server support.
- Updated Android Gradle Plugin from `9.3.0` to `9.3.1` and validated unit tests, lint, and debug assembly with Gradle `9.5.0`.
- Replaced destructive existing-Server Room `REPLACE` persistence with non-destructive `@Upsert` semantics, preserving SSH trust and connection-history rows across Server updates while retaining explicit Server-delete cascades, without changing Room schema version `5`.
- Tightened ADR governance so ordinary implementation defaults to no new ADR, accepted ADRs remain immutable decision records, and implementation plans, test evidence, benchmarks, and current-state reporting stay in their appropriate living documents.
- Synchronized project state, documentation governance, development process, release process, README, and related document indexes with the new policy and current technical baseline.
- Clarified that the pinned Android NDK currently supports the verified release workflow and required `llvm-strip`, even though project-owned C or C++ source is not currently documented.
- Clarified that proposed dependency or toolchain versions are not current until implemented, validated, documented, and merged.
- Clarified that toolchain changes capable of affecting a pending release artifact invalidate existing candidate evidence and require complete rebuild and verification.
- Marked version 0.5.0 Operations as in progress while retaining Android application metadata at the released version 0.4.0 baseline.
- Replaced the incomplete package-structure document with a factual current baseline covering Dashboard, Server Inventory, SSH, and the new `savedcommands` boundary.
- Updated the Server Toolkit Room database from version `4` to version `5` without destructive fallback.
- Synchronized README, project state, roadmap, changelog, documentation governance, package structure, and Saved Commands status with the implemented persistence foundation.
- Refined the product direction from Linux-specific administration to platform-neutral remote systems operations without claiming universal platform support.
- Defined separate architecturally permitted, implemented, and verified support states.
- Defined Core, Capability Gateway, and Provider/Adapter responsibilities and inward dependency direction for gateway-backed remote capabilities.
- Reframed named services and vendors as optional future integrations rather than committed Core roadmap direction.
- Synchronized README, Product Vision, Architecture, Engineering Strategy, Project State, Roadmap, changelog, and the ADR index with ADR-015 and ADR-016.
- Corrected stale architecture documentation to record Room database version `5` and the implemented Saved Commands persistence foundation.
- Updated the core Android build cluster to Gradle `9.5.0`, Android Gradle Plugin `9.3.0`, Kotlin `2.4.10`, and KSP `2.3.10`.
- Regenerated the Gradle Wrapper, removed the obsolete `android.disallowKotlinSourceSets=false` compatibility option, and aligned tracked IDE language and bytecode metadata with Java `17`.
- Added an explicit `Any?` bind-argument type in the migration test for Kotlin `2.4.10` compatibility without changing migration behavior.
- Manually verified Saved Command persistence after restart, exact command-text storage, and confirmed deletion after a second restart on a physical Android device.
- Synchronized Saved Commands status, project state, roadmap, changelog, package structure, README, and architecture documentation with the implemented management workflow.
- Updated the SSH command field from single-line to bounded multiline presentation so exact persisted multiline commands remain visible and editable.
- Updated AndroidX JUnit from `1.1.5` to `1.3.0` and Espresso Core from `3.5.1` to `3.7.0` to align Compose instrumentation with the current Android emulator runtime.
- Synchronized Saved Commands status, SSH status, project state, roadmap, changelog, README, architecture, and package structure with the implemented SSH input integration.
- Registered the Architecture Atlas and published-review roles in documentation governance and primary documentation navigation.
- Corrected stale Project State, SSH Status, and Release Process claims against evidence baseline `0135faf89b1035fd91c75b37a25ec51bc7c71074`.
- Synchronized Server Inventory current-state documentation with implemented per-server SSH connection history and the verified existing-Server `REPLACE` defect tracked by Issue `#140`.
- Synchronized Project State with accepted review `RA-2026.07-v2`, bounded defect handoff, completed decision synthesis, final documentation-integrity validation, and successful Android Validation run `#88` on substantive head `0af71c70133e8fd27277ef50cf4b801fd0c3a618`.
- Published architecture review `RA-2026.07-v2` through a metadata-only follow-up after acceptance PR `#139` was squash-merged as `8070830dfae14f908b9dd128846f66112b36423e` and the merge passed Android Validation run `#90`; no review conclusions, production behavior, Room schema, navigation, or support claims changed.
- Corrected the stale milestone names in the Development Process release roadmap so versions `0.7.0`, `0.8.0`, and `1.0.0` match the current Roadmap and Release Process documents.

### Fixed

- Fixed SSH connected-session cancellation and timeout handoff so an authenticated session that is registered but not safely delivered is removed from application ownership and best-effort closed without replacing the primary cancellation or timeout outcome.
- Fixed SSH command-output backpressure and unbounded retention by draining stdout/stderr concurrently, retaining at most `256 KiB` per stream with explicit truncation, and keeping channel completion plus stream draining inside the command operation timeout/cancellation boundary.
- Fixed SSH host-key fingerprint compatibility by using canonical OpenSSH SHA-256 over the SSH public-key wire representation for new observation and trust while preserving explicit verification of historical SSHJ MD5 and Java-encoded SHA-256 records without silent rewrite or Room schema migration.

### Not Changed

- Java source and target compatibility and the Kotlin JVM toolchain remain on `17`; Gradle daemon JVM criteria remain on `21`.
- Android compile SDK, target SDK, minimum SDK, Build Tools, NDK, application version metadata, and application-library versions remain unchanged by the core build-cluster update.
- Android `versionName` and `versionCode` remain unchanged.
- No Saved Command categories, favorites, templates, variables, server assignment, automatic execution, background execution, synchronization, backup, or credential storage was added.
- No Android production code, package hierarchy, Capability Gateway implementation, Provider registry, platform detection, transport, monitoring, service-management workflow, or named-service integration was added by the architecture decision.
- The immutable version 0.4.0 release tag, artifacts, checksums, and release evidence remain unchanged.

---

## [0.4.0] - 2026-07-14

### Added

- Added documentation governance rules for version metadata, source-of-truth ordering, changelog usage, and ADR documentation boundaries.
- Added AI change-impact workflow rules for scoped implementation, affected-file review, validation, and documentation synchronization gates.
- Added GitHub Actions validation for Kotlin compilation, Android test compilation, unit tests, lint, and debug builds.
- Added ADR-012 for Android backup and data extraction policy.
- Added ADR-013 for the accepted ephemeral SSH private-key authentication boundary.
- Added ADR-014 for Android release signing identity, repository-external secret handling, artifact verification, and signing-key recovery requirements.
- Added a fail-closed local post-build Android APK signing workflow using `zipalign`, `apksigner`, `aapt2`, and Android Build Tools `36.1.0`.
- Added repository-controlled non-secret release metadata and the accepted public signing-certificate SHA-256 fingerprint.
- Added a validation mode that performs complete signing and verification while deleting the signed validation artifact afterward.
- Added official-mode guards for clean `main`, source-commit alignment, external signing material, recovery-readiness attestation, artifact overwrite protection, certificate identity, application metadata, alignment, checksum generation, and release evidence.
- Verified the project release signing identity and an independently protected recovery copy without recording private recovery locations or signing credentials in the repository.
- Added the project-owned one-shot SSH private-key source with atomic lifecycle transitions, bounded reading, stable failures, scoped cleanup, and focused unit coverage.
- Added the Android SSH private-key content factory with cancellable descriptor opening, joint descriptor-stream ownership, and instrumentation coverage.
- Added ephemeral SSH private-key document selection with immediate source conversion, private ViewModel ownership, lifecycle invalidation, configuration-safe secret clearing, one-attempt transfer coverage, and manual Android workflow verification.
- Added end-to-end ephemeral SSH private-key authentication with in-memory SSHJ parsing for encrypted and unencrypted OpenSSH v1 Ed25519 and RSA keys, without temporary private-key files or persistent secret storage.
- Added preflight validation of encrypted OpenSSH v1 bcrypt KDF metadata with a maximum accepted work factor of `64` rounds before SSHJ parsing, backed by boundary tests and Android benchmark evidence.
- Added stable private-key source, format, passphrase, authentication-rejection, cancellation, cleanup, and unexpected-failure mapping with focused automated coverage.
- Added Android runtime verification of the supported OpenSSH matrix and stable unsupported-format handling for tested PKCS#8 RSA keys.
- Added Hilt-enabled application setup.
- Added Dashboard ViewModel.
- Added Dashboard UI state.
- Added Dashboard empty-state screen.
- Added app-level Navigation Compose infrastructure.
- Added Dashboard navigation destination.
- Added Server Inventory navigation destination.
- Added Server Inventory domain model.
- Added Server Inventory environment model.
- Added Server Inventory filter state.
- Added Server Inventory UI state.
- Added Server Inventory ViewModel.
- Added Server Inventory empty screen.
- Added Server Inventory empty-state action.
- Added Add Server navigation destination.
- Added Add Server placeholder route.
- Added Add Server placeholder screen.
- Added shared Server Form UI state.
- Added Add Server ViewModel.
- Added Add Server form fields.
- Added Add Server validation state.
- Added Add Server validation-only save behavior.
- Added Add Server repository-backed in-memory save behavior.
- Added Add Server automatic return after successful save.
- Added Edit Server navigation destination.
- Added Edit Server route.
- Added Edit Server ViewModel.
- Added SSH workflow architecture decision.
- Added secure storage strategy decision.
- Added SSH client library selection decision.
- Added SSHJ dependency through the Gradle version catalog.
- Added SSHJ connection service shell.
- Added SSH connection service dependency injection binding.
- Added SSH user-triggered connect event shell.
- Added SSH placeholder Connect button.
- Added SSH connect event shell ViewModel tests.
- Added ADR-009 for SSH host trust and authentication input strategy.
- Added SSHJ adapter shell unit test.
- Added SSH domain connection request model.
- Added SSH domain connection result model.
- Added SSH domain connection error model.
- Added SSH domain connection service contract.
- Added SSH domain model unit tests.
- Added test-only fake SSH connection service.
- Added SSH ViewModel fake result unit tests.
- Added SSH UI connection status model.
- Added SSH connection result UI mapper.
- Added SSH presentation state unit tests.
- Added SSH navigation destination.
- Added SSH placeholder screen.
- Added SSH placeholder ViewModel.
- Added SSH placeholder UI state.
- Added Connect action from Server Inventory to the SSH placeholder.
- Added Server repository contract.
- Added in-memory Server repository implementation.
- Added Server Inventory repository dependency injection binding.
- Added basic Server Inventory list rendering.
- Added Server Inventory delete action with confirmation dialog.
- Added Server Inventory search and filtering controls.
- Added Server Inventory filter matcher unit tests.
- Added Server Inventory stabilization checklist.
- Added Dashboard navigation action to open Server Inventory.
- Added ADR-003 for local persistence with Room.
- Added Room dependencies with KSP compiler configuration.
- Added Room schema export directory configuration.
- Added initial Room schema export.
- Added Server Toolkit Room database class.
- Added Server entity and DAO.
- Added Server entity/domain mapper.
- Added Room-backed Server repository implementation.
- Added Hilt providers for the Room database and Server DAO.
- Added Room test helper dependency.
- Added Server DAO instrumentation tests.
- Added Room-backed Server repository instrumentation tests.
- Added Server entity/domain mapper unit tests.
- Added Server Inventory UI-state regression coverage for deleting the final server while a filter remains active.
- Added core connection target-resolution contract.
- Added Server Inventory-backed connection target resolver.
- Added SSH ViewModel target-resolution tests.
- Added SSH connection attempt use case with timeout, exception mapping, and cancellation preservation.
- Added SSH connection history domain model and repository contract.
- Added Room-backed SSH connection history persistence.
- Added failure-contained automatic SSH connection history recording for resolved connection attempts.
- Added SSH connection history recording coverage for connected, failed, timed-out, cancelled, unrecorded, and persistence-failure paths.
- Added per-server SSH connection history destination, screen, ViewModel, UI state, and mapper.
- Added repository-backed loading, error, empty, and newest-first connection history presentation.
- Added connection history mapper and ViewModel tests plus manual runtime navigation verification.
- Added SSH duplicate-attempt prevention at the ViewModel boundary.
- Added SSH failure-containment unit tests.
- Added SSH host trust domain models.
- Added SSH trusted host storage contract.
- Added SSH host trust evaluator and unit tests.
- Added SSH trusted host Room persistence skeleton.
- Added SSH trusted host DAO, mapper, repository, and dependency injection binding.
- Added Server Toolkit database migration from version 1 to version 2 for trusted host keys.
- Added trusted host persistence and migration tests.
- Added SSH host trust decision model.
- Added SSH host trust decision use case.
- Added explicit unknown-host trust confirmation use case.
- Added changed-host-key blocking decision-flow tests.
- Added SSH host-key review presentation state.
- Added SSH host-key review UI mapper and tests.
- Added SSH host-key review confirmation and cancellation ViewModel events.
- Added SSH screen actions for host-key review.
- Added SSH authentication method model.
- Added UI-safe SSH authentication input state without secret values.
- Added ViewModel events for ephemeral authentication input.
- Added authentication input clearing tests.
- Added credential-bearing SSH connection request boundary.
- Added redacted SSH authentication input request model.
- Added authentication input clearing at the connection attempt boundary.
- Added SSH host-key observation service boundary.
- Added SSH connection attempt outcome model for connection results and host-trust decisions.
- Added host-trust gating before SSH connection service execution.
- Added SSHJ host-key observation adapter mapping.
- Added tests for observed, unavailable, and failed SSHJ host-key observation mapping.
- Added project-owned SSH session handle model.
- Added SSH session close result model.
- Added SSH session lifecycle service contract.
- Added SSHJ session lifecycle shell.
- Added deterministic active-session cleanup before permanent SSH workflow exit.
- Added cleanup-before-navigation orchestration for back, system-back, and connection-history navigation.
- Added explicit user-facing SSH disconnect through the existing lifecycle boundary, with shared close orchestration, duplicate-close suppression, command-execution blocking, reconnect support, focused regression coverage, and Android runtime verification.
- Added focused SSH session-close lifecycle and presentation regression coverage.
- Added SSHJ session owner registry boundary.
- Added SSHJ authentication adapter mapping.
- Added SSHJ authentication executor boundary.
- Added SSHJ trusted host-key verifier boundary.
- Added SSHJ trusted connection execution shell.
- Added SSH trusted connection cancellation cleanup-failure regression coverage.
- Added SSHJ password authentication execution shell.
- Added SSHJ session ownership execution shell.
- Added SSH command channel execution strategy decision.
- Added SSH credential ownership and secure storage strategy decision.
- Added SSH command execution planning boundary.
- Added Engineering Strategy documentation for standards-aligned, business-roadmap-first, shortest-safe-job-first development.
- Added current SSH architecture guardrails to the project state documentation.
- Added SSH command execution result model.
- Added SSHJ command channel planning shell.
- Added SSH command execution routing through the data-layer session owner registry.
- Added SSHJ command channel lifecycle executor.
- Added SSH command execution service contract.
- Added SSHJ-backed command execution service and dependency injection binding.
- Added SSH command execution use case.
- Added SSH command timeout stream-suppression regression coverage.
- Added SSH command cancellation cleanup-failure regression coverage.
- Added SSH command execution presentation state and UI mapper.
- Added SSH failure UI mapper state coverage.
- Added SSH ViewModel command execution wiring through active project-owned session handles.
- Added SSH command input and Run command UI controls.
- Added SSH command output rendering for stdout, stderr, and exit status.

### Changed

- Pinned Android NDK `28.2.13676358` and made release signing fail closed when the required NDK, `llvm-strip`, or Gradle NDK declaration is unavailable or inconsistent.
- Replaced the deprecated Android test-assets source-set API while retaining the required built-in Kotlin and KSP compatibility setting.
- Synchronized security, release-process, project-state, changelog, and operator documentation with the implemented Android release signing workflow.
- Clarified ADR ownership and relationships with a decision-boundary map and aligned ADR-006 references with its canonical title without changing accepted decisions.
- Strengthened AI project-context reconstruction rules and synchronized AI Memory with the current documentation source-of-truth hierarchy.
- Synchronized current-state SSH planning with accepted ADR-013 before private-key implementation began.
- Synchronized current-state, roadmap, README, changelog, and ADR implementation-outcome documentation with verified ephemeral private-key authentication.
- Synchronized current-state documentation with the complete SSH connection history domain, persistence, recording, and presentation workflow.
- Synchronized SSH current-state and review documentation after completing the current timeout, cleanup, cancellation, and failure-mapping hardening coverage pass.
- Synchronized SSH current-state documentation with trusted-host cascade deletion, duplicate host-key confirmation protection, trusted-host accepted copy, and current non-interactive command execution behavior.
- Changed SSH workflow-exit navigation to wait for active-session cleanup and remain on the route when cleanup cannot complete.
- Added deterministic disconnecting, close-failure retry, duplicate-exit suppression, and stale command-output cleanup behavior.
- Synchronized project state, SSH status, roadmap, changelog, and engineering review documentation with runtime-verified workflow-exit cleanup.
- Synchronized README and Documentation Governance indexes with the focused `docs/state/` documents.
- Split detailed project state into focused current-state documents for Server Inventory and SSH while keeping `PROJECT_STATE.md` as the source-of-truth entry point.
- Synchronized architecture and README documentation with the accepted SSH credential ownership, Android backup policy, host trust persistence, and ephemeral password SSH connection status.
- Synchronized Android application `versionName` with the current project milestone.
- Changed SSH host-key fingerprint generation from MD5 to SHA256 for host-key observation and trusted verification.
- Removed unused SSH authentication username presentation state because SSH username ownership belongs to inventory-backed connection target resolution.
- Stabilized SSH command execution UI state when an active SSH session becomes unavailable.
- Updated the SSH screen from placeholder-only connection UI to include non-interactive command execution controls.
- Disabled Android backup and data extraction for the alpha release to protect infrastructure inventory metadata.
- Completed explicit backup and transfer exclusions across all supported app storage domains.
- Replaced SSH placeholder connection metadata with resolved inventory-backed connection targets.
- Moved SSH connection attempt orchestration from the ViewModel into a dedicated domain use case.
- Updated the Server Toolkit database schema to version 2 for SSH trusted host keys.
- Aligned package structure with the current implementation.
- Replaced obsolete `feature/servers` package scaffolding with `feature/serverinventory`.
- Removed obsolete placeholder navigation packages.
- Renamed the shared Add/Edit form state from Add Server naming to Server Form naming.
- Renamed the shared Add/Edit form screen from Add Server naming to Server Form naming.
- Reviewed Server Inventory package naming and kept `feature/serverinventory` as the current implemented feature boundary.
- Documented that a broader `inventory` package should wait until non-server asset types are implemented.
- Added an explicit Server Inventory stabilization gate before SSH implementation.
- Accepted the Server Inventory 0.3.0 baseline as stable enough to support SSH workflow design.
- Updated architecture documentation to reflect the current implemented inventory behavior.
- Updated package structure documentation to reflect current Server Inventory files.
- Updated package structure documentation to reflect app-level navigation.
- Updated Dashboard empty-state copy to point users toward Server Inventory.
- Improved Server Inventory screen structure by extracting shared centered content and message content.
- Clarified Server Inventory UI state semantics for inventory-empty and filter-empty states.
- Fixed inventory-empty rendering after deleting the final server while a filter remains active.
- Connected Server Inventory empty-state action to the Add Server placeholder route.
- Connected Server Inventory ViewModel to the Server repository.
- Connected Add Server save behavior to the Server repository.
- Connected Server Inventory delete behavior to the Server repository.
- Connected Server Inventory filter state to visible list rendering.
- Connected Server Inventory edit behavior to the existing Server repository contract.
- Reused the shared Server Form implementation for Add Server and Edit Server routes.
- Injected the SSH connection service contract into the SSH ViewModel.
- Updated SSH ViewModel tests for connection service injection.
- Updated the test-only fake SSH connection service for event-state verification.
- Updated the ADR index to include accepted application architecture, Room persistence, navigation, dependency injection, SSH, secure storage, and SSH client selection decisions.
- Clarified the project state after the accepted Room persistence decision.
- Switched the production Server repository binding from the in-memory implementation to the Room-backed implementation.
- Updated architecture and package structure documentation for the Room persistence skeleton.
- Updated project state documentation after adding persistence tests.
- Updated project state documentation after adding mapper tests.
- Updated project state documentation after adding delete behavior.
- Updated project state documentation after adding search and filtering behavior.
- Updated project state documentation after adding edit behavior.
- Updated project state documentation after renaming the shared server form.
- Updated project state documentation after adding the Server Inventory stabilization checklist.
- Updated project state and roadmap after accepting the Server Inventory 0.3.0 baseline.
- Updated project state documentation after adding the SSH placeholder.
- Updated project state documentation after selecting the SSH client library.
- Updated project state documentation after adding the SSHJ dependency.
- Updated project state documentation after adding SSH domain contracts.
- Updated project state documentation after aligning SSH UI state with domain results.
- Updated project state documentation after adding fake SSH service tests.
- Updated project state documentation after adding the SSHJ adapter shell.
- Updated project state documentation after binding the SSH connection service.
- Updated project state documentation after adding the SSH connect event shell.
- Updated project state documentation for SSH trust and authentication planning.
- Accepted ADR-009 and documented the implementation gates required before real SSH behavior.
- Updated architecture documentation to include the accepted SSH-related ADRs and implemented SSH shell boundaries.
- Replaced stale current branch tracking with stable Git workflow context.
- Recorded successful manual verification of the delete flow after application restart.
- Recorded successful manual verification of search and filtering behavior.
- Recorded successful automated verification after search and filtering implementation.
- Recorded successful automated and manual verification after Edit Server implementation.
- Recorded successful automated verification after shared Server Form naming cleanup.
- Recorded successful manual and automated verification after SSH placeholder implementation.

### Fixed

- Serialized SSH session close cleanup and command execution at the SSHJ session-owner boundary.
- Improved SSH connection and command execution failure-state messages with specific non-interactive SSH guidance.
- Clarified blank SSH command idle-state behavior and prevented blank command execution from reaching session validation.
- Prevented SSH command text changes while command execution is running.
- Disabled the SSH command input while command execution is running.
- Cleared stale SSH command stdout, stderr, and exit status when the active session is invalidated.
- Ignored stale SSH command results that complete after the active session has changed or become unavailable.
- Ignored stale SSH command cancellation updates after the active session has changed or become unavailable.
- Preserved SSH command channel cancellation semantics while keeping command-channel cleanup.
- Preserved SSH session close cancellation semantics while keeping failed-close containment.
- Preserved SSH password authentication cancellation semantics while keeping authentication failure mapping.

### Removed

- Removed obsolete `feature/servers` source package placeholders.
- Removed obsolete `core/navigation` placeholder.
- Removed obsolete Add Server-specific form screen file after introducing the shared Server Form screen.
- Removed obsolete Add Server-specific form UI state after introducing the shared Server Form UI state.
- Removed redundant `.gitkeep` files from packages that now contain implementation files.

### Not Added

The following items are intentionally not implemented yet:

- Interactive terminal workflow.
- Saved command history.
- Persistent sensitive credential storage.
- Monitoring workflow.
- Xray or x-ui management workflow.
- Room migrations beyond database version 4.
- Migration tests beyond the trusted-host v1-to-v2, trusted-host v2-to-v3, and connection-history v3-to-v4 migrations.

---

## [0.1.0] - 2026-07-01

### Added

- Initial Android application skeleton.
- Initial application package structure.
- Initial project documentation structure.
- Initial architecture documentation.
- ADR process.
- Development workflow documentation.
- AI collaboration rules.
- Engineering memory document.
