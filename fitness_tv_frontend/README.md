# Fitness TV Frontend

## Overview and Features
Fitness TV Frontend is an Android TV application optimized for DPAD navigation and 10-foot experiences. It provides a focused fitness experience with mock/local data and no backend dependency. Core capabilities include:

- Workout videos with an ExoPlayer overlay showing live elapsed time and a calorie estimate that progresses during playback.
- Guided exercises presented as card-based rails: Dashboard Hero, Daily & Continue, Recommended, Progress (last 7 days), Motivational Badges, and Profile & Goals shortcuts.
- Personalized recommendations that bias by onboarding preferences (goals, preferred duration) with a fallback guarantee that results are never empty.
- Progress charts and metrics:
  - A hero panel with three circular ring gauges (Active %, Alerts, Energy).
  - A mini bar chart showing calories for the last seven days.
- Motivational badges with accessible content descriptions.
- Profiles and goals management via dialogs with validation and persistence using SharedPreferences (MockRepository).
- Voice search with graceful fallback to text entry and IME action; focus moves to results after search.
- TV-first accessibility:
  - Clear focus states, larger hit targets, and content descriptions on cards and charts.
  - DPAD-friendly navigation between rails and items.

## Architecture Overview
The app is built on Android TV’s Leanback library, using presenters and fragments for a familiar TV browsing UI.

- Leanback-based UI and Presenters
  - HomeFragment (BrowseSupportFragment) composes the home screen as multiple ListRows backed by ArrayObjectAdapter.
  - Custom Presenters:
    - WorkoutCardPresenter: Media cards with focus scaling, accent outline, and elevation changes.
    - HeroMetricsPresenter: Draws a composite hero card with three circular gauges and a mini bar strip.
    - ProgressChartPresenter: Renders a weekly calories mini bar chart.
- Fragments and Activities
  - MainActivity: Hosts HomeFragment. If onboarding is not completed, it launches OnboardingActivity.
  - OnboardingActivity: Multi-step onboarding using GuidedStepSupportFragment (welcome → goals → duration → analytics → finish).
  - SearchFragment: DialogFragment providing voice and text search. Results display in a HorizontalGridView using presenters.
  - ProfileFragment, GoalsFragment: DialogFragments to edit profile and goals using ViewBinding with validation and persistence.
  - PlayerActivity: Video playback with Media3 ExoPlayer. Displays overlay with title, elapsed time, and live calorie estimate.
- Data Layer
  - MockRepository: Local mock data for workouts, badges, progress, and simple persistence in SharedPreferences. Recommendation logic biases results by goals and preferred duration with a safe fallback.

## Remote Navigation and Accessibility Notes
- DPAD Navigation
  - Rows are focusable and scroll horizontally; DPAD Up/Down switches between rails; DPAD Left/Right navigates within a rail.
  - Focus feedback includes scale-up, accent ring, and elevation increase on focusable cards.
  - Initial focus is requested on the fragment root; Leanback manages card focus transitions.
- Accessibility
  - Cards and charts have content descriptions summarizing titles and key metrics.
  - Text sizes are chosen for TV readability, avoiding thin strokes for charts.
- Voice Search
  - Voice search is triggered via the search orb in HomeFragment or by SEARCH keycode.
  - If a speech recognizer is unavailable, the app falls back to text input and focuses the text field.

## Build Instructions
Prerequisites:
- JDK 17
- Android SDK with Android TV system images (recommend API 33 or 34) and standard build tools

Project uses Gradle wrapper; no global Gradle install required.

Common commands (run from the container root: fitness_tv_frontend):
- Clean and build debug APK:
  - ./gradlew clean assembleDebug
- Build with diagnostic stacktraces:
  - ./gradlew clean assembleDebug --no-daemon --stacktrace
- Run unit tests (hosted JVM):
  - ./gradlew testDebugUnitTest

Notes:
- ViewBinding is enabled and DataBinding is disabled.
- Java 17 is configured; core library desugaring is enabled for java.time APIs on minSdk 21.

## Installation to Device/Emulator
1) Start an Android TV emulator (or connect a physical Android TV device via USB/Wi‑Fi ADB):
- Create an Android TV AVD (e.g., Android TV (1080p) API 33/34) in Android Studio, then start it.
- Or connect a device: adb connect <ip>:5555

2) Install the debug APK after building:
- The debug APK is typically at: app/build/outputs/apk/debug/app-debug.apk
- Install with ADB:
  - adb install -r app/build/outputs/apk/debug/app-debug.apk

3) Launch the app
- Navigate to the app on the TV launcher.
- On first run, onboarding appears. You can re-run onboarding from Home > Profile & Goals if needed.

## Testing Instructions
There are two categories of tests:

1) Unit tests (hosted JVM)
- Command:
  - ./gradlew testDebugUnitTest
- Location:
  - app/src/test/java/com/example/fitness_tv_frontend
- Purpose:
  - Quick checks that do not require Android framework or device.

2) Instrumentation tests (on Android TV emulator/device)
- Command:
  - Start a TV emulator or connect a device.
  - ./gradlew connectedAndroidTest
- Location:
  - app/src/androidTest/java/com/example/fitness_tv_frontend
- What they cover:
  - HomeFlowTest: Rails render, DPAD smoke focus navigation, and launching PlayerActivity from a workout card.
  - SearchVoiceTest: Voice recognizer intent stubbing, IME fallback, and focus movement to results grid after search.
  - RecommendationLogicInstrumentedTest: Validates recommendation bias (goals/duration), non-empty guarantees, and intensity/category filtering.
- Emulator/device setup tips:
  - Use an Android TV system image (not phone/tablet).
  - Ensure Google APIs or voice services availability if you want to exercise voice flows (tests stub intents when needed).
  - If onboarding interferes with flows, tests mark onboarding as completed via SharedPreferences as part of setup.

## Troubleshooting
- Build fails with Java version errors:
  - Ensure JAVA_HOME points to JDK 17 and that Gradle uses Java 17. The project’s kotlinOptions.jvmTarget and compileOptions are set to 17.
- “Duplicate class” or dependency conflicts:
  - The current dependency set is verified clean. If adding libraries, check for transitive conflicts and align versions, especially AndroidX and Media3.
- DataBinding class not found:
  - DataBinding is disabled on purpose. Dialog bindings (DialogGoalsBinding, DialogProfileBinding, DialogSearchBinding) are generated by ViewBinding from XML layouts. Do not enable DataBinding unless migrating layouts to use a <layout> root.
- Instrumentation tests fail to discover device:
  - Verify an emulator is running: adb devices
  - Use Android TV system images. Phone images will not correctly host Leanback UIs or certain key events.
- Voice recognizer ActivityNotFoundException:
  - This is handled in-app with a toast and focus fallback to text search. On emulators without voice services, rely on text search and intent stubbing in tests.
- Media playback issues:
  - PlayerActivity uses a sample HLS URL from Media3 test content. Ensure the emulator/device has network access.
- Espresso focus assertions are flaky:
  - TV UIs can be timing-sensitive. If running on slow CI/emulator, consider re-running or adding small idling/timing tweaks locally during investigation.

## Future Integrations
This project is currently offline-first with a mock repository. To integrate services later:

- Backend API
  - Replace MockRepository with a data layer backed by Retrofit/OkHttp already declared in dependencies.
  - Introduce DTOs and mappers to models; keep UI contracts stable (Workout, Profile, Goal).
  - Add a simple Repository interface and DI wiring to swap implementations.

- Analytics
  - Onboarding stores analytics opt-in. Wire this preference to an analytics client (e.g., Firebase Analytics) behind a simple Analytics interface to respect opt-in.
  - Ensure events (open workout, complete workout, search, onboarding steps) use a centralized tracker.

- Configuration and Environment Variables
  - If server endpoints or keys are needed, prefer Gradle buildConfigField or resValue for environment-like configuration rather than runtime .env files on Android.
  - For secrets, use Play Console or CI/CD-managed keystores/vars; do not hardcode tokens in source.

- Extended Accessibility and Internationalization
  - Maintain descriptive contentDescription updates in presenters and player overlay.
  - Externalize more strings in res/values/strings.xml for easy localization.

## Quick Reference Commands
- Build APK (debug): ./gradlew clean assembleDebug
- Run unit tests (JVM): ./gradlew testDebugUnitTest
- Run instrumentation tests (TV emulator/device): ./gradlew connectedAndroidTest
- Install APK via ADB: adb install -r app/build/outputs/apk/debug/app-debug.apk

## Credits
- Leanback UI components and Media3 ExoPlayer are used under their respective AndroidX libraries.
- Design language adapted from internal style notes with a dark + green TV theme.

---
Sources: 
- app/build.gradle.kts
- app/src/main/AndroidManifest.xml
- app/src/main/java/com/example/fitness_tv_frontend/MainActivity.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/home/HomeFragment.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/player/PlayerActivity.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/search/SearchFragment.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/onboarding/OnboardingActivity.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/onboarding/OnboardingFragments.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/onboarding/OnboardingPrefs.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/widgets/HeroMetricsPresenter.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/widgets/ProgressChartPresenter.kt
- app/src/main/java/com/example/fitness_tv_frontend/ui/widgets/WorkoutCardPresenter.kt
- app/src/main/java/com/example/fitness_tv_frontend/data/MockRepository.kt
- app/src/androidTest/java/com/example/fitness_tv_frontend/HomeFlowTest.kt
- app/src/androidTest/java/com/example/fitness_tv_frontend/SearchVoiceTest.kt
- app/src/androidTest/java/com/example/fitness_tv_frontend/RecommendationLogicInstrumentedTest.kt
- BUILD_REPORT.md
