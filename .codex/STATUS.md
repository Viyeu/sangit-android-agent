# SangIT Android Agent — STATUS

## 2026-11-11 — v0.2 source implementation
- Based on v0.1 source ZIP and its unchanged baseline: 15/15 Java assertions + static guards PASS.
- Added local manual metric snapshots (followers/views/interactions) with previous-date follower delta; does NOT claim API data.
- Added inbox shortcuts, six offline reply templates, opt-in sharing of draft to ChatGPT via Android Sharesheet (user selects the receiver); no generative model or unattended message access.
- Added 7-day content guide and user-confirmed Calendar event composer; no Calendar permission, alarm service or auto-post.
- Maintains original personal-profile sharing, notifications opt-in and checklist.
- Preserves applicationId and existing SharedPreferences keys for v0.1 backward compatibility, version 0.2.0/code 2.
- Android permissions: no uses-permission entries; no INTERNET/AccessibilityService; notification listener exported=false; backups off.
- Source / validation: `bash scripts/test-offline.sh` => 49/49 Java business assertions PASS; 9 Java files pass syntax parser; static manifest + privacy guards PASS. No errors found in the offline suite.
- Android SDK: unavailable in isolated container; `gradle`, `sdkmanager`, `android.jar` absent and external `dl.google.com` not resolvable. `:app:assembleDebug` NOT run; no APK generated.
- Android 16 device: NOT connected, so live UI/permission/calendar/Facebook E2E and RAM/pin tests NOT run.
- No Core/Social/Panel/WordPress live mutation, write gate, Facebook post, notification transmission or credentials changed.

## Next gates
1. Build debug APK in controlled Android Studio/GitHub Actions with SDK35/Gradle8.9/JDK17; capture artifact SHA and Gradle log.
2. Inspect APK permissions and install on Android16; notification opt-in/revoke, share/copy, Calendar cancel/confirm, orientation, metrics history, UI and resource E2E.
3. Fix device defects, rerun full regression, then authorize release signing before distributing a stable APK.
4. Separate scoped approval and contract for future Core device pairing (not implemented).

STATE: **v0.2 SOURCE IMPLEMENTED / OFFLINE TEST PASS; APK AND ANDROID16 E2E BLOCKED BY SDK/DEVICE**.

## 2026-11-14 — Verified GitHub Actions debug APK build
- User approved publishing Android Agent v0.2 source to public GitHub repo Viyeu/sangit-android-agent.
- Archived source was reassembled with SHA-256 `0e327a23e1e02cb3e1710f64ee346fb329f086b1ee50b676c0679e4b5d197545`, verified and committed as browseable source files.
- Source compile error: Java local variable `share` duplicated inside `MainActivity.render()` lambda. Scoped rename to `aiShare` only; original share behavior retained.
- CI run https://github.com/Viyeu/sangit-android-agent/actions/runs/43605169862: **SUCCESS**.
- Toolchain: GitHub Ubuntu runner, JDK 17, Android SDK 35, build-tools 35.0.0, Gradle 8.9.
- Offline Java regression 49/49 PASS, Java syntax 9/9 PASS, static privacy guards PASS, `gradle --no-daemon --stacktrace :app:assembleDebug` PASS, `aapt dump badging` PASS, `apksigner verify` PASS.
- Debug APK SHA-256: `0d34203f9191ca8705a8e331f184186a335a98ea5d279cbe46f891299df2007a`; size 31,436 bytes; package `vn.sangit.androidagent`; version 0.2.0 (code 2).
- Artifact: `sangit-android-agent-v0.2-debug` on run 43605169862 (GitHub retention until 2026-11-28). Artifact downloaded independently; ZIP CRC and extracted APK SHA-256 verified.
- Android 16 physical-device UI/permission/Facebook share/calendar E2E: **NOT RUN** (no connected device). Production release signing and deployment: **NOT RUN**.
- No Core, VPS, WordPress, Panel or production state changed by this task.

CURRENT STATE: **DEBUG APK BUILT AND VERIFIED / DEVICE E2E PENDING / PRODUCTION RELEASE NOT AUTHORIZED**.

## 2026-11-14 — Play Protect report and minimal-permission v0.3.1
- User provided physical Android 16 screenshot: Internet-downloaded v0.3 blocked by Google Play Protect for sensitive access.
- Confirmed BIND_NOTIFICATION_LISTENER_SERVICE in v0.3 manifest. Google Play Protect guidance explicitly identifies NOTIFICATION_LISTENER as a sideloading high-risk permission.
- Built separate v0.3.1 minimal-permission candidate: remove listener registration and notification-access UI, preserve manual Facebook/Messenger and standalone MCP pairing; keep same applicationId and migrate no user data.
- Offline source test 49 Java assertions + Java parser + standalone MCP tests PASS locally; CI APK and device test pending at commit time. Do not disable Play Protect. No SangIT Core/VPS/prod changes.

## 2026-11-14 — v0.3.1 Play Protect minimal-permission debug APK VERIFIED
- User observed physical device blocking the v0.3 debug APK with Google Play Protect sensitive-data warning. Google developer guidance ties this specific Internet sideload warning to NOTIFICATION_LISTENER and other high-risk permissions.
- Removed NotificationListenerService registration from manifest and access-permission UI in v0.3.1; manual Facebook/Messenger shortcuts and independent MCP remain; old v0.3 source is retained in Git history.
- GitHub Actions CI: https://github.com/Viyeu/sangit-android-agent/actions/runs/43614618249, SUCCESS, commit 7f0980bca28d3f2d06a387f207b2ea0f7bf45b84.
- CI compiled Android SDK 35/JDK 17/Gradle 8.9, offline Java 49/49, static and syntax PASS, `aapt dump xmltree` verified no NotificationListenerService/BIND_NOTIFICATION_LISTENER_SERVICE, `apksigner verify` PASS.
- Extracted debug APK 35,716 bytes, SHA-256 d6c42e9e44c3db209177bf777baa5d3543012d99384eb0dbddadd0e68d334e55, verified artifact ZIP CRC and APK checksum independently.
- Android physical installation and Play Protect outcome: NOT TESTED. Cannot claim Google Play Protect accepts it until device verifies. If blocked again, collect exact new warning and examine alternate causes or Google Play Protect appeal. Do not bypass Play Protect.
- Standalone MCP server HTTPS/OAuth LIVE still BLOCKED; SangIT Core, VPS/Panel and production untouched during this fix.

## 2026-11-14 — Android Agent v0.3.2 UI/UX
- Scope: UI MainActivity, Android version, static assertions, CI labels and documentation. No Core/VPS/standalone backend edits.
- Reorganized existing functional sections into 4 persistent bottom tabs (Tổng quan, Bài viết, Lịch, MCP), 52dp tap targets, updated palette/cards/button feedback and fixed stale footer.
- Tab changes only visibility, do not destroy editors or current in-memory MCP session. Existing handlers compared against prior source and retained.
- Offline: 49/49 Java assertions, 10/10 Java syntax, privacy/static checks PASS on prepared source. CI APK and device responsive/E2E remain separate gates.

## 2026-11-14 — v0.3.2 UI/UX debug APK CI verified
- CI run https://github.com/Viyeu/sangit-android-agent/actions/runs/43617877035: SUCCESS, source commit 64470d24185828229cf680c929868aeda1c6e7ab.
- GitHub JDK17 + SDK35 + Gradle8.9: compile PASS, 49/49 Java offline, 10/10 syntax parser, static privacy and navigation guards PASS.
- APK manifest check explicitly rejects BIND_NOTIFICATION_LISTENER_SERVICE / NotificationListenerService; apksigner verify PASS.
- Downloaded artifact ZIP CRC PASS; APK SHA-256 2d40cbf9ba96316c1f529ea7bc1aba1a6bd05db24e1ce4dc84fe166792d7f68e; size 37,284 bytes; package vn.sangit.androidagent, version 0.3.2/code5.
- Handler-count regression preserved all MCP (pair/checkIn/disconnect), draft, metrics, sharing, Calendar and suggestion actions.
- Real Android 16 responsive, gesture, focus/keyboard, draft retention, upgrade signature and UI E2E: NOT RUN; user confirmation required. Production signing not done.
- No SangIT Core, VPS/Caddy, standalone MCP server or production changes.
STATE: **UI SOURCE + DEBUG APK BUILT / STATIC + CI PASS / PHYSICAL ANDROID E2E PENDING**.

## 2026-11-15 — Android screenshots reviewed, UI v0.3.3
- User screenshots confirm the four tabs render, selected tab states are visible and no horizontal overflow appears. Screenshots alone do not verify button/tap E2E.
- Identified root cause of old day shown after midnight: dated planner and checklist were built in render() once; onResume() updated metrics only.
- UI-only patch: rebuild dated planner/checklist after date change on resume or entering Lịch; preserve draft editors and in-memory MCP session. Show three planner days by default, seven on request. Shorten explanatory text and clarify that MCP URL must be supplied by the standalone server.
- No changes to network/auth, Android permissions, stored data keys, Core or VPS. Debug APK build and physical-device interactive E2E must be separately verified.

## 2026-11-15 — v0.3.3 Android UI verified CI
- GitHub Actions run https://github.com/Viyeu/sangit-android-agent/actions/runs/43619533804: SUCCESS at source commit fc8273ae7b37f255d46623a0232c9c6bd5f28d1e.
- After updating a wording-sensitive static assertion in `scripts/verify-static.py`, 49/49 Java assertions, 10/10 syntax parser and static privacy guards PASS.
- Gradle assembleDebug, compiled AndroidManifest no notification listener, apksigner verify, upload artifact: PASS.
- Independently downloaded CI artifact and verified ZIP CRC + APK ZIP integrity, APK SHA-256 f32bbbd9a907238179b61cae2ce6401c59700abfefea417fc2d61d6710f9faa5, size 37,792 bytes; `vn.sangit.androidagent`, v0.3.3 / code 6.
- Calendar date changes detected on resume and Lịch tab navigation without rerendering editor or pairing state. Three-day compact view expandable to seven days; placeholders no longer advertise a fake MCP hostname.
- Physical Android 16 UX interaction/regression test: PENDING device confirmation. Standalone MCP HTTPS/OAuth LIVE: BLOCKED, unaffected.
- No Core/WordPress/VPS/Panel/Caddy/production modification.
STATE: **UI v0.3.3 DEBUG APK BUILD VERIFIED / DEVICE INTERACTION E2E PENDING**.

## 2026-11-15 — Full native icons v0.3.4
- Adds 20 internal VectorDrawable icons to the 4 bottom tabs, card headers, buttons, metric labels, MCP inputs and status messages. No network dependency or new permissions.
- Selected tab icon tint matches active state, 64dp navigation remains accessible; previous UI event handlers and independent MCP remain unchanged.
- CI build and device-interaction E2E checked separately. No Core/VPS/production changes.

## 2026-11-15 — v0.3.4 full icon APK verified
- GitHub Actions https://github.com/Viyeu/sangit-android-agent/actions/runs/43621249490: SUCCESS at e5571921054d2d1c5e4c32560af7e0e6386b924c.
- Offline Java 49/49, Java syntax 10/10, static privacy and 20-vector-XML guards PASS.
- Gradle SDK35 JDK17 build PASS, apksigner verify PASS; compiled AndroidManifest contains no notification listener. ZIP CRC PASS.
- Extracted APK SHA-256: 9f8c2f6b84e9ef183b522bd5a16fddb80e64e005a6358b3d270cef31006e6f66, size 51731 bytes, package vn.sangit.androidagent v0.3.4/code7.
- Binary APK ZIP readback confirms 20 bundled res/drawable/ic_sangit_*.xml resources. Existing Java handlers remain unchanged; native icon UI state tint implemented.
- Physical device taps/layout/accessibility and state regression NOT tested yet. No Core/VPS/MCP server production mutations.
STATE: **v0.3.4 DEBUG APK + SOURCE VERIFIED / PHYSICAL UI E2E PENDING**.

## 2026-11-15 — Android Agent v0.3.5 compact full-function UI preflight
- Baseline: v0.3.4 complete icons, run 43621249490 PASS; screenshots of four tabs reviewed previously.
- Source-only UX changes: shorter heading/card copy, smaller card chrome, responsive dual-action rows (>=360dp) with 1-column fallback, 6-line draft and 3-line reply editors, 48dp secondary and 52dp primary button targets, improved MCP keyboard and visible state panel, soft keyboard adjustResize.
- Lịch without draft now routes to Bài viết to guide completion; no data removal.
- Preserves existing 4 tabs/20 native vectors, all actions, SharedPreferences keys, package ID, permissions and standalone MCP protocol.
- Static handler-count preflight: 12 handler paths preserved.
- CI/manifest/signature and physical Android E2E remain separate verification gates. No Core/VPS/production changes.

## 2026-11-15 — v0.3.5 compact UI/UX debug APK VERIFIED
- GitHub Actions run https://github.com/Viyeu/sangit-android-agent/actions/runs/43623143154: SUCCESS, source commit e3d1f875243878a458296dab6efeb2834b7428e3.
- CI SDK35 + JDK17 + Gradle8.9: 49/49 Java assertions, 10/10 Java syntax, static UI/permissions guards, assembled APK and apksigner verify PASS; compiled manifest retains INTERNET only with no NotificationListenerService.
- Artifact ZIP CRC PASS; APK SHA-256 37368b5fdced608ed1b314a05f09d80a4145b7e4efb4b8a0ad736f68e44ea05c, 51,815 bytes, package vn.sangit.androidagent, version 0.3.5/code8; 20 vector resources verified in APK ZIP.
- UX scope: compact header/cards, adaptive dual-action shortcuts (>=360dp, narrow fallback), lighter editors and help text, 48dp secondary/52dp primary actions, keyboard adjustment and MCP state panel. Empty Calendar draft takes user to Bài viết.
- 12 categories of preexisting handlers checked for unchanged call counts; all callbacks, local storage keys, 4 tabs, manual share and standalone MCP preserved.
- DEVICE E2E NOT RUN: requires Android 16 smoke of tab taps, narrow viewport, keyboard focus, share/photo/Calendar/MCP and upgrade without losing draft. No Core/VPS/Panel/WordPress/backend change.
STATE: **v0.3.5 SOURCE + DEBUG APK CI PASS / ANDROID DEVICE E2E PENDING**.
