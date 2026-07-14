# Security Policy

**Project:** Server Toolkit
**Version:** 0.4.0
**Status:** Foundational
**Last Updated:** 2026-07-14

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

# Android Release Signing

The Android application signing identity is a long-lived release and supply-chain security boundary governed by ADR-014.

Current implementation:

- Gradle `assembleRelease` produces unsigned technical output and does not require access to release signing secrets.
- Official distributable APKs are produced through the local post-build signing workflow in `scripts/release/sign-android-apk.sh`.
- The release keystore, private signing key, passwords, and recovery locations remain outside the Git repository.
- The accepted public signing-certificate SHA-256 fingerprint is stored in `config/release/android-signing-certificate.sha256`.
- Validation mode exercises the complete signing and verification workflow but deletes the signed validation artifact afterward.
- Official mode requires a clean `main` branch matching `origin/main`, the accepted signing identity, and explicit maintainer confirmation that recovery readiness has been verified.
- The primary release keystore and at least one independently protected recovery copy were verified before first distribution.
- Signed APK verification includes certificate identity, signer count, debug-certificate rejection, application metadata, alignment, and final SHA-256 checksum generation.
- Automated signing in GitHub Actions, GitHub-hosted signing secrets, Google Play distribution, and Play App Signing remain outside the current implementation scope.

Signing passwords, keystore files, private keys, private recovery locations, and operational recovery details must never appear in Git history, documentation, issues, pull requests, logs, or release notes.

---

# Credential Storage

Credentials must never be stored as plain text.

Planned secure storage mechanisms include:

- Android Keystore
- EncryptedSharedPreferences
- Encrypted file storage where appropriate

Credential storage architecture must be documented in an ADR before implementation.

SSH credential ownership is separate from server inventory. Passwords, private keys, private key passphrases, access tokens, certificates, and complete credential-bearing connection strings must not be stored in the server inventory Room table.

Persistent credential storage requires a separate reviewed implementation with a secure storage abstraction before any secret material is saved.

---

# Android Backup and Data Extraction Policy

Android backup and data extraction are disabled for the alpha release.

Current policy:

- Android backup is disabled through the application manifest.
- Cloud backup is excluded for all app-managed data.
- Device transfer is excluded for all app-managed data.
- Infrastructure inventory metadata must not be automatically backed up.
- Trusted SSH host key material must not be automatically backed up.
- Future credential metadata and secret material must not be added to automatic backup.

This policy protects server inventory metadata, trusted host keys, and future credential-related data from unreviewed backup, restore, transfer, or synchronization behavior.

Backup, restore, device transfer, encrypted export, or synchronization support requires a separate reviewed ADR before implementation.

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
- adr/ADR-012-android-backup-and-data-extraction-policy.md
- adr/ADR-014-android-release-signing-strategy.md
- release/ANDROID_RELEASE_SIGNING.md
