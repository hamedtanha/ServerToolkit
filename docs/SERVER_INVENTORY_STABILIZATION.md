# Server Inventory Stabilization Checklist

**Project:** Server Toolkit  
**Version Target:** 0.3.0  
**Status:** Accepted  
**Last Updated:** 2026-07-04

---

## Purpose

This document defines the stabilization checklist for the Server Inventory foundation before the project starts SSH-related implementation.

The goal is to keep the application releasable, protect the established architecture, and avoid adding security-sensitive workflows before local inventory management is stable.

---

## Stabilization Scope

The stabilization scope includes the implemented local Server Inventory foundation:

- Local server persistence with Room.
- Server list rendering.
- Add Server workflow.
- Edit Server workflow.
- Delete Server workflow.
- Search and filtering behavior.
- Shared Server Form naming.
- Documentation alignment.

The stabilization scope does not include:

- SSH connectivity.
- Credential storage.
- Command execution.
- Monitoring.
- Xray or x-ui management.

---

## Completion Criteria

Server Inventory 0.3.0 can be considered stable when all of the following criteria are true.

### Automated Verification

- Unit tests pass.
- Instrumented tests pass.
- Debug build succeeds.
- Room schema export remains intentional and committed.
- No obsolete references remain after naming refactors.

### Manual Verification

- A server can be added.
- Added servers remain visible after app restart.
- A server can be edited without creating a duplicate server.
- Edited values remain visible after app restart.
- A server can be deleted after confirmation.
- Deleted servers remain deleted after app restart.
- Search works for the currently implemented search fields.
- Environment and favorites filters work with existing inventory data.
- Clearing filters restores the full visible inventory.

### Documentation Verification

- `docs/PROJECT_STATE.md` reflects the current implementation.
- `docs/ROADMAP.md` reflects the current milestone state.
- `docs/ARCHITECTURE.md` does not describe unimplemented functionality as implemented.
- `PACKAGE_STRUCTURE.md` matches the current source tree.
- `docs/CHANGELOG.md` records the implemented changes.
- Any architectural boundary decision is documented in an ADR or architecture document.

---

## Acceptance Result

The Server Inventory 0.3.0 baseline is accepted.

The implemented Server Inventory foundation is stable enough to support SSH workflow design and implementation planning.

This acceptance is based on completed automated verification, completed manual verification, and synchronized documentation for the current Server Inventory scope.

---

## Current Known Follow-Up Items

The following items may be addressed after 0.3.0 stabilization depending on priority:

- Review Server Inventory UI density after adding edit, delete, search, and filter controls.
- Review one-character search behavior to reduce broad host matches such as `.com` when appropriate.
- Add migration tests when the Room database version changes.

These items do not block the accepted Server Inventory foundation unless they are promoted to release blockers.

---

## SSH Entry Gate

SSH-related implementation may proceed after this acceptance, but only within the following constraints:

1. SSH implementation must build on the accepted Server Inventory foundation.
2. SSH implementation must not introduce credential storage without a separate security decision.
3. SSH implementation must preserve existing inventory tests and behavior.
4. SSH implementation must document any new architecture decision that changes dependency boundaries, persistence, or security posture.

Credential handling requires a separate security decision before implementation.

---

## Current Decision

`feature/serverinventory` remains the active feature boundary for the current implementation.

A broader `inventory` domain or package should be introduced only after the project implements additional non-server asset types or shared inventory behavior that is no longer server-specific.
