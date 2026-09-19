package com.eventlogger.events_logger

import android.os.Bundle
import android.util.Log

/**
 * Analytics event logger for debug collection.
 *
 * Converts each call into a compact JSON payload and prints it to Logcat with tag
 * [LOG_TAG] so a host process (for example a Node.js log collector) can filter and parse it:
 *
 * ```javascript
 * const event = JSON.parse(log.message);
 * ```
 *
 * Logging is controlled by [isEnabled] (on by default). Set it from the host app,
 * for example `AnalyticsEventLogger.isEnabled = BuildConfig.DEBUG`, so a published
 * release AAR still works in the app's debug builds.
 *
 * Event payloads are limited to 3,800 UTF-8 bytes. Parameters that would exceed the
 * limit, or cannot be represented as JSON, are skipped. Logging and conversion errors
 * are caught so this API does not throw to the caller.
 *
 * Example using a predefined provider and Android [Bundle]:
 * ```kotlin
 * val params = Bundle().apply {
 *     putString("product_id", "P1001")
 *     putDouble("price", 999.0)
 * }
 *
 * AnalyticsEventLogger.logEvent(
 *     eventTag = AnalyticsEventTag.GOOGLE_ANALYTICS,
 *     eventName = "purchase",
 *     eventParams = params
 * )
 * ```
 *
 * Example using a custom provider and map parameters:
 * ```kotlin
 * AnalyticsEventLogger.logEvent(
 *     eventTag = "clevertap",
 *     eventName = "login",
 *     eventParams = mapOf("method" to "email")
 * )
 * ```
 */
object AnalyticsEventLogger {

    /** Logcat tag used for every event line. */
    const val LOG_TAG: String = "AnalyticsEvent"

    /**
     * When `false`, [logEvent] returns immediately.
     * Set this from the host app (typically `BuildConfig.DEBUG`).
     */
    @JvmStatic
    @Volatile
    var isEnabled: Boolean = true

    /**
     * Logs an analytics event for a predefined provider.
     *
     * @param eventTag analytics provider
     * @param eventName event name, such as `"purchase"`
     * @param eventParams optional event parameters
     */
    @JvmStatic
    @JvmOverloads
    fun logEvent(
        eventTag: AnalyticsEventTag,
        eventName: String,
        eventParams: Bundle? = null
    ) {
        logEvent(eventTag.value, eventName, eventParams)
    }

    /**
     * Logs an analytics event for any provider tag, including custom values such as `"clevertap"`.
     *
     * @param eventTag analytics provider identifier
     * @param eventName event name, such as `"purchase"`
     * @param eventParams optional event parameters
     */
    @JvmStatic
    @JvmOverloads
    fun logEvent(
        eventTag: String,
        eventName: String,
        eventParams: Bundle? = null
    ) {
        writeLog {
            EventJsonBuilder.build(
                eventName = eventName,
                eventTag = eventTag,
                eventParams = eventParams
            )
        }
    }

    /**
     * Logs an analytics event with parameters supplied as a map.
     *
     * Nested maps, lists, arrays, JSON objects, JSON arrays, strings, booleans, and numeric
     * values are supported. Unsupported values are skipped. The 3,800-byte payload limit
     * is applied after JSON conversion.
     *
     * @param eventTag analytics provider
     * @param eventName event name, such as `"purchase"`
     * @param eventParams event parameters to convert to JSON
     */
    @JvmStatic
    fun logEvent(
        eventTag: AnalyticsEventTag,
        eventName: String,
        eventParams: Map<String, Any?>
    ) {
        logEvent(eventTag.value, eventName, eventParams)
    }

    /**
     * Logs an analytics event for any provider tag with parameters supplied as a map.
     *
     * Nested maps, lists, arrays, JSON objects, JSON arrays, strings, booleans, and numeric
     * values are supported. Unsupported values are skipped. The 3,800-byte payload limit
     * is applied after JSON conversion.
     *
     * @param eventTag analytics provider identifier, including custom values such as `"clevertap"`
     * @param eventName event name, such as `"purchase"`
     * @param eventParams event parameters to convert to JSON
     */
    @JvmStatic
    fun logEvent(
        eventTag: String,
        eventName: String,
        eventParams: Map<String, Any?>
    ) {
        writeLog {
            EventJsonBuilder.build(
                eventName = eventName,
                eventTag = eventTag,
                eventParams = eventParams
            )
        }
    }

    private fun writeLog(json: () -> String) {
        if (!isEnabled) return
        try {
            Log.d(LOG_TAG, json())
        } catch (_: Exception) {
            // Library must never crash the host application.
        }
    }
}
