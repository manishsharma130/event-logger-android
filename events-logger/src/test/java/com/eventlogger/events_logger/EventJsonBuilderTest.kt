package com.eventlogger.events_logger

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class EventJsonBuilderTest {

    @Test
    fun pack_includesNameTagAndParams() {
        val params = JSONObject()
            .put("product_id", "P1001")
            .put("price", 999)

        val json = EventJsonBuilder.pack(
            eventName = "purchase",
            eventTag = AnalyticsEventTag.GOOGLE_ANALYTICS.value,
            paramsJson = params
        )

        val parsed = JSONObject(json)
        assertEquals("purchase", parsed.getString("eventName"))
        assertEquals("google_analytics", parsed.getString("eventTag"))
        assertEquals("P1001", parsed.getJSONObject("eventParams").getString("product_id"))
        assertEquals(999, parsed.getJSONObject("eventParams").getLong("price"))
        assertTrue(json.startsWith("""{"eventName":"purchase","eventParams":"""))
        assertTrue(json.endsWith(""","eventTag":"google_analytics"}"""))
    }

    @Test
    fun pack_keepsNameAndTagWhenParamsExceedBudget() {
        val params = JSONObject()
            .put("small", "ok")
            .put("huge", "x".repeat(4000))

        val json = EventJsonBuilder.pack(
            eventName = "purchase",
            eventTag = "clevertap",
            paramsJson = params,
            maxUtf8Bytes = EventJsonBuilder.MAX_UTF8_BYTES
        )

        val parsed = JSONObject(json)
        assertEquals("purchase", parsed.getString("eventName"))
        assertEquals("clevertap", parsed.getString("eventTag"))
        assertEquals("ok", parsed.getJSONObject("eventParams").getString("small"))
        assertTrue(!parsed.getJSONObject("eventParams").has("huge"))
        assertTrue(EventJsonBuilder.utf8Size(json) <= EventJsonBuilder.MAX_UTF8_BYTES)
    }

    @Test
    fun pack_neverTruncatesAParameterValue() {
        val maxBytes = 70
        val params = JSONObject().put("note", "abcdefghijklmnopqrstuvwxyz")

        val json = EventJsonBuilder.pack(
            eventName = "view",
            eventTag = "branch",
            paramsJson = params,
            maxUtf8Bytes = maxBytes
        )

        val parsed = JSONObject(json)
        assertTrue(!parsed.getJSONObject("eventParams").has("note"))
        assertEquals("view", parsed.getString("eventName"))
        assertEquals("branch", parsed.getString("eventTag"))
        assertTrue(EventJsonBuilder.utf8Size(json) <= maxBytes)
    }

    @Test
    fun build_fromMap_convertsDirectlyToJson() {
        val json = EventJsonBuilder.build(
            eventName = "purchase",
            eventTag = AnalyticsEventTag.MOENGAGE.value,
            eventParams = mapOf(
                "product_id" to "P1001",
                "price" to 999.0,
                "in_stock" to true
            )
        )

        val parsed = JSONObject(json)
        val params = parsed.getJSONObject("eventParams")
        assertEquals("purchase", parsed.getString("eventName"))
        assertEquals("moengage", parsed.getString("eventTag"))
        assertEquals("P1001", params.getString("product_id"))
        assertEquals(999, params.getLong("price"))
        assertEquals(true, params.getBoolean("in_stock"))
    }

    @Test
    fun pack_emptyParamsStillValidJson() {
        val json = EventJsonBuilder.pack(
            eventName = "login",
            eventTag = "moengage",
            paramsJson = JSONObject()
        )

        val parsed = JSONObject(json)
        assertEquals(0, parsed.getJSONObject("eventParams").length())
        assertEquals("login", parsed.getString("eventName"))
    }
}
