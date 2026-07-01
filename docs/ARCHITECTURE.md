# Architecture

Server Toolkit follows Google's recommended Android architecture.

---

Architecture Pattern

MVVM

```
UI
│
▼
ViewModel
│
▼
Repository
│
├──────── Local
│
└──────── Remote
```

---

Packages

model

Business models

viewmodel

Presentation logic

repository

Data provider

remote

Network layer

local

Database layer

navigation

Navigation graph

ui

Compose UI

utils

Shared utilities

---

Design Principles

- SOLID
- Clean Architecture (lightweight)
- Single Source of Truth
- Unidirectional Data Flow