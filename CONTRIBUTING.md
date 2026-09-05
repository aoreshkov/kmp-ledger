# Contributing to Ledger

Thanks for your interest! Ledger is primarily a reference project, but issues and
pull requests are welcome.

## Before you start

- For bugs and questions, use the [issue templates](https://github.com/aoreshkov/kmp-ledger/issues/new/choose).
- For security problems, **do not open a public issue** — follow the
  [security policy](.github/SECURITY.md).
- For anything beyond a small fix, please open an issue first so the approach can be
  discussed before you invest time.

## Building and testing

JDK 21+ is required. Useful commands:

```bash
./gradlew jvmTest      # fastest test pass (JVM targets only, no emulator)
./gradlew allTests     # all tests across all platforms
./gradlew check        # full gate: tests + API check + lint + coverage floors
```

## Running the desktop app with hot reload

```bash
./gradlew :desktopApp:hotRun
```

Code edits are applied to the running window without a restart. The first run downloads a
JetBrains Runtime (hot reload needs its enhanced class redefinition) and caches it outside
the project; normal builds are unaffected and keep using the daemon JDK.

The same plugin exposes `:desktopApp:hotMcpServer`, an MCP endpoint that lets a coding
agent reload, screenshot, read the semantic tree and drive the running app. `.mcp.json`
wires it up for MCP clients that read that file. It invokes `./gradlew`, so on Windows
register the server with the `gradlew.bat` form locally instead.

## Pull request expectations

- **CI must pass** — the `CI Success` status check gates merges. It runs `check`,
  which includes tests, binary-compatibility validation, and Kover coverage floors.
- **Public API changes** require regenerated dumps: run `./gradlew apiDump` and commit
  the updated `<module>/api/` files alongside the code change, or `apiCheck` fails.
- **Tests use fakes, not mocks** — see `core:test` (`FakePostingRepository`,
  `FakeSettingsRepository`) for the pattern. No mocking libraries.
- **Architecture rules** (layering, feature api/impl split, Koin annotations vs DSL
  boundaries) are documented in [`CLAUDE.md`](CLAUDE.md) and the
  [README architecture section](README.md#architecture) — new code should follow them.
- Keep commits focused, with plain conventional-style messages
  (`fix: …`, `feat: …`, `docs: …`).

## Code of conduct

By participating you agree to abide by the
[Code of Conduct](CODE_OF_CONDUCT.md).
