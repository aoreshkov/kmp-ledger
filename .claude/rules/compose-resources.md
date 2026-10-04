---
paths:
  - "**/build.gradle.kts"
  - "**/composeResources/**"
---

# Compose resources: `packageOfResClass`

Every module that generates a Compose `Res` class pins `packageOfResClass` explicitly in its own `build.gradle.kts`, as `app.oreshkov.ledger.<module path>.resources` (`:iosExport` → `app.oreshkov.ledger.iosexport.resources`). The Compose default is `{group}.{module}.generated.resources`, which — with no `group` set — derives from `rootProject.name`, so renaming the root project would silently repackage every module's accessors. `Res` stays internal (`publicResClass` defaults to `false`); only `core:compose` sets `publicResClass = true`, because its `back_content_description` string is consumed by the feature modules. A `Res` class is generated in any module with an explicit `implementation`/`api` dependency on `compose.components.resources` — which the `ledger.kotlin.multiplatform.koin.compose` convention plugin adds — so modules with no `composeResources/` directory (`core:bootstrap`, `core:navigation`, `core:ui`, `iosExport`) still get an empty one and still need the pin. The generated accessors surface in the klib API dumps, so changing a package requires `./gradlew apiDump`.
