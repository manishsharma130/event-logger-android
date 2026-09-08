# Analytics Event Logger

A lightweight Android library that writes analytics events as compact JSON in Logcat. It is useful for debugging analytics integrations and collecting events through tools that read `adb logcat`.

The library does not send events to Google Analytics, Branch, MoEngage, or any other analytics provider.

## Installation

After the library is published to Maven Central, add:

```kotlin
implementation("io.github.manishsharma130:events-logger:0.1.0")
```

The minimum supported Android version is API 21.

To test the library locally:

```bash
./gradlew :events-logger:publishToMavenLocal
```

Add `mavenLocal()` to the consuming project's repositories:

```kotlin
repositories {
    mavenLocal()
}
```

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

## Disable production logging

Logging is enabled by default. Set it from your application so release builds remain quiet:

```kotlin
AnalyticsEventLogger.isEnabled = BuildConfig.DEBUG
```

The logger catches conversion and logging errors so it does not crash the host application.

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
