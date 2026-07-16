# Product Vision

**Project:** Server Toolkit
**Document Baseline:** 0.4.0
**Status:** Foundational
**Last Updated:** 2026-07-16

---

## Purpose

Server Toolkit is a modern Android application for structured operations on remote systems.

The product helps technical users organize remote-system inventory, establish trusted connections, execute explicit operational actions, and access repeatable workflows from a mobile device without reducing the experience to a traditional terminal client.

SSH is currently the verified remote-access capability. It is one capability of the product, not the product identity.

---

## Problem Statement

Operating remote systems from a mobile device is often fragmented and error-prone.

Existing mobile tools commonly focus on opening a terminal session. Real operational work also requires users to:

- Find the correct target quickly.
- Understand connection and trust context.
- Execute repeated commands consistently.
- Preserve useful local operational history.
- Distinguish supported from unsupported actions.
- Avoid accidental or automatic execution.
- Reduce switching between disconnected tools.

A product that hardcodes one operating system, service manager, shell, vendor, or infrastructure service into its core creates avoidable limitations and inconsistent behavior as its scope grows.

---

## Product Vision

Server Toolkit aims to become a practical remote-systems operations companion for Android.

The application should provide a structured, reliable, secure, and capability-aware interface for operating supported remote systems.

The product core remains neutral toward:

- Operating-system family and distribution.
- Shell and command language.
- Service manager and package manager.
- Transport protocol.
- Cloud provider.
- Infrastructure service or vendor.

Concrete support is implemented incrementally through explicit contracts, gateways, providers, and adapters where the selected capability requires them.

---

## Support Model

Server Toolkit distinguishes three levels of support language.

### Architecturally Permitted

The accepted architecture can accommodate a target platform or capability without violating Core boundaries.

This state does not mean an implementation exists.

### Implemented

A concrete workflow, provider, adapter, or transport exists in the codebase.

This state does not automatically mean all expected environments have been verified.

### Verified

Automated tests, runtime evidence, or both confirm the supported behavior for a documented environment.

Only verified behavior may be presented as confirmed platform or capability support.

---

## Target Users

The primary users are:

- System administrators.
- DevOps and site-reliability engineers.
- Infrastructure and network engineers.
- Homelab operators.
- Operators managing VPS, dedicated servers, appliances, or other remote systems.
- Technical users who need efficient, repeatable mobile operations.

The product is designed for users who understand infrastructure and remote-operation concepts. It is not a beginner training application.

---

## Core Value Proposition

Server Toolkit combines remote-system context, access, and repeatable operations in one Android application.

The product should help users:

- Work faster from a mobile device.
- Reduce repetitive operational work.
- Avoid switching between multiple tools.
- Keep remote-system information organized.
- Execute explicit actions consistently.
- Understand whether a capability is supported, unsupported, unknown, or temporarily unavailable.
- Preserve secure and predictable operation boundaries.

---

## Product Principles

Server Toolkit follows these principles:

- Remote-systems operations first.
- Platform-neutral Core concepts.
- Capability-based feature design.
- Evidence-based support claims.
- SSH as a capability, not the entire product.
- Mobile-first operational workflows.
- Explicit user-triggered execution.
- Secure handling of sensitive data.
- Clear ownership and lifecycle boundaries.
- Simple and predictable user experience.
- Maintainable architecture over service-specific shortcuts.
- Documentation synchronized with implementation.

---

## Feature Classification

Proposed features are classified before implementation.

### Core Product Capabilities

Broadly useful local or remote-system workflows, such as:

- Remote-system inventory.
- Search, filtering, grouping, and favorites.
- Connection workflows.
- Saved commands.
- Explicit command execution.
- Connection and operation history where justified.
- Capability-aware status presentation.

### Platform Capabilities

General operational concepts whose implementation differs by target environment, such as:

- Resource inspection.
- Service management.
- System log access.
- Process or package information.

These capabilities require platform-neutral Core contracts and concrete implementations behind gateway and provider boundaries.

### Optional Integrations

Vendor-, service-, or product-specific workflows.

Examples may include container platforms, VPN technologies, cloud providers, certificate authorities, or other infrastructure products. Examples are not roadmap commitments.

An optional integration must remain isolated and must not redefine Core domain models.

---

## Initial Product Scope

The current product scope focuses on a reliable local and SSH-backed foundation:

- Dashboard and navigation.
- Server inventory.
- Room-backed local persistence.
- SSH host trust and ephemeral authentication.
- Project-owned SSH session lifecycle.
- Explicit non-interactive command execution.
- SSH connection history.
- Saved Command domain and persistence foundation.
- Saved Commands management.
- Later explicit SSH command-input integration.

---

## Version 1.0 Direction

Version 1.0 should establish a reliable, focused, maintainable core product.

The intended direction includes:

- Add, edit, remove, search, and organize remote-system profiles.
- Connect through explicitly supported transports and authentication workflows.
- Execute saved commands through an explicit user action.
- Present basic operational context through capabilities that have accepted contracts and verified implementations.
- Provide a useful operational dashboard.
- Store local non-secret configuration and history safely.
- Maintain complete documentation for implemented and verified behavior.

Version 1.0 does not need to support every operating system, transport, service manager, or infrastructure platform.

---

## Out of Scope for the Current Core Roadmap

The following are not committed Core deliverables:

- Universal operating-system support.
- A public plugin marketplace or dynamic plugin runtime.
- A complete cloud-management platform.
- Full container orchestration.
- Team collaboration and role-based access control.
- A complete monitoring platform.
- A complex automation engine.
- Automatic or background command execution without a separate accepted decision.
- Persistent credentials without an accepted secure-storage implementation.
- Any named vendor or service integration that lacks a focused accepted plan.

---

## Long-Term Direction

Future versions may add new providers, adapters, transports, platform capabilities, or optional integrations.

Each addition must be selected incrementally and must define:

- Platform-neutral user value.
- Core contract ownership.
- Gateway need and responsibility.
- Provider or adapter boundary.
- Capability support states.
- Security and lifecycle implications.
- Automated and runtime verification.
- Factual support claims.

---

## Success Criteria

Server Toolkit is successful if it becomes:

- Useful for real remote-system operations.
- Reliable enough for repeated operational use.
- Secure in handling credentials, trust data, and server information.
- Predictable when a capability is unavailable or unsupported.
- Extensible without coupling the Core to one platform or service.
- Easy to understand, test, and maintain.
- Professionally documented.
- Suitable as a production-quality Android engineering project.

---

## Non-Goals

Server Toolkit does not aim to be:

- A generic SSH terminal clone.
- A beginner operating-system training application.
- A full replacement for desktop administration tools.
- A universal compatibility claim without evidence.
- A vendor-specific control panel.
- A quick prototype or tutorial project.

---

## References

- `README.md`
- `ARCHITECTURE.md`
- `ENGINEERING_STRATEGY.md`
- `ROADMAP.md`
- `PROJECT_STATE.md`
- ADR-001: Project Vision
- ADR-015: Platform-Neutral Remote Systems Product Direction
- ADR-016: Three-Level Remote Capability Architecture
