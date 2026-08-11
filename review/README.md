# Repository Reviews

## Purpose

This directory contains versioned, evidence-backed assessments of the ServerToolkit repository.

Living current-state documentation remains under `docs/`. Review records capture time-bound evidence, observations, findings, assessments, and proposals against an explicit repository baseline.

## Authority Boundary

Review records are not executable specifications and do not authorize implementation by themselves.

The authority order is:

1. repository implementation and configuration;
2. current-state documents under `docs/`;
3. accepted ADRs for durable decision rationale;
4. published review evidence and recommendations.

Accepted recommendations must be translated into the appropriate ADRs, current-state documents, roadmap decisions, and focused implementation Issues before production changes begin.

## Review Lifecycle

| Status | Meaning | Change policy |
|---|---|---|
| `In Progress` | Evidence collection, assessment, or review content is still evolving. | Review content may change and must be revalidated. |
| `Accepted` | Scope, evidence, findings, and conclusions are approved and merge-ready. | Review content is frozen; only publication metadata may change. A substantive correction returns the review to `In Progress` and requires re-review. |
| `Published` | The accepted review version has been merged into `main` through its publication pull request. | The review version is immutable. Corrections or changed conclusions require a new review version. |

## Immutability

A `Published` review version is immutable.

Corrections, later evidence, changed conclusions, or a new assessment require a new review version. Review files may evolve while `In Progress`; accepted files remain content-frozen until publication.

## Naming

Architecture review versions use:

```text
review/architecture/<year>/RA-<year>.<month>-v<number>/
```

Each review records:

- exact evidence baseline;
- scope and non-goals;
- evidence hierarchy;
- confidence labels;
- findings and recommendations;
- decision and publication status.

## Current Review

The current ServerToolkit architecture review is:

```text
review/architecture/2026/RA-2026.07-v2/
```

It is governed by GitHub Issue `#138`. The accepted package was merged through PR `#139` as `8070830dfae14f908b9dd128846f66112b36423e`; this metadata-only publication change records the review as `Published`. Once the publication change reaches `main`, the review is immutable and Issue `#138` can close as completed.

The previously published architecture-knowledge review remains immutable at:

```text
review/architecture/2026/RA-2026.07-v1/
```
