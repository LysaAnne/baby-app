# Development

## Requirements

- Android Studio 2026.1 or another version compatible with Android Gradle Plugin 9.2
- JDK 17 or newer; Android Studio's bundled JDK is supported
- Android SDK 37

The project intentionally targets Android API 37 and supports API 26 and newer. API 26 is the Stage 1 baseline and can be reconsidered only when device research shows a meaningful need.

## Commands

Run these from the repository root:

```shell
./gradlew checkCodeStyle
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew connectedDebugAndroidTest
```

The connected test command requires an emulator or Android device. A suitable CI verification command is:

```shell
./gradlew checkCodeStyle testDebugUnitTest lintDebug assembleDebug
```

If Java is not on the shell path on macOS, use Android Studio's bundled runtime:

```shell
JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" ./gradlew assembleDebug
```

## Module strategy

Stage 1 uses one `app` module with explicit package boundaries. This keeps the foundation easy to understand while the product has no implemented features. Shared and feature Gradle modules will be extracted in later stages when their boundaries and reuse are real.

## Logging policy

Logs must not include personal or health information. This includes child or caregiver names, dates of birth, event records, notes, identifiers, attachment paths, and contact details.

The logging abstraction only accepts predefined event names. Debug logging is disabled in release builds. Any future telemetry requires a separate privacy review and an explicit product decision.

