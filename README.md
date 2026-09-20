# Analytics Event Logger

A lightweight Android library that writes analytics events as compact JSON in Logcat. It is useful for debugging analytics integrations and collecting events through tools that read `adb logcat`.

The library does not send events to Google Analytics, Branch, MoEngage, or any other analytics provider.

## Installation

The minimum supported Android version is API 21. Choose one of the following approaches based on whether the library should be present in release builds.

### Approach 1: include only in debug builds (recommended)

Use `debugImplementation` to exclude the library completely from the release APK:

```kotlin
dependencies {
    debugImplementation("io.github.manishsharma130:events-logger:0.1.0")
}
```

Code under `src/main` is compiled for both debug and release, so it cannot directly reference a debug-only dependency. Use matching build-specific wrappers instead.

Create the debug implementation at `app/src/debug/java/com/example/app/AppEventLogger.kt`:

```kotlin
package com.example.app

import com.eventlogger.events_logger.AnalyticsEventLogger

object AppEventLogger {
    fun logEvent(
        eventTag: String,
        eventName: String,
        eventParams: Map<String, Any?> = emptyMap()
    ) {
        AnalyticsEventLogger.logEvent(eventTag, eventName, eventParams)
    }
}
```

Create the release no-op implementation at `app/src/release/java/com/example/app/AppEventLogger.kt`:

```kotlin
package com.example.app

object AppEventLogger {
    fun logEvent(
        eventTag: String,
        eventName: String,
        eventParams: Map<String, Any?> = emptyMap()
    ) {
        // Intentionally disabled in release builds.
    }
}
```

Both wrappers must have the same package, object name, and function signature. Application code under `src/main` calls only the wrapper:

```kotlin
AppEventLogger.logEvent(
    eventTag = "google_analytics",
    eventName = "purchase",
    eventParams = mapOf("product_id" to "P1001")
)
```

Android uses `src/main` plus `src/debug` for debug builds, and `src/main` plus `src/release` for release builds. The debug wrapper forwards events to this library; the release wrapper does nothing.

### Approach 2: include in all builds and disable release logging

Use `implementation` when direct access from `src/main` is preferred:

```kotlin
dependencies {
    implementation("io.github.manishsharma130:events-logger:0.1.0")
}
```

Enable logging only for debug builds:

```kotlin
AnalyticsEventLogger.isEnabled = BuildConfig.DEBUG
```

With this approach the library remains in the release APK, but `logEvent` returns without logging when `isEnabled` is `false`.

### Test a locally published version

Publish the library to your local Maven repository:

```bash
./gradlew :events-logger:publishToMavenLocal
```

Then add `mavenLocal()` to the consuming project's `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
}
```

For Groovy `settings.gradle`, use the same repository order without Kotlin-specific syntax.

## Usage

```kotlin
val params = Bundle().apply {
    putString("product_id", "P1001")
    putDouble("price", 999.0)
}

AnalyticsEventLogger.logEvent(
    eventTag = AnalyticsEventTag.GOOGLE_ANALYTICS,
    eventName = "purchase",
    eventParams = params
)
```

You can also pass a `Map<String, Any?>` or use a custom provider name:

```kotlin
AnalyticsEventLogger.logEvent(
    eventTag = "clevertap",
    eventName = "login",
    eventParams = mapOf("method" to "email")
)
```

Supported predefined tags are `GOOGLE_ANALYTICS`, `BRANCH`, and `MOENGAGE`.

## Output

Each event is written as one JSON line using the Logcat tag `AnalyticsEvent`:

```json
{"eventName":"purchase","eventParams":{"product_id":"P1001","price":999},"eventTag":"google_analytics"}
```

The library keeps output within 3,800 UTF-8 bytes. Parameters that do not fit are skipped, and the JSON remains valid.

## Production behavior

Logging is enabled by default. Use Approach 1 to exclude the library from release APKs, or Approach 2 with `BuildConfig.DEBUG` to keep release builds quiet. The logger catches conversion and logging errors so it does not crash the host application.

## Tests

```bash
./gradlew :events-logger:testDebugUnitTest
```

## Issues and pull requests

If you find a bug, please open a GitHub issue and include:

- A clear description of the problem
- Steps to reproduce it
- Expected and actual behavior
- Android version, device details, and library version
- Relevant logs or a small code example
- A screenshot or short video, if it helps explain the issue

Please remove passwords, tokens, personal information, and sensitive analytics data before sharing logs, screenshots, or videos.

To contribute a fix or improvement:

1. Fork the repository and create a branch for your change.
2. Make a focused change and add or update tests when applicable.
3. Run the tests locally.
4. Open a pull request with a clear description of what changed and why.
5. Link the related GitHub issue in the pull request, if one exists.

## Maintainer

[Manish Sharma](https://www.linkedin.com/in/manish-sharma-894b3b146/)

## License

Licensed under the [Apache License 2.0](LICENSE).
