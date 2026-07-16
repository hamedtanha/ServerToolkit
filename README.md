# Server Toolkit

Server Toolkit is a modern Android application for structured operations on remote systems.

The product is designed for system administrators, DevOps engineers, infrastructure engineers, network engineers, homelab operators, and other technical users who need organized mobile access to remote-system inventory and operational workflows.

Server Toolkit is not a traditional SSH terminal clone. SSH is currently the verified remote-access capability, but it is one implementation boundary rather than the product identity.

---

## Product Direction

Server Toolkit uses a platform-neutral product model:

- Core application meaning must not depend on one operating system, distribution, shell, service manager, transport, vendor, or infrastructure service.
- Platform- and service-specific behavior belongs behind explicit capability, gateway, provider, and adapter boundaries when a concrete feature requires them.
- Architectural extensibility is not a support claim.
- A platform or capability is described as supported only after implementation and verification evidence exist.
- Specific services and vendors are optional future integrations, not automatic core-roadmap commitments.

The product direction is defined by:

- [ADR-015: Platform-Neutral Remote Systems Product Direction](docs/adr/ADR-015-platform-neutral-remote-systems-product-direction.md)
- [ADR-016: Three-Level Remote Capability Architecture](docs/adr/ADR-016-three-level-remote-capability-architecture.md)

---

## Current Implementation

- Dashboard.
- Server inventory.
- Local Room-backed server persistence.
- Server search and filtering.
- SSH host-key trust review and Room-backed trusted-host persistence.
- Ephemeral password-based and private-key SSH connections.
- Verified OpenSSH v1 Ed25519 and RSA private-key authentication with optional passphrases.
- Project-owned SSH session management.
- Explicit SSH disconnect and reconnection workflow.
- User-facing non-interactive SSH command execution workflow.
- SSH command output display for stdout, stderr, and exit status.
- Per-server SSH connection history presentation backed by automatic Room recording.
- Global Saved Command domain and Room persistence foundation.
- Database version `5` with explicit migrations and exported schemas.
- Repository-defined build-toolchain and dependency maintenance policy.
- Living current-state documentation for the Android, JVM, dependency, CI, and release-toolchain baseline.

### Current Support Boundary

The current implementation and runtime evidence are centered on SSH workflows and tested OpenSSH-compatible targets.

The platform-neutral architecture permits additional target families and transports over time, but this repository does not claim universal Linux, Windows, BSD, network-appliance, cloud-provider, or service-manager support.

---

## Planned Direction

The next accepted product slice is the Saved Commands Management workflow:

- Saved Commands navigation destination and entry point.
- Loading, empty, content, and failure states.
- Create workflow with validation.
- Delete confirmation workflow.
- Persistence verification after application restart.

A later independent slice will allow a user to select a saved command from the SSH workflow, populate the existing command input, and execute it only through the existing explicit Run action.

Future gateway-backed capabilities must be selected through focused planning and must define their Core contract, support states, Gateway responsibility, first Provider or Adapter, security boundary, and verification evidence before implementation begins.

---

## Architecture

Server Toolkit preserves two complementary architectural views.

### Android Application Architecture

```text
UI
 ↓
Presentation
 ↓
Domain contracts and models
 ↑
Data implementations
```

### Remote Capability Architecture

```text
Core capability contract
 ↑
Capability Gateway
 ↑
Provider / Adapter
 ↑
Transport or external system
```

The remote-capability levels are introduced only when a feature requires translation, routing, discovery, normalization, orchestration, or external-provider isolation. Purely local features do not receive unnecessary gateway abstractions.

---

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM
- Navigation Compose
- Room Database
- Hilt
- Coroutines
- Flow
- Material 3

Detailed implemented versions are recorded in [Build Toolchain Status](docs/state/BUILD_TOOLCHAIN_STATUS.md).

---

## Project Status

Current Version:

```text
v0.4.0 (Released)
```

Next Milestone:

```text
v0.5.0-alpha — Operations
```

---

## Documentation

Project documentation can be found inside the **docs** directory.

- [Product Vision](docs/PRODUCT_VISION.md)
- [Project State](docs/PROJECT_STATE.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Engineering Strategy](docs/ENGINEERING_STRATEGY.md)
- [Roadmap](docs/ROADMAP.md)
- [Architecture Decision Records](docs/adr/README.md)
- [Documentation Governance](docs/DOCUMENTATION.md)
- [Build Toolchain and Dependency Policy](docs/BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Build Toolchain Status](docs/state/BUILD_TOOLCHAIN_STATUS.md)
- [Server Inventory Status](docs/state/SERVER_INVENTORY_STATUS.md)
- [SSH Status](docs/state/SSH_STATUS.md)
- [Saved Commands Status](docs/state/SAVED_COMMANDS_STATUS.md)
- [Development Process](docs/DEVELOPMENT.md)
- [Changelog](docs/CHANGELOG.md)
- [Release Process](docs/RELEASES.md)

---

## License

MIT License
