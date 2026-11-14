# SangIT Android Agent — v0.2

## Scope
- Android native Java, independent APK for Facebook personal profile `facebook.com/sangitinfo/`.
- Only change this project. No Core, Social Provider, Page automation or production edits.
- Maintain v0.1 `applicationId`/preferences and no additional runtime dependencies.

## Boundaries
- Lightweight, Vietnamese, mobile-first, no external runtime libraries.
- No INTERNET, AccessibilityService, reading message content or autonomous posting/engagement.
- NotificationListener opt-in AND OS grant; only counts currently active packages from allowlist.
- Do not store names/messages/PII; user-entered drafts, local metrics and reminder stored in SharedPreferences private to app.
- User explicitly chooses Sharesheet recipients, confirms edits/comments in Facebook and calendar events in Calendar.
- ReplyAssistant is deterministic template engine; never call it generative AI. User may choose to share prompt to ChatGPT via Android Sharesheet.
- No credentials, cookies or Core tokens in Android source or on Core.
- Debug build is not a signed production release.

## Workflow
SCOPE -> BASELINE -> IMPLEMENT -> JAVA + STATIC TESTS -> SDK COMPILE -> DEVICE E2E -> REGRESSION -> RELEASE.
Final report must distinguish source-complete from APK/device-complete.

## Integration constraints
- Core v1.5.0 declares personal.publish but no dispatcher route; Social Provider Page-only and independent OAuth missing, so Core pairing / production writes are not enabled.
- Future pairing only after separately approved account/authorization contract with scoped device permissions; no reuse of Page tokens.

## Artifacts
- `README.md` usage + build.
- `scripts/test-offline.sh` Java business tests + syntax/parser + static safeguards.
- `.github/workflows/android-ci.yml` debug build template; no release secrets.
