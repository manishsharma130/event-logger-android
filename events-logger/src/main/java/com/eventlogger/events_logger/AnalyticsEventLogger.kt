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
 * release AAR still works in the app's debug builds. The API never throws to the caller.
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
     * The map is converted directly to JSON; the 3,800-byte packing rules are unchanged.
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
