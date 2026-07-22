---
name: check-agp-ide-currency
description: Determine the latest Kotlin + AGP versions compatible with BOTH the current Android Studio and the current IntelliJ IDEA (the lagging IDE is the cap), compare against this project's pins, and present a dated inline report. Researches live; report-only — proposes a bump but never edits without confirmation. Do not invoke automatically.
disable-model-invocation: true
argument-hint: "[optional: e.g. 'agp only' or 'assume IDEA <version>']"
allowed-tools: Read, Grep, Glob, Bash(./gradlew*), Bash(git status*), WebSearch, WebFetch, Edit
---

## Today
!`date +%F`

Answer one question, as of the date above: **what are the latest Kotlin and AGP
versions that this project can pin while still opening cleanly in BOTH the current
Android Studio and the current IntelliJ IDEA?** Then compare that against the
project's actual pins and present a dated report **inline** (in the conversation).

The governing fact: **IntelliJ IDEA's bundled Android plugin lags the AGP release
train and lags Android Studio**, so IDEA is almost always the binding constraint —
the highest AGP that opens in both IDEs equals the IDEA ceiling. This project
deliberately pins AGP to that ceiling (see the `agp-pinned-to-idea-ceiling` project
memory). This skill re-validates that pin whenever the IDEs or AGP move.

This skill **makes no changes on its own** — it researches, reports, and then offers
to apply a bump. It never commits or pushes. Do not persist the report to a file
unless the user asks.

## Steps

### 1. Read the local pins

- `gradle/libs.versions.toml` → `android-gradle-plugin`, `kotlin`, and `ksp`.
- `gradle/wrapper/gradle-wrapper.properties` → the Gradle wrapper `distributionUrl`
  version.
- The `agp-pinned-to-idea-ceiling` project memory (the last-recorded IDEA AGP ceiling
  and the rationale) and `material3-version-pin` if relevant. Treat memory as a
  point-in-time baseline to reconcile against live data — not as current truth.

### 2. Research the latest — live, do not trust memory

Use WebSearch/WebFetch. Establish, as of today, and **cite each URL with its date or
version**:

- **Latest AGP (stable)** and its minimum Gradle / JDK / bundled-KGP, plus the
  **Android Studio ↔ AGP** support table and 3-year policy —
  `developer.android.com/build/releases/about-agp` (and the specific
  `agp-<x>-<y>-0-release-notes` page for the ceiling version).
- **Kotlin ↔ AGP/D8/R8** required versions (does the target Kotlin need a newer AGP?) —
  `developer.android.com/build/kotlin-support`.
- **Latest stable Kotlin** (exclude Beta/RC/EAP) — `kotlinlang.org/docs/releases.html`.
- **Latest Android Studio stable** and the AGP range it accepts —
  `androidstudio.googleblog.com` / `developer.android.com/studio/releases`.
- **Latest IntelliJ IDEA stable** and its bundled **Android-plugin AGP ceiling** — the
  hard part, and usually the binding constraint. Cross-check:
  - `youtrack.jetbrains.com/issue/IDEA-385007` (AGP 9 support tracking; watch for the
    fix-version that raises the ceiling) and related KMT/IDEA issues,
  - the community matrix `github.com/rafaeltonholo/agp-intellij-compatibility-matrix`
    (corroborating, not authoritative — it often trails),
  - recent "Latest supported version is AGP X" sync-failure reports.

  If sources disagree or lag, say so and treat the **lower, verified** value as the
  ceiling. State the ceiling as a range if it genuinely can't be pinned.

### 3. Local sync probe (CLI) — and the real IDE caveat

Run a read-only `./gradlew help` (or `:androidApp:tasks`) to confirm Gradle resolves
the AGP plugin and configures on the CLI with the current pins. **State clearly that
this only proves CLI/Gradle compatibility — it does NOT prove the IDE ceiling.** The
true IntelliJ IDEA ceiling is confirmed only by opening the project in IDEA and getting
a green Gradle sync (no "incompatible version… Latest supported version is AGP X"
balloon). Include the per-machine escape hatch for contributors who need a newer AGP
than their IDEA supports: `gradle.ide.support.future.agp.versions=true` in
`idea.properties` (per-machine, never committed).

### 4. Compute the recommendation

- **Recommended AGP** = `min(Android-Studio-supports, IntelliJ-IDEA-ceiling)`, capped
  also by what the pinned Gradle wrapper supports (flag if a Gradle bump would be
  required).
- **Recommended Kotlin** = the latest **stable** Kotlin that both IDEs' bundled Kotlin
  plugin accept and that satisfies the Kotlin↔AGP table for the recommended AGP.
- Respect the deliberate pin. Only raise a flag when the project pin is **above** the
  IDEA ceiling (risky — IDEA won't sync) or **below** a *confirmed-raised* ceiling
  (safe headroom now available). "Latest AGP exists" alone is **not** a reason to bump —
  the IDEA ceiling is the cap.

### 5. Present the dated report — inline

Head it with today's date (printed above). Sections:

- **Scope & sources** — what was checked; the source URLs with their dates/versions.
- **Current pins** — AGP, Kotlin, KSP, Gradle from step 1.
- **Currency table** — one row each for AGP and Kotlin:
  `component | pinned | latest stable | Android Studio cap | IntelliJ IDEA cap | recommended-for-both`.
- **Binding constraint** — the IDEA ceiling, how it was established, and the CLI-vs-IDE
  confirmation caveat from step 3.
- **Recommendation** — hold or bump, with the exact target versions and why.
- **Deliberately not adopted** — e.g. "AGP `<latest>` — blocked on the IDEA ceiling;
  revisit when IDEA-385007's fix-version ships."

If the user passed an argument, scope accordingly (e.g. "agp only", or
"assume IDEA <version>" to model a ceiling the sources don't yet confirm).

### 6. Offer — do not edit yet

Ask: **"Apply the recommended AGP/Kotlin bump to `gradle/libs.versions.toml`, or stop
here?"**

- **apply** — edit only the relevant `[versions]` entries in
  `gradle/libs.versions.toml`; change nothing else. A Kotlin bump also moves `ksp` to
  its matching `<kotlin>-<n>` build and may touch `compose-compiler` — call those out
  before editing. Remind the user that any public-API surface change then needs
  `./gradlew apiDump` + committed `*/api/` dumps (BCV), and that commit/push follow the
  project's own `commit` conventions — never commit or push from here.
- **stop** — leave everything untouched; the report stands on its own.

Whenever the established IDEA ceiling differs from what the `agp-pinned-to-idea-ceiling`
memory records, note that the memory should be updated (new ceiling, date, and the
IDEA/AGP versions verified).
