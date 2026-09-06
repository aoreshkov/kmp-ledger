---
name: backup-and-privacy-posture
description: Android target is local-first with backup deliberately disabled and zero permissions — verified against current backup docs 2026-09-06; confirm, don't re-flag
metadata:
  type: project
---

The `androidApp` target is intentionally local-first and privacy-minimal. Confirm these on each
currency audit rather than proposing to "add" anything.

- **Backup deliberately OFF** (commit adca851): `android:allowBackup="false"` plus both
  `data_extraction_rules.xml` (cloud-backup + device-transfer) and `backup_rules.xml` exclude every
  domain. The exclude-all rule files are intentionally redundant with `allowBackup=false` — they
  silence lint and keep intent explicit. Pinned decision; never propose re-enabling backup.
- **Zero permissions**: the app manifest declares no `uses-permission`. Verified 2026-09-06 against
  the *merged* debug manifest too: the only entry is the signature-level
  `DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` androidx.core injects. No INTERNET, no storage.
- **Single exported component**: only `MainActivity` (LAUNCHER). The merged manifest also carries
  upstream's `androidx.profileinstaller.ProfileInstallReceiver` (`exported="true"` but guarded by
  `android.permission.DUMP`) — that is library-owned and expected, not a finding.

**Verified against upstream 2026-09-06** (developer.android.com/identity/data/autobackup,
page updated 2026-02-26): the redundancy is *load-bearing*, not decorative — on Android 12+ some
OEMs treat `allowBackup="false"` as disabling cloud backup only, leaving device-to-device transfer
on; the `<device-transfer>` excludes are what actually close that path. Keep both rule files.

**Known doc-vs-platform nit:** the docs call `path` a required attribute on `<include>`/`<exclude>`;
these rule files omit it. AOSP `FullBackup.extractCanonicalFile` explicitly tolerates that
("Allow things like `<include domain="sharedpref"/>`", `filePathFromXml = ""`), so the excludes do
take effect. Adding `path="."` is a documented-syntax nicety only — never report it above Optional,
and never as a data-leak.

**Why:** local-first app, ledger DB must never leave the app sandbox.
**How to apply:** a currency audit should *verify these still hold* against current Android
backup/privacy guidance and report "matches best practice," not invent gaps.
Related: [[sdk-and-behavior-currency]].
