package com.eventlogger.events_logger

/**
 * Built-in analytics providers that can be passed to [AnalyticsEventLogger.logEvent].
 *
 * Use the string overload of `logEvent` for any provider that is not listed here.
 *
 * Example:
 * ```kotlin
 * AnalyticsEventLogger.logEvent(
 *     eventTag = AnalyticsEventTag.GOOGLE_ANALYTICS,
 *     eventName = "screen_view"
 * )
 * ```
 *
 * @property value provider identifier written to the `eventTag` JSON field
 */
enum class AnalyticsEventTag(val value: String) {
    /** Google Analytics provider identifier. */
    GOOGLE_ANALYTICS("google_analytics"),

    /** Branch provider identifier. */
    BRANCH("branch"),

    /** MoEngage provider identifier. */
    MOENGAGE("moengage")
}
