# Build & CI Agent Memory

- [JVM target config](jvm-target-config.md) — all JVM targets standardized on JVM 21; JDK-21 daemon/CI is the hard ceiling
- [Kover floor topology](kover-floor-topology.md) — aggregate vs per-module coverage floors, values, and the duplicated excludes list
- [CI hardening posture](ci-hardening-posture.md) — SHA pins, least-privilege permissions, concurrency, timeouts already in place (don't re-flag)
- [Swift Export & catalog quirks](swift-export-and-catalog-quirks.md) — bootstrap's bare swiftExport (no @OptIn) and material-icons 1.7.3 divergence are intentional/benign
