# ADR-016: Three-Level Remote Capability Architecture

**Status:** Accepted

**Date:** 2026-07-16

---

# Context

ADR-002 established the Android application architecture: feature-first ownership, MVVM, repository boundaries, unidirectional state flow, and separation between presentation, domain-oriented logic, and data access.

That architecture remains suitable for local features and Android application concerns. It does not, by itself, define how a platform-neutral request such as service management, resource inspection, log access, or another remote operation should be translated into platform-, transport-, or service-specific behavior.

Without an additional boundary, future remote capabilities could leak shell commands, operating-system assumptions, raw provider responses, or third-party client types into domain models and presentation state.

The project needs a restrained architecture that isolates those details without introducing a speculative plugin framework or forcing unnecessary gateways onto purely local features.

---

# Decision

Server Toolkit adopts a **three-level remote capability architecture** for features that require platform discovery, translation, routing, orchestration, policy enforcement, or external-system access.

The three levels are:

1. **Core**
2. **Capability Gateway**
3. **Providers and Adapters**

Dependencies point inward toward stable, project-owned Core contracts.

```text
Presentation / Use Case
        ↓
Core capability contract
        ↓
Capability Gateway
        ↓
Provider / Adapter
        ↓
Transport or external system
```

This architecture complements rather than replaces the accepted Android application layers.

---

# Level 1 — Core

Core owns platform-neutral application meaning.

## Responsibilities

- Domain models and value objects.
- Capability contracts.
- Use cases when orchestration or reusable business rules justify them.
- Normalized operation results and errors.
- Capability support states.
- Security, lifecycle, and policy rules that are independent from a concrete provider.

## Prohibited Dependencies

Core must not depend on:

- Android framework or Compose types;
- Room entities or DAOs;
- SSHJ, WinRM, HTTP client, cloud SDK, or vendor SDK types;
- shell commands or command-output formats;
- operating-system, service-manager, or provider-specific response models;
- concrete provider implementations.

Core contracts describe **what the application means**, not **how a target system implements it**.

---

# Level 2 — Capability Gateway

The Capability Gateway is the translation, routing, normalization, orchestration, and policy boundary between Core and concrete providers.

## Responsibilities

- Resolve the target system context required by the capability.
- Determine or consume capability support information.
- Select an appropriate provider or adapter.
- Translate a Core request into a provider request.
- Coordinate multiple provider operations when the capability requires it.
- Enforce capability-specific safety and lifecycle guardrails.
- Normalize provider results and failures into project-owned Core results.
- Prevent platform-, transport-, and service-specific details from leaking upward.

## Gateway Introduction Rule

A gateway is justified only when a concrete capability requires one or more of the following:

- provider selection;
- platform or environment discovery;
- request translation;
- result normalization;
- multi-step orchestration;
- policy enforcement across implementations;
- external integration abstraction.

A gateway must not be introduced only to satisfy a theoretical pattern.

Purely local features such as Saved Commands management, server favorites, local tags, search, filtering, or Room-backed history continue to use their direct feature-owned domain and repository boundaries unless a real translation or routing requirement appears.

---

# Level 3 — Providers and Adapters

Providers and Adapters own concrete external access and implementation-specific behavior.

## Responsibilities

- Transport implementations such as SSH, WinRM, or HTTP APIs.
- Operating-system and service-manager behavior.
- Vendor- or service-specific integrations.
- Concrete command construction.
- External response and output parsing.
- Third-party library and SDK integration.
- Mapping raw failures into provider-level results.
- Implementation-specific validation required for safe external calls.

Examples such as systemd, OpenRC, Windows Service Control Manager, Docker, Kubernetes, or cloud-provider adapters illustrate the boundary only. They are not accepted roadmap commitments by this ADR.

Provider-specific models must remain inside the provider boundary unless deliberately mapped to a project-owned Core model.

---

# Capability Support States

Gateway-backed capabilities must represent support explicitly.

The baseline semantic states are:

- **Supported** — an appropriate implementation is available and the required capability has been confirmed.
- **Unsupported** — the target has been evaluated and does not provide the capability through an accepted implementation.
- **Unknown** — support has not yet been determined or evidence is insufficient.
- **Unavailable** — the capability may exist, but it cannot currently be used because of a runtime condition such as permissions, connectivity, configuration, or dependency availability.

Unsupported and unknown states must not be converted into guessed commands or silent fallback behavior.

Presentation consumes project-owned support states and operation results. It must not infer capability support by parsing raw provider output.

---

# Relationship to Existing Android Architecture

The application continues to use feature-first MVVM with presentation, domain-oriented contracts, and data implementations.

```text
Android application concern:
UI → Presentation → Domain contracts/models ← Data implementations

Remote capability concern:
Core contract ← Capability Gateway ← Provider/Adapter ← External system
```

A feature may contain all three remote-capability levels when the behavior is feature-owned, or shared capability contracts may move to an appropriate shared package after reuse is proven.

The architecture does not require a new Gradle module or root package immediately.

No empty package hierarchy, generic base gateway, generic base provider, registry framework, or plugin system is created until concrete implementation needs justify it.

---

# Dependency Rules

- Core owns stable interfaces and normalized models.
- Gateways depend on Core contracts and narrow provider contracts.
- Providers implement narrow provider contracts and may depend on transport or third-party libraries.
- Dependency injection wires implementations to contracts at the application boundary.
- Presentation depends on project-owned use cases, repositories, or Core contracts, never concrete providers.
- Raw stdout, stderr, API payloads, SDK exceptions, and provider enums must not become presentation state.
- Platform-specific parsing belongs to the relevant provider or adapter.
- Feature A must not access Feature B's concrete provider or data implementation directly.
- Cross-feature use requires an explicit stable contract or navigation boundary.

---

# Security and Safety Rules

- Translation must not weaken existing explicit-execution requirements.
- Provider selection must fail predictably when no accepted implementation exists.
- Commands, identifiers, and arguments must be validated by the owning layer before external execution.
- Secrets must not be introduced into Core observable state or persisted without a separately accepted secure-storage boundary.
- Provider diagnostics may be retained for troubleshooting, but user-facing and domain-facing results must remain normalized and safe.
- Automatic or background execution requires a separate reviewed decision.

---

# Alternatives Considered

## Use Existing Repository Boundaries for Every Remote Operation

Model every remote capability as a repository and place concrete behavior directly in the data layer.

### Pros

- Reuses existing terminology.
- Minimal initial structure.

### Cons

- Does not clearly separate persistence/data retrieval from capability translation and provider routing.
- Encourages large repository implementations with mixed responsibilities.
- Makes multi-provider normalization and support-state handling unclear.

Rejected as the general remote-capability model. Repositories remain appropriate for persistence and data ownership.

---

## Direct Platform Logic in Each Feature

Allow each feature to issue commands or call external clients directly.

### Pros

- Fast initial delivery.
- Few abstractions.

### Cons

- Duplicates platform detection and parsing.
- Leaks implementation details into presentation and domain code.
- Makes support claims and error behavior inconsistent.
- Scales poorly across operating systems and transports.

Rejected.

---

## Generic Plugin Framework

Create a universal plugin registry, base gateway, base provider, and dynamic extension mechanism immediately.

### Pros

- Maximum theoretical extensibility.
- Uniform registration model.

### Cons

- Premature abstraction without implemented capability diversity.
- High complexity and testing cost.
- Unclear Android lifecycle and dependency-injection behavior.
- Risks creating infrastructure before product needs are known.

Rejected for the current project stage.

---

## Incremental Three-Level Capability Architecture

Introduce Core, Gateway, and Provider responsibilities only as concrete capabilities require them.

### Pros

- Preserves platform neutrality.
- Keeps dependencies and ownership explicit.
- Supports multiple implementations without leaking details.
- Avoids forcing gateway abstractions onto local features.
- Allows incremental testing and documentation.

### Cons

- Requires careful judgement about when a gateway is justified.
- Adds mapping and normalization work for external capabilities.
- Provider support matrices and runtime evidence must be maintained.

Accepted.

---

# Consequences

## Positive

- Platform-specific details remain isolated.
- Core and presentation models remain stable across implementations.
- Unsupported behavior becomes explicit and testable.
- New transports or providers can be added incrementally.
- Local features remain simple.
- Service-specific integrations cannot silently redefine the product core.

## Negative

- Gateway-backed capabilities require additional contracts and mappings.
- Capability discovery and support-state handling add implementation work.
- Incorrect boundary placement could create unnecessary abstraction.
- Documentation must remain synchronized with implemented and verified providers.

---

# Implementation Guidance

The first implementation using this architecture must begin with a concrete user-facing capability and a focused issue.

Before creating packages or contracts, that issue must define:

- the platform-neutral Core operation;
- the required support states;
- why a Gateway is necessary;
- the first Provider or Adapter;
- the transport and security boundaries;
- automated and runtime verification;
- the factual support claim after completion.

Saved Commands Management does not require a Capability Gateway because it is currently a local persistence and presentation workflow.

---

# Relationship to ADR-002

This ADR refines ADR-002 for remote-system capabilities. It does not supersede feature-first MVVM, repository pattern, Hilt, Room, Navigation Compose, or unidirectional presentation state.

If future implementation requires changing the default Android application architecture itself, a separate ADR must address that change explicitly.

---

# References

- ADR-002: Application Architecture
- ADR-006: SSH Workflow and Security Boundaries
- ADR-010: SSH Command Channel Execution Strategy
- ADR-015: Platform-Neutral Remote Systems Product Direction
- `ARCHITECTURE.md`
- `ENGINEERING_STRATEGY.md`
- `PRODUCT_VISION.md`

---

# Notes

This ADR defines responsibility and dependency boundaries. It does not add production classes, packages, transports, platform detection, or service integrations.
