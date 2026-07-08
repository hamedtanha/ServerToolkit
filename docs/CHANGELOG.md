# Changelog

All notable changes to this project are documented in this file.

The project follows Conventional Commits and Semantic Versioning principles.

---

## [Unreleased]

### Added

- Added documentation governance rules for version metadata, source-of-truth ordering, changelog usage, and ADR documentation boundaries.
- Added ADR-012 for Android backup and data extraction policy.
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
- Added core connection target-resolution contract.
- Added Server Inventory-backed connection target resolver.
- Added SSH ViewModel target-resolution tests.
- Added SSH connection attempt use case with timeout, exception mapping, and cancellation preservation.
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
- Added SSHJ session owner registry boundary.
- Added SSHJ authentication adapter mapping.
- Added SSHJ authentication executor boundary.
- Added SSHJ trusted host-key verifier boundary.
- Added SSHJ trusted connection execution shell.
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
- Added SSH command execution presentation state and UI mapper.
- Added SSH ViewModel command execution wiring through active project-owned session handles.
- Added SSH command input and Run command UI controls.
- Added SSH command output rendering for stdout, stderr, and exit status.

### Changed

- Synchronized README and Documentation Governance indexes with the focused `docs/state/` documents.
- Split detailed project state into focused current-state documents for Server Inventory and SSH while keeping `PROJECT_STATE.md` as the source-of-truth entry point.
- Synchronized architecture and README documentation with the accepted SSH credential ownership, Android backup policy, host trust persistence, and ephemeral password SSH connection status.
- Synchronized Android application `versionName` with the current project milestone.
- Changed SSH host-key fingerprint generation from MD5 to SHA256 for host-key observation and trusted verification.
- Removed unused SSH authentication username presentation state because SSH username ownership belongs to inventory-backed connection target resolution.
- Stabilized SSH command execution UI state when an active SSH session becomes unavailable.
- Updated the SSH screen from placeholder-only connection UI to include non-interactive command execution controls.
- Disabled Android backup and data extraction for the alpha release to protect infrastructure inventory metadata.
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
- Private-key authentication.
- Monitoring workflow.
- Xray or x-ui management workflow.
- Room migrations beyond database version 2.
- Migration tests beyond the trusted-host v1-to-v2 migration.

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
