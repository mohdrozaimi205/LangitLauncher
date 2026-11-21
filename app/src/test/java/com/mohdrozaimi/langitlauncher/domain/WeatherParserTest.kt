package com.mohdrozaimi.langitlauncher.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeatherParserTest {
    private val sample = """
        {"latitude":3.1,"longitude":101.7,
         "current":{"time":"2026-11-21T15:45","temperature_2m":30.6,"weather_code":3},
         "daily":{"time":["2026-11-21"],"temperature_2m_max":[33.2],"temperature_2m_min":[24.9]}}
    """.trimIndent()

    @Test
    fun parsesAndRounds() {
        assertEquals(WeatherInfo(31, 3, 33, 25), WeatherParser.parse(sample))
    }

    @Test
    fun brokenJsonGivesNull() {
        assertNull(WeatherParser.parse("{oops"))
        assertNull(WeatherParser.parse("{}"))
    }

    @Test
    fun labels() {
        assertEquals("Cerah", WeatherParser.conditionLabel(0))
        assertEquals("Berawan", WeatherParser.conditionLabel(3))
        assertEquals("Berkabus", WeatherParser.conditionLabel(45))
        assertEquals("Hujan", WeatherParser.conditionLabel(61))
        assertEquals("Hujan lebat", WeatherParser.conditionLabel(81))
        assertEquals("Ribut petir", WeatherParser.conditionLabel(95))
    }

    @Test
    fun unavailableShowsDashes() {
        assertEquals("--°", WeatherParser.tempText(WeatherState.Unavailable))
        assertEquals("31°", WeatherParser.tempText(WeatherState.Ready(WeatherInfo(31, 3, 33, 25), "KL")))
    }
}
