package com.eventlogger.events_logger

import android.os.Bundle
import android.os.Parcelable
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.util.ArrayList

/**
 * Builds compact, valid JSON for [AnalyticsEventLogger].
 *
 * Payload size is capped at [MAX_UTF8_BYTES]. [eventName] and [eventTag] are always kept.
 * Parameters are added only when the complete key/value pair still fits; values are never truncated.
 */
internal object EventJsonBuilder {

    const val MAX_UTF8_BYTES: Int = 3800

    fun build(
        eventName: String,
        eventTag: String,
        eventParams: Bundle?,
        maxUtf8Bytes: Int = MAX_UTF8_BYTES
    ): String {
        return pack(eventName, eventTag, bundleToJsonObject(eventParams), maxUtf8Bytes)
    }

    fun build(
        eventName: String,
        eventTag: String,
        eventParams: Map<String, Any?>?,
        maxUtf8Bytes: Int = MAX_UTF8_BYTES
    ): String {
        return pack(eventName, eventTag, mapToJsonObject(eventParams), maxUtf8Bytes)
    }

    fun pack(
        eventName: String,
        eventTag: String,
        paramsJson: JSONObject,
        maxUtf8Bytes: Int = MAX_UTF8_BYTES
    ): String {
        val packedParams = JSONObject()
        var json = assemble(eventName, eventTag, packedParams)

        val keys = paramsJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = paramsJson.get(key)
            packedParams.put(key, value)
            val candidate = assemble(eventName, eventTag, packedParams)
            if (utf8Size(candidate) <= maxUtf8Bytes) {
                json = candidate
            } else {
                packedParams.remove(key)
            }
        }
        return json
    }

    fun utf8Size(value: String): Int = value.toByteArray(StandardCharsets.UTF_8).size

    private fun assemble(eventName: String, eventTag: String, eventParams: JSONObject): String {
        return buildString {
            append("{\"eventName\":")
            append(JSONObject.quote(eventName))
            append(",\"eventParams\":")
            append(eventParams.toString())
            append(",\"eventTag\":")
            append(JSONObject.quote(eventTag))
            append('}')
        }
    }

    private fun mapToJsonObject(params: Map<String, Any?>?): JSONObject {
        val json = JSONObject()
        if (params == null) return json
        for ((key, value) in params) {
            try {
                val jsonValue = toJsonValue(value) ?: continue
                json.put(key, jsonValue)
            } catch (_: Exception) {
                // Skip values that cannot be represented in JSON.
            }
        }
        return json
    }

    private fun bundleToJsonObject(bundle: Bundle?): JSONObject {
        val json = JSONObject()
        if (bundle == null) return json
        for (key in bundle.keySet()) {
            if (key == null) continue
            try {
                @Suppress("DEPRECATION")
                val jsonValue = toJsonValue(bundle.get(key)) ?: continue
                json.put(key, jsonValue)
            } catch (_: Exception) {
                // Skip values that cannot be represented in JSON.
            }
        }
        return json
    }

    private fun toJsonValue(value: Any?): Any? {
        return when (value) {
            null -> JSONObject.NULL
            is JSONObject, is JSONArray -> value
            is Bundle -> bundleToJsonObject(value)
            is Map<*, *> -> {
                val nested = linkedMapOf<String, Any?>()
                value.forEach { (nestedKey, nestedValue) ->
                    if (nestedKey != null) nested[nestedKey.toString()] = nestedValue
                }
                mapToJsonObject(nested)
            }
            is Boolean, is Int, is Long, is String -> value
            is Double -> toCompactNumber(value)
            is Float -> toCompactNumber(value.toDouble())
            is Short -> value.toInt()
            is Byte -> value.toInt()
            is CharSequence -> value.toString()
            is BooleanArray -> JSONArray().also { array -> value.forEach { array.put(it) } }
            is IntArray -> JSONArray().also { array -> value.forEach { array.put(it) } }
            is LongArray -> JSONArray().also { array -> value.forEach { array.put(it) } }
            is DoubleArray -> JSONArray().also { array ->
                value.forEach { array.put(toCompactNumber(it)) }
            }
            is FloatArray -> JSONArray().also { array ->
                value.forEach { array.put(toCompactNumber(it.toDouble())) }
            }
            is Array<*> -> JSONArray().also { array ->
                value.forEach { item ->
                    val mapped = toJsonValue(item)
                    if (mapped != null) array.put(mapped)
                }
            }
            is ArrayList<*>, is List<*> -> JSONArray().also { array ->
                value.forEach { item ->
                    val mapped = toJsonValue(item)
                    if (mapped != null) array.put(mapped)
                }
            }
            is Parcelable -> null
            else -> null
        }
    }

    private fun toCompactNumber(value: Double): Any {
        if (value.isFinite() &&
            value % 1.0 == 0.0 &&
            value >= Long.MIN_VALUE.toDouble() &&
            value <= Long.MAX_VALUE.toDouble()
        ) {
            return value.toLong()
        }
        return value
    }
}
