package com.mohdrozaimi.langitlauncher.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.mohdrozaimi.langitlauncher.domain.WeatherParser
import com.mohdrozaimi.langitlauncher.domain.WeatherState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.coroutines.resume

class WeatherRepository(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun fetch(): WeatherState = withContext(Dispatchers.IO) {
        val location = findLocation() ?: return@withContext WeatherState.Unavailable
        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=${location.latitude}&longitude=${location.longitude}" +
                    "&current=temperature_2m,weather_code&daily=temperature_2m_max,temperature_2m_min" +
                    "&timezone=auto&forecast_days=1",
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000
            val body = try {
                conn.inputStream.bufferedReader().use { it.readText() }
            } finally {
                conn.disconnect()
            }
            val info = WeatherParser.parse(body) ?: return@withContext WeatherState.Unavailable
            WeatherState.Ready(info, cityName(location))
        } catch (e: Exception) {
            WeatherState.Unavailable
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun findLocation(): Location? {
        if (!hasLocationPermission()) return null
        val lm = context.getSystemService(LocationManager::class.java) ?: return null
        val last = try {
            lm.getProviders(true).mapNotNull { lm.getLastKnownLocation(it) }.maxByOrNull { it.time }
        } catch (e: SecurityException) {
            null
        }
        if (last != null) return last
        val provider = when {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> return null
        }
        return withTimeoutOrNull(15_000) {
            suspendCancellableCoroutine<Location?> { cont ->
                val signal = CancellationSignal()
                cont.invokeOnCancellation { signal.cancel() }
                try {
                    LocationManagerCompat.getCurrentLocation(
                        lm, provider, signal, ContextCompat.getMainExecutor(context),
                    ) { loc -> if (cont.isActive) cont.resume(loc) }
                } catch (e: Exception) {
                    if (cont.isActive) cont.resume(null)
                }
            }
        }
    }

    private fun cityName(location: Location): String = try {
        if (!Geocoder.isPresent()) {
            ""
        } else {
            @Suppress("DEPRECATION")
            val list = Geocoder(context, Locale.getDefault()).getFromLocation(location.latitude, location.longitude, 1)
            val a = list?.firstOrNull()
            a?.locality ?: a?.subAdminArea ?: a?.adminArea ?: ""
        }
    } catch (e: Exception) {
        ""
    }
}
