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
