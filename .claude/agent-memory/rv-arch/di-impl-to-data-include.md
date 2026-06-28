---
name: di-impl-to-data-include
description: Why feature:posting:impl depends on core:data despite the impl→api→domain→data diagram; it is a DI include, not a logic dependency
metadata:
  type: project
---

`feature:posting:impl` declares `implementation(project(":core:data"))` and its
`PostingModule` does `@Module(includes = [DataModule::class, DomainModule::class])`.
This is the ONLY coupling from impl to data — the ViewModels themselves import only
`core:domain` use cases and `core:model` types, never `core:data`.

**Why:** Koin annotation graph is validated at compile time. The feature's screens
need the `PostingRepository` binding (declared in `DataModule` as
`OfflineFirstPostingRepository`), so the feature DI module must include the data
module to make the graph complete. The dependency direction is still downward
(data sits below impl), so there is no cycle or upward/sideways leak.

**How to apply:** Do NOT flag the impl→data build dependency as a Critical layering
violation. It is an intentional DI-aggregation tradeoff. Only escalate if impl
starts importing concrete `core:data` *types* (repository impls, mappers, entities)
into feature logic — that would be a real leak. The clean alternative (a separate
DI-only module that composes data+domain bindings) is an Optional refactor, not a
correctness issue.

NOTE on data→domain build dependency: `core:data` declares
`implementation(project(":core:domain"))`, which looks like an UPWARD edge against
the CLAUDE.md diagram (`core:domain → core:data → core:database`). It is NOT a
violation. The `PostingRepository` interface lives in `core:domain`
(`repository/PostingRepository.kt`) and `OfflineFirstPostingRepository` in
`core:data` implements it — textbook dependency inversion. The diagram arrow
domain→data is the logical call flow; the compile arrow is data→domain because data
owns the impl of an interface domain owns. `core:test` (FakePostingRepository) also
depends on domain for the same reason. Do NOT flag data→domain as a layering
inversion.

Verified clean as of review on 2026-06-28 (re-confirmed; no change since 2026-06-24,
2026-06-23):
entities (`PostingEntity`) confined to core:database + core:data mappers; use cases
each wrap one repository method; core:model is pure (no framework deps); core:ui
reaches features only via runtime Koin, not a compile dependency;
`postingNavigationModule` DSL wired only by platform apps + iosExport. See
[[no-coauthored-by-trailer]] for commit conventions.

NOTE on `SavePostingUseCase`: it branches between `repository.insertPosting` and
`repository.updatePosting` on null id. This is NOT a "one use case wraps one repo
method" violation — its single primary action is "persist a posting"; insert-vs-update
is the same logical operation keyed on id presence. Do not flag it.
