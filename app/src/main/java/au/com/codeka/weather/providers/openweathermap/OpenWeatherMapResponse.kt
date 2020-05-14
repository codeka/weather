package au.com.codeka.weather.providers.openweathermap

import android.util.SparseArray
import au.com.codeka.weather.R
import com.google.gson.annotations.SerializedName
import java.util.*

/**
 * Represents the response we receive from openweathermap.org about the various weather conditions.
 */
// lots of unused at the moment... don't really care
class OpenWeatherMapResponse(val currentConditions: CurrentConditions, private val forecast: Forecast) {

  /** Get the forecast for the given day. 0 == today, 1 == tomorrow, 2 == the day after, etc.  */
  fun getForecast(day: Int): ForecastEntry {
    return forecast.list[day]
  }

  override fun toString(): String {
    return currentConditions.temp.toString() + "°C, " + currentConditions.description
  }

  class CurrentConditions {
    private val sys: Sys? = null
    private lateinit var weather: Array<Weather>
    private val main: Main? = null
    private val rain: Precipitation? = null
    private val snow: Precipitation? = null
    private val clouds: Clouds? = null
    private val dt: Long = 0
    private val name: String? = null
    private val cod = 0

    /** Current temperature in degrees celcius.  */
    val temp: Float
      get() = main!!.temp - 273.15f

    /** Gets a description of the weather (clear, cloudy, rain, etc)  */
    val description: String
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id]
            ?: return String.format(Locale.ENGLISH, "ID: %d", weather[0].id)
        return cond.description
      }

    val largeIcon: Int
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id] ?: return R.drawable.weather_clear
        return cond.getLargeIcon(isNight)
      }

    val smallIcon: Int
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id] ?: return R.drawable.weather_clear_small
        return cond.getSmallIcon(isNight)
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

  class Forecast {
    lateinit var list: Array<ForecastEntry>
  }

  class ForecastEntry {
    private val dt: Long = 0
    private val temp: Temp? = null
    private val pressure = 0f
    private val humidity = 0f
    lateinit var weather: Array<Weather>
    private val speed = 0f
    private val deg = 0f
    private val clouds = 0f
    val timeStamp: Date
      get() = Date(dt * 1000)

    /** Gets a description of the weather (clear, cloudy, rain, etc)  */
    val description: String
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id]
            ?: return String.format(Locale.ENGLISH, "ID: %d", weather[0].id)
        return cond.description
      }

    val largeIcon: Int
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id] ?: return R.drawable.weather_clear
        return cond.getLargeIcon(false)
      }

    val smallIcon: Int
      get() {
        val cond = WEATHER_CONDITIONS[weather[0].id] ?: return R.drawable.weather_clear_small
        return cond.getSmallIcon(false)
      }

    val maxTemp: Float
      get() = temp!!.max
  }

  class Temp {
    private val day = 0f
    private val min = 0f
    val max = 0f
    private val night = 0f
    private val eve = 0f
    private val morn = 0f
  }

  class Sys {
    private val message = 0f
    private val country: String? = null
    private val sunrise: Long = 0
    private val sunset: Long = 0
  }

  class Weather {
    val id = 0
    private val main: String? = null
    private val description: String? = null
    private val icon: String? = null
  }

  class Main {
    val temp = 0f
    private val pressure = 0f
    private val tempMin = 0f
    private val tempMax = 0f
    private val humidity = 0f
  }

  class Wind {
    private val speed = 0f
    private val gust = 0f
    private val deg = 0f
  }

  class Precipitation {
    @SerializedName("3h")
    private val threeHours = 0f
  }

  class Clouds {
    private val all = 0f
  }

  /**
   * An entry in the WEATHER_CONDITIONS map, for a description of the values, see:
   * http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
   */
  class WeatherCondition {
    var description: String
      private set
    private var mIconLarge: Int
    private var mIconSmall: Int
    private var mIconNightLarge: Int
    private var mIconNightSmall: Int

    constructor(description: String, iconLarge: Int, iconSmall: Int) {
      this.description = description
      mIconLarge = iconLarge
      mIconSmall = iconSmall
      mIconNightLarge = iconLarge
      mIconNightSmall = iconSmall
    }

    constructor(description: String, iconLarge: Int, iconSmall: Int, nightLarge: Int, nightSmall: Int) {
      this.description = description
      mIconLarge = iconLarge
      mIconSmall = iconSmall
      mIconNightLarge = nightLarge
      mIconNightSmall = nightSmall
    }

    fun getLargeIcon(isNight: Boolean): Int {
      return if (isNight) mIconNightLarge else mIconLarge
    }

    fun getSmallIcon(isNight: Boolean): Int {
      return if (isNight) mIconNightSmall else mIconSmall
    }
  }

  companion object {
    private val WEATHER_CONDITIONS: SparseArray<WeatherCondition> = object : SparseArray<WeatherCondition>() {
      init {
        put(200, WeatherCondition("Light Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(201, WeatherCondition("Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(202, WeatherCondition("Heavy Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(210, WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(211, WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(212, WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(221, WeatherCondition("Ragged Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(230, WeatherCondition("Light Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(231, WeatherCondition("Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(232, WeatherCondition("Heavy Storm", R.drawable.weather_storm, R.drawable.weather_storm_small))
        put(300, WeatherCondition("Light Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(301, WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(302, WeatherCondition("Heavy Drizzle", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small))
        put(310, WeatherCondition("Light Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small))
        put(311, WeatherCondition("Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small))
        put(312, WeatherCondition("Heavy Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(313, WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(314, WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(321, WeatherCondition("Drizzle", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(500, WeatherCondition("Light Rain", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(501, WeatherCondition("Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small))
        put(502, WeatherCondition("Heavy Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(503, WeatherCondition("Intense Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(504, WeatherCondition("Extreme Rain", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(511, WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small))
        put(520, WeatherCondition("Showers", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(521, WeatherCondition("Showers", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small))
        put(522, WeatherCondition("Heavy Showers", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(531, WeatherCondition("Ragged Showers", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small))
        put(600, WeatherCondition("Light Snow", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(601, WeatherCondition("Snow", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(602, WeatherCondition("Heavy Snow", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(611, WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small))
        put(612, WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small))
        put(615, WeatherCondition("Rainy Snow", R.drawable.weather_sleet, R.drawable.weather_sleet_small))
        put(616, WeatherCondition("Rainy Snow", R.drawable.weather_sleet, R.drawable.weather_sleet_small))
        put(620, WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(621, WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(622, WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small))
        put(701, WeatherCondition("Misty", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(711, WeatherCondition("Smokey", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(721, WeatherCondition("Hazy", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(731, WeatherCondition("Dusty", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(741, WeatherCondition("Foggy", R.drawable.weather_fog, R.drawable.weather_fog_small))
        put(751, WeatherCondition("Sandy", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(761, WeatherCondition("Dusty", R.drawable.weather_hazy, R.drawable.weather_hazy_small))
        put(762, WeatherCondition("Volcanic Ash", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(771, WeatherCondition("Squalls", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(781, WeatherCondition("Tornado", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(800, WeatherCondition("Clear", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(801, WeatherCondition("Scattered Clouds", R.drawable.weather_partlycloudy, R.drawable.weather_partlycloudy_small, R.drawable.weather_nt_partlycloudy, R.drawable.weather_nt_partlycloudy_small))
        put(802, WeatherCondition("Partly Cloudy", R.drawable.weather_partlycloudy, R.drawable.weather_partlycloudy_small, R.drawable.weather_nt_partlycloudy, R.drawable.weather_nt_partlycloudy_small))
        put(803, WeatherCondition("Mostly Cloudy", R.drawable.weather_mostlycloudy, R.drawable.weather_mostlycloudy_small, R.drawable.weather_nt_mostlycloudy, R.drawable.weather_nt_mostlycloudy_small))
        put(804, WeatherCondition("Overcast", R.drawable.weather_cloudy, R.drawable.weather_cloudy_small))
        put(900, WeatherCondition("Tornado", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(901, WeatherCondition("Tropical Storm", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(902, WeatherCondition("Hurricane", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(903, WeatherCondition("Cold", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(904, WeatherCondition("Hot", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(905, WeatherCondition("Windy", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(906, WeatherCondition("Hail", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(950, WeatherCondition("Setting", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(951, WeatherCondition("Calm", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(952, WeatherCondition("Light Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(953, WeatherCondition("Gentle Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(954, WeatherCondition("Breezy", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(955, WeatherCondition("Fresh Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(956, WeatherCondition("Strong Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small))
        put(957, WeatherCondition("High Winds", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(958, WeatherCondition("Gale Winds", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(959, WeatherCondition("Severe Gale", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(960, WeatherCondition("Storm", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(961, WeatherCondition("Violent Storm", R.drawable.weather_severe, R.drawable.weather_severe_small))
        put(962, WeatherCondition("Hurrincae", R.drawable.weather_severe, R.drawable.weather_severe_small))
      }
    }
  }

}