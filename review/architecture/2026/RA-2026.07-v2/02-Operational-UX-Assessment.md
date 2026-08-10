# Operational UX Assessment

> **Review ID:** `RA-2026.07-v2`
>
> **Status:** In Progress
>
> **Evidence baseline:** `ca10ef764b500e5f4a9c87e558ab45412832e9a9`
>
> **Governing Issue:** `#138`
>
> **Assessment scope:** Current Server navigation, SSH workflow ownership, lifecycle, session-bound UX, connection history, and adaptive-layout implications

## Purpose

This assessment records the implemented operational user experience and compares focused-screen and Server-workspace alternatives before any navigation, Compose, state-ownership, session-lifecycle, or adaptive-layout implementation is authorized.

The document distinguishes:

- verified current behavior;
- architectural constraints imposed by current security and lifecycle decisions;
- provisional target candidates;
- decisions that still require evidence.

It does not make a final product or architecture decision.

## Evidence Basis

The assessment inspected the following repository evidence at the recorded baseline:

- `navigation/AppNavHost.kt`;
- `navigation/AppDestinations.kt`;
- `MainActivity.kt`;
- `feature/serverinventory/presentation/screen/ServerInventoryScreen.kt`;
- `feature/ssh/presentation/screen/SshScreen.kt`;
- `feature/ssh/presentation/state/SshUiState.kt`;
- `feature/ssh/presentation/viewmodel/SshViewModel.kt`;
- `feature/ssh/presentation/screen/SshConnectionHistoryScreen.kt`;
- `feature/ssh/presentation/viewmodel/SshConnectionHistoryViewModel.kt`;
- ADR-009;
- ADR-013;
- ADR-015;
- ADR-016;
- repository code search for current adaptive-layout and window-size APIs.

## Executive Assessment

The implemented user journey is destination-oriented:

```text
Dashboard
  └── Server Inventory
        ├── Add Server
        ├── Edit Server
        └── SSH Workflow
              └── Connection History
```

There is no Server details destination and no Server workspace abstraction.

The Server Inventory screen presents each Server as a list card with direct `Connect`, `Edit`, and `Delete` actions. The `Connect` action navigates to a dedicated SSH destination carrying only `serverId`.

The SSH destination is a cohesive, session-bound workflow. One `SshViewModel` coordinates:

- the selected Server id;
- authentication method and non-sensitive presence state;
- private pending secret references;
- connection attempts;
- host-key review;
- active project-owned session handle;
- command input and execution;
- Saved Command selection;
- disconnect and workflow-exit cleanup.

The UI renders these responsibilities in one vertically scrollable screen.

Connection History is a separate destination with a separate ViewModel. Navigating from SSH to history first requests workflow exit. Workflow exit closes the active SSH session before navigation proceeds.

Therefore, the current History destination is not a secondary panel inside an active Server operation context. It is a separate read-only workflow reached after session cleanup.

A future Server workspace is a plausible information-architecture target, but it is not a simple visual refactor. It would require explicit decisions about:

- parent and child state ownership;
- session lifetime while changing workspace sections;
- compact versus expanded navigation behavior;
- secret disposal;
- back behavior;
- history access while connected;
- process and configuration lifecycle;
- whether SSH remains a focused capability boundary.

## Current Navigation Model

### Verified Destination Graph

The app uses one `NavHost` with a Dashboard start destination.

Current destinations are:

```text
dashboard
server_inventory
add_server
edit_server/{serverId}
ssh/{serverId}
ssh_history/{serverId}
saved_commands
```

The SSH destination and Connection History destination each receive `serverId` independently through navigation arguments.

### Navigation Ownership

`AppNavHost` owns app-level destination transitions.

Feature routes receive callbacks and do not hold the `NavController`.

This is consistent with the current feature-first navigation boundary and should be preserved unless a concrete navigation architecture defect is demonstrated.

### No Server Details Destination

The current Server Inventory card exposes three direct actions:

- Connect;
- Edit;
- Delete.

Selecting the card itself does not open a Server overview, profile, detail, or workspace destination.

This means the product currently treats a Server primarily as:

- an inventory row;
- a connection target;
- an edit/delete subject.

There is no implemented place for platform facts, capabilities, trust summary, operation history, health, or operational actions to coexist under one Server context.

## Server Inventory UX

### Verified Behavior

The loaded inventory screen contains:

- title and server count;
- search;
- environment filters;
- favorites-only filter;
- Add Server;
- a vertical list of Server cards.

Each Server card displays:

- name;
- host and SSH port;
- optional SSH username;
- environment;
- Connect, Edit, and Delete actions.

### Current Strengths

- Actions are explicit.
- Connection is initiated from a concrete Server record.
- Edit and delete remain distinct from connection.
- List identity uses stable `server.id`.
- The navigation flow is easy to trace and test.

### Current Constraints

- The card action row may become crowded as capabilities grow.
- The list cannot host a meaningful operational overview without becoming overloaded.
- There is no user-facing Server context after leaving the inventory list beyond a raw Server id in current SSH copy.
- The current structure provides no destination for future profile layers.
- Direct actions do not define how additional capabilities should be discovered or grouped.

These constraints do not prove that a workspace is required immediately. They establish the point at which continued addition of direct card actions would stop scaling cleanly.

## SSH Workflow Ownership

### Route Responsibilities

`SshRoute` owns Android and Compose integration concerns:

- collecting `SshUiState`;
- creating the Android private-key source factory;
- launching the system content picker;
- converting a selected Android `Uri` to a project-owned one-shot source;
- intercepting Back;
- clearing authentication-input UI state on disposal;
- requesting workflow exit before navigation;
- routing user actions to the ViewModel.

### ViewModel Responsibilities

`SshViewModel` owns the current workflow coordination boundary:

- route-derived `serverId`;
- connection-attempt guard;
- command-execution guard;
- host-key-confirmation guard;
- session-close guard;
- Saved Command observation job;
- active project-owned `SshSessionHandle`;
- pending observed host key;
- pending one-shot private-key source;
- pending authentication secrets;
- connection, trust, execution, and cleanup orchestration;
- user-visible status and detail mapping through project-owned UI state.

### UI-State Responsibilities

`SshUiState` exposes non-sensitive presentation meaning:

- Server id;
- connection status;
- message and detail;
- optional host-key review;
- authentication method and presence flags;
- command execution state;
- Saved Command selector state;
- derived eligibility rules.

It does not expose raw passwords, private-key bytes, Android `Uri` values, SSHJ objects, or raw third-party exceptions.

### Data and Domain Responsibilities

Project-owned use cases and services own:

- connection attempt;
- host-trust confirmation;
- command execution;
- session close.

SSHJ and transport details remain outside presentation.

This separation is a strong current architecture boundary and must not be weakened by a workspace design.

## Current Screen Composition

The SSH screen uses one full-screen vertically scrollable `Column` with fixed `24.dp` padding.

Content is rendered sequentially:

```text
Title
Server id
Connection status
Message and detail
Host-key review, when required
Authentication method and secret input
Connect or Disconnect
Command input
Saved Command selector
Run command
Command status and output
Connection History
Back
```

Most major actions use full-width buttons.

### Consequences

- The workflow is understandable as a linear sequence.
- Compact-screen vertical scrolling is naturally supported.
- Trust, authentication, connection, command, and history entry are visible in one destination.
- As output grows, navigation controls remain below command output.
- The visual hierarchy does not distinguish persistent Server context from transient session operations.
- A raw Server id is shown instead of a richer Server identity summary.
- The screen has no explicit primary-detail or master-detail structure.
- The screen contains no adaptive-layout branch.

## Secret and Authentication UX Boundary

### Password

The password value is held in local Compose state and copied into a private ViewModel secret holder.

Observable UI state records only whether password input exists.

The password is cleared:

- after connect is initiated;
- when the authentication method changes;
- when authentication input is cleared;
- when the UI is disposed;
- on workflow transitions that clear authentication state.

### Private Key

The Android content picker returns a `Uri`, but the route immediately converts it into a project-owned one-shot source.

The ViewModel privately owns at most one pending source and one optional passphrase.

The source and passphrase are not placed in:

- `StateFlow`;
- `SavedStateHandle`;
- navigation arguments;
- Room;
- diagnostic output.

### UX Constraint

A workspace or adaptive navigation implementation must not move authentication secrets into shared observable workspace state.

Server context may be durable. Authentication material remains attempt-scoped.

Any design that attempts to make credentials a convenient Server Profile field conflicts with current accepted security boundaries.

## Session and Workflow Lifecycle

### Connection and Command Eligibility

The current UI allows authentication editing only when not:

- connecting;
- connected;
- disconnecting.

Command input is editable only when:

- connected;
- no command is running.

Saved Command selection follows the same command-input mutation boundary.

Command execution remains an explicit user action.

### Workflow Exit

Back handling follows this order:

1. close the Saved Command selector when visible;
2. otherwise request workflow exit;
3. block exit while connection or command execution is active;
4. close the active session;
5. navigate only after cleanup succeeds.

When session close fails or is cancelled, the active session handle is restored and navigation does not continue.

### Connection History Navigation

Opening Connection History also requests workflow exit.

Therefore:

```text
Open History
  -> close selector and clear authentication input
  -> block during active connection attempt or command execution
  -> close active SSH session
  -> navigate to history only after cleanup succeeds
```

This is a security and resource-lifecycle invariant, not incidental navigation code.

### ViewModel Clearing

`onCleared()` cancels Saved Command observation and clears pending authentication input.

Current normal navigation relies on explicit workflow-exit cleanup before leaving the SSH destination.

A future navigation structure must not assume that changing destinations or panes is harmless while a session is active.

## Connection History UX

Connection History is a read-only destination scoped by `serverId`.

It provides:

- loading state;
- empty state;
- failure state;
- chronological entry rendering from the repository;
- endpoint snapshot;
- username snapshot;
- status;
- timestamp;
- optional duration;
- optional normalized error;
- Back action.

The History ViewModel observes repository data independently of the SSH session ViewModel.

This separation is maintainable and should not be replaced by direct ViewModel-to-ViewModel access.

## Durable and Transient State Classification

| State | Current owner | Current lifetime | Classification |
|---|---|---|---|
| Server inventory metadata | Server Inventory repository | Durable | Verified |
| Trusted SSH host key | SSH trust repository | Durable | Verified |
| Connection history | SSH history repository | Durable | Verified |
| Saved Commands | Saved Commands repository | Durable | Verified |
| Navigation `serverId` | Navigation argument / SavedStateHandle | Destination-restorable | Verified |
| Connection status | SSH ViewModel UI state | Workflow lifetime | Transient |
| Active session handle | SSH ViewModel private field | Active workflow/session | Transient |
| Password | Compose local state and private secret holder | One attempt | Sensitive transient |
| Private-key source | SSH ViewModel private field | One attempt | Sensitive transient |
| Private-key passphrase | Compose local state and private secret holder | One attempt | Sensitive transient |
| Pending observed host key | SSH ViewModel private field | Trust-review transition | Transient |
| Command input | SSH UI state | Workflow lifetime | Transient |
| Command output/result | SSH UI state | Workflow lifetime | Transient |
| Saved Command selector | SSH UI state and observation job | Selector visibility | Transient |

A future Server workspace must not convert transient or sensitive state into durable Server Profile state accidentally.

## Adaptive-Layout Evidence

Repository code search found no current implementation of:

- `WindowSizeClass`;
- adaptive navigation components;
- a list-detail scaffold;
- a supporting-pane scaffold;
- compact, medium, or expanded layout branches.

`MainActivity` renders the theme and `AppNavHost` directly.

### Classification

```text
Verified absence of adaptive-layout implementation
```

The review may assess adaptive alternatives, but it cannot claim any current compact/expanded support beyond Compose layouts naturally occupying available space.

## Compact Layout Assessment

The current single-destination model is suitable as the compact baseline:

- one task-focused screen at a time;
- explicit Back behavior;
- vertical scrolling;
- no simultaneous panes;
- predictable session cleanup when leaving SSH.

A future compact Server workspace should preserve single-pane navigation.

It should not force a desktop-style multi-pane shell onto narrow screens.

A plausible compact flow is:

```text
Server Inventory
  -> Server Overview or Operations
       -> SSH
       -> History
       -> future capabilities
```

Whether an intermediate Server Overview provides enough value to justify the extra navigation step remains a product decision.

## Expanded Layout Assessment

An expanded layout could use a persistent Server context area and one active content pane.

A plausible target structure is:

```text
Server context / section selection
        |
        +-- Overview
        +-- Connection
        +-- History
        +-- future verified capabilities

Active content pane
```

Potential benefits:

- Server identity remains visible;
- operations are grouped under one Server;
- history can be inspected without returning to the inventory list;
- future profile layers have a coherent location;
- capability growth does not add more inventory-card buttons.

Risks:

- session lifetime may become ambiguous when switching sections;
- secret input may survive longer than intended;
- parent-scoped state may become overly broad;
- a generic workspace ViewModel may become a God object;
- compact and expanded behavior may diverge semantically;
- supporting panes may tempt the UI to expose unimplemented profile or capability claims.

### Classification

```text
Plausible target candidate; exact adaptive behavior needs implementation evidence
```

No specific breakpoint, Material adaptive component, pane strategy, or navigation API is accepted by this assessment.

## Workspace Ownership Alternatives

### Alternative A — Preserve Focused Destinations

Description:

- keep Inventory, SSH, and History as separate destinations;
- continue direct Connect, Edit, and Delete actions;
- close the session before leaving SSH.

Advantages:

- current behavior;
- simple state ownership;
- clear session cleanup;
- low migration risk;
- existing tests and lifecycle rules remain applicable.

Limitations:

- Server context is fragmented;
- history requires leaving the active SSH workflow;
- future capabilities may crowd inventory actions;
- no home for Server Profile or capability summaries;
- expanded displays remain underused.

Assessment:

```text
Verified current baseline; viable until additional Server-scoped capabilities exist
```

### Alternative B — Server Workspace Shell with Capability-Owned Content

Description:

- add a Server-scoped parent destination or navigation scope;
- parent owns only stable Server context and section selection;
- SSH retains a capability-owned ViewModel and lifecycle;
- History retains repository-owned read-only state;
- compact mode uses one child destination at a time;
- expanded mode may render Server context and active content together.

Advantages:

- creates a coherent home for Server-scoped information;
- preserves feature ownership;
- can support compact and expanded presentation;
- avoids placing SSH internals in generic Server state;
- scales better when more Server-scoped capabilities are implemented.

Limitations:

- requires explicit session behavior when switching sections;
- parent/child navigation and ViewModel scoping need careful design;
- may add an extra step before current Connect behavior;
- is premature if no additional Server-scoped capability is selected.

Assessment:

```text
Preferred target candidate if the product accepts multiple Server-scoped capabilities
```

### Alternative C — Parent Workspace Owns SSH Session and All Operational State

Description:

- one broad Server workspace ViewModel owns Server data, trust, credentials, session, command, history, and all future capabilities.

Advantages:

- superficially easy cross-section state access;
- one owner for the whole screen.

Limitations:

- creates a high-risk God ViewModel;
- expands secret and session lifetime;
- couples independent features;
- weakens repository and capability boundaries;
- makes lifecycle and testing substantially harder;
- conflicts with the restrained architecture direction.

Assessment:

```text
Rejected
```

### Alternative D — Application-Wide Persistent Operations Shell

Description:

- maintain SSH sessions and operational state outside feature navigation so they survive arbitrary app navigation.

Advantages:

- background-like session continuity;
- easy return to an existing operation.

Limitations:

- no accepted product requirement;
- major lifecycle, security, notification, process-death, and cleanup scope;
- risks implicit background execution;
- changes the existing explicit workflow model;
- requires separate architecture and security decisions.

Assessment:

```text
Rejected for the current project stage
```

## Provisional UX Direction

The following direction is provisional and must be confirmed in the decision-recommendation document:

1. Preserve the current focused-destination implementation until a concrete additional Server-scoped capability justifies a workspace.
2. Treat a Server workspace shell with capability-owned content as the preferred scalable target candidate.
3. Do not create a generic workspace ViewModel that owns SSH session, secrets, history, profile, and future capabilities.
4. Keep SSH session lifecycle inside the SSH capability boundary.
5. Keep authentication secrets attempt-scoped and outside shared workspace state.
6. Keep Connection History repository-owned and read-only.
7. Preserve explicit command execution and command-input blocking.
8. Preserve cleanup-before-exit unless a separately accepted session-continuity decision changes that rule.
9. Use single-pane semantics for compact layouts.
10. Consider context-plus-content presentation for expanded layouts only after device and window evidence is collected.
11. Do not introduce adaptive dependencies or pane frameworks solely to satisfy a theoretical architecture.
12. Do not expose platform or capability sections before implemented evidence exists.

## Session Behavior Requiring Decision

Before accepting a workspace target, the product must select one of these behaviors when the user leaves the active SSH section:

### Close-on-Section-Change

- preserves current lifecycle semantics;
- simplest cleanup model;
- history and other sections cannot coexist with an active session.

### Keep Session While Workspace Remains Active

- allows read-only sibling sections while connected;
- requires a stable SSH capability owner scoped above one visible pane;
- must define timeout, process death, Back, Server switch, app backgrounding, and cleanup behavior;
- may require a new ADR or amendment to existing SSH lifecycle decisions.

### Explicit Minimize or Detach Session

- creates a new user-visible session-management capability;
- requires active-session navigation, notification, retention, and cleanup policy;
- is outside current scope.

Current evidence supports only close-on-workflow-exit behavior.


## Remaining Product and UX Decision Assessment

### Additional Server-Scoped Capability

No concrete additional Server-scoped remote capability has been accepted after
the current SSH and Connection History workflows.

The roadmap intentionally leaves the first gateway-backed remote capability
unselected.

Therefore, current evidence does not justify introducing a Server Workspace
only to prepare for hypothetical future capabilities.

Assessment:

```text
Preserve focused destinations now.

Keep a capability-owned Server Workspace as a deferred target candidate rather
than an implementation commitment.
```

### History During an Active SSH Session

Current implementation closes the active SSH workflow before navigating to
Connection History.

No current product requirement or runtime evidence establishes that History
must remain accessible while an SSH session stays active.

Assessment:

```text
Preserve cleanup-before-navigation.

Defer session continuity across sections until a concrete workflow requires it.
```

This avoids expanding session lifetime, secret lifetime, process-death
behavior, and cleanup policy without user value.

### Minimum Server Context During Operations

The current SSH screen exposes a raw Server id as presentation context.

A future visual and UX polish pass may improve the visible Server context
without changing ownership or navigation architecture.

The minimum useful operational context is expected to be derived from existing
implemented inventory data rather than speculative profile information.

Candidate presentation data includes:

- Server display name;
- active host;
- SSH port;
- username when present.

This is a presentation concern unless it changes ownership, lifecycle, or
navigation semantics.

### Platform-Neutral UI Copy Finding

`AddServerViewModel` initializes the default `ServerFormUiState`.

That default state currently contains:

```text
Enter the connection details for a Linux server.
```

ADR-015 supersedes Linux-specific product-scope assumptions and defines
Server Toolkit as platform-neutral.

Classification:

```text
Verified production UI-copy inconsistency.
```

This review PR must not modify production Compose or presentation code.

The correction should be translated into a bounded implementation follow-up
after review acceptance.

The replacement copy must describe implemented behavior without claiming
unsupported platform compatibility.

### Adaptive Presentation Decision

No adaptive-layout framework is accepted by current evidence.

The current compact focused-destination semantics remain valid.

Exact medium and expanded layouts require runtime and visual evidence, but that
evidence is not required to accept a speculative workspace architecture because
the workspace itself remains deferred.

Representative window testing should therefore be performed as part of the
future visual/UX foundation or a focused adaptive-layout task before selecting:

- breakpoints;
- pane scaffolds;
- navigation rails;
- permanent Server context panes;
- adaptive dependencies.

Assessment:

```text
Compact focused navigation: preserve.

Medium/expanded visual structure: Needs More Evidence.

Adaptive framework selection: deferred.
```

### UX Review Conclusion

The review can reach a stable pre-visual-design boundary without implementing a
Server Workspace.

The accepted current UX baseline should preserve:

1. focused capability ownership;
2. SSH cleanup-before-navigation;
3. attempt-scoped secrets;
4. explicit Run-only command execution;
5. repository-owned read-only History;
6. single-pane compact semantics;
7. platform-neutral user-facing copy;
8. visual redesign freedom that does not silently alter architecture or
   lifecycle behavior.

This creates a safe boundary for a later visual UX foundation without forcing
unfinished Server Profile or workspace architecture into that effort.

## Required Next Evidence

No additional runtime UX evidence is required to preserve the current
focused-destination architecture for this review.

Before final UX recommendations:

- identify which accepted navigation or lifecycle statements require an ADR;
- keep Server switching inside the current destination model unless a future
  workspace requirement introduces an explicit in-workspace Server switch;
- keep session continuity across sections deferred because no current product
  requirement justifies changing cleanup-before-navigation.

The following evidence is explicitly deferred to the future visual/UX
foundation or a separately bounded adaptive-layout task:

- representative narrow, medium, and expanded Android window testing;
- command-output readability and action reachability with large output;
- accessibility, keyboard, landscape, and large-screen behavior;
- exact Back and Up presentation for any future expanded workspace;
- whether an intermediate Server Overview improves the primary Connect flow;
- whether internal SSH Compose decomposition improves presentation without
  changing ownership or lifecycle;
- selection of adaptive breakpoints, pane scaffolds, navigation components, or
  new adaptive dependencies.

These deferred items must not be treated as implemented behavior or as blockers
for accepting the current focused-navigation baseline.

## ADR Impact Assessment

### No ADR Required Yet

Creating this review assessment does not require an ADR.

Pure internal Compose decomposition without changed ownership or behavior would not normally require an ADR.

### ADR Review Required

A new ADR or explicit update to current SSH decisions is required before accepting:

- SSH session continuity across destination or section changes;
- a parent workspace owner for an active SSH capability;
- background or detached sessions;
- changed cleanup-before-navigation semantics;
- a generic operational-state owner spanning capabilities;
- persistent authentication references or credentials.

An adaptive layout implementation may remain a presentation decision when compact and expanded modes preserve the same ownership and lifecycle semantics.

## Assessment Boundary

This assessment does not authorize:

- a Server workspace destination;
- a Server overview screen;
- adaptive-layout dependencies;
- compact/expanded implementation;
- navigation graph changes;
- SshViewModel re-scoping;
- session continuity across sections;
- background or detached sessions;
- credential/profile persistence;
- SSH screen redesign;
- Room schema changes;
- new capability claims.

Final decisions remain pending focused persistence evidence, profile and capability assessment, and the decision-recommendation document.
