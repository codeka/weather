package au.com.codeka.weather.providers.wunderground

import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * POJO which we use Gson to decode the weather underground response into.
 */
class WundergroundResponse {
  @SerializedName("current_observation")
  var currentObservation: CurrentObservation? = null
  var forecast: Forecast? = null

  @SerializedName("hourly_forecast")
  var hourlyForecast: ArrayList<HourlyForecast>? = null

  class CurrentObservation {
    var observationLocation: ObservationLocation? = null
    var stationId: String? = null

    @SerializedName("observation_epoch")
    var observationTime // seconds since epoch
        : String? = null
    var weather // e.g. "Light Rain", "Sunny" etc.
        : String? = null

    @SerializedName("temp_c")
    var temp // temperature in degrees celsius
        : Double? = null
    var relativeHumidity: String? = null
    var windDir // e.g. N, NE, etc.
        : String? = null
    var windKph: Double? = null
    var windGustKph: Double? = null

    @SerializedName("feelslike_c")
    var feelsLike // "feels like" temperaure in degrees celcius
        : Double? = null

    @SerializedName("precip_1hr_metric")
    var precipitationLastHour // in mm
        : Double? = null

    @SerializedName("precip_today_metric")
    var precipitationToday // in mm
        : Double? = null
    var icon: String? = null
  }

  class ObservationLocation {
    var full: String? = null
    var city: String? = null
    var state: String? = null
    var country: String? = null
    var lat: String? = null
    var lng: String? = null
    var elevation: String? = null
  }

  class Forecast {
    var txtForecast: TextForecast? = null

    @SerializedName("simpleforecast")
    var simpleForecast: SimpleForecast? = null
  }

  class TextForecast {
    @SerializedName("forecastday")
    var days: ArrayList<TextForecastDay>? = null
  }

  class TextForecastDay {
    var period = 0
    var icon: String? = null
    var title: String? = null

    @SerializedName("fcttext_metric")
    var forecastText: String? = null
  }

  class SimpleForecast {
    @SerializedName("forecastday")
    var days: ArrayList<SimpleForecastDay>? = null
  }

  class SimpleForecastDay {
    var period = 0
    var high: SimpleForecastTemp? = null
    var low: SimpleForecastTemp? = null
    var conditions: String? = null
    var icon: String? = null
  }

  class SimpleForecastTemp {
    var fahrenheit: String? = null
    var celsius: String? = null
  }

  class HourlyForecast {
    @SerializedName("FCTTIME")
    var time: HourlyForecastTime? = null
    var temp: HourlyForecastValue? = null
    var icon: String? = null

    @SerializedName("wc")
    var shortDescription: String? = null
    var qpf: HourlyForecastValue? = null
  }

  class HourlyForecastValue {
    var english // who is naming these, seriously??
        : String? = null
    var metric: String? = null
  }

  class HourlyForecastTime {
    var hour: String? = null
    var epoch: String? = null
  }
}