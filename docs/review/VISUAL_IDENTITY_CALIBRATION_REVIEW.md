# Visual Identity Calibration Review

**Project:** Server Toolkit
**Issue:** #157
**Status:** Active — Baseline Evidence Review
**Started:** 2026-08-12
**Last Reviewed:** 2026-09-04

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

### Usage Inventory

Production-source scan at
`69dc6d15a7ad7373484e4fc6a535d831eb3527d2` found no direct `Color(...)`
literal outside the design-system theme color definition.

Explicit `MaterialTheme.colorScheme` role usage:

| Role | Explicit references |
|---|---:|
| `onSurfaceVariant` | 26 |
| `error` | 13 |
| `onSurface` | 2 |
| `onPrimary` | 2 |
| `onError` | 2 |
| `primary` | 1 |
| `background` | 1 |
| `onBackground` | 1 |

This count covers only explicit feature-source references. Material components
such as buttons, outlined controls, text fields, chips, and surfaces also consume
`ColorScheme` roles implicitly, so absence from this table does not prove that a
role is unused at runtime.

The core fixtures directly exercise:

- primary-action emphasis;
- neutral background and surface hierarchy;
- secondary text through `onSurfaceVariant`;
- outlined controls and field boundaries;
- destructive/error semantics;
- cards, controls, and form surfaces across Light and Dark themes.

`secondary`, `tertiary`, and fixed-role families are not explicitly referenced by
the feature-source scan. Changes to those families require either demonstrated
implicit component impact or additional evidence; they must not be changed
speculatively.

### Current Numerical Baseline

Representative current role pairings:

| Pair | Light | Dark |
|---|---:|---:|
| `primary` / `onPrimary` | 5.17:1 | 7.36:1 |
| `primaryContainer` / `onPrimaryContainer` | 12.04:1 | 8.49:1 |
| `surface` / `onSurface` | 17.85:1 | 14.48:1 |
| `surfaceVariant` / `onSurfaceVariant` | 6.15:1 | 9.85:1 |
| `error` / `onError` | 6.47:1 | 5.84:1 |
| `outline` / `surface` | 4.76:1 | 3.75:1 |

These sampled pairings satisfy the text or meaningful non-text contrast
thresholds defined by this review. They do not establish that every component
state or alpha-composited runtime combination is compliant.

Therefore the current evidence does not justify starting color calibration from
a contrast-failure assumption. Candidate evaluation must instead focus on
operational clarity, trust, product fit, semantic hierarchy, platform neutrality,
and cross-screen consistency while preserving accessibility.

### Material Reference Control

A read-only Material Color Utilities reference was generated from the current
Light primary `#2563EB` using:

```text
MCU source commit: f05459ea2170f3be610f89a4ddeee8843c2deb61
Implementation:    official Java source
Scheme:            SchemeTonalSpot
Spec version:      SPEC_2021
Platform:          PHONE
Contrast level:    0.0
Source color:      #2563EB
Source HCT:        H=272.219 C=69.575 T=46.063
```

Representative generated roles:

| Role | Light | Dark |
|---|---|---|
| `primary` | `#4B5C92` | `#B4C5FF` |
| `primaryContainer` | `#DBE1FF` | `#324478` |
| `secondary` | `#595E72` | `#C1C5DD` |
| `tertiary` | `#745470` | `#E2BBDB` |
| `background` | `#FAF8FF` | `#121318` |
| `surfaceVariant` | `#E2E2EC` | `#45464F` |
| `error` | `#BA1A1A` | `#FFB4AB` |

This control is intentionally not a candidate palette.

For `TONAL_SPOT` in the pinned Material specification, MCU derives:

```text
Primary palette:         source hue, chroma 36
Secondary palette:       source hue, chroma 16
Tertiary palette:        source hue + 60 degrees, chroma 24
Neutral palette:         source hue, chroma 6
Neutral-variant palette: source hue, chroma 8
Error palette:           hue 25, chroma 84
```

With the current Azure source hue near `272 degrees`, the generic tertiary
rotation produces a hue near `332 degrees`, resulting in a purple/magenta
tertiary family rather than the documented limited cyan direction. The Tonal
Spot primary family also reduces chroma substantially relative to the current
Azure source.

The control therefore demonstrates that a single-source `SchemeTonalSpot`
generation would replace product-owned visual decisions with generic Material
palette relationships. That conflicts with the current Server Toolkit direction
of controlled Azure emphasis, cool graphite/slate neutrals, and limited cyan
tertiary use.

Decision:

```text
SchemeTonalSpot whole-scheme generation:
Rejected as a direct Server Toolkit palette generator.
Retained as an external Material reference control.
```

Material Color Utilities remains useful at design time through HCT and
`TonalPalette`. `TonalPalette` provides constant hue and chroma while varying
tone, allowing each product-owned color family to be derived reproducibly
without forcing unrelated families from one source color.

The current family assessment below completes that measurement step before any
product-derived candidate values are proposed.

### Current Family Assessment

The current baseline was assessed with two independent classification axes:

1. semantic role function;
2. Material reference tonal-palette assignment.

The assignment axis describes the Material role model only. It does not claim
that the current Server Toolkit hex value was historically generated from that
palette. The current baseline reuses some exact colors across role families, so
that distinction is required for correct interpretation.

Measurement inputs:

```text
Server Toolkit commit:
a24ace45f9d4d6c672752294f7e870e91b2b6553

Color.kt SHA-256:
9a9f1d7ebbd736233573f2d83692dfbc5880fbb1ed5233ebe2cf33876b9f2f6d

Material Color Utilities:
f05459ea2170f3be610f89a4ddeee8843c2deb61

Dual-axis report SHA-256:
6f46b674f4b9ef90844bc1d383657a6e7d3b3987f452a7145b4bdbcf5b91cc72

Family-fit report SHA-256:
eff65c961c7fa797c97985f2c6d6fd466fd0cac5e4c8a46bc8233f89b9bc2d91
```

For each Material reference family, role aliases sharing one current hex value
were deduplicated. Each current color retained its measured HCT tone, and one
descriptive `TonalPalette(hue, chroma)` was fitted by minimizing mean squared
CAM16-UCS distance between current colors and model colors at those same tones.

The fit is descriptive only. It is not a candidate palette, an acceptance
threshold, or evidence by itself that a current family must change.

| Family | Best-fit H | Best-fit C | RMSE | Mean distance | Max distance |
|---|---:|---:|---:|---:|---:|
| Primary | 266.8 | 66.1 | 3.3788 | 3.1148 | 4.6777 |
| Secondary | 256.8 | 16.7 | 2.2513 | 1.8866 | 4.2027 |
| Tertiary | 217.2 | 51.6 | 2.8871 | 2.6652 | 3.5310 |
| Error | 21.5 | 81.9 | 2.7356 | 2.3103 | 4.3820 |
| Neutral | 265.6 | 16.2 | 2.3051 | 1.9927 | 4.3170 |
| Neutral Variant | 256.1 | 18.1 | 2.4341 | 1.8814 | 4.3842 |

Family findings:

- **Primary:** the descriptive model remains a high-chroma Azure family. Primary
  has the largest RMSE among the six fitted families. Its largest
  residual is `DarkOnPrimary` (`#0B1220`), which is also the current
  `DarkBackground`; this demonstrates cross-family color reuse and does not by
  itself establish a Primary carrier defect.
- **Secondary:** the descriptive `H=256.8 C=16.7` model nearly overlaps Neutral
  Variant at `H=256.1 C=18.1`. This is consistent with the current restrained
  slate support direction. The production usage scan has no explicit Secondary
  references, so no token change is justified from this fit alone.
- **Tertiary:** the descriptive `H=217.2 C=51.6` model remains a distinct cyan
  family, consistent with the documented limited-cyan direction. The production
  usage scan has no explicit Tertiary references, so no token change is justified
  from this fit alone.
- **Error:** the descriptive `H=21.5 C=81.9` model is close to the Material
  reference error parameters `H=25 C=84` already recorded above. Representative
  current Error contrast pairings also pass the review thresholds. Current
  evidence therefore provides no change trigger for this family.
- **Neutral:** the descriptive `H=265.6 C=16.2` model confirms a cool
  graphite/slate family rather than a near-achromatic generic Material neutral.
  Several larger residuals occur in high-tone Light colors, including
  `#E2E8F0`; no pass/fail meaning is assigned to those residuals without a
  justified threshold and fixture evidence.
- **Neutral Variant:** the descriptive `H=256.1 C=18.1` model is closely related
  to Secondary and remains distinctly cool/slate. Its largest residual is the
  high-tone Light `#E2E8F0`. Because `onSurfaceVariant` is the most frequently
  referenced explicit production color role in the current usage inventory,
  this family has strong operational evidence for careful visual assessment even
  though its fit residual is not the largest.

The measurements show distinct high-chroma Primary, Tertiary, and Error
directions alongside closely related cool slate Secondary, Neutral, and Neutral
Variant directions. They do not establish that the current palette must be
normalized into six mathematically independent constant-H/C palettes.

No arbitrary CAM16-UCS acceptance threshold has been introduced. No exact token
change has been proposed.

### Family Dispositions

Reviewed on 2026-09-04.

The disposition decision applies the calibration rule already defined by this
review: a global color change requires a repeated cross-fixture problem and a
reproducible candidate derivation. Numerical fit residuals, family overlap, or
aesthetic preference in isolation are not sufficient change triggers.

| Family | Disposition | Rationale |
|---|---|---|
| Primary | Retained | The current high-chroma Azure direction remains consistent with the documented product direction. The largest descriptive-fit residual is explained by the current `DarkOnPrimary` / `DarkBackground` cross-family reuse and is not accompanied by a repeated cross-fixture failure. |
| Secondary | Retained | The restrained slate relationship to Neutral Variant is consistent with the current supporting-role direction. No explicit production usage or fixture evidence establishes a defect requiring normalization or replacement. |
| Tertiary | Retained | The current distinct cyan family remains consistent with the documented limited-cyan direction. No explicit production usage or repeated fixture problem justifies speculative change. |
| Error | Retained | The family is close to the Material reference error direction and the representative current Error contrast pairings satisfy the review thresholds. No repeated semantic or accessibility problem has been recorded. |
| Neutral | Retained | The family confirms the intended cool graphite/slate direction. Descriptive residuals in high-tone Light colors do not establish a visual defect without a justified threshold and repeated fixture evidence. |
| Neutral Variant | Retained | The family remains a coherent cool/slate support family. `onSurfaceVariant` is the most frequently referenced explicit production color role, so it remains a focus for integrated validation, but current evidence records no repeated problem that justifies a token change. |

Color-phase outcome:

```text
Primary:         Retained
Secondary:       Retained
Tertiary:        Retained
Error:           Retained
Neutral:         Retained
Neutral Variant: Retained

Normalize candidates:    None
Investigate candidates:  None
Production token change: None
```

These are Color-phase dispositions, not final visual-identity acceptance.
Typography and Shape calibration remain independent work, and the retained color
baseline must still pass the final integrated Light/Dark validation.

For each future color candidate record:

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
Completed — all assessed color families retained; integrated validation pending
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

Color:                 Completed; all assessed families retained
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
