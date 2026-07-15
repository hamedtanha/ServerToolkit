# Server Toolkit

Server Toolkit is a modern Android application for managing Linux servers from a mobile device.

The application is designed for system administrators, DevOps engineers, and infrastructure engineers who need quick access to their servers.

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
- Repository-defined build-toolchain and dependency maintenance policy.
- Living current-state documentation for the implemented Android, JVM, dependency, CI, and release-toolchain baseline.

## Planned Direction

- Saved commands and repeatable operational workflows.
- Ping and latency checks.
- Server status monitoring.
- Favorite servers.
- Xray service management.
- x-ui management.
- Let's Encrypt helper workflows.
- System logs viewer.

---

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM
- Navigation Compose
- Room Database
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

- [Project State](docs/PROJECT_STATE.md)
- [Documentation Governance](docs/DOCUMENTATION.md)
- [Build Toolchain and Dependency Policy](docs/BUILD_TOOLCHAIN_AND_DEPENDENCY_POLICY.md)
- [Build Toolchain Status](docs/state/BUILD_TOOLCHAIN_STATUS.md)
- [Server Inventory Status](docs/state/SERVER_INVENTORY_STATUS.md)
- [SSH Status](docs/state/SSH_STATUS.md)
- [Development Process](docs/DEVELOPMENT.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Roadmap](docs/ROADMAP.md)
- [Changelog](docs/CHANGELOG.md)
- [Release Process](docs/RELEASES.md)

---

## License

MIT License
