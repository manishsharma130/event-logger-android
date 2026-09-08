package com.eventlogger.events_logger

/**
 * Built-in analytics providers that can be passed to [AnalyticsEventLogger.logEvent].
 *
 * Use the string overload of `logEvent` for any provider that is not listed here.
 */
enum class AnalyticsEventTag(val value: String) {
    GOOGLE_ANALYTICS("google_analytics"),
    BRANCH("branch"),
    MOENGAGE("moengage")
}
