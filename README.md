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
- Ephemeral password-based SSH connections.
- Project-owned SSH session management.
- User-facing non-interactive SSH command execution workflow.
- SSH command output display for stdout, stderr, and exit status.
- Per-server SSH connection history presentation backed by automatic Room recording.

## Planned Direction

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

---

## Project Status

Current Version:

```
v0.4.0-alpha (Development)
```

---

## Documentation

Project documentation can be found inside the **docs** directory.

- PROJECT_STATE.md
- DOCUMENTATION.md
- state/SERVER_INVENTORY_STATUS.md
- state/SSH_STATUS.md
- DEVELOPMENT.md
- ARCHITECTURE.md
- ROADMAP.md
- CHANGELOG.md

---

## License

MIT License