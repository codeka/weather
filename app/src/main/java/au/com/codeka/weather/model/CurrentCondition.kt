package au.com.codeka.weather.model

import java.util.*

/** POJO that represents the "current" conditions. */
class CurrentCondition {
  var observationLocation: String? = null
    private set
  var observationTime: Date? = null
    private set
  var temperature: Double? = null
    private set
  var feelsLike: Double? = null
    private set
  var precipitationLastHour = 0.0
    private set
  var precipitationToday = 0.0
    private set
  var relativeHumidity: Double? = null
    private set
  var description: String? = null
    private set
  var icon: WeatherIcon? = null
    private set

  class Builder {
    private val currentCondition: CurrentCondition = CurrentCondition()

    fun setObservationLocation(location: String?): Builder {
      currentCondition.observationLocation = location
      return this
    }

    fun setObservationTime(dt: Date?): Builder {
      currentCondition.observationTime = dt
      return this
    }

    fun setTemperature(temp: Double?): Builder {
      currentCondition.temperature = temp
      return this
    }

    fun setFeelsLike(temp: Double?): Builder {
      currentCondition.feelsLike = temp
      return this
    }

    fun setPrecipitation(lastHour: Double?, today: Double?): Builder {
      currentCondition.precipitationLastHour = lastHour ?: 0.0
      currentCondition.precipitationToday = today ?: 0.0
      return this
    }

    fun setRelativeHumidity(percent: Double?): Builder {
      currentCondition.relativeHumidity = percent
      return this
    }

    fun setDescription(description: String?): Builder {
      currentCondition.description = description
      return this
    }

    fun setIcon(icon: WeatherIcon?): Builder {
      currentCondition.icon = icon
      return this
    }

    fun build(): CurrentCondition {
      return currentCondition
    }
  }
}