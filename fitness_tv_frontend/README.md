# Fitness TV Frontend

Android TV app built with Leanback:
- Home rails: Dashboard Hero, Daily & Continue, Recommended, Progress (7 days), Badges, Profile & Goals
- Voice search with graceful fallback to text
- Player overlay with live timer and calorie estimate
- Profile & Goals dialogs with validation and persistence (MockRepository)
- Recommendations respect onboarding preferences (intensity/duration), always non-empty
- Accessible content descriptions on cards and charts

Build
- ./gradlew clean assembleDebug

Run
- Launch on Android TV emulator/device
- Initial onboarding can be rerun from Home > Profile & Goals

Notes
- No external network dependencies for UI beyond ExoPlayer sample HLS URL
- Dark + green theme per assets/style_guide.md
