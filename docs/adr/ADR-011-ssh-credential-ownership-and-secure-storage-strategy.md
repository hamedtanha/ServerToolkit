# ADR-011: SSH Credential Ownership and Secure Storage Strategy

**Status:** Accepted

**Date:** 2026-07-06

---

# Context

Server Toolkit now supports server inventory persistence, explicit SSH host trust, ephemeral authentication input, owned SSH sessions, and non-interactive SSH command execution.

Persistent credential storage is still intentionally not implemented.

This decision is required because SSH credentials are not ordinary server metadata. Passwords, private keys, private key passphrases, access tokens, certificates, and future authentication secrets require a separate ownership model and secure storage boundary.

The project must avoid coupling server inventory records to secret material. The server inventory table stores operational metadata such as name, host, port, environment, favorite state, category, tags, description, and a non-sensitive username hint. It must not become a credential store.

The current SSH workflow may accept authentication input for a connection attempt, but credential values must remain ephemeral unless a reviewed secure storage implementation is introduced.

---

# Decision

Server Toolkit will treat SSH credentials as a separate security-owned concept from server inventory.

Persistent SSH credential storage must not be implemented by adding password, private key, passphrase, token, certificate, or similar secret fields to `Server`, `ServerEntity`, Room server tables, UI state snapshots, saved state, logs, or diagnostic messages.

## Server Inventory Ownership

Server inventory owns non-sensitive connection metadata.

Server inventory may store:

- Server name.
- Host or IP address.
- SSH port.
- Non-sensitive username hint.
- Environment.
- Category.
- Tags.
- Favorite state.
- Description.

Server inventory must not store:

- Passwords.
- Private keys.
- Private key passphrases.
- Access tokens.
- Certificates.
- Complete credential-bearing connection strings.
- Any other secret authentication material.

The existing `sshUsername` field remains a non-sensitive login hint, not a credential ownership boundary.

## Credential Ownership

A future persistent credential feature must introduce a separate credential model.

The credential model should represent metadata such as:

- Credential identifier.
- Display name.
- Username.
- Authentication method.
- Optional secret reference.
- Creation timestamp.
- Update timestamp.

The credential model must not require secret material to be stored in plain Room tables.

A server may later reference a credential profile by identifier, but the server record must not own the credential secret.

## Secret Storage Boundary

Secret material must be stored only through a dedicated secure storage abstraction.

The secure storage abstraction must isolate secret reads, writes, deletion, and error mapping from UI, domain models, Room entities, and general repositories.

The preferred future implementation direction is:

- Room for credential metadata only.
- Android Keystore-backed encrypted storage for secret material.
- Explicit deletion of orphaned secrets when credential metadata is removed.
- Tests covering secret redaction, deletion behavior, and failure mapping.

## Ephemeral Authentication

The current ephemeral authentication input workflow remains valid.

Authentication input may be collected for a single connection attempt and held only for the minimum duration required to attempt authentication.

Credential values must be cleared after the connection attempt completes, fails, is cancelled, or moves into a host-key review flow.

Credential values must not be exposed through `StateFlow`, saved state, logs, crash-safe diagnostics, or string representations.

## Implementation Gate

Persistent credential storage may be implemented only after a reviewed implementation slice adds:

- A credential metadata model.
- A secure secret storage abstraction.
- A secure storage implementation.
- Deletion and cleanup behavior.
- Tests for redaction, lifecycle clearing, persistence behavior, and failure containment.
- Documentation updates for architecture, security, roadmap, and project state.

---

# Alternatives Considered

## Store Credentials Directly in Server Inventory

Add password, private key, or passphrase fields to the server inventory model and Room table.

### Pros

- Fast implementation.
- Simple form integration.
- Minimal new abstractions.

### Cons

- Stores secrets in the wrong bounded context.
- Expands the Room database security surface.
- Couples inventory metadata to credential lifecycle.
- Makes future secure storage migration harder.
- Increases the risk of logging, backup, export, and diagnostic leakage.

Rejected.

---

## Store Credential Metadata in Room and Secret Material in Secure Storage

Represent persistent credentials through a separate credential profile while storing secret material through a dedicated secure storage abstraction.

### Pros

- Separates inventory metadata from authentication secrets.
- Supports future credential reuse across servers.
- Keeps Room focused on metadata.
- Enables Android Keystore-backed protection.
- Supports explicit deletion and lifecycle management.

### Cons

- Requires more implementation work.
- Requires additional tests.
- Requires careful backup, migration, and deletion rules.

Accepted as the future persistent credential direction.

---

## Keep Only Ephemeral Authentication Forever

Never persist credential metadata or secret material.

### Pros

- Small security surface.
- No long-term secret storage risk.
- Simple current implementation.

### Cons

- Poor user experience for repeated operations.
- Limits practical infrastructure management workflows.
- Does not support reusable credential profiles.

Rejected as the long-term strategy, but accepted for the current implementation stage.

---

# Consequences

## Positive

- Server inventory remains a metadata boundary.
- Credential ownership becomes explicit.
- Secret material cannot be added casually to Room entities.
- Future credential persistence has a clear implementation gate.
- The current ephemeral authentication workflow remains compatible with future secure storage.
- The architecture remains aligned with MVVM, repository boundaries, dependency inversion, and security documentation.

## Negative

- Persistent credentials remain unavailable until a secure storage implementation is added.
- Users must continue entering authentication input for connection attempts.
- Future credential UI requires additional design and tests.

---

# Follow-up Work

- Introduce a credential metadata model when persistent credential profiles become part of the roadmap.
- Introduce a secure secret storage abstraction before storing any secret material.
- Define backup and data extraction behavior for credential metadata and secret storage.
- Add tests for credential redaction, clearing, deletion, and failure handling.
- Update the server form only after the credential ownership model is implemented.
