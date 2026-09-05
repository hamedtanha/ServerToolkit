# Review Index

## Current Review

### RA-2026.09-v1 — ServerToolkit Architecture Review

- **Status:** Accepted
- **External report ID:** `STK-ARCH-2026-09-05`
- **Original evidence baseline:** `2800f3a250e9b2733dc040a69a9a1f851538d84e`
- **Revalidation baseline:** `e526b6d6f73713ce23e419ee275ad8ba3d4745a6`
- **Governing Issue:** `#166`
- **Acceptance PR:** `#167`
- **Purpose:** Register and revalidate the 2026-09-05 read-only architecture and targeted implementation review before focused remediation begins.

Current documents:

- `architecture/2026/RA-2026.09-v1/ServerToolkit-Architecture-Review-2026-09-05.md`
- `architecture/2026/RA-2026.09-v1/REVALIDATION.md`
- `architecture/2026/RA-2026.09-v1/STATUS.md`

The original external review artifact is preserved against commit `2800f3a250e9b2733dc040a69a9a1f851538d84e`. Current-HEAD changes are recorded only in the separate revalidation document so historical evidence is not rewritten. The review is accepted and content-frozen for merge through PR `#167`; publication remains a metadata-only follow-up after the accepted package reaches `main`.

## Published Reviews

### RA-2026.07-v2 — Server Domain and Operational UX Architecture

- **Status:** Published
- **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
- **Governing Issue:** `#138`
- **Purpose:** Assess the central `Server` domain, stable identity, profile-layer ownership, platform and capability evidence, SSH workspace boundaries, operational UX, persistence implications, security, and support claims before any related implementation begins.

Documents:

- `architecture/2026/RA-2026.07-v2/00-Review-Charter.md`
- `architecture/2026/RA-2026.07-v2/01-Server-Domain-Assessment.md`
- `architecture/2026/RA-2026.07-v2/02-Operational-UX-Assessment.md`
- `architecture/2026/RA-2026.07-v2/03-Decision-Recommendations.md`
- `architecture/2026/RA-2026.07-v2/STATUS.md`

The review was accepted through PR `#139` and squash-merged into `main` as `8070830dfae14f908b9dd128846f66112b36423e`. Final substantive head `0af71c70133e8fd27277ef50cf4b801fd0c3a618` passed Android Validation run `#88`, accepted head `f569e96245e2a552028d1ec22fb560762adb2e1c` passed run `#89`, and the acceptance merge passed main run `#90`. Publication PR `#142` records the review as `Published`; the published version is immutable.

### RA-2026.07-v1 — Architecture Knowledge Foundation

- **Status:** Published
- **Evidence baseline:** `0135faf89b1035fd91c75b37a25ec51bc7c71074`
- **Governing Issue:** `#135`
- **Purpose:** Establish the Architecture Atlas, engineering-handbook navigation, immutable review structure, and evidence model required before the next Server-domain or user-interface architecture decision.

Documents:

- `architecture/2026/RA-2026.07-v1/00-Review-Charter.md`
- `architecture/2026/RA-2026.07-v1/01-Executive-Summary.md`
- `architecture/2026/RA-2026.07-v1/02-Current-Repository-Atlas.md`
- `architecture/2026/RA-2026.07-v1/STATUS.md`

Living current-state map:

- `../docs/ARCHITECTURE_ATLAS.md`

The review was accepted through PR `#136` and published through PR `#137`. Review version `RA-2026.07-v1` is immutable; corrections or changed conclusions require a new review version.
