---
name: expect-actual-inventory
description: Complete inventory of expect/actual declarations in kmp-ledger and why each is a sanctioned platform seam
metadata:
  type: project
---

Full expect/actual inventory (all sound as of review). House rule: expect/actual is for genuine platform primitives; business/domain deps use interface + Koin DI instead. Every set below is a real platform primitive, so none violate that rule.

1. **PlatformDatabaseModule** (`core:database`, expect *class*, `di/`) — the canonical example named in CLAUDE.md. commonMain expect; actuals in androidMain (Context-based path), iosMain (NSDocumentDirectory), jvmMain (OS-specific app-data dir). Each provides `RoomDatabase.Builder<LedgerDatabase>`. Annotated `@Module`; consumed by `DatabaseModule(includes=[PlatformDatabaseModule::class])`.
2. **LedgerDatabaseConstructor** (`core:database`, expect *object* : RoomDatabaseConstructor) — mandatory Room-KMP codegen pattern, actual is generated. `@Suppress("NO_ACTUAL_FOR_EXPECT")` is expected here.
3. **getPlatformLogWriters()** (`core:common`, expect *fun*) — returns `List<LogWriter>` (Kermit). Actuals: LogcatWriter (android), OSLogWriter (ios), slf4j wrapper (jvm). Platform logging sinks — legit seam.
4. **PlatformComposeUiTest** (`core:test`, expect abstract *class*) — test base; android actual adds Robolectric `@RunWith`/`@Config`, jvm/ios empty. Test infra primitive.
5. **createTestDatabase()** (`core:database` commonTest, expect *fun*) — actuals in androidHostTest/jvmTest/iosTest. NOT hoistable to commonTest because Android's `Room.inMemoryDatabaseBuilder` needs a Context on-device; the no-Context KMP overload only resolves per-platform. Identical bodies are coincidental, not duplication-to-hoist.

**Why:** Knowing the full set + rationale prevents future me from re-flagging sanctioned seams or missing a newly-added one.

**How to apply:** A new expect/actual that abstracts a domain/business dependency (repository, use case, mapper) instead of a platform primitive is the thing to flag — house pattern there is interface + Koin DI/factory. Also verify every expect has an actual on all 4 targets (iosMain covers both ios targets).
