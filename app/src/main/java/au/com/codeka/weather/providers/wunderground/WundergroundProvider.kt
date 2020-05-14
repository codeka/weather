package au.com.codeka.weather.providers.wunderground

import android.util.Log
import au.com.codeka.weather.BuildConfig
import au.com.codeka.weather.DebugLog
import au.com.codeka.weather.LenientDoubleTypeAdapter
import au.com.codeka.weather.model.*
import au.com.codeka.weather.providers.Provider
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*

/**
 * Provider for fetching weather details from weather underground.
 */
class WundergroundProvider : Provider() {
  /** Fetches weather info from Weather Underground.  */
  override fun fetchWeather(builder: WeatherInfo.Builder) {
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Double::class.java, LenientDoubleTypeAdapter())
        .create()
    try {
      val latlng = builder.lat.toString() + "," + builder.lng
      Log.d(TAG, "Querying Wunderground for weather info for: $latlng")
      val url = URL(String.format(
          "http://api.wunderground.com/api/%s/conditions/forecast10day/hourly/q/%s.json",
          API_KEY,
          latlng))
      Log.d(TAG, "Connecting to: $url")
      val conn = url.openConnection()
      val ins: InputStream = BufferedInputStream(conn.getInputStream())
      val json = JsonReader(InputStreamReader(ins, "UTF-8"))
      val response = gson.fromJson<WundergroundResponse>(json, WundergroundResponse::class.java)
      if (response == null || response.currentObservation == null) {
        Log.i(TAG, "Response is empty!")
        DebugLog.current()!!.log("Response is empty.")
        return
      }
      Log.d(TAG, "Response parsed successfully.")
      val seconds = response.currentObservation!!.observationTime!!.toLong()
      val dt = Date(seconds * 1000)
      builder.setCurrentConditions(CurrentCondition.Builder()
          .setObservationLocation(response.currentObservation!!.observationLocation!!.full)
          .setObservationTime(dt)
          .setTemperature(response.currentObservation!!.temp)
          .setFeelsLike(response.currentObservation!!.feelsLike)
          .setDescription(response.currentObservation!!.weather)
          .setPrecipitation(
              response.currentObservation!!.precipitationLastHour,
              response.currentObservation!!.precipitationToday)
          .setRelativeHumidity(
              response.currentObservation!!.relativeHumidity!!.substring(
                  0, response.currentObservation!!.relativeHumidity!!.length - 1).toDouble())
          .setIcon(getWeatherIcon(response.currentObservation!!.icon!!))
          .build())
      for (i in 0 until response.forecast!!.txtForecast!!.days!!.size / 2) {
        builder.addForecast(Forecast.Builder()
            .setOffset(i)
            .setDescription(response.forecast!!.txtForecast!!.days!![i * 2]!!.forecastText)
            .setShortDescription(response.forecast!!.simpleForecast!!.days!![i]!!.conditions)
            .setIcon(getWeatherIcon(response.forecast!!.txtForecast!!.days!![i * 2]!!.icon!!))
            .setHighTemperature(response.forecast!!.simpleForecast!!.days!![i]!!.high!!.celsius!!.toDouble())
            .setLowTemperature(response.forecast!!.simpleForecast!!.days!![i]!!.low!!.celsius!!.toDouble())
            .build())
      }
      for (i in response.hourlyForecast!!.indices) {
        builder.addHourlyForecast(HourlyForecast.Builder()
            .setHour(response.hourlyForecast!![i]!!.time!!.hour!!.toInt())
            .setTemperature(response.hourlyForecast!![i]!!.temp!!.metric!!.toDouble())
            .setShortDescription(response.hourlyForecast!![i]!!.shortDescription)
            .setQpfMillimeters(response.hourlyForecast!![i]!!.qpf!!.metric!!.toDouble())
            .setIcon(getWeatherIcon(response.hourlyForecast!![i]!!.icon!!))
            .build())
      }
    } catch (e: IOException) {
      Log.e(TAG, "Error fetching weather information.", e)
      DebugLog.current()!!.log("Error fetching weather: " + e.message)
    }
  }

  override fun fetchMapOverlay(latLngBounds: LatLngBounds?, width: Int, height: Int): InputStream? {
    return try {
      val url = URL(String.format("http://api.wunderground.com/api/%s/animatedradar/image.gif?"
          + "minlat=%f&minlon=%f&maxlat=%f&maxlon=%f&width=%d&height=%d&timelabel=1&"
          + "timelabel.x=%d&timelabel.y=40&reproj.automerc=1&num=10&delay=50",
          API_KEY, latLngBounds!!.southwest.latitude, latLngBounds.southwest.longitude,
          latLngBounds.northeast.latitude, latLngBounds.northeast.longitude,
          width, height, width - 120))
      Log.d(TAG, "Connecting to: $url")
      val conn = url.openConnection()
      BufferedInputStream(conn.getInputStream())
    } catch (e: IOException) {
      Log.e(TAG, "Error fetching weather information.", e)
      DebugLog.current()!!.log("Error fetching weather: " + e.message)
      null
    }
  }

  companion object {
    private const val TAG = "codeka.weather"
    private const val API_KEY = BuildConfig.WUNDERGROUND_API_KEY
    private fun getWeatherIcon(name: String): WeatherIcon {
      // ignore the "chance" ones, they're just the same as non-chance ones.
      var name = name
      if (name.startsWith("chance")) {
        name = name.substring(6)
      }
      // ignore the night ones, too, we do our own night calculations
      if (name.startsWith("nt_")) {
        name = name.substring(3)
      }
      return when (name) {
        "clear" -> WeatherIcon.CLEAR
        "cloudy" -> WeatherIcon.CLOUDY
        "flurries" -> WeatherIcon.LIGHT_RAIN
        "fog" -> WeatherIcon.FOG
        "hazy" -> WeatherIcon.HAZE
        "mostlycloudy" -> WeatherIcon.MOSTLY_CLOUDY
        "mostlysunny" -> WeatherIcon.PARTLY_CLOUDY
        "partlysunny" -> WeatherIcon.MOSTLY_CLOUDY
        "partlycloudy" -> WeatherIcon.PARTLY_CLOUDY
        "sleet" -> WeatherIcon.SLEET
        "rain" -> WeatherIcon.MEDIUM_RAIN
        "snow" -> WeatherIcon.SNOW
        "sunny" -> WeatherIcon.CLEAR
        "tstorms" -> WeatherIcon.STORM
        else -> {
          Log.w(TAG, "Unknown icon: $name")
          WeatherIcon.SEVERE
        }
      }
    }
  }
}