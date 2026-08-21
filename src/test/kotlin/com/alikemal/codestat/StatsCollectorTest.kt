package com.alikemal.codestat

import com.alikemal.codestat.model.XP
import com.alikemal.codestat.model.XpResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class StatsCollectorTest {

    @Test
    fun testXpJsonSerialization() {
        val xp = XP("Java", 15)
        assertEquals("""{"language":"Java","xp":15}""", xp.toJson())
    }

    @Test
    fun testXpResponseJsonSerialization() {
        val xp1 = XP("Java", 10)
        val xp2 = XP("Kotlin", 5)
        val response = XpResponse("2026-08-22T01:00:00+02:00", listOf(xp1, xp2))

        val json = response.toJson()
        assertTrue(json.contains(""""coded_at":"2026-08-22T01:00:00+02:00""""))
        assertTrue(json.contains("""{"language":"Java","xp":10}"""))
        assertTrue(json.contains("""{"language":"Kotlin","xp":5}"""))
    }
}
