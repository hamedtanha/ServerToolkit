# Engineering Workflow

**Project:** ServerToolkit

**Document Version:** 1.0

**Status:** Active

**Last Updated:** 2026-07-03

---

# Purpose

This document defines the engineering workflow used throughout the ServerToolkit project.

Its purpose is to ensure that every architectural decision, source code change, documentation update, and release activity follows a consistent, maintainable, and production-quality process.

The workflow described in this document is mandatory for all future development within this repository.

---

# Engineering Principles

The ServerToolkit project follows a quality-first engineering philosophy.

The primary objectives are:

- Build maintainable software rather than quick solutions.
- Keep the project releasable at all times.
- Prefer readability over clever implementations.
- Prefer simplicity over unnecessary complexity.
- Treat documentation as part of the implementation.
- Minimize technical debt.
- Keep Git history meaningful and easy to review.
- Build features incrementally using small, reviewable commits.
- Validate every implementation through successful project builds.
- Protect long-term architecture consistency.

Every change must improve the overall quality of the project or preserve the existing quality level.

No implementation should intentionally introduce architectural inconsistencies.

---

# Development Workflow

Development in ServerToolkit follows a small-step, reviewable workflow.

The goal is to keep the project buildable, understandable, and easy to review after every change.

Every development task should follow this sequence:

1. Understand the scope of the change.
2. Verify the current Git status.
3. Make the smallest meaningful change.
4. Build the project.
5. Review the diff.
6. Commit with a clear Conventional Commit message.
7. Re-check the working tree.
8. Update related documentation when required.

Large changes must be split into smaller commits whenever possible.

A single commit should represent one clear engineering step.

Examples of acceptable commit scopes include:

- Adding a dependency.
- Creating an application bootstrap class.
- Adding a navigation destination.
- Creating a feature skeleton.
- Adding a UI state model.
- Connecting a screen to the navigation graph.
- Updating one documentation section.

Avoid commits that mix unrelated changes such as code, formatting, documentation, and architecture changes together.

The main branch must remain releasable at all times.

Feature work must happen on short-lived branches according to GitHub Flow.
