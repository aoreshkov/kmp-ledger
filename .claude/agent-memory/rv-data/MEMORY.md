# Data/Persistence Memory Index

- [Schema and migration posture](schema-migration-posture.md) — Room schema export is on; no migrations/destructive fallback yet (only v1)
- [Insert conflict strategy](insert-conflict-strategy.md) — PostingDao @Insert uses default ABORT; duplicate id crashes
