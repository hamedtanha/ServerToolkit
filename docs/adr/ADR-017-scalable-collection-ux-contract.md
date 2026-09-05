# ADR-017: Scalable Collection UX Contract

**Status:** Accepted

**Date:** 2026-09-04

---

# Context

Server Toolkit contains operational collections that are expected to grow over
the lifetime of the product, including Server Inventory and Saved Commands.
Future capabilities may introduce additional collections with similar
requirements.

Collection usability is not determined only by item count. A layout that works
for a small fixture can become unusable when any of the following dimensions
increase independently:

- number of items;
- length of names, addresses, commands, or other operational values;
- user font scale;
- viewport constraint caused by device size, window size, or localization.

A collection design that assumes short content, normal font scale, or a fixed
amount of horizontal space creates structural coupling between content and
actions. Correcting those failures later can require screen redesign,
presentation-state changes, and repository-query changes after those assumptions
have already spread across features.

The project therefore requires a durable collection UX contract rather than
screen-specific corrective rules.

Detailed implementation evidence and the first identified follow-up are tracked
outside this ADR in Issue #161 and the visual identity calibration review.

---

# Decision

Server Toolkit collection UI must remain usable as item count, content length,
font scale, and available viewport space increase independently.

The following rules apply to current and future collection features.

## Collection Rendering

Growing collections must use lazy rendering or an equivalent bounded rendering
strategy appropriate to the platform.

Items must have stable domain identity where the collection API supports keyed
identity.

The architecture must not assume that users can efficiently browse the entire
collection visually once the collection becomes large.

## Content and Action Layout

Primary operational content must retain enough layout space to remain readable.

Action controls must not destructively compress primary content in order to
preserve a single horizontal arrangement.

When available space becomes constrained, the item layout may adapt the
placement, grouping, or presentation of actions. The exact adaptive layout
remains an implementation-level decision for the owning feature.

Global typography must not be reduced to compensate for a local collection
layout constraint.

## Operational Content

Long server names, host values, identifiers, command text, and similar
machine-oriented values are first-class content rather than exceptional input.

Presentation decisions such as wrapping, truncation, scrolling, or alternate
layout must preserve the information required for the operational task.

## Collection Query Evolution

Search, filtering, sorting, grouping, favorites, recency, and similar collection
dimensions should evolve through cohesive query or UI-state models rather than
an accumulating set of unrelated presentation flags.

Features are not required to implement collection operations before product need
exists, but their boundaries must not make those operations unnecessarily
expensive to introduce later.

## Pagination

Pagination is not a default dependency.

Local collections may continue to use ordinary Room and Flow-based loading while
measured behavior remains acceptable.

Paging or another incremental-loading mechanism should be introduced only when
realistic data volume, memory behavior, rendering cost, or responsiveness
provides evidence that it is required.

The absence of pagination today must not be treated as a guarantee that all
future collections will always be loaded eagerly.

## Validation Boundary

Collection UI changes must be evaluated under more than the ideal short-content,
normal-font, full-width case.

Constrained width, enlarged text, and realistically long operational content are
part of the supported design space.

---

# Alternatives Considered

## Optimize Only for Current Small Collections

Design each collection for the current number and length of items and defer
scalability concerns until a user-visible failure occurs.

### Pros

- minimal immediate implementation effort;
- fewer explicit design constraints.

### Cons

- embeds short-content and fixed-width assumptions into feature code;
- makes accessibility and localization failures more likely;
- increases the cost of adapting established screens later;
- encourages inconsistent solutions across collection features.

This option is rejected.

---

## Introduce Pagination and Advanced Collection Features Immediately

Adopt Paging and implement sorting, grouping, tagging, and other large-collection
features before demonstrated product need.

### Pros

- prepares for very large data sets immediately;
- establishes advanced collection infrastructure early.

### Cons

- adds state, dependency, repository, and testing complexity without evidence;
- increases maintenance cost during an early product stage;
- risks designing abstractions around hypothetical rather than observed needs.

This option is rejected.

---

## Solve Collection Constraints Independently Per Screen

Allow each feature to choose unrelated rules for long content, actions,
large-font behavior, and collection growth.

### Pros

- maximizes local implementation freedom;
- avoids a cross-cutting project decision.

### Cons

- produces inconsistent operational UX;
- duplicates reasoning and validation effort;
- makes future shared components and design-system evolution harder;
- permits the same structural failure to recur in multiple features.

This option is rejected.

---

# Consequences

## Positive

- collection features share explicit scalability assumptions;
- accessibility and narrow-layout behavior are treated as structural concerns
  rather than typography defects;
- long operational values are supported as normal product data;
- current simple local persistence remains valid while pagination stays
  evidence-driven;
- future search, filter, sort, and grouping evolution has a defined architectural
  direction without requiring premature implementation;
- collection-specific adaptive layouts remain owned by their features.

## Negative

- collection UI requires additional validation beyond ideal fixture content;
- some item layouts may need adaptive behavior instead of a single fixed
  arrangement;
- feature state models may require deliberate query modeling as collection
  capabilities grow;
- future large-data evidence may still require introducing Paging or another
  incremental-loading strategy.

---

# References

- ADR-002: Application Architecture
- ADR-015: Platform-Neutral Remote Systems Product Direction
- `docs/review/VISUAL_IDENTITY_CALIBRATION_REVIEW.md`
- GitHub Issue #157: Visual Identity Calibration
- GitHub Issue #161: Collection UI resilience under constrained space

---

# Notes

This ADR establishes the durable collection contract only.

It does not mandate a specific responsive Server Inventory card layout, overflow
menu, grouping model, sorting model, tagging system, or pagination library.

Those choices remain feature-level implementation decisions and must be justified
by the applicable product and engineering evidence.

Acceptance of this ADR does not expand the implementation scope of
`feature/visual-identity-calibration`.
