# Visual Identity Calibration Review

**Project:** Server Toolkit
**Issue:** #157
**Status:** Active — Baseline Evidence Review
**Started:** 2026-08-12
**Last Reviewed:** 2026-08-14

---

## Purpose

This review records the reproducible evidence used to derive, evaluate, and
accept the Server Toolkit visual identity baseline.

The design-system foundation was established through PRs #147–#149. This review
does not rebuild that foundation and does not redesign application screens.

Document ownership:

```text
docs/DESIGN_SYSTEM.md
= durable design rules + accepted visual state

Issue #157
= scope + guardrails + acceptance criteria

this review
= fixtures + observations + derivation + candidates + decisions
```

While Issue #157 is active, this review may evolve. After merge, it becomes
historical evidence and must not be silently rewritten.

---

## Source Baseline

```text
Runtime source:
main @ 5bdbc0393874ac7850cd7d980ef5df783e1db070

Calibration recovery checkpoint:
6f5673c2a9d2a793541b13d35151bc3e2b6e97ee
docs: synchronize design system after foundation validation
```

At this checkpoint, the branch differs from `main` only in
`docs/DESIGN_SYSTEM.md`. No production source or visual token has changed.

Graphite + Azure remains the calibration starting profile, not an accepted final
identity.

---

## Reference Baseline

Reviewed on 2026-08-14.

Primary references:

- Material 3 for Compose:
  https://developer.android.com/develop/ui/compose/designsystems/material3
- Material Color Utilities:
  https://github.com/material-foundation/material-color-utilities
- WCAG 2.2 text contrast:
  https://www.w3.org/WAI/WCAG22/Understanding/contrast-minimum.html
- WCAG 2.2 non-text contrast:
  https://www.w3.org/WAI/WCAG22/Understanding/non-text-contrast
- Android accessibility foundations:
  https://developer.android.com/design/ui/mobile/guides/foundations/accessibility
- Android app accessibility:
  https://developer.android.com/guide/topics/ui/accessibility/apps.html

Calibration constraints:

```text
Normal text contrast                   >= 4.5:1
Large text contrast                    >= 3:1
Meaningful non-text contrast           >= 3:1
Body text                              >= 12 sp
Text sizing                            uses sp
```

For Android contrast evaluation, text smaller than 18 sp, or bold text smaller
than 14 sp, uses the 4.5:1 threshold. Larger text may use 3:1.

Material 3 role semantics remain authoritative for role meaning. Material Color
Utilities provide the design-time HCT, tonal-palette, and scheme-generation
reference. Material Theme Builder may be used only as an exploratory cross-check.

---

## Calibration Rule

Every global token candidate must be traceable through:

```text
fixed fixture
→ current token
→ repeated observation
→ product/design constraint
→ reference baseline
→ reproducible derivation
→ candidate
→ light/dark evidence
→ cross-screen evidence
→ accept / reject
```

A single-screen problem does not justify a global token change.

No change is a valid calibration result.

Rejected candidates remain recorded.

Calibration order is:

```text
Color
↓
Typography
↓
Shape
```

Spacing remains at `4 / 8 / 12 / 16 / 24 dp` unless repeated evidence justifies
reconsideration.

---

## Deterministic Fixture Method

Primary evidence is produced from Compose instrumentation fixtures under
`app/src/androidTest/`.

Each fixture must:

- render an existing production screen composable directly;
- construct deterministic project-owned UI state;
- use no-op callbacks;
- wrap content in `ServerToolkitTheme`;
- set `darkTheme` explicitly;
- use `DefaultServerToolkitVisualProfile`;
- avoid Room, network, authentication, SSH session, and navigation dependencies;
- wait for Compose to become idle before capture;
- use the same environment for directly compared evidence;
- introduce no production behavior.

No new runtime dependency is justified for fixture capture.

---

## Capture Environment

Official baseline capture environment:

| Field | Value |
|---|---|
| Device / AVD | `Leannect_API_36` / `Google sdk_gphone64_x86_64` |
| Android API | `36` |
| Resolution | `1080 × 2424 px` |
| Density | `420 dpi` |
| Font scale | `1.0` |
| Display scale | Default; no `wm size` or `wm density` override |
| Fixture source commit | `81122c88adad114dfee5fbbf87c2e1fdba9ec94a` |
| Dynamic color | Disabled |

Typography also requires a separate maximum-font-scale usability check. That
check is not used as a pixel-diff baseline.

Evidence root:

```text
docs/review/assets/visual-identity-calibration/
```

Official baseline evidence:

```text
docs/review/assets/visual-identity-calibration/baseline/
```

The baseline directory contains ten canonical PNG captures and `environment.txt`.
The metadata sidecar records the immutable fixture source commit and runtime
capture environment used for direct comparison.

Naming:

```text
<stage>/<fixture-id>-<theme>[-<anchor>].png
```

Only evidence referenced by this review is retained.

---

## Core Fixtures

### F01 — Dashboard

State:

```text
DashboardUiState(title = "Server Toolkit")
```

Evaluate:

- hierarchy;
- primary vs secondary actions;
- spacing;
- button shape.

Capture:

```text
F01-<theme>.png
```

### F02 — Server Inventory

State:

```text
Loaded inventory
Default filters
No operation message

server-prod-01
Production Gateway
203.0.113.10:22
User: ops
Environment: PRODUCTION

server-stage-01
Staging API
198.51.100.20:2222
User: deploy
Environment: STAGING
```

Evaluate:

- dense content;
- card/surface separation;
- filter controls;
- secondary text;
- action hierarchy;
- shape behavior.

Capture:

```text
F02-<theme>.png
```

### F03 — Add Server Form

State:

```text
title:       Add server
name:        Production Gateway
host:        203.0.113.10
port:        22
username:    ops
errors:      none
saving:      false
save enabled
```

Evaluate:

- form readability;
- label hierarchy;
- vertical density;
- action hierarchy;
- field/button geometry.

Capture:

```text
F03-<theme>.png
```

### F04 — SSH Connected Command Execution

State:

```text
serverId:       server-prod-01
status:         Connected
statusLabel:    Connected
message:        SSH connection is ready.
detail:         A project-owned SSH session handle was opened.

command:        uptime
command status: Completed
status label:   Command completed
message:        Command execution completed.
detail:         Exit status: 0
stdout:         up 14 days, 3:21
stderr:         empty
exit status:    0
```

The fixed calibration viewport captures the complete connected-command state
without scrolling, so use one capture per theme:

```text
F04-<theme>.png
```

The capture evaluates connection hierarchy, authentication controls, command input,
machine-oriented output, status labels, command actions, connection history, and
back navigation in one deterministic view.

### F05 — Saved Commands

State:

```text
Loaded content
No error
No dialog

command-uptime | System uptime | uptime
command-disk   | Disk usage    | df -h
```

Evaluate:

- card separation;
- command-text readability;
- secondary/destructive actions;
- shape behavior.

Capture:

```text
F05-<theme>.png
```

Supplementary fixtures require a written reason and may only cover a token,
component state, or semantic role not meaningfully exercised above.

---

## Baseline Evidence Review

The official baseline was captured on 2026-08-14 from fixture source commit
`81122c88adad114dfee5fbbf87c2e1fdba9ec94a`.

It records the existing Graphite + Azure calibration starting profile before any
visual-token calibration.

Initial observations:

- All ten canonical captures were produced at `1080 × 2424 px` in the fixed
  Light/Dark environment with no capture or encoding artifact observed.
- F02 shows the `TESTING` filter chip clipped at the right edge of the viewport
  in both Light and Dark captures. This is recorded as a screen-level layout
  observation and does not by itself justify a global token change.
- F04 confirms that the complete connected-command state is visible in the fixed
  calibration viewport without scrolling, validating one capture per theme.

These observations are evidence inputs only. Token changes still require the
derivation and cross-fixture evidence defined by this review.

---

## Color Derivation Record

For each color candidate record:

1. repeated problem;
2. affected Material role family;
3. product/design constraint;
4. key/source color;
5. HCT or equivalent reproducible parameters;
6. tonal palette / role mapping;
7. exact candidate values;
8. required contrast calculations;
9. Light/Dark fixture evidence;
10. decision.

Status:

```text
Not started
```

---

## Typography Derivation Record

For each typography candidate record:

1. roles exercised by fixtures;
2. current value vs Material 3 baseline;
3. repeated hierarchy/density/readability problem;
4. product/design constraint;
5. smallest justified change;
6. Light/Dark evidence at font scale `1.0`;
7. maximum-font-scale usability check;
8. decision.

Global monospace typography is out of scope unless separate component-level
evidence justifies it.

Status:

```text
Not started
```

---

## Shape Derivation Record

For each shape candidate record:

1. consuming components;
2. repeated roundedness problem;
3. current radius vs Material 3 reference scale;
4. smallest justified global change;
5. cross-fixture evidence;
6. decision.

Do not introduce new component architecture to exercise a shape candidate.

Status:

```text
Not started
```

---

## Candidate Template

| Field | Value |
|---|---|
| Candidate ID | Pending |
| Area | Pending |
| Role / Token | Pending |
| Current value | Pending |
| Fixture evidence | Pending |
| Repeated observation | Pending |
| Product / design constraint | Pending |
| External reference | Pending |
| Derivation method | Pending |
| Candidate value | Pending |
| Numerical validation | Pending / N/A |
| Light result | Pending |
| Dark result | Pending |
| Cross-screen result | Pending |
| Decision | Pending |
| Rationale | Pending |

Decision values:

```text
Retained
Accepted change
Rejected candidate
```

---

## Calibration Log

```text
Fixture harness:       Implemented and runtime-validated
Capture environment:   Recorded
Baseline evidence:     Captured and initial review recorded

Color:                 Not started
Typography:            Not started
Shape:                 Not started
Spacing:               No calibration justified at review start

Integrated validation: Not started
Accepted baseline:     None
```

---

## Repository References

- `docs/DESIGN_SYSTEM.md`
- `docs/DOCUMENTATION.md`
- Issue `#157`
- Issue `#158`
- PR `#147`
- PR `#148`
- PR `#149`
