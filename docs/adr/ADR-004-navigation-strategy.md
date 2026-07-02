# ADR-004: Navigation Strategy

**Status:** Accepted

**Date:** 2026-07-02

---

# Context

Server Toolkit is a modern Android application focused on infrastructure management workflows.

The application will grow beyond a single screen and will include multiple functional areas, such as:

- Dashboard
- Server inventory
- Server details
- Server creation and editing
- Settings
- Future SSH workflows
- Future monitoring workflows
- Future command execution workflows
- Future Xray and x-ui management workflows

Navigation must be defined early because it affects screen structure, feature boundaries, ViewModel ownership, state restoration, deep links, testing, and long-term maintainability.

The first implementation scope is intentionally small, but the navigation design must not become a short-term shortcut that blocks future features.

The project requires a navigation approach that is simple enough for Sprint 1 while still compatible with future expansion.

---

# Decision

Server Toolkit will use Jetpack Navigation Compose as the primary navigation solution.

Navigation will be implemented with a single application-level `NavHost` owned by the app shell.

Each major screen will be represented by a typed route definition instead of hard-coded string routes scattered across the UI.

Initial top-level destinations are:

- Dashboard
- Servers
- Settings

Initial server-related destinations are:

- Server list
- Add server
- Edit server
- Server details

The navigation graph will be organized around feature-level route groups, but without introducing unnecessary abstraction or over-engineered navigation frameworks.

Screen composables must not directly construct arbitrary navigation paths. Navigation actions should be expressed through clearly named callbacks passed from the navigation layer into the screen layer.

Example principle:

```kotlin
ServerListScreen(
    onAddServerClick = { /* navigation handled outside the screen */ },
    onServerClick = { serverId -> /* navigation handled outside the screen */ }
)
```

This keeps UI components reusable, previewable, and easier to test.

---

# Navigation Principles

## Single App-Level NavHost

The application will start with one `NavHost` at the app shell level.

This avoids premature complexity while providing a clear central location for navigation behavior.

Nested navigation graphs may be introduced later when feature complexity justifies them.

## Typed Route Definitions

Routes must be defined in one location per feature or navigation module.

Hard-coded route strings must not be duplicated across composables.

Route arguments must be explicit and minimal.

For Sprint 1, server identity may be passed as a server ID.

## Navigation Outside Screen Composables

Screen composables should expose events through callbacks.

They should not depend directly on `NavController` unless there is a strong technical reason.

This separation improves:

- testability
- previewability
- reusability
- UI purity
- long-term maintainability

## Bottom Navigation

The application may use bottom navigation for primary top-level destinations if it improves usability.

Initial top-level destinations are:

- Dashboard
- Servers
- Settings

Bottom navigation must only contain stable top-level destinations.

Transactional screens such as Add Server or Edit Server must not appear as bottom navigation items.

## Back Stack Behavior

Back navigation must follow Android platform expectations.

Add and Edit screens should return to the previous server-related screen after completion or cancellation.

Top-level destination switching should avoid creating excessive duplicate destinations in the back stack.

## Deep Links

Deep links are not part of Sprint 1.

The route design should not prevent future deep link support, but deep link implementation will be deferred until there is a concrete product requirement.

---

# Alternatives Considered

## Manual Navigation State

Manage the current screen manually with Compose state and conditional rendering.

### Pros

- Simple for very small applications
- No additional navigation dependency
- Easy to understand initially

### Cons

- Poor scalability
- Weak back stack handling
- Harder state restoration
- Difficult deep link support
- Increased risk of inconsistent navigation behavior
- Not aligned with standard Android app navigation practices

Rejected.

Manual navigation is too fragile for a production-quality application that is expected to grow beyond a few screens.

---

## Jetpack Navigation Compose

Use the official Jetpack Navigation integration for Compose.

### Pros

- Official Android navigation solution
- Good Compose integration
- Supports back stack management
- Supports route arguments
- Supports nested graphs when needed
- Compatible with future deep links
- Familiar to Android developers
- Suitable for testing and maintainability

### Cons

- Requires disciplined route management
- String-based routes can become error-prone if unmanaged
- Can become messy if navigation logic leaks into screen composables

Accepted.

Jetpack Navigation Compose provides the best balance between simplicity, official support, and long-term scalability.

---

## Third-Party Navigation Framework

Use a third-party Compose navigation framework.

### Pros

- Some frameworks provide stronger typed route APIs
- May reduce boilerplate
- May offer advanced navigation patterns

### Cons

- Additional dependency risk
- Smaller ecosystem than Jetpack Navigation
- Potential long-term maintenance concerns
- Unnecessary complexity for the current project scope
- Less aligned with official Android recommendations

Rejected.

The project does not currently require capabilities that justify introducing a third-party navigation framework.

---

# Consequences

## Positive

- Navigation follows standard Android practices.
- The app has a clear navigation structure from the beginning.
- Screens remain decoupled from direct `NavController` usage.
- The navigation approach supports future feature expansion.
- Testing and Compose previews remain easier to maintain.
- Back stack behavior can follow Android user expectations.

## Negative

- Route definitions require discipline and consistency.
- Navigation boilerplate is unavoidable.
- Poorly managed route strings could create maintenance problems.
- Future nested graphs may require refactoring if feature complexity increases significantly.

---

# Implementation Guidelines

- Define routes centrally instead of duplicating strings.
- Keep screen composables free from direct navigation implementation details.
- Pass navigation events as named callbacks.
- Keep top-level destinations stable and limited.
- Do not expose transactional screens in bottom navigation.
- Avoid introducing nested graphs until there is a clear need.
- Avoid third-party navigation libraries unless a future ADR justifies the change.

---

# Sprint 1 Scope

Sprint 1 will implement only the navigation needed for Server Inventory.

Required destinations:

- Dashboard
- Server list
- Add server
- Edit server
- Server details
- Settings

Deep links, multi-pane layouts, tablet-specific navigation, and nested feature graphs are outside Sprint 1.

---

# References

- ADR-001-project-vision.md
- ADR-002-application-architecture.md
- ADR-003-local-persistence-with-room.md
- ARCHITECTURE.md
- ROADMAP.md

---

# Notes

This decision should remain stable unless the application grows to a point where the current navigation model becomes a real maintainability problem.

If a different navigation framework or a substantially different navigation model is introduced later, a new ADR must supersede this decision.
