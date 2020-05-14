package au.com.codeka.weather.providers.openweathermap

import android.util.Log
import android.util.SparseArray
import au.com.codeka.weather.BuildConfig
import au.com.codeka.weather.R
import au.com.codeka.weather.model.WeatherIcon
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Represents the response we receive from OpenWeatherMap's "One Call" API.
 *
 * @see https://openweathermap.org/api/one-call-api
 */
// lots of unused at the moment... don't really care
class OpenWeatherMapResponse() {
  @SerializedName("current")
  var currentConditions: CurrentConditions = CurrentConditions()

  @SerializedName("daily")
  var forecast: Array<ForecastEntry> = arrayOf()

  /** Get the forecast for the given day. 0 == today, 1 == tomorrow, 2 == the day after, etc.  */
  fun getForecast(day: Int): ForecastEntry {
    return forecast[day]
  }

  override fun toString(): String {
    return "${currentConditions.temp}°C, ${currentConditions.description}"
  }

  class CurrentConditions {
    @SerializedName("dt")
    var timestamp: Long = 0

    var sunrise: Long = 0
    var sunset: Long = 0

    @SerializedName("temp")
    var tempKelvin: Double = 0.0
    val temp: Double
      get() = tempKelvin - 273.15

    @SerializedName("feels_like")
    var feelsLike: Double = 0.0

    var weather: Array<Weather> = arrayOf()

    /** Gets a description of the weather (clear, cloudy, rain, etc)  */
    val description: String
      get() = (weather[0].description ?: "??").split(" ").joinToString(" ") { it.capitalize() }


    val icon: WeatherIcon
      get() {
        return WEATHER_ICONS[weather[0].id] // TODO
      }

    // TODO: the below doesn't work due to the fact that we may have a forecast for yesterday
    // TODO: still apparently?
//      long now = System.currentTimeMillis() / 1000;
//      Log.i("OWM", "now=" + now + " sys.sunrise=" + sys.sunrise + " sys.sunset=" + sys.sunset);
//      return (now < sys.sunrise || now > sys.sunset);
    private val isNight: Boolean
      private get() {
        val hour = GregorianCalendar()[Calendar.HOUR_OF_DAY]
        return hour < 6 || hour > 8

        // TODO: the below doesn't work due to the fact that we may have a forecast for yesterday
        // TODO: still apparently?
//      long now = System.currentTimeMillis() / 1000;
//      Log.i("OWM", "now=" + now + " sys.sunrise=" + sys.sunrise + " sys.sunset=" + sys.sunset);
//      return (now < sys.sunrise || now > sys.sunset);
      }
  }

  class ForecastEntry {
    @SerializedName("dt")
    var timestamp: Long = 0

    var sunrise: Long = 0
    var sunset: Long = 0
    var temp: ForecastTemp = ForecastTemp()

    @SerializedName("feels_like")
    var feelsLike: ForecastTemp = ForecastTemp()

    var weather: Array<Weather> = arrayOf()

    val description: String
      get() = if (weather.isEmpty()) {
          ""
        } else {
        (weather[0].description ?: "").split(" ").joinToString(" ") { it.capitalize() }
      }

    val icon: WeatherIcon
      get() = WEATHER_ICONS[weather[0].id]

  }

  class ForecastTemp {
    var day = 0.0

    @SerializedName("min")
    var minKelvin: Double = 0.0
    val min: Double
      get() = minKelvin - 273.15

    @SerializedName("max")
    var maxKelvin: Double = 0.0
    val max: Double
      get() = maxKelvin - 273.15

    var night = 0.0
    var eve = 0.0
    var morn = 0.0
  }

  class Weather {
    var id = 0
    var main: String? = null
    var description: String? = null
    var icon: String? = null
  }

  companion object {
    private val WEATHER_ICONS: SparseArray<WeatherIcon> = object : SparseArray<WeatherIcon>() {
      init {
        put(200, WeatherIcon.STORM)
        put(201, WeatherIcon.STORM)
        put(202, WeatherIcon.STORM)
        put(210, WeatherIcon.STORM)
        put(211, WeatherIcon.STORM)
        put(212, WeatherIcon.STORM)
        put(221, WeatherIcon.STORM)
        put(230, WeatherIcon.STORM)
        put(231, WeatherIcon.STORM)
        put(232, WeatherIcon.STORM)
        put(300, WeatherIcon.LIGHT_RAIN)
        put(301, WeatherIcon.LIGHT_RAIN)
        put(302, WeatherIcon.MEDIUM_RAIN)
        put(310, WeatherIcon.MEDIUM_RAIN)
        put(311, WeatherIcon.MEDIUM_RAIN)
        put(312, WeatherIcon.HEAVY_RAIN)
        put(313, WeatherIcon.LIGHT_RAIN)
        put(314, WeatherIcon.LIGHT_RAIN)
        put(321, WeatherIcon.LIGHT_RAIN)
        put(500, WeatherIcon.LIGHT_RAIN)
        put(501, WeatherIcon.MEDIUM_RAIN)
        put(502, WeatherIcon.HEAVY_RAIN)
        put(503, WeatherIcon.HEAVY_RAIN)
        put(504, WeatherIcon.SEVERE)
        put(511, WeatherIcon.SLEET)
        put(520, WeatherIcon.LIGHT_RAIN)
        put(521, WeatherIcon.LIGHT_RAIN)
        put(522, WeatherIcon.HEAVY_RAIN)
        put(531, WeatherIcon.HEAVY_RAIN)
        put(600, WeatherIcon.SNOW)
        put(601, WeatherIcon.SNOW)
        put(602, WeatherIcon.SNOW)
        put(611, WeatherIcon.SLEET)
        put(612, WeatherIcon.SLEET)
        put(615, WeatherIcon.SLEET)
        put(616, WeatherIcon.SLEET)
        put(620, WeatherIcon.SNOW)
        put(621, WeatherIcon.SNOW)
        put(622, WeatherIcon.SNOW)
        put(701, WeatherIcon.HAZE)
        put(711, WeatherIcon.HAZE)
        put(721, WeatherIcon.HAZE)
        put(731, WeatherIcon.HAZE)
        put(741, WeatherIcon.FOG)
        put(751, WeatherIcon.HAZE)
        put(761, WeatherIcon.HAZE)
        put(762, WeatherIcon.SEVERE)
        put(771, WeatherIcon.SEVERE)
        put(781, WeatherIcon.SEVERE)
        put(800, WeatherIcon.CLEAR)
        put(801, WeatherIcon.PARTLY_CLOUDY)
        put(802, WeatherIcon.PARTLY_CLOUDY)
        put(803, WeatherIcon.MOSTLY_CLOUDY)
        put(804, WeatherIcon.CLOUDY)
        put(900, WeatherIcon.SEVERE)
        put(901, WeatherIcon.SEVERE)
        put(902, WeatherIcon.SEVERE)
        put(903, WeatherIcon.CLEAR)
        put(904, WeatherIcon.CLEAR)
        put(905, WeatherIcon.SEVERE)
        put(906, WeatherIcon.SEVERE)
        put(950, WeatherIcon.CLEAR)
        put(951, WeatherIcon.CLEAR)
        put(952, WeatherIcon.CLEAR)
        put(953, WeatherIcon.CLEAR)
        put(954, WeatherIcon.CLEAR)
        put(955, WeatherIcon.CLEAR)
        put(956, WeatherIcon.CLEAR)
        put(957, WeatherIcon.SEVERE)
        put(958, WeatherIcon.SEVERE)
        put(959, WeatherIcon.SEVERE)
        put(960, WeatherIcon.SEVERE)
        put(961, WeatherIcon.SEVERE)
        put(962, WeatherIcon.SEVERE)
      }
    }
  }

}