package au.com.codeka.weather.providers.openweathermap

import android.util.Log
import au.com.codeka.weather.BuildConfig
import au.com.codeka.weather.DebugLog
import au.com.codeka.weather.LenientDoubleTypeAdapter
import au.com.codeka.weather.model.*
import au.com.codeka.weather.providers.WeatherProvider
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class OpenWeatherMapProvider : WeatherProvider {
  companion object {
    private const val TAG = "codeka.weather"
    private const val API_KEY = BuildConfig.OPENWEATHERMAP_API_KEY
  }

  override fun fetchWeather(builder: WeatherInfo.Builder) {
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Double::class.java, LenientDoubleTypeAdapter())
        .create()

    try {
      Log.d(TAG, "Querying Wunderground for weather info for: ${builder.lat},${builder.lng}")
      val url = URL("https://api.openweathermap.org/data/2.5/onecall?lat=${builder.lat}&lon=${builder.lng}&appid=$API_KEY")
      Log.d(TAG, "Connecting to: $url")
      val conn = url.openConnection()
      val ins: InputStream = BufferedInputStream(conn.getInputStream())
     // Log.i(TAG, InputStreamReader(ins).readText())

      val json = JsonReader(InputStreamReader(ins, "UTF-8"))
      val response = gson.fromJson<OpenWeatherMapResponse>(json, OpenWeatherMapResponse::class.java)
      if (response?.currentConditions == null) {
        Log.i(TAG, "Response is empty!")
        DebugLog.current().log("Response is empty.")
        return
      }
      Log.d(TAG, "Response parsed successfully.")
      val seconds = response.currentConditions.timestamp
      val dt = Date(seconds * 1000)
      builder.setCurrentConditions(CurrentCondition.Builder()
          .setObservationTime(dt)
          .setTemperature(response.currentConditions.temp)
          .setFeelsLike(response.currentConditions.feelsLike)
          .setDescription(response.currentConditions.weather[0].description)
          .setPrecipitation(/* TODO: precipitation */ 0.0, 0.0)
          .setRelativeHumidity(/* TODO: relative humidity */ 0.0)
          .setIcon(response.currentConditions.icon)
          .build())
      for (i in response.forecast.indices) {
        builder.addForecast(Forecast.Builder()
            .setOffset(i)
            .setDescription(response.forecast[i].description)
            .setShortDescription(response.forecast[i].description /* TODO: short? */)
            .setIcon(response.forecast[i].icon)
            .setHighTemperature(response.forecast[i].temp.max)
            .setLowTemperature(response.forecast[i].temp.min)
            .build())
      }
/*      for (i in response.hourlyForecast!!.indices) {
        builder.addHourlyForecast(HourlyForecast.Builder()
            .setHour(response.hourlyForecast!![i]!!.time!!.hour!!.toInt())
            .setTemperature(response.hourlyForecast!![i]!!.temp!!.metric!!.toDouble())
            .setShortDescription(response.hourlyForecast!![i]!!.shortDescription)
            .setQpfMillimeters(response.hourlyForecast!![i]!!.qpf!!.metric!!.toDouble())
            .setIcon(WundergroundProvider.getWeatherIcon(response.hourlyForecast!![i]!!.icon!!))
            .build())
      }
       */
    } catch (e: Throwable) {
      Log.e(TAG, "Error fetching weather information.", e)
      DebugLog.current().log("Error fetching weather: " + e.message)
    }
  }
}