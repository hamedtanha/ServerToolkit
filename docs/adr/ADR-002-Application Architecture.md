# ADR-002: Application Architecture

**Status:** Accepted

**Date:** 2026-07-02

---

# Context

Server Toolkit is intended to become a production-quality Android application for Linux server administration and infrastructure management.

Before feature implementation begins, the project requires a stable architectural foundation that supports long-term maintainability, scalability, readability, and testability.

The architecture should align with modern Android development recommendations while avoiding unnecessary complexity during the early stages of the project.

---

# Decision

The project adopts Google's recommended Android application architecture with the following baseline:

- Kotlin
- Jetpack Compose
- Single Activity architecture
- MVVM for the presentation layer
- Repository Pattern for data access
- Unidirectional Data Flow (UDF)

The project initially uses a single Android application module.

Package boundaries are designed to allow future modularization if the project grows significantly.

The Domain layer is intentionally omitted from the initial implementation and will only be introduced if increasing business complexity justifies it.

Major technology selections, including dependency injection, persistence, secure storage, and SSH implementation, are intentionally deferred to dedicated ADRs.

---

# Alternatives Considered

## Clean Architecture with Mandatory Domain Layer

### Pros

- Strong separation of concerns
- Highly testable
- Suitable for very large projects

### Cons

- Introduces unnecessary complexity for the current project size
- Increases boilerplate
- Slows early development

Rejected for the initial implementation.

---

## Multi-Module Architecture from the Beginning

### Pros

- Strong isolation between features
- Faster incremental builds for large projects
- Clear ownership boundaries

### Cons

- Higher maintenance cost
- Increased Gradle complexity
- Unnecessary overhead during early development

Rejected for the initial implementation.

---

## Traditional MVC

### Pros

- Simple structure
- Low learning curve

### Cons

- Weak separation of concerns
- Difficult to scale
- Poor alignment with modern Android practices

Rejected.

---

# Consequences

## Positive

- Stable architectural foundation
- Simple initial project structure
- Alignment with Android best practices
- Lower implementation complexity
- Easier onboarding for contributors
- Clear evolution path toward future modularization

## Negative

- Future modularization may require refactoring
- Domain layer is deferred and may need to be introduced later
- Some technology decisions remain intentionally open until dedicated ADRs are accepted

---

# Related ADRs

Planned:

- ADR-003: Navigation Strategy
- ADR-004: Dependency Injection
- ADR-005: Persistence Strategy
- ADR-006: SSH Integration
- ADR-007: Project Structure

---

# References

- ARCHITECTURE.md
- PRODUCT_VISION.md
- ROADMAP.md
- DEVELOPMENT.md

---

# Notes

This ADR establishes the architectural baseline for the project.

Future architectural changes must supersede this ADR rather than modifying it directly.

Technology-specific decisions are intentionally documented in separate ADRs to keep this decision focused on the overall application architecture.