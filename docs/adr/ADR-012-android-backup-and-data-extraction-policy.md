# ADR-012: Android Backup and Data Extraction Policy

**Status:** Accepted
**Date:** 2026-07-07
**Related Milestone:** Version 0.4.0-alpha — SSH

---

## Context

Server Toolkit is an infrastructure management application.

The application stores operational server inventory metadata locally, including hostnames, usernames, environments, categories, tags, descriptions, and favorite state.

The application also persists trusted SSH host key material as part of the explicit SSH host trust workflow.

Persistent SSH credentials are intentionally not implemented. Passwords, private keys, private key passphrases, tokens, certificates, and complete credential-bearing connection strings must remain outside server inventory and must not be stored without a separate secure storage implementation.

Even without stored credentials, infrastructure inventory metadata and trusted host key material are security-sensitive. Automatic Android backup, cloud backup, and device transfer could expose or restore data without a reviewed trust, deletion, migration, or recovery model.

This decision is required because Android backup behavior is part of the application's security boundary.

---

## Decision

Server Toolkit will disable Android backup and data extraction for the alpha release.

The application must keep:

- Android backup disabled.
- Cloud backup disabled.
- Device transfer disabled.
- Infrastructure inventory metadata excluded from automatic backup.
- Trusted SSH host key material excluded from automatic backup.
- Future credential metadata and secret material excluded unless a later ADR explicitly accepts a secure backup and restore model.

The current implementation uses a defense-in-depth policy:

- `android:allowBackup` is disabled in the application manifest.
- `backup_rules.xml` excludes all app-managed data.
- `data_extraction_rules.xml` excludes all app-managed data from cloud backup and device transfer.

A future change that enables backup, restore, device transfer, credential synchronization, encrypted export, or cross-device recovery requires a separate ADR.

---

## Alternatives Considered

### Keep Android Backup Enabled

Allow Android to back up app-managed data using the platform defaults.

#### Pros

- Convenient for users during device replacement.
- Minimal implementation effort.
- Uses platform-supported behavior.

#### Cons

- May expose infrastructure inventory metadata.
- May back up trusted SSH host key material without a reviewed restore model.
- Creates future risk for credential metadata if credential persistence is later added.
- Makes security behavior dependent on platform defaults instead of explicit project policy.

Rejected.

---

### Exclude Only Known Sensitive Files

Keep backup enabled but exclude specific files such as databases, credential stores, or trusted host key tables.

#### Pros

- More flexible than disabling all backup behavior.
- Could allow harmless preferences to be backed up later.
- Provides a path toward selective backup.

#### Cons

- Easy to miss newly added sensitive storage locations.
- Requires continuous audit of Room tables, DataStore, files, caches, and future secure storage.
- Still lacks a reviewed restore model for trusted host keys and credential metadata.
- Adds complexity before the alpha security model is stable.

Rejected for the alpha release.

---

### Disable Backup and Data Extraction

Disable Android backup and explicitly exclude all app-managed data from backup and device transfer.

#### Pros

- Secure default for an infrastructure management application.
- Minimizes accidental disclosure of server inventory metadata.
- Protects trusted host key material from unreviewed restore behavior.
- Keeps future credential storage from inheriting unsafe backup behavior.
- Simple to reason about during alpha development.

#### Cons

- Users cannot automatically restore app data through Android backup.
- Device migration remains unsupported until a secure migration model exists.
- Future secure backup requires a separate implementation.

Accepted.

---

## Consequences

### Positive

- The alpha release has a conservative backup and restore security boundary.
- Server inventory metadata is not automatically backed up.
- Trusted SSH host key material is not automatically backed up.
- Future credential persistence remains protected from accidental platform backup behavior.
- Any future backup, restore, transfer, export, or synchronization feature must be explicitly reviewed.

### Negative

- Android auto-backup does not preserve Server Toolkit data.
- Users must recreate local inventory if the app is removed or the device is replaced.
- Secure backup and migration remain future work.
- The project must document this limitation clearly before release.

---

## Implementation Requirements

The application must keep the following behavior until superseded by a later ADR:

- `android:allowBackup` remains `false`.
- `backup_rules.xml` excludes all app-managed data.
- `data_extraction_rules.xml` excludes all app-managed cloud backup data.
- `data_extraction_rules.xml` excludes all app-managed device-transfer data.
- No credential metadata or secret material may be added to automatic backup.
- No trusted host key restore behavior may be introduced without review.
- No encrypted export, secure backup, or synchronization feature may be added incidentally.

---

## Future Considerations

A future secure backup or migration feature may be considered only after the project defines:

- User consent requirements.
- Backup encryption model.
- Restore validation behavior.
- Trusted host key restore semantics.
- Credential metadata handling.
- Secret storage handling.
- Deletion and revocation behavior.
- Failure and partial-restore handling.
- Tests for backup exclusion and restore safety.

---

## References

- ADR-007: Secure Storage Strategy
- ADR-009: SSH Host Trust and Authentication Input Strategy
- ADR-011: SSH Credential Ownership and Secure Storage Strategy
- SECURITY.md
- PROJECT_STATE.md
- CHANGELOG.md
- RELEASES.md

---

## Notes

This ADR documents the current alpha policy.

It does not introduce secure backup, encrypted export, credential synchronization, or device migration support.
