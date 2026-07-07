# Change Proposal 001: Security and Release Readiness

**Project:** Server Toolkit
**Status:** Accepted for documentation
**Date:** 2026-07-07
**Related Milestone:** 0.4.0-alpha

---

## Context

Server Toolkit has moved beyond the foundation and server inventory milestones and now includes real SSH password connection behavior, explicit host trust handling, owned SSH sessions, and non-interactive command execution.

The project stores infrastructure inventory metadata locally and now also persists trusted SSH host key material.

Although credential persistence remains intentionally out of scope, the application already handles security-sensitive operational metadata such as server hostnames, usernames, tags, descriptions, environment labels, favorites, and trusted host keys.

The repository currently disables Android backup and data extraction for the alpha release. This proposal formalizes that direction and separates security/release-readiness work from lower-priority form completion and UI polish.

---

## Problem

The original technical review identified several valid risks, but parts of it were based on an outdated project state.

The current source-of-truth repository already disables Android backup and data extraction in the application manifest and backup XML rules.

The remaining problem is documentation governance:

- The backup and data extraction behavior exists in implementation.
- The behavior is mentioned in project state and changelog documentation.
- The security policy still treats Android backup partly as a future security feature instead of a current enforced alpha policy.
- No ADR currently records the accepted Android backup and data extraction decision.

Without a dedicated ADR, future work could accidentally re-enable backup, device transfer, or credential synchronization before a reviewed restore model exists.

---

## Decision

Create a dedicated ADR for Android backup and data extraction behavior.

Keep Android backup, cloud backup, and device transfer disabled for the alpha release.

Do not enable automatic backup, restore, device transfer, credential synchronization, or infrastructure inventory export until a separate reviewed design defines:

- What data may be backed up.
- What data must never be backed up.
- How restored trusted host keys are validated.
- How credential metadata and secret material are separated.
- How user consent and recovery behavior work.
- How deletion, migration, and device replacement are handled.

---

## Scope

This proposal includes:

- ADR-012 for Android backup and data extraction policy.
- ADR index update.
- Security policy update.
- Project state synchronization.
- Changelog synchronization.

This proposal does not include:

- Application code changes.
- SSH host trust redesign.
- Credential persistence.
- Secure backup implementation.
- Server form expansion.
- Server inventory UI polish.
- Release build optimization.

---

## Priority

Security and release-readiness work must stay ahead of form expansion and UI polish.

Recommended order:

1. Formalize Android backup and data extraction policy.
2. Keep version and release documentation synchronized.
3. Continue enforcing SSH host trust and credential boundaries.
4. Add secure credential storage only through a separate reviewed implementation slice.
5. Improve Server Inventory form completeness.
6. Improve Server Inventory list polish.
7. Harden release build and CI gates.

---

## Consequences

### Positive

- Prevents accidental backup of infrastructure inventory metadata.
- Preserves the current alpha security boundary.
- Makes future backup, restore, and synchronization work require explicit review.
- Keeps documentation aligned with the current implementation.
- Reduces release-readiness ambiguity.

### Negative

- Users cannot rely on Android auto-backup or device-transfer restore during alpha.
- Device migration remains manual until a secure restore model exists.
- Future secure backup requires a separate design and implementation effort.

---

## Related Documents

- ADR-009: SSH Host Trust and Authentication Input Strategy
- ADR-011: SSH Credential Ownership and Secure Storage Strategy
- ADR-012: Android Backup and Data Extraction Policy
- SECURITY.md
- PROJECT_STATE.md
- CHANGELOG.md
- RELEASES.md
