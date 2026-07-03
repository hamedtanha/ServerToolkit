# Changelog

All notable changes to this project are documented in this file.

The project follows Conventional Commits and Semantic Versioning principles.

---

## [Unreleased]

### Added

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
- Added Add Server UI state.
- Added Add Server ViewModel.
- Added Add Server form fields.
- Added Add Server validation state.
- Added Add Server validation-only save behavior.
- Added Add Server repository-backed in-memory save behavior.
- Added Add Server automatic return after successful save.
- Added Server repository contract.
- Added in-memory Server repository implementation.
- Added Server Inventory repository dependency injection binding.
- Added basic Server Inventory list rendering.
- Added Dashboard navigation action to open Server Inventory.

### Changed

- Aligned package structure with the current implementation.
- Replaced obsolete `feature/servers` package scaffolding with `feature/serverinventory`.
- Removed obsolete placeholder navigation packages.
- Updated package structure documentation to reflect app-level navigation.
- Updated Dashboard empty-state copy to point users toward Server Inventory.
- Improved Server Inventory screen structure by extracting shared centered content and message content.
- Clarified Server Inventory UI state semantics for inventory-empty and filter-empty states.
- Connected Server Inventory empty-state action to the Add Server placeholder route.
- Connected Server Inventory ViewModel to the Server repository.
- Connected Add Server save behavior to the Server repository.

### Removed

- Removed obsolete `feature/servers` source package placeholders.
- Removed obsolete `core/navigation` placeholder.
- Removed redundant `.gitkeep` files from packages that now contain implementation files.

### Not Added

The following items are intentionally not implemented yet:

- Room persistence.
- Server entity.
- Server DAO.
- Room-backed Add Server persistence workflow.
- Edit server workflow.
- SSH workflow.
- Credential storage.
- Monitoring workflow.

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
