package com.josh.tvhome

import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

// Location comes from local.properties (tv.lat, tv.lon). Open-Meteo needs no API key.
private const val LAT = BuildConfig.TV_LAT
private const val LON = BuildConfig.TV_LON

object Weather {
    /** Current outside temperature in Fahrenheit, or null if the request fails. */
    suspend fun fetchFahrenheit(): Int? = withContext(Dispatchers.IO) {
        try {
            val url = URL(
                "https://api.open-meteo.com/v1/forecast?latitude=$LAT&longitude=$LON" +
                    "&current=temperature_2m&temperature_unit=fahrenheit"
            )
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            conn.disconnect()
            JSONObject(body).getJSONObject("current").getDouble("temperature_2m").roundToInt()
        } catch (e: Exception) {
            null
        }
    }
}
