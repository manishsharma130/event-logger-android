# Changelog

## 0.1.0

- Initial release: `logEvent` with `AnalyticsEventTag` or custom string, `Bundle` or `Map` params
- Compact JSON Logcat line (`AnalyticsEvent`), max 3,800 UTF-8 bytes
- `AnalyticsEventLogger.isEnabled` so host apps can limit logging to debug builds
