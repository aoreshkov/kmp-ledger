---
name: sqlite-wrapper-version-pin
description: room3-sqlite-wrapper in libs.versions.toml is pinned to androidx-sqlite (2.7.0-rc01) but that coordinate does not exist; must track the Room version
metadata:
  type: project
---

`gradle/libs.versions.toml`: `room3-sqlite-wrapper` is declared with
`version.ref = "androidx-sqlite"` (2.7.0-rc01). That coordinate does NOT exist on
Google Maven — `androidx.room3:room3-sqlite-wrapper` only publishes 3.0.0-alpha01..rc01
(POM 404 at 2.7.0-rc01, 200 at 3.0.0-rc01, verified 2026-06-26).

**Why:** It's a Room artifact, so it must be versioned with the Room version
(`androidx-room` = 3.0.0-rc01), exactly as the official KMP Room docs show
(`androidx-room-sqlite-wrapper ... version.ref = "room"`). The build currently survives
only because Gradle conflict resolution silently upgrades the declared 2.7.0-rc01 to
3.0.0-rc01 via a transitive constraint from room3-runtime (`2.7.0-rc01 -> 3.0.0-rc01`).
The declared version is dead/misleading and would break if the constraint chain changed.

**How to apply:** Recommend changing `version.ref` to `androidx-room`. Also note the
wrapper is an Android-only *optional* compat artifact (SupportSQLite); the repo has no
`getSupportWrapper`/`SupportSQLite`/`openHelper` usage, so it could be dropped entirely
instead — that's a hygiene call, but the version pin is the currency defect.
