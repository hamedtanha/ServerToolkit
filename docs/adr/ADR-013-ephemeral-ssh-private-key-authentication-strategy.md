# ADR-013: Ephemeral SSH Private-Key Authentication Boundary

**Status:** Accepted

**Date:** 2026-07-10

**Related Milestone:** Version 0.4.0 — SSH

---

# Context

Server Toolkit currently supports ephemeral password-based SSH authentication, explicit SSH host trust, project-owned SSH session handles, non-interactive command execution, and per-server connection history.

Private-key authentication is not implemented. The current domain model contains a private-key passphrase input shape, while the SSHJ authentication executor deliberately reports private-key authentication as unsupported.

The project now needs a focused decision for ephemeral private-key authentication before implementation begins.

This decision must preserve the accepted boundaries from ADR-007, ADR-008, ADR-009, ADR-011, and ADR-012:

- Server Inventory owns non-sensitive connection metadata only.
- Private keys and passphrases are credential material, not inventory metadata.
- Secret material must not be persisted without a separately reviewed secure-storage implementation.
- SSHJ types and parsing behavior must remain inside the SSH data layer.
- Authentication input must remain ephemeral and must not be exposed through UI state, saved state, logs, crash diagnostics, connection history, or string representations.
- Android backup, cloud backup, and device transfer remain disabled for the alpha release.

A new decision is still required because private-key authentication introduces Android document access, key-source ownership, bounded reading, parsing, optional passphrase handling, cleanup, cancellation, and user-facing failure mapping that are not defined by the existing password workflow.

---

# Decision

Server Toolkit will implement private-key authentication as a one-attempt, non-persistent authentication workflow.

The initial implementation will not import, copy, generate, persist, remember, synchronize, or reuse private keys.

## User Selection Boundary

The user will select one private-key document through the Android system content picker.

The initial implementation will use `ActivityResultContracts.GetContent` with a broad `*/*` MIME filter. `GetContent` provides a `content://` `Uri` that can be opened as a stream and applies openable-content behavior by default.

The picker result is an Android `Uri` and must remain inside the Android integration boundary.

The selected `Uri` must:

- be converted immediately into a project-owned one-shot private-key source;
- not be stored in `StateFlow`, `SavedStateHandle`, Room, preferences, logs, crash diagnostics, connection history, or long-lived application state;
- not be converted into a persistent URI permission;
- become invalid after one connection attempt, cancellation, host-key review transition, or explicit replacement by another selection.

The initial implementation will not use `ActivityResultContracts.OpenDocument` because durable document access is not part of the ephemeral workflow.

Because private-key files do not have one reliable MIME type across providers, file size and content validation remain mandatory after selection.

## Project-Owned One-Shot Source

A project-owned private-key source contract will isolate Android document access from domain and SSHJ implementation details.

An Android-specific factory will convert the picker `Uri` into the project-owned source immediately inside the picker-result callback. Only the project-owned source may cross from Android integration code into SSH presentation and connection-attempt orchestration.

The contract will:

- expose no Android `Uri`, `ContentResolver`, `InputStream`, SSHJ key type, or parser exception outside its implementation boundary;
- allow one bounded read only;
- perform the available-to-consumed or available-to-invalidated transition atomically;
- reject repeated or concurrent consumption;
- close the underlying stream on every success, failure, timeout, and cancellation path;
- return project-owned failure categories;
- support explicit invalidation before consumption.

The Android implementation will open the selected content with `ContentResolver.openAssetFileDescriptor(uri, "r", cancellationSignal)` and read through the returned descriptor.

Coroutine cancellation and timeout must cancel the associated Android `CancellationSignal`. The descriptor and derived stream are application-owned resources and must be closed in every outcome.

`ContentResolver.openInputStream()` must not be used for the cancellable provider-opening path because it has no `CancellationSignal` overload. Cancellation remains best-effort after a stream is open because a provider may ignore cancellation or block inside a read operation.

## Ownership and Transfer

The SSH ViewModel may own at most one pending project-owned private-key source and one optional passphrase in private, non-observable fields.

The pending source and passphrase must not be included in UI state, `StateFlow`, `SavedStateHandle`, navigation arguments, or diagnostic output.

Ownership rules are:

- selecting a new key invalidates the previously pending source before replacement;
- starting a connection attempt transfers the source and passphrase exactly once to connection-attempt orchestration;
- the ViewModel clears its references as part of that transfer and must not retain a second copy;
- connection-attempt orchestration owns cleanup after transfer, including validation failures before SSHJ authentication;
- cancellation or leaving the SSH workflow invalidates any source that has not been transferred.

A configuration change may retain the pending source and passphrase only through the existing ViewModel instance.

Process death must discard the source and passphrase. Restored presentation state must report that no private key or passphrase is selected and must require fresh user input.

## Bounded Key Material

Private-key content will be read only when the authentication attempt consumes the one-shot source.

The initial maximum accepted private-key document size will be `256 KiB`.

This limit is intentionally far above normal private-key document sizes while still bounding memory use and limiting abuse by malformed or hostile content providers. The limit must be implemented as a named configuration constant rather than a scattered literal.

The implementation must:

- perform document access and metadata queries on an I/O dispatcher;
- treat provider-reported size as an optional early rejection signal only;
- enforce the authoritative size limit while streaming because providers may report an unknown or inaccurate size;
- reject empty documents;
- reject documents larger than the configured limit;
- avoid unbounded `readBytes()` behavior;
- bridge coroutine cancellation and timeout to the Android `CancellationSignal` used for provider metadata queries and descriptor opening;
- check cancellation between bounded read operations and close owned descriptors and streams promptly;
- handle providers that fail, block, ignore cancellation, disappear, or change content between metadata lookup and stream reading;
- use a mutable byte buffer for application-owned key material;
- clear application-owned mutable buffers on a best-effort basis after parsing or failure;
- release references promptly.

This decision does not claim guaranteed memory erasure. Kotlin, Java, third-party libraries, and the runtime may create internal copies that the application cannot reliably overwrite. The security objective is minimum lifetime, no intentional persistence, best-effort clearing of owned mutable buffers, and prompt reference release.

## Passphrase Handling

An optional private-key passphrase may be collected for one authentication attempt.

The passphrase must:

- remain outside observable UI state and saved state;
- be represented in UI state only by non-sensitive method and presence flags;
- be redacted from string representations;
- be cleared after success, failure, timeout, cancellation, or host-key review transition;
- never be recorded in connection history or diagnostics.

The implementation should prefer mutable secret representations where library APIs permit them. When a third-party API requires an immutable `String`, the application must keep its lifetime as short as possible and release references promptly.

## Host-Trust Transition

Private-key material and passphrases must not remain retained while the application waits for user confirmation of an unknown host key.

When a connection attempt moves into host-key review:

- the one-shot private-key source must be invalidated;
- any loaded key material must be cleared on a best-effort basis;
- the passphrase must be cleared;
- the user must select the key and provide the passphrase again when starting the trusted connection attempt.

This preserves the existing ADR-011 rule that authentication secrets are cleared when an attempt moves into host-key review.

## SSHJ Boundary

Private-key parsing, key-provider creation, and SSHJ authentication will remain inside the SSH data layer.

The data layer must:

- map SSHJ and parser failures to stable project-owned errors;
- expose no SSHJ key-provider or parser type to domain or presentation code;
- support only key formats verified by automated tests and Android runtime verification;
- report unsupported or malformed formats without exposing sensitive parser details;
- preserve coroutine cancellation;
- close and clean up temporary resources even when authentication fails.

The exact supported key-format matrix must be recorded in the implementation pull request and synchronized into SSH current-state documentation after automated and Android runtime verification.

Because the matrix depends on implementation evidence, it is an implementation acceptance output rather than a prerequisite for accepting this design decision.

## Key-Format Verification Plan

The implementation will use dedicated test-only private keys that have no operational use and are clearly identified as fixtures.

The initial fixture set will include:

- unencrypted OpenSSH Ed25519;
- passphrase-protected OpenSSH Ed25519;
- unencrypted OpenSSH RSA;
- passphrase-protected OpenSSH RSA;
- unencrypted PKCS#8 RSA;
- passphrase-protected PKCS#8 RSA;
- malformed and truncated key documents;
- a valid encrypted key with an incorrect passphrase.

The first implementation must successfully support both unencrypted and passphrase-protected OpenSSH Ed25519 and OpenSSH RSA fixtures.

PKCS#8 outcomes must be tested and documented. PKCS#8 support may be included only when automated tests and Android runtime verification succeed; otherwise it must map to a stable unsupported-format outcome.

The final supported key-format matrix must list only formats that pass automated adapter tests and Android runtime verification. Algorithms and containers outside the verified matrix remain unsupported rather than implicitly accepted.

Empty, oversized, unavailable, cancelled, and hostile-provider cases belong to the one-shot source test plan rather than the cryptographic key-format fixture set.

## Failure Model

The implementation must provide stable project-owned outcomes for at least:

- key document unavailable;
- empty key document;
- key document too large;
- unsupported or malformed key format;
- private-key passphrase required;
- private-key passphrase rejected;
- SSH authentication rejected by the server;
- cancellation;
- unexpected internal failure.

The implementation may combine parser-specific failures when SSHJ cannot distinguish them reliably. User-facing messages must remain actionable without exposing raw exceptions, key content, file-system details, or provider internals.

## Scope Boundary

This decision authorizes design for ephemeral private-key authentication only.

It does not authorize:

- persistent credential profiles;
- copying private keys into app-private storage;
- storing persistent URI permissions;
- private-key generation;
- key synchronization;
- biometric unlock;
- hardware-backed client keys;
- SSH agent integration;
- certificate authentication;
- terminal UI;
- saved command workflows;
- background reconnect;
- persistent credential storage.

Those capabilities require separate reviewed decisions or implementation slices.

---

# Alternatives Considered

## Pass Raw Key Bytes Through the UI and ViewModel

Read the selected document in the UI layer and pass a `ByteArray` through presentation code.

### Pros

- Simple initial wiring.
- Easy to call from a Compose route.

### Cons

- Moves secret I/O into the UI layer.
- Expands key-material lifetime across presentation objects.
- Makes accidental state retention and logging more likely.
- Violates the project boundary that external data access belongs behind project-owned abstractions.

Rejected.

---

## Store the Selected URI for Later Reuse

Persist the document URI or take a persistent URI permission so the key can be reused without another selection.

### Pros

- Better repeated-use convenience.
- Avoids repeated document selection.

### Cons

- Introduces persistent credential-reference behavior.
- Expands backup, restore, revocation, deletion, and broken-reference requirements.
- Conflicts with the current ephemeral-only milestone.
- Creates a partial persistent credential feature without the accepted secure-storage implementation gate.

Rejected for the current implementation slice.

---

## Copy the Key into App-Private Storage

Import the selected key into app-private files and authenticate from the copied file.

### Pros

- Predictable local access.
- Avoids provider availability during later authentication.

### Cons

- Persists private-key material.
- Requires encryption, deletion, migration, backup, restore, and corruption handling.
- Bypasses the current persistent credential implementation gate.

Rejected.

---

## Use a Project-Owned One-Shot Private-Key Source

Wrap the selected document behind a project-owned source that can be consumed once by the SSH data layer.

### Pros

- Keeps Android and SSHJ details behind implementation boundaries.
- Minimizes secret lifetime.
- Prevents persistent key references.
- Supports bounded reads, cleanup, cancellation, and focused tests.
- Preserves future compatibility with a separate secure-storage implementation.

### Cons

- Requires an additional source abstraction.
- Requires explicit invalidation and single-consumption tests.
- Requires the user to reselect the key after host-key review.

Accepted for the initial private-key authentication implementation.

---

# Consequences

## Positive

- Private-key authentication can be added without turning Server Inventory into a credential store.
- Android document access remains isolated from stable domain contracts.
- Key material and passphrases remain bounded to one attempt.
- The design preserves existing host-trust, session-ownership, cancellation, and history boundaries.
- Persistent credential storage remains a separate future capability.
- The implementation is testable without exposing SSHJ or Android file APIs across layers.

## Negative

- Users must reselect the key for each attempt and after host-key confirmation.
- Encrypted keys require repeated passphrase entry.
- The project must add a one-shot source abstraction and failure mapping.
- Best-effort buffer clearing cannot guarantee complete runtime memory erasure.
- Key-format support depends on SSHJ behavior verified on Android.

---

# Implementation Gates

The design review accepted this ADR after confirming:

- the project-owned one-shot source contract;
- the Android `GetContent` integration boundary;
- pending-source ownership and one-time transfer behavior;
- configuration-change and process-death behavior;
- the `256 KiB` bounded-read limit and hostile-provider handling;
- the authentication input ownership model;
- host-key review invalidation behavior;
- stable failure categories;
- the key-format verification plan and initial test-fixture set;
- the cancellation and cleanup test plan;
- documentation impact for implementation.

Before private-key authentication implementation may merge, validation must include:

- targeted unit tests for atomic one-shot consumption, invalidation, replacement, size limits, provider failures, failure mapping, redaction, and cleanup;
- tests for configuration-change retention and process-death reset behavior;
- SSHJ adapter tests for unencrypted and encrypted keys;
- cancellation-preservation tests;
- host-key review secret-discard tests;
- full unit-test, lint, and debug-build validation;
- manual Android runtime verification through the system document picker;
- documentation synchronization after verified behavior exists.

---

# Implementation Outcome

**Verified:** 2026-07-12

The initial ADR-013 implementation slice is complete.

The implementation preserves the accepted one-attempt and non-persistent credential boundary:

- the Android system picker supplies the selected private-key document;
- the Android document reference is converted immediately into a project-owned one-shot source;
- key-document content is read within the accepted `256 KiB` boundary;
- private-key parsing and SSHJ key-provider creation remain inside the SSH data layer;
- parsing and authentication operate in memory without application-created temporary private-key files;
- application-owned key buffers and mutable passphrase arrays are cleared on a best-effort basis;
- cancellation remains preserved;
- private-key documents, key material, and passphrases are not persisted.

## Verified OpenSSH KDF Work Boundary

Encrypted OpenSSH v1 private keys contain a file-controlled bcrypt KDF work factor. SSHJ uses that value during key decryption, so the accepted document-size boundary alone does not bound CPU consumption.

The implementation therefore performs project-owned OpenSSH v1 envelope preflight validation before SSHJ parsing:

- the maximum accepted bcrypt KDF work factor is `64` rounds;
- zero rounds and values greater than `64` map to the stable unsupported-format outcome;
- malformed KDF metadata maps to the stable invalid-key outcome;
- the decoded application-owned metadata buffer is cleared on a best-effort basis after validation;
- SSHJ parsing and decryption begin only after the metadata passes this boundary.

The `64`-round boundary was measured on a Pixel 9 Android Virtual Device using actual encrypted OpenSSH v1 Ed25519 keys and complete SSHJ provider parsing. The median of three measured parses was approximately `204.11 ms` at `16` rounds and `724.18 ms` at `64` rounds, a `3.55` ratio.

These measurements are implementation evidence rather than a universal performance guarantee. Physical devices, especially lower-performance devices, may require more time. The boundary is intended to preserve compatibility with reasonably hardened keys while preventing an attacker-controlled unbounded bcrypt loop.

## Verified Key-Format Matrix

| Container | Algorithm | Passphrase | Outcome |
|---|---|---|---|
| OpenSSH v1 | Ed25519 | None | Supported |
| OpenSSH v1 | Ed25519 | Required | Supported |
| OpenSSH v1 | RSA | None | Supported |
| OpenSSH v1 | RSA | Required | Supported |
| PKCS#8 | RSA | None | Unsupported format |
| PKCS#8 | RSA | Required | Unsupported format |

Algorithms and containers outside this verified matrix remain unsupported.

## Validation Evidence

The implementation gates were completed with:

- focused unit coverage for private-key parsing, passphrase handling, unsupported and malformed formats, unauthorized keys, one-shot source ownership, cancellation preservation, failure mapping, and best-effort cleanup;
- focused boundary coverage for accepted, zero, and excessive OpenSSH bcrypt KDF rounds;
- Android benchmark evidence for complete SSHJ parsing at `16` and `64` bcrypt rounds;
- successful complete unit-test execution;
- successful Android lint validation;
- successful debug APK assembly;
- manual Android system-picker runtime verification through an emulator;
- successful authentication with all four supported OpenSSH combinations;
- successful non-interactive `whoami` execution after private-key authentication;
- verified incorrect-passphrase rejection;
- verified server rejection for a valid but unauthorized key;
- verified stable unsupported-format outcomes for both tested PKCS#8 variants;
- cleanup of all runtime-only private keys, passphrases, authorized-key entries, and emulator test documents.

Persistent credential profiles, key import, durable URI access, key synchronization, hardware-backed client keys, and persistent secret storage remain outside the scope authorized by this ADR.

---

# References

- ADR-007: Secure Storage Strategy
- ADR-008: SSH Client Library Selection
- ADR-009: SSH Host Trust and Authentication Input Strategy
- ADR-011: SSH Credential Ownership and Secure Storage Strategy
- ADR-012: Android Backup and Data Extraction Policy
- [Android Developers: Access documents and other files from shared storage](https://developer.android.com/training/data-storage/shared/documents-files)
- [Android Developers: ActivityResultContracts.GetContent](https://developer.android.com/reference/androidx/activity/result/contract/ActivityResultContracts.GetContent)
- [Android Developers: ContentResolver.openAssetFileDescriptor](https://developer.android.com/reference/android/content/ContentResolver#openAssetFileDescriptor(android.net.Uri,%20java.lang.String,%20android.os.CancellationSignal))
- [SSHJ](https://github.com/hierynomus/sshj)
- PROJECT_STATE.md
- state/SSH_STATUS.md

---

# Notes

This ADR remains the accepted architecture and security decision for ephemeral SSH private-key authentication.

The original Context and Decision sections record the state and reasoning at decision time. The Implementation Outcome section records completion and verification of the initial implementation on 2026-07-12.

This ADR does not authorize persistent credential storage.
