# Security Policy

**Project:** Server Toolkit  
**Version:** 0.1.0  
**Status:** Frozen  
**Last Updated:** 2026-07-02

---

# Purpose

This document defines the security principles followed throughout the development of Server Toolkit.

Security is a fundamental project requirement and must be considered during architecture, implementation, testing, and release preparation.

---

# Security Objectives

Server Toolkit is designed to:

- Protect sensitive user data
- Protect server credentials
- Prevent accidental information disclosure
- Minimize attack surface
- Follow Android security best practices
- Fail safely when security-sensitive operations cannot be completed

---

# Security Principles

The project follows these principles:

- Least Privilege
- Defense in Depth
- Secure by Default
- Minimal Exposure
- Fail Securely

---

# Sensitive Information

Sensitive information must never be committed to Git.

Examples include:

- Passwords
- Private keys
- SSH keys
- API tokens
- Access tokens
- Client secrets
- Certificates
- Database dumps
- Real server credentials
- Real production server inventories

---

# Credential Storage

Credentials must never be stored as plain text.

Planned secure storage mechanisms include:

- Android Keystore
- EncryptedSharedPreferences
- Encrypted file storage where appropriate

Credential storage architecture must be documented in an ADR before implementation.

---

# Network Security

All remote communication should use encrypted protocols whenever possible.

Expected protocols include:

- SSH
- HTTPS
- TLS

Unencrypted communication should be avoided unless explicitly justified and documented.

---

# SSH Security

SSH-related functionality must be designed with strict security boundaries.

Required principles:

- Verify host fingerprints before trusting a server.
- Avoid silently accepting unknown hosts.
- Avoid exposing private keys to logs, UI previews, or crash reports.
- Prefer encrypted private key handling.
- Keep authentication logic outside the UI layer.

---

# Authentication

The application may support:

- Password authentication
- Public key authentication

Authentication methods must be implemented only after the secure storage strategy is defined.

---

# Logging Policy

Logs must never contain:

- Passwords
- Tokens
- Private keys
- Session IDs
- Sensitive user data
- Full credential-bearing connection strings

Log messages should contain only information required for debugging and operational diagnosis.

---

# Local Storage

The application should minimize local storage of sensitive information.

Rules:

- Encrypt sensitive data.
- Avoid caching secrets.
- Remove temporary files when they are no longer needed.
- Keep test data separate from real user data.

---

# Third-Party Libraries

Only actively maintained libraries should be used.

Libraries should:

- Receive security updates
- Have an active community
- Be widely adopted or technically justified
- Have a compatible license
- Avoid unnecessary permissions or excessive transitive dependencies

---

# Security Updates

Known vulnerabilities should be fixed as soon as practical.

Dependency updates should be reviewed regularly, especially before release milestones.

---

# Reporting Security Issues

Security issues should not be reported through public GitHub Issues.

Until a public disclosure process exists, security concerns should be communicated privately to the project maintainer.

---

# Future Security Features

The following features are planned candidates and are not guaranteed to exist until implemented:

- Biometric authentication
- Certificate pinning
- Android backup is disabled for the alpha release until a reviewed restore model exists.
- Secure backup
- Encrypted export
- Automatic session lock
- Optional root detection

---

# Security Review Checklist

Before every release verify:

- No secrets committed
- Dependencies reviewed
- Build succeeds
- Sensitive logs removed
- Release configuration verified
- Debug-only behavior disabled in release builds
- Documentation reflects actual security behavior

---

# Document Governance

This document is foundational and frozen.

Changes are allowed only when:

- A new security requirement is accepted.
- A vulnerability or risk requires policy correction.
- Android security guidance materially changes.
- An ADR changes credential, storage, SSH, or network security architecture.

---

# Related Documents

- DEVELOPMENT.md
- ARCHITECTURE.md
- RELEASES.md
- CHANGELOG.md
- PROJECT_STATE.md
