# SSH Runtime Failure Mapping Review

**Project:** Server Toolkit  
**Area:** SSH  
**Status:** Reviewed  
**Date:** 2026-07-09  

---

## Purpose

This document records the review of SSH runtime failure mapping after improving SSH user-facing failure-state messages.

The review checks whether runtime SSH connection and command execution failures are mapped to stable domain-level error categories without exposing SSHJ-specific exception types outside the data layer.

---

## Findings

### Connection Runtime Mapping

The SSHJ trusted connection executor already maps the most important network failures explicitly:

- Unknown host failures are mapped to `SshConnectionError.UnknownHost`.
- Socket timeout failures are mapped to `SshConnectionError.ConnectionTimeout`.
- Generic I/O failures remain mapped to `SshConnectionError.Unknown`.

This is acceptable for the current implementation because no dedicated domain-level connection transport failure category exists yet.

### Command Runtime Mapping

The command channel executor already distinguishes between command channel opening failures and command execution failures:

- Failures before a command channel is opened are mapped to `SshCommandExecutionError.ChannelOpenFailed`.
- Failures after the command channel is opened are mapped to `SshCommandExecutionError.CommandExecutionFailed`.
- Command timeout and cancellation behavior are already represented separately.

This is acceptable for the current non-interactive command execution model.

### Boundary Catch-All Mapping

Outer catch-all mappings in the SSH connection and command execution services remain acceptable as defensive containment boundaries.

They preserve cancellation behavior and prevent third-party or infrastructure exceptions from leaking into domain or presentation layers.

---

## Decision

No implementation change is required at this time.

Adding new domain error categories, such as a dedicated connection transport failure category, should wait until runtime testing identifies a recurring failure mode that cannot be represented clearly by the existing domain model and presentation messages.

---

## Follow-Up

Future runtime testing may justify adding a more specific connection transport failure category if generic `Unknown` failures become common and diagnosable.

This should be handled as a separate implementation slice with matching domain, presentation, test, and documentation updates.

