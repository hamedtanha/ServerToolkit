# ADR-003: Local Persistence with Room

**Status:** Accepted

**Date:** 2026-07-02

---

# Context

Server Toolkit needs local data persistence for the first product feature: server inventory.

The application must store server records reliably on the device so users can create, view, edit, delete, and reuse server information across application launches.

The persistence solution must support:

- Structured local storage
- Type-safe database access
- Kotlin-first development
- Integration with Android architecture components
- Testability
- Future schema migrations
- Offline-first behavior

The project is a production-quality Android application, not a prototype. The persistence layer must therefore be maintainable, explicit, and suitable for long-term evolution.

---

# Decision

Server Toolkit will use **Room** as the local persistence library.

Room will be used as the abstraction layer over SQLite for structured local application data.

The initial database scope is limited to the server inventory feature.

The initial persisted entity will be:

- `ServerEntity`

The initial DAO will be:

- `ServerDao`

The initial database class will be:

- `ServerToolkitDatabase`

Room entities will remain internal to the data layer.

Domain models will be kept separate from database entities.

Repository classes will be responsible for coordinating access between the domain layer and the persistence layer.

Schema migrations must be handled explicitly whenever the database version changes.

Destructive migrations are not allowed for production releases unless there is a documented engineering reason and an accepted ADR.

---

# Alternatives Considered

## SharedPreferences / DataStore Only

Use SharedPreferences or Jetpack DataStore to persist server records.

### Pros

- Simple setup
- Low implementation overhead
- Suitable for small key-value settings

### Cons

- Poor fit for structured relational data
- Weak query capabilities
- Harder to evolve complex records
- Not suitable for server inventory management
- Increased risk of ad-hoc serialization logic

Rejected.

SharedPreferences and DataStore may still be used later for application preferences, but not for structured inventory data.

---

## Raw SQLite

Use SQLite directly without Room.

### Pros

- Full control over SQL
- No ORM abstraction
- Mature Android platform support

### Cons

- More boilerplate
- Higher risk of runtime SQL errors
- Less Kotlin-friendly
- Weaker integration with modern Android architecture
- More manual mapping and migration work

Rejected.

Raw SQLite gives control, but that control is not valuable enough for the current project scope.

---

## Room

Use Room as the local database abstraction over SQLite.

### Pros

- Recommended Android persistence solution for structured local data
- Compile-time validation of SQL queries
- Kotlin and coroutine support
- Works well with MVVM and Repository Pattern
- Supports migrations
- Good testability
- Suitable for offline-first workflows

### Cons

- Requires entity and DAO definitions
- Requires migration discipline
- Adds a persistence library dependency
- Introduces mapping between database entities and domain models

Accepted.

Room provides the best balance between maintainability, Android ecosystem alignment, and long-term scalability.

---

# Consequences

## Positive

- Server inventory data can persist across application launches.
- The data layer remains structured and testable.
- SQL queries can be validated at compile time.
- The project gains a scalable foundation for future local data features.
- The persistence approach aligns with modern Android development practices.
- Database migrations can be tracked and reviewed explicitly.

## Negative

- Additional boilerplate is required for entities, DAOs, database configuration, and mappers.
- Schema changes require migration planning.
- A clear boundary must be maintained between Room entities and domain models.
- Poor migration discipline could create production data-loss risks.

---

# Implementation Rules

- Room entities must not be exposed directly to UI code.
- ViewModels must not access DAOs directly.
- Repositories must mediate persistence access.
- Domain models and database entities must be mapped explicitly.
- Database version changes must include migrations.
- Production code must not rely on destructive migrations.
- Persistence tests must cover DAO behavior and repository mapping where practical.

---

# Initial Database Scope

The first database version will support only server inventory storage.

Out of scope for the initial database version:

- SSH credentials
- Private keys
- Password storage
- Cloud synchronization
- Monitoring history
- Command execution history
- Xray configuration storage
- Certificate management records

These capabilities require separate security and architecture decisions before implementation.

---

# References

- ADR-001-project-vision.md
- ADR-002-application-architecture.md
- ARCHITECTURE.md
- DEVELOPMENT.md
- ROADMAP.md

---

# Notes

Room is selected because Server Toolkit needs structured local persistence from the first real feature.

This ADR does not define the final server inventory schema. The exact entity fields may evolve during implementation, but the persistence technology decision remains Room unless superseded by a future ADR.
