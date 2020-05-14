package au.com.codeka.weather.model

/**
 * POJO that represents a "forecast".
 */
class Forecast {
  /**
   * Gets the "offset" of this forecast. 0 means it's todays, 1 means tomorrow, 2 the day after etc.
   */
  var offset = 0
    private set
  var description: String? = null
    private set
  var shortDescription: String? = null
    private set
  var highTemperature = 0.0
    private set
  var lowTemperature = 0.0
    private set
  var icon: WeatherIcon? = null
    private set

  class Builder {
    private val forecast: Forecast
    fun setOffset(offset: Int): Builder {
      forecast.offset = offset
      return this
    }

    fun setDescription(description: String?): Builder {
      forecast.description = description
      return this
    }

    fun setShortDescription(shortDescription: String?): Builder {
      forecast.shortDescription = shortDescription
      return this
    }

    fun setHighTemperature(temp: Double): Builder {
      forecast.highTemperature = temp
      return this
    }

    fun setLowTemperature(temp: Double): Builder {
      forecast.lowTemperature = temp
      return this
    }

    fun setIcon(icon: WeatherIcon?): Builder {
      forecast.icon = icon
      return this
    }

    fun build(): Forecast {
      return forecast
    }

    init {
      forecast = Forecast()
    }
  }
}