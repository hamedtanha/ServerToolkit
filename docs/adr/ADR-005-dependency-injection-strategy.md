# ADR-005: Dependency Injection Strategy

**Status:** Accepted

**Date:** 2026-07-02

---

# Context

Server Toolkit is being developed as a production-quality Android application using Kotlin, Jetpack Compose, MVVM, the Repository Pattern, and local persistence with Room.

The application will contain multiple layers and components, including:

- UI screens
- ViewModels
- repositories
- Room database access
- application-level services
- feature-specific dependencies
- future SSH and infrastructure-management services

Without a clear dependency injection strategy, object creation would quickly become inconsistent across the project. This would increase coupling, reduce testability, and make feature growth harder to manage.

A dependency injection strategy is required before implementing the first technical foundation components.

---

# Decision

Server Toolkit will use **Hilt** as the dependency injection framework.

Hilt will be used to provide and manage application dependencies across Android components, including:

- application-level dependencies
- repositories
- Room database instances
- DAO instances
- ViewModels
- feature-level services where appropriate

The project will use Hilt incrementally.

The first implementation step will only introduce Hilt and the application class. Additional dependencies such as Room repositories and feature services will be added when their corresponding features are implemented.

---

# Dependency Injection Rules

## General Rules

- Dependencies must be injected instead of manually constructed inside consumers.
- ViewModels must receive dependencies through constructor injection.
- Repositories must depend on abstractions where that improves testability and maintainability.
- Hilt modules must remain focused and small.
- Hilt must not be used to hide poor architecture or unnecessary indirection.
- Dependency scopes must be explicit and justified.

## Application Scope

Application-wide dependencies may use `@Singleton` when they represent a single shared instance across the app lifecycle.

Examples:

- Room database
- DAO providers
- repository implementations when appropriate
- application-level configuration providers

## ViewModel Scope

ViewModels will use Hilt integration with AndroidX Lifecycle.

ViewModels must not directly access Android framework APIs unless technically justified.

## Feature Scope

Feature-specific dependencies should remain close to their feature package unless they are shared across multiple features.

Shared dependencies must be promoted deliberately, not prematurely.

---

# Alternatives Considered

## Manual Dependency Injection

Use manual constructors, factory objects, and application-level containers.

### Pros

- No additional framework dependency
- Full control over object creation
- Simple for very small projects

### Cons

- Becomes verbose as the project grows
- Easy to implement inconsistently
- More boilerplate for ViewModels and Android components
- Higher risk of hidden coupling
- More maintenance cost as features expand

Rejected.

Manual dependency injection is acceptable for very small applications, but Server Toolkit is expected to grow into a multi-feature infrastructure-management application. Manual DI would become a liability.

---

## Koin

Use Koin as a Kotlin-first dependency injection framework.

### Pros

- Simple setup
- Kotlin-friendly DSL
- Less boilerplate than some alternatives
- Fast to adopt

### Cons

- Runtime resolution instead of compile-time validation
- Less aligned with Google's Android recommendations than Hilt
- Weaker integration with standard Android architecture tooling
- Higher risk of dependency errors appearing later in execution

Rejected.

Koin is convenient, but convenience is not the primary goal of this project. Compile-time safety and Android ecosystem alignment are more important.

---

## Hilt

Use Hilt as the dependency injection framework.

### Pros

- Officially recommended Android DI solution
- Strong Android integration
- Good support for ViewModels
- Works well with Room and Repository Pattern
- Compile-time dependency graph validation
- Scales well for production Android applications

### Cons

- Additional setup complexity
- Generated code can make build errors more verbose
- Requires clear module organization to avoid overuse
- Adds annotation processing dependency

Accepted.

Hilt provides the best long-term balance between maintainability, testability, Android integration, and production-readiness.

---

# Consequences

## Positive

- Consistent dependency creation across the project
- Improved testability
- Better ViewModel and Repository integration
- Clearer ownership of shared dependencies
- Reduced manual wiring boilerplate
- Stronger compile-time validation
- Better scalability for future features

## Negative

- More initial setup work
- Additional build-time complexity
- Requires discipline to avoid oversized modules
- Generated-code errors may be harder for beginners to interpret

---

# Implementation Guidelines

Initial implementation will include:

- Hilt Gradle plugin
- Hilt Android dependency
- Kotlin annotation processing or KSP configuration depending on compatibility
- `ServerToolkitApplication`
- `@HiltAndroidApp`
- Manifest application registration

The first implementation step must not introduce unrelated dependencies such as Room, Navigation, or SSH libraries.

Those dependencies must be added in separate commits when their implementation phase begins.

---

# Out of Scope

This ADR does not decide:

- Room schema design
- Navigation graph implementation
- SSH library selection
- Remote API strategy
- Background work strategy
- Testing framework selection

These topics require separate decisions when they become necessary.

---

# References

- ADR-002: Application Architecture
- ADR-003: Local Persistence with Room
- ARCHITECTURE.md
- PACKAGE_STRUCTURE.md

---

# Notes

Dependency injection is an architectural tool, not a design goal.

Hilt must support clean architecture boundaries. It must not be used as an excuse to create unnecessary abstraction or global service objects.
