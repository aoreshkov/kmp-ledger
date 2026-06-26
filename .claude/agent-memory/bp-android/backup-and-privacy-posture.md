---
name: backup-and-privacy-posture
description: Android target is local-first with backup deliberately disabled and zero permissions — confirm, don't re-flag, in currency audits
metadata:
  type: project
---

The `androidApp` target is intentionally local-first and privacy-minimal. Confirm these on each currency audit rather than proposing to "add" anything.

- **Backup deliberately OFF** (commit adca851): `android:allowBackup="false"` plus both `data_extraction_rules.xml` (cloud-backup + device-transfer) and `backup_rules.xml` exclude every domain. The exclude-all rule files are intentionally redundant with `allowBackup=false` — they silence lint and keep intent explicit. This is a pinned decision; do not propose re-enabling backup.
- **Zero permissions**: the manifest declares no `uses-permission`. Correct for a local-first ledger; do not propose storage/network/legacy permissions.
- **Single exported component**: only `MainActivity` (LAUNCHER), `exported="true"` correctly.

**Why:** local-first app, ledger DB must never leave the app sandbox.
**How to apply:** a currency audit should *verify these still hold* against current Android backup/privacy guidance and report "matches best practice," not invent gaps. Related: [[sdk-and-behavior-currency]].
