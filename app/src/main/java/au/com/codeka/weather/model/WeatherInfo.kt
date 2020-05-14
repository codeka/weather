package au.com.codeka.weather.model

import android.content.SharedPreferences
import au.com.codeka.weather.location.GeocodeInfo
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * POKO-like object that represents a combinations of the current weather conditions and a forecast
 * of what's coming up.
 */
class WeatherInfo private constructor (var lat: Double, var lng: Double) {
  var geocodeInfo: GeocodeInfo? = null
    private set
  val forecasts = ArrayList<Forecast>()
  val hourlyForecasts = ArrayList<HourlyForecast>()
  var currentCondition: CurrentCondition? = null
    private set


  fun save(editor: SharedPreferences.Editor) {
    editor.putBoolean("Weather.Exists", true)
    editor.putFloat("Weather.Lat", lat.toFloat())
    editor.putFloat("Weather.Lng", lng.toFloat())
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    editor.putString("Weather.Geocode", gson.toJson(geocodeInfo))
    editor.putString("Weather.Forecasts", gson.toJson(forecasts))
    editor.putString("Weather.HourlyForecasts", gson.toJson(hourlyForecasts))
    editor.putString("Weather.Current", gson.toJson(currentCondition))
  }

  class Builder(lat: Double, lng: Double) {
    private val weatherInfo = WeatherInfo(lat, lng)

    val lat: Double
      get() = weatherInfo.lat

    val lng: Double
      get() = weatherInfo.lng

    fun withGeocodeInfo(geocodeInfo: GeocodeInfo): Builder {
      weatherInfo.geocodeInfo = geocodeInfo
      return this
    }

    val geocodeInfo: GeocodeInfo?
      get() = weatherInfo.geocodeInfo

    fun addForecast(forecast: Forecast): Builder {
      weatherInfo.forecasts.add(forecast)
      return this
    }

    fun setForecasts(forecasts: List<Forecast>): Builder {
      weatherInfo.forecasts.addAll(forecasts)
      return this
    }

    fun addHourlyForecast(hourlyForecast: HourlyForecast): Builder {
      weatherInfo.hourlyForecasts.add(hourlyForecast)
      return this
    }

    fun setCurrentConditions(currentConditions: CurrentCondition): Builder {
      weatherInfo.currentCondition = currentConditions
      return this
    }

    fun build(): WeatherInfo {
      return weatherInfo
    }

    companion object {
      fun load(prefs: SharedPreferences): WeatherInfo? {
        if (!prefs.getBoolean("Weather.Exists", false)) {
          return null
        }
        val weatherInfo =
            WeatherInfo(
                prefs.getFloat("Weather.Lat", 0.0f).toDouble(),
                prefs.getFloat("Weather.Lng", 0.0f).toDouble())
        val gson = GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .create()
        weatherInfo.geocodeInfo = gson.fromJson(
            prefs.getString("Weather.Geocode", ""),
            GeocodeInfo::class.java)
        weatherInfo.forecasts.addAll(gson.fromJson<MutableList<Forecast>>(
            prefs.getString("Weather.Forecasts", ""),
            object : TypeToken<ArrayList<Forecast>>() {}.type))
        weatherInfo.hourlyForecasts.addAll(gson.fromJson<MutableList<HourlyForecast>>(
            prefs.getString("Weather.HourlyForecasts", ""),
            object : TypeToken<ArrayList<HourlyForecast>>() {}.type))
        try {
          weatherInfo.currentCondition = gson.fromJson(
              prefs.getString("Weather.Current", ""),
              CurrentCondition::class.java)
        } catch (e: Exception) {
          weatherInfo.currentCondition = null
        }
        return weatherInfo
      }
    }
  }
}