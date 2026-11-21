package com.mohdrozaimi.langitlauncher.domain

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.math.roundToInt

data class WeatherInfo(val tempC: Int, val code: Int, val hiC: Int, val loC: Int)

sealed interface WeatherState {
    data object Loading : WeatherState
    data class Ready(val info: WeatherInfo, val city: String) : WeatherState
    data object Unavailable : WeatherState
}

object WeatherParser {
    /** Baca jawapan Open-Meteo. JSON rosak -> null. */
    fun parse(json: String): WeatherInfo? = try {
        val root = Json.parseToJsonElement(json).jsonObject
        val current = root.getValue("current").jsonObject
        val daily = root.getValue("daily").jsonObject
        WeatherInfo(
            tempC = current.getValue("temperature_2m").jsonPrimitive.double.roundToInt(),
            code = current.getValue("weather_code").jsonPrimitive.int,
            hiC = daily.getValue("temperature_2m_max").jsonArray[0].jsonPrimitive.double.roundToInt(),
            loC = daily.getValue("temperature_2m_min").jsonArray[0].jsonPrimitive.double.roundToInt(),
        )
    } catch (e: Exception) {
        null
    }

    fun conditionLabel(code: Int): String = when (code) {
        0 -> "Cerah"
        1, 2, 3 -> "Berawan"
        45, 48 -> "Berkabus"
        in 51..67 -> "Hujan"
        in 71..77 -> "Salji"
        in 80..82 -> "Hujan lebat"
        in 95..99 -> "Ribut petir"
        else -> "-"
    }

    fun conditionSymbol(code: Int): String = when (code) {
        0 -> "☀️"
        1, 2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        in 51..67, in 80..82 -> "🌧️"
        in 71..77 -> "❄️"
        in 95..99 -> "⛈️"
        else -> "☁️"
    }

    fun tempText(state: WeatherState): String =
        if (state is WeatherState.Ready) "${state.info.tempC}°" else "--°"
}
