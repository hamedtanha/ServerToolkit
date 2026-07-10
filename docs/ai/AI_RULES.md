# AI Rules

These rules define how AI assistants should collaborate on the Server Toolkit project.

---

## General

Treat this project as a production-quality software product.

Do not treat this project as a tutorial, prototype, experiment, or code playground.

AI assistance may include explanations for learning purposes, but the delivered output must always remain suitable for a maintainable production codebase.

---

## Learning-Oriented Development

The maintainer has prior programming experience mainly with C and Pascal from university-level development.

During implementation, AI assistants should support the maintainer in gradually learning modern software engineering concepts, especially:

* Object-Oriented Programming
* Kotlin language concepts
* Android application architecture
* MVVM
* Repository Pattern
* Jetpack Compose
* Separation of Concerns
* Testable code structure

Learning support must never reduce engineering quality.

Explanations should be connected directly to the actual Server Toolkit codebase, architecture, and implementation tasks.

Avoid disconnected tutorials unless they are explicitly requested.

When introducing a new concept, explain:

* Why it is needed
* Where it belongs in the architecture
* How it differs from procedural programming
* What bad design it prevents
* How it supports maintainability

Code examples must remain aligned with the existing project architecture and should not introduce artificial examples that do not belong to the codebase.

---

## Repository Source of Truth

The current files in the Git repository are the authoritative source of truth.

When repository documentation conflicts with previous chat context, uploaded documentation snapshots, assistant memory, or earlier recommendations, the repository documentation takes precedence.

Before making architecture, implementation, or documentation recommendations, AI assistants should inspect the relevant current repository files whenever possible.

Do not rely on stale project documentation, previous conversation summaries, or uploaded snapshots when current repository files are available.

If the current repository state is unknown, ask the maintainer to provide the relevant files or command output before making project-changing recommendations.

## Project Context Reconstruction

Before recommending the next project step, a new ADR, a new project document, or an architecture change, AI assistants must reconstruct the current project context from the repository.

At minimum, inspect:

1. `docs/PROJECT_STATE.md`.
2. The relevant focused documents under `docs/state/`.
3. `docs/adr/README.md` and the relevant accepted or draft ADRs.
4. Recent merged pull requests and commits that changed the affected area.
5. Relevant engineering review records under `docs/review/`.
6. `docs/ai/AI_RULES.md` and `docs/ai/AI_MEMORY.md` when the recommendation affects project workflow, continuity, or AI collaboration.

Before recommending a new ADR or document, verify that the proposed content is not already covered by an accepted decision, focused state document, closed review finding, or existing implementation boundary.

Before reopening a closed review area, identify the concrete trigger, runtime evidence, repository change, or new scope that justifies reopening it.

Do not infer the current project phase from one document, one pull request, a conversation summary, or assistant memory alone.

State the reconstructed current phase and the evidence that supports the recommendation before proposing project-changing work.

## Change Impact Workflow

Before recommending or applying any source, test, or documentation change, AI assistants must follow this workflow:

1. Define the scope.
2. Identify affected source and test files.
3. Identify affected documentation.
4. Decide explicitly which documents change and which documents do not change.
5. Implement only inside the agreed scope.
6. Run validation appropriate to the change.
7. Run a documentation synchronization gate.
8. Commit only after all decisions are recorded.

For each relevant document, the assistant must state whether it changes or remains unchanged and why.

Do not treat documentation synchronization as an afterthought.

Do not proceed directly to implementation when the repository state, affected files, affected documentation, or validation scope is unclear.

## Architecture

Preserve the existing architecture unless there is a clear technical reason to change it.

Avoid introducing unnecessary abstraction.

Never redesign the architecture for convenience, personal preference, or short-term speed.

Every significant architectural or technical decision requires an Architecture Decision Record.

Prefer:

* Kotlin
* Jetpack Compose
* MVVM
* Repository Pattern
* SOLID principles
* Single Responsibility Principle
* Google's Android recommendations

---

## Documentation

Documentation is part of the implementation.

Keep documentation synchronized with the source code.

Never document functionality that does not exist.

Never leave implemented functionality undocumented.

Whenever one document changes, review related documents for consistency.

Prefer complete, production-ready document replacements over fragmented edits when practical.

All project documentation must be written in professional English.

All code comments must be written in English.

---

## Code

Generate production-quality code.

Prioritize:

* readability
* maintainability
* simplicity
* security
* scalability
* testability

Avoid quick fixes unless they are explicitly requested and clearly documented as temporary.

Do not introduce clever code when simple code is sufficient.

Do not introduce abstractions before there is a real architectural need.

Keep responsibilities small, explicit, and easy to review.

---

## Git

Use:

* GitHub Flow
* Conventional Commits
* Semantic Versioning

Never suggest direct development on the main branch.

Keep the main branch releasable at all times.

Every commit should represent a coherent and reviewable change.

---

## Review

When reviewing code or documentation:

* identify strengths
* identify weaknesses
* identify inconsistencies
* explain trade-offs
* recommend the best long-term solution

Do not approve weak design only because it works locally.

Working code is not enough. The solution must also be maintainable, understandable, and consistent with the project architecture.

---

## Documentation Maintainer

Actively monitor consistency across the entire documentation set.

If a document becomes inconsistent with another document, recommend updates to every affected document.

Never optimize one document while leaving the project documentation inconsistent.

Documentation must describe the current implementation, not planned or imagined functionality.

---

## Technical Leadership

Continuously evaluate:

* architecture
* documentation
* maintainability
* scalability
* technical debt
* security
* consistency

Raise concerns proactively.

Prefer long-term engineering quality over short-term implementation speed.

Respect previous engineering decisions, but challenge them when there is clear technical evidence that a better long-term solution exists.
