# Project Bootstrap

**Project:** Server Toolkit  
**Released Application Version:** 0.4.0  
**Active Development Milestone:** 0.5.0 — Operations  
**Status:** Active  
**Last Updated:** 2026-09-06

---

## Purpose

This document provides a compact bootstrap for contributors, reviewers, and technical assistants.

It is an entry point only. Current repository documentation and executable evidence override this summary whenever they differ.

Do not place secrets, credentials, private keys, tokens, server addresses, or private infrastructure details in this file.

---

## Product Direction

Server Toolkit is a platform-neutral remote systems operations Android application.

SSH is the current verified remote-access capability; it is one capability of the product, not the product identity.

Architectural extensibility is not a support claim. The repository distinguishes:

1. architecturally permitted;
2. implemented;
3. verified.

Named products, vendors, operating systems, service managers, cloud providers, and infrastructure integrations are not committed Core scope merely because the architecture could support them.

---

## Current Implemented Baseline

The current repository includes:

- Single Activity Android architecture with Kotlin, Jetpack Compose, MVVM, Hilt, Navigation Compose, and Room;
- Dashboard and Server Inventory workflows;
- accepted local Server Inventory persistence baseline;
- released SSH milestone with explicit host trust, ephemeral password/private-key authentication, project-owned session lifecycle, explicit disconnect/reconnect, non-interactive command execution, and connection history;
- Saved Commands domain, Room persistence, management workflow, editing/deletion, and SSH command-input selection without automatic execution;
- accepted Graphite + Azure visual identity baseline and ADR-017 scalable collection UX contract;
- protected `main`, pull-request delivery, build/unit validation, executable architecture dependency validation, and managed-device Android instrumentation in the required CI gate;
- Android application metadata and immutable release evidence remaining at released version `0.4.0` while `0.5.0 — Operations` development continues.

---

## Current Architecture Guardrails

- Preserve feature-first ownership and inward dependency direction.
- Domain code remains platform-neutral and independent from Android, Room, SSHJ, data implementations, and presentation implementations.
- Cross-feature integration uses stable project-owned contracts or app-level navigation boundaries.
- Do not introduce a Capability Gateway until a concrete remote capability requires routing, translation, discovery, normalization, orchestration, or policy enforcement.
- Do not introduce empty package scaffolding, generic provider/plugin registries, or a module split without concrete evidence.
- Secret values remain transient and outside observable UI state and ordinary Room storage.
- Saved Command selection never executes automatically; explicit Run remains the execution trigger.
- Accepted ADRs are immutable decision records; changed decisions require a new ADR.

The executable production dependency contract is documented in `PACKAGE_STRUCTURE.md` and `docs/ARCHITECTURE.md` and enforced by `scripts/architecture/check-dependencies.sh`.

---

## Intentionally Not Implemented

Do not infer these capabilities from old bootstrap text or historical roadmap examples:

- interactive terminal UI;
- persistent credential storage;
- automatic or background command execution;
- operating-system or service-manager discovery;
- production Capability Gateway abstractions;
- additional remote transports;
- monitoring, service-management, log, or package-management providers;
- Xray, x-ui, Docker, Kubernetes, cloud-provider, certificate-authority, or other named integrations;
- public plugin infrastructure.

---

## Source-of-Truth Reading Order

Before implementation or review, inspect at minimum:

1. `docs/PROJECT_STATE.md`;
2. the relevant focused document under `docs/state/`;
3. `docs/ARCHITECTURE.md` and `PACKAGE_STRUCTURE.md` for affected boundaries;
4. `docs/adr/README.md` and applicable accepted/draft ADRs;
5. recent merged pull requests and commits affecting the area;
6. the current source tree and executable validation for factual behavior.

For architecture review history, use `review/INDEX.md`. Published reviews are immutable evidence against their recorded baselines; they are not mutable current-state documents.

---

## Delivery Rules

- Keep `main` releasable.
- Use short-lived GitHub Flow branches and pull requests.
- Use Conventional Commits and Semantic Versioning.
- Keep implementation and documentation synchronized.
- Prefer the smallest maintainable correction over speculative abstraction.
- Select new product work only after current review, state, and roadmap evidence has been checked.

---

## Primary Navigation

- `docs/PROJECT_STATE.md`
- `docs/ARCHITECTURE.md`
- `docs/ARCHITECTURE_ATLAS.md`
- `PACKAGE_STRUCTURE.md`
- `docs/ENGINEERING_STRATEGY.md`
- `docs/DOCUMENTATION.md`
- `docs/adr/README.md`
- `review/INDEX.md`
