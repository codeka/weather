package au.com.codeka.weather.model

/**
 * Like [Forecast], but for an hourly forecast, slightly simplified.
 */
class HourlyForecast {
  /**
   * Gets the "hour" of this forecast, in 24 hour time. We expect it to be the current day or
   * tomorrow if it passes midnight.
   */
  var hour = 0
    private set
  var shortDescription: String? = null
    private set
  var temperature = 0.0
    private set
  var icon: WeatherIcon? = null
    private set
  var qpfMillimeters = 0.0
    private set

  class Builder {
    private val forecast: HourlyForecast
    fun setHour(hour: Int): Builder {
      forecast.hour = hour
      return this
    }

    fun setShortDescription(shortDescription: String?): Builder {
      forecast.shortDescription = shortDescription
      return this
    }

    fun setQpfMillimeters(qpf: Double): Builder {
      forecast.qpfMillimeters = qpf
      return this
    }

    fun setTemperature(temp: Double): Builder {
      forecast.temperature = temp
      return this
    }

    fun setIcon(icon: WeatherIcon?): Builder {
      forecast.icon = icon
      return this
    }

    fun build(): HourlyForecast {
      return forecast
    }

    init {
      forecast = HourlyForecast()
    }
  }
}