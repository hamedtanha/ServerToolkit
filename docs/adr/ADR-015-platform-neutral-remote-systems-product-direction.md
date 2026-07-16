# ADR-015: Platform-Neutral Remote Systems Product Direction

**Status:** Accepted

**Date:** 2026-07-16

---

# Context

Server Toolkit was initially described as an Android application for Linux server administration. That framing was useful during the first implementation stages because the verified remote workflow uses SSH and the maintainer's immediate operational environment is Linux-oriented.

The existing product documents also named individual technologies and services, including Xray, x-ui, certificate helpers, Docker, Kubernetes, and cloud providers. Treating those examples as first-class product direction would couple feature selection and domain language to particular operating systems, service managers, shells, vendors, or infrastructure products.

The intended product is broader: a structured mobile application for operating remote systems through explicitly supported capabilities. The architecture should permit multiple system families and access mechanisms over time without claiming support that has not been implemented and verified.

ADR-001 established the enduring infrastructure-management identity of Server Toolkit and the principle that SSH is one capability rather than the entire product. It also stated that a significant product-vision change must be recorded in a new ADR instead of rewriting the historical decision.

---

# Decision

Server Toolkit will be developed as a **platform-neutral remote systems operations application**.

The product core must not assume one:

- operating system or distribution;
- shell or command language;
- service manager;
- package manager;
- transport protocol;
- cloud provider;
- infrastructure service;
- vendor-specific API.

Platform neutrality is an architectural and product-selection rule. It is not a claim that every platform is currently supported.

## Support Claim Model

Documentation and user-facing claims must distinguish three separate states:

1. **Architecturally permitted** — the accepted architecture can accommodate the platform or capability without violating core boundaries.
2. **Implemented** — a concrete provider, adapter, transport, or workflow exists in the codebase.
3. **Verified** — automated tests, runtime evidence, or both confirm the supported behavior for an identified environment.

Only the third state may be presented as verified support. Architectural extensibility must never be described as universal compatibility.

## Core Product Direction

The committed core direction prioritizes broadly useful remote-operations capabilities, including:

- remote-system inventory and organization;
- connection workflows;
- explicit command execution;
- saved operational commands;
- connection and operation history where justified;
- capability-aware status and operational workflows;
- clear supported, unsupported, unknown, and unavailable states.

Specific technologies may be added later only as optional, isolated integrations after a concrete product need, ownership boundary, security review, support policy, and validation plan are accepted.

Naming a technology as an architectural example does not place it on the committed roadmap.

## Relationship to ADR-001

This ADR preserves the following accepted decisions from ADR-001:

- Server Toolkit is an infrastructure-management application rather than a traditional SSH client.
- SSH is one application capability, not the product identity.
- Operational efficiency, maintainability, and production-quality Android engineering remain priorities.

This ADR **supersedes the Linux-specific product-scope assumptions** in ADR-001 and related documents. ADR-001 remains authoritative for the preserved decisions listed above and as the historical project-vision baseline.

---

# Feature Selection Rules

A proposed product feature must answer the following before implementation:

- What platform-neutral user or operational value does it provide?
- Is its domain language independent from a specific implementation technology?
- Does it belong to the core product, a platform capability, or an optional integration?
- What concrete implementation boundary is required?
- How will unsupported targets behave?
- What support claim can be justified by evidence?
- Does the feature introduce premature abstraction or a service-specific product bias?

A feature must not be accepted merely because it is technically interesting, convenient for one deployment, or easy to implement.

---

# Alternatives Considered

## Linux-First Product with Additional Platforms Later

Continue describing Linux administration as the product identity and add other platforms as exceptions.

### Pros

- Matches the current verified SSH environment.
- Requires fewer immediate documentation changes.
- Simplifies early feature descriptions.

### Cons

- Encourages Linux commands and service-manager assumptions in core models.
- Makes other platforms appear secondary or exceptional.
- Biases the roadmap toward individual services instead of reusable capabilities.
- Creates avoidable migration cost when broader support is introduced.

Rejected.

---

## Universal Multi-Platform Support Claim

Present Server Toolkit as supporting all common server platforms immediately.

### Pros

- Broad marketing language.
- Simple product positioning.

### Cons

- Factually unsupported.
- Hides major transport, authentication, command, and lifecycle differences.
- Creates unsafe user expectations.
- Violates evidence-based documentation rules.

Rejected.

---

## Platform-Neutral Core with Evidence-Based Support

Define neutral product concepts while implementing and verifying concrete platform support incrementally.

### Pros

- Preserves long-term extensibility.
- Keeps current claims factual.
- Supports capability-driven architecture.
- Isolates platform and service integrations.
- Improves feature prioritization and maintainability.

### Cons

- Requires explicit support-state modelling.
- Requires disciplined documentation wording.
- Some future capabilities need gateway and adapter layers.
- Verification matrices may grow as support expands.

Accepted.

---

# Consequences

## Positive

- Product identity is no longer coupled to Linux or a named service.
- Feature selection must prioritize reusable operational value.
- Architecture and support claims are separated clearly.
- Platform-specific implementation can evolve without contaminating core domain meaning.
- Optional integrations can remain isolated from the committed core roadmap.

## Negative

- Product and architecture documentation require coordinated updates.
- Future platform-specific capabilities require explicit support-state and validation work.
- Some apparently simple features may need additional adapter or gateway design.
- The project must resist both service-specific shortcuts and premature generic frameworks.

---

# References

- ADR-001: Project Vision
- ADR-002: Application Architecture
- ADR-016: Three-Level Remote Capability Architecture
- `PRODUCT_VISION.md`
- `ARCHITECTURE.md`
- `ENGINEERING_STRATEGY.md`
- `ROADMAP.md`

---

# Notes

This decision changes product direction and documentation rules. It does not by itself implement support for any additional operating system, transport, service manager, or infrastructure integration.
