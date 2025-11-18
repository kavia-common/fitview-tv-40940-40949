# Android Instrumentation Tests

This folder contains UI instrumentation tests that run on device/emulator:
- HomeFlowTest: verifies home rails render, DPAD focus movement, and launching PlayerActivity.
- SearchVoiceTest: stubs voice recognition result or falls back to IME search and verifies focus on results.

Notes:
- Onboarding is marked complete via SharedPreferences to avoid first-run flow.
- Tests use Leanback UI, so view targeting relies on texts and container visibility.
