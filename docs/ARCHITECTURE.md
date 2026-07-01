# Architecture

**Project:** Server Toolkit

**Version:** 0.1.0

**Status:** Draft

**Last Updated:** 2026-07-01

---

# Overview

This document describes the high-level software architecture of the Server Toolkit application.

The primary objective of this architecture is to create a scalable, maintainable, testable, and secure Android application that follows modern Android development best practices.

---

# Design Goals

The architecture is designed with the following goals:

- Maintainability
- Scalability
- Readability
- Testability
- Security
- Performance

Every architectural decision should support at least one of these goals.

---

# Architecture Pattern

Server Toolkit follows Google's recommended Android application architecture.

The application is based on the MVVM (Model-View-ViewModel) pattern.

```
UI
│
▼
ViewModel
│
▼
Repository
│
├──────── Local Data Source
│
└──────── Remote Data Source
```

---

# Application Layers

## UI Layer

Responsible for rendering the user interface.

Contains:

- Compose Screens
- Reusable Components
- Navigation
- Theme

The UI layer must not communicate directly with databases or network services.

---

## Presentation Layer

Responsible for presentation logic.

Contains:

- ViewModels

Responsibilities:

- Preparing UI state
- Calling repositories
- Handling user actions
- Managing screen state

---

## Domain Layer

Currently not implemented.

This layer may be introduced if business logic becomes more complex.

Possible future responsibilities:

- Use Cases
- Validation
- Business Rules

---

## Data Layer

Responsible for providing data.

Contains:

- Repository
- Local Data Source
- Remote Data Source

The data layer hides implementation details from the presentation layer.

---

# Package Structure

```
de.hamedtanha.servertoolkit

├── data
│   ├── local
│   ├── remote
│   └── repository
│
├── model
│
├── navigation
│
├── ui
│   ├── components
│   ├── screens
│   └── theme
│
├── utils
│
└── viewmodel
```

---

# Navigation

The application follows a Single Activity architecture.

Each screen is implemented using Jetpack Compose Navigation.

The initial application screens are:

- Home
- Add Server
- Server Details
- Settings

Navigation decisions are documented in ADRs.

---

# Data Flow

Application data always flows in one direction.

```
User
   │
   ▼
Compose UI
   │
   ▼
ViewModel
   │
   ▼
Repository
   │
   ├──── Local
   └──── Remote
```

This architecture prevents tight coupling between layers.

---

# Dependency Rules

The following dependency rules must always be respected.

UI

↓

ViewModel

↓

Repository

↓

Data Sources

Higher layers must never depend on lower implementation details.

---

# Design Principles

The project follows these engineering principles.

- SOLID
- Separation of Concerns
- Single Responsibility Principle
- Dependency Inversion
- Single Source of Truth
- Unidirectional Data Flow

---

# Future Architecture

The following components may be introduced as the project grows.

- Dependency Injection (Hilt)
- Room Database
- Retrofit
- SSH Client
- Secure Storage
- WorkManager
- Background Synchronization

These components will only be introduced when required.

---

# Architecture Decisions

Significant architecture decisions are documented separately.

See:

- docs/adr/

---

# Related Documents

- PRODUCT_VISION.md
- DEVELOPMENT.md
- ROADMAP.md
- adr/README.md