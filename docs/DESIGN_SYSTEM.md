# Design System

**Project:** Server Toolkit
**Status:** Active — Accepted Visual Identity Baseline
**Last Updated:** 2026-08-12

---

## Purpose

This document defines the design-system boundaries, foundation-validation
process, visual-profile exploration, token derivation rules, validation method,
and acceptance criteria for Server Toolkit.

Its purpose is to keep visual-design reasoning in the repository rather than
only in temporary conversations or implementation experiments.

This is a living design document, not an Architecture Decision Record.

---

## Scope

The Server Toolkit design system owns reusable visual decisions that apply
across the application.

Current concerns include:

- Material 3 color roles;
- typography;
- shapes;
- spacing;
- light and dark visual profiles;
- dynamic-color policy;
- shared visual patterns only when repeated evidence justifies them.

Material 3 remains the underlying component and theming foundation.

The design system must remain independent from feature ViewModels, domain
models, repositories, transports, providers, and platform-specific behavior.

---

## Non-Goals

Visual-identity calibration is not a screen-redesign initiative.

The current phase does not authorize:

- Dashboard information-architecture redesign;
- Server Inventory restructuring;
- Server Form workflow redesign;
- SSH workflow restructuring;
- Saved Commands workflow redesign;
- navigation architecture changes;
- new feature ownership;
- new session or credential lifecycle semantics;
- broad shared-component architecture;
- speculative semantic-token systems;
- custom motion infrastructure.

Screen-level defects discovered during calibration are classified separately
from design-token problems.

---

## Foundation

### PR #147 — Design-System Foundation

PR #147 established:

- a dedicated design-system theme package;
- project-owned Material 3 typography and shapes;
- project-owned spacing through `CompositionLocal`;
- a lightweight design-system boundary inside the existing app module.

It intentionally introduced no visual change.

### PR #148 — Resolved Visual Profile

PR #148 established `ServerToolkitVisualProfile` as the resolved runtime
contract for:

- light and dark color schemes;
- typography;
- shapes;
- spacing;
- dynamic-color policy.

Design-time heuristic reasoning remains outside the production runtime model.

```text
Design-time reasoning
        ↓
Resolved visual tokens
        ↓
ServerToolkitVisualProfile
        ↓
ServerToolkitTheme
        ↓
Existing Compose UI
```

---

## Visual Identity Workflow

Visual identity is developed in this order:

```text
Design-system foundation
        ↓
Visual identity exploration
        ↓
Compare candidate style profiles
        ↓
Select a working direction
        ↓
Derive tokens systematically
        ↓
Apply tokens to existing UI
        ↓
Stress-test across existing screens
        ↓
Calibrate tokens
        ↓
Accept visual identity baseline
```

Existing screens remain the calibration baseline.

A screen must not be redesigned merely to make a token candidate appear
successful.

---

## Product Visual Character

Server Toolkit is an operational tool for technical users.

The intended visual character is:

- professional;
- technical;
- trustworthy;
- operational;
- calm;
- restrained;
- information-dense but readable;
- platform-neutral.

The product should avoid becoming:

- playful;
- decorative;
- consumer-oriented;
- candy-colored;
- excessively rounded;
- terminal-themed;
- hacker-themed;
- visually tied to Linux, SSH, or one infrastructure platform.

---

## Style-Profile Dimensions

The following dimensions guide design-time evaluation.

They are heuristic design parameters, not runtime application properties.

| Dimension | Working Target | Interpretation |
|---|---:|---|
| Roundedness | 4 / 10 | Moderate corners; avoid pill-heavy presentation |
| Density | 7 / 10 | Compact and operational without becoming cramped |
| Contrast | 8 / 10 | Strong hierarchy and state readability |
| Motion | 2 / 10 | Restrained and functional |
| Materialness | 8 / 10 | Strong Material 3 alignment without template-like identity |

These values are provisional and may change from runtime evidence.

---

## Candidate Evaluation

A candidate direction must first satisfy all hard constraints.

### Hard Constraints

A candidate must:

- remain usable in light and dark themes;
- preserve accessible text and control contrast;
- not rely on color alone for operational state meaning;
- keep primary, secondary, and destructive actions distinguishable;
- preserve readability of machine-oriented values and command output;
- remain platform-neutral;
- avoid terminal, cyber, or hacker product identity;
- remain suitable for dense operational screens;
- preserve explicit user-triggered action semantics.

### Weighted Evaluation

Candidate comparison uses these priorities:

| Criterion | Weight |
|---|---:|
| Operational clarity | 20% |
| Trust and safety | 20% |
| Accessibility | 15% |
| Technical and product fit | 15% |
| Platform neutrality | 10% |
| Material 3 compatibility | 10% |
| Future capability scalability | 10% |

The scoring model guides comparison but does not replace runtime inspection.

---

## Validation Profile Directions

### Graphite + Azure

**Status:** Foundation validation completed; current calibration starting profile.

Characteristics:

- cool graphite and slate neutral surfaces;
- controlled Azure primary accent;
- limited cyan tertiary accent;
- strong primary-action visibility;
- high platform neutrality;
- professional operational character.

Current assessment:

Graphite + Azure provides a concrete, production-relevant profile for validating
the design-system foundation across real application screens.

It is not the final product visual identity. Exact token values may evolve
later through focused UI issues without changing the foundation contract.

### Graphite + Cyan

**Status:** Alternative candidate.

Strength:

- stronger technical distinctiveness.

Risk:

- excessive cyan can create a terminal, cyber, or hacker-oriented identity.

### Slate + Teal

**Status:** Alternative candidate.

Strength:

- calm and mature visual character.

Risk:

- potentially weaker primary-action emphasis than Azure.

---

## Calibration Starting Profile

PR #149 completed foundation validation using:

```text
Graphite + Azure
```

Graphite + Azure is now the starting profile for visual-identity calibration.
It is not yet the accepted final product visual identity baseline.

The validation profile uses explicit light and dark color roles.

Dynamic color remains disabled during visual-identity calibration so the resolved
profile can be evaluated consistently across devices.

Dynamic color may be reconsidered after the core visual identity is stable.

---

## Token Derivation

### Color

Color should:

- use cool neutral surfaces;
- use a controlled Azure primary accent;
- reserve strong saturation for meaningful emphasis;
- preserve strong foreground/background contrast;
- define light and dark roles explicitly;
- avoid platform-specific or terminal-specific metaphors.

Operational semantic colors are introduced only when implemented UI semantics
justify them.

### Typography

Typography should:

- preserve a compact professional hierarchy;
- use stronger weight rather than excessive size for hierarchy;
- remain readable on dense operational screens;
- use the Android/system font baseline unless evidence justifies another family;
- avoid global monospace styling.

Monospace may be used selectively for machine-oriented values or command
content when a concrete component requires it.

### Shapes

Shapes should:

- use moderate corner radii;
- avoid exaggerated soft or playful geometry;
- avoid making every control pill-shaped;
- remain compatible with Material 3 components.

Exact radii remain subject to calibration.

### Spacing

The current spacing scale remains:

```text
4 dp
8 dp
12 dp
16 dp
24 dp
```

Spacing is not mechanically migrated or globally changed during identity
calibration.

Existing UI provides evidence for whether the scale is suitable.

### Elevation

No project-owned elevation system is currently justified.

Material 3 remains the baseline.

### Motion

No project-owned motion system is currently justified.

Motion remains restrained and functional.

---

## Runtime Application Rule

Token calibration must be tested against existing UI before structural screen
changes are considered.

When a screen looks wrong, ask first:

```text
Is this a global token-system problem?
```

Only after that is excluded should the question become:

```text
Is this an independent screen-level UI defect?
```

Repeated problems across several screens provide evidence for changing global
tokens.

Problems isolated to one screen do not automatically justify changing global
tokens.

---

## Validation Screens

The working visual profile is stress-tested against the existing:

1. Dashboard;
2. Server Inventory;
3. Add/Edit Server Form;
4. SSH workflow;
5. Saved Commands.

These screens exercise different visual pressures:

- overview and hierarchy;
- dense structured information;
- forms and validation;
- operational status and command execution;
- reusable operational content.

---

## Validation Modes

Each candidate must be inspected in:

- light theme;
- dark theme.

Validation considers:

- hierarchy;
- contrast;
- density;
- action prominence;
- secondary-action restraint;
- form readability;
- card and surface separation;
- machine-value readability;
- operational-state clarity;
- visual consistency.

---

## Screen-Change Boundary

During visual-identity calibration:

- existing screen structure is preserved;
- existing information architecture is preserved;
- existing copy is preserved unless a separate copy defect exists;
- design-system tokens may be exercised globally against existing screens;
- structural screen redesign and feature-specific visual optimization are deferred.

Applying `ServerToolkitButtonShape` to existing Material buttons through PR
`#149` was foundation integration validation: it proved that an app-owned visual
token can be consumed consistently by real feature UI. That historical
validation does not authorize structural screen redesign or feature-specific
visual optimization during the current calibration phase.

If an independent UI defect prevents meaningful validation, it is handled
separately instead of being silently absorbed into foundation work.

System-bar, safe-inset, adaptive-layout, navigation, action-hierarchy, and
screen-specific presentation defects remain separate feature or UI concerns.

---

## Calibration Rules

Global token changes require repeated evidence.

Examples:

```text
Weak hierarchy across several screens
→ typography calibration candidate

Excessive rounding across several controls
→ shape calibration candidate

Only one screen overlaps a system bar
→ independent UI/platform defect
```

Calibration should change the smallest global token set necessary to address
the repeated problem.

---

## Foundation Validation Exit Criteria

These criteria were satisfied by the foundation-validation work merged through
PR #149:

- the runtime visual-profile contract works end-to-end;
- light and dark modes resolve and render through the application theme;
- Dashboard, Server Inventory, Server Form, SSH, and Saved Commands can consume
  the design system without structural redesign;
- color, typography, shape, and spacing channels are proven usable by real UI;
- at least one app-owned shared visual token is exercised through feature UI;
- dynamic-color policy is explicitly recorded;
- no architecture, navigation, state, workflow, or lifecycle semantics change
  as a consequence of foundation validation.

Exact visual token values do not need to be final. Later UI-focused issues may
refine them while continuing to use the same design-system foundation.

Focused screen optimization and additional shared-component extraction should
proceed later through normal Issue -> feature branch -> PR work when concrete
usability or repetition evidence justifies them.

---

## Foundation Validation Outcome

Completed:

- design-system foundation through PR `#147`;
- resolved runtime visual-profile contract through PR `#148`;
- concrete Graphite + Azure validation profile;
- light and dark runtime validation;
- representative validation against Dashboard, Server Inventory, Server Form,
  SSH, and Saved Commands;
- cross-screen shape validation using `ServerToolkitButtonShape`;
- confirmation that feature-level integration does not require structural UI,
  navigation, state, workflow, or behavior changes.

Current status:

- Graphite + Azure is the accepted Server Toolkit visual identity baseline;
- color, typography, shape, and spacing calibration are complete;
- existing token values were retained because repeated evidence did not justify
  global token changes;
- integrated Light/Dark validation passed across the fixed calibration surfaces;
- dynamic color remains disabled for the accepted baseline;
- root theme surface application remains theme-delivery infrastructure;
- structural screen redesign and feature-specific UI optimization remain outside
  the design-system baseline.

Future visual changes require new focused evidence and must not silently rewrite
the accepted calibration record.

---

## References

- `PRODUCT_VISION.md`
- `ENGINEERING_STRATEGY.md`
- `ROADMAP.md`
- `DOCUMENTATION.md`
- `ARCHITECTURE.md`
- PR `#147`
- PR `#148`
- PR `#149`
- Issue `#157`
