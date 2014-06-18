package au.com.codeka.weather;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.util.SparseArray;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the response we receive from openweathermap.org about the various weather conditions.
 */
@SuppressWarnings("unused") // lots of unused at the moment... don't really care
public class OpenWeatherMapInfo {
  private CurrentConditions mCurrentConditions;
  private Forecast mForecast;

  public OpenWeatherMapInfo(CurrentConditions currentConditions, Forecast forecast) {
    mCurrentConditions = currentConditions;
    mForecast = forecast;
  }

  public CurrentConditions getCurrentConditions() {
    return mCurrentConditions;
  }

  public ForecastEntry getForecast(int day) {
    return mForecast.list[day - 1];
  }

  @Override
  public String toString() {
    return mCurrentConditions.getTemp() + "°C, " + mCurrentConditions.getDescription();
  }

  public static class CurrentConditions {
    private Sys sys;
    private Weather[] weather;
    private Main main;
    private Precipitation rain;
    private Precipitation snow;
    private Clouds clouds;
    private long dt;
    private String name;
    private int cod;

    /** Current temperature in degrees celcius. */
    public float getTemp() {
      return main.temp - 273.15f;
    }

    /** Gets a description of the weather (clear, cloudy, rain, etc) */
    public String getDescription() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return String.format(Locale.ENGLISH, "ID: %d", weather[0].id);
      }
      return cond.getDescription();
    }

    public int getLargeIcon() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return R.drawable.weather_clear;
      }
      return cond.getLargeIcon(isNight());
    }

    public int getSmallIcon() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return R.drawable.weather_clear_small;
      }
      return cond.getSmallIcon(isNight());
    }

    private boolean isNight() {
      long now = System.currentTimeMillis() / 1000;
      return (now < sys.sunrise || now > sys.sunset);
    }
  }

  public static class Forecast {
    private ForecastEntry[] list;
  }

  public static class ForecastEntry {
    private long dt;
    private Temp temp;
    private float pressure;
    private float humidity;
    private Weather[] weather;
    private float speed;
    private float deg;
    private float clouds;

    /** Gets a description of the weather (clear, cloudy, rain, etc) */
    public String getDescription() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return String.format(Locale.ENGLISH, "ID: %d", weather[0].id);
      }
      return cond.getDescription();
    }

    public int getLargeIcon() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return R.drawable.weather_clear;
      }
      return cond.getLargeIcon(false);
    }

    public int getSmallIcon() {
      WeatherCondition cond = sWeatherConditions.get(weather[0].id);
      if (cond == null) {
        return R.drawable.weather_clear_small;
      }
      return cond.getSmallIcon(false);
    }
  }

  public static class Temp {
    private float day;
    private float min;
    private float max;
    private float night;
    private float eve;
    private float morn;
  }

  public static class Sys {
    private float message;
    private String country;
    private long sunrise;
    private long sunset;
  }

  public static class Weather {
    private int id;
    private String main;
    private String description;
    private String icon;
  }

  public static class Main {
    private float temp;
    private float pressure;
    private float tempMin;
    private float tempMax;
    private float humidity;
  }

  public static class Wind {
    private float speed;
    private float gust;
    private float deg;
  }

  public static class Precipitation {
    @SerializedName("3h")
    private float threeHours;
  }

  public static class Clouds {
    private float all;
  }

  /**
   * An entry in the sWeatherConditions map, for a description of the values, see:
   * http://bugs.openweathermap.org/projects/api/wiki/Weather_Condition_Codes
   */
  public static class WeatherCondition {
    private String mDescription;
    private int mIconLarge;
    private int mIconSmall;
    private int mIconNightLarge;
    private int mIconNightSmall;

    public WeatherCondition(String description, int iconLarge, int iconSmall) {
      mDescription = description;
      mIconLarge = iconLarge;
      mIconSmall = iconSmall;
      mIconNightLarge = iconLarge;
      mIconNightSmall = iconSmall;
    }

    public WeatherCondition(String description, int iconLarge, int iconSmall, int nightLarge, int nightSmall) {
      mDescription = description;
      mIconLarge = iconLarge;
      mIconSmall = iconSmall;
      mIconNightLarge = nightLarge;
      mIconNightSmall = nightSmall;
    }

    public String getDescription() {
      return mDescription;
    }

    public int getLargeIcon(boolean isNight) {
      return isNight ? mIconNightLarge : mIconLarge;
    }

    public int getSmallIcon(boolean isNight) {
      return isNight ? mIconNightSmall : mIconSmall;
    }
  }

  private static SparseArray<WeatherCondition> sWeatherConditions = new SparseArray<WeatherCondition>() {{
    put(200, new WeatherCondition("Light Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(201, new WeatherCondition("Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(202, new WeatherCondition("Heavy Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(210, new WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(211, new WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(212, new WeatherCondition("Thunderstorm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(221, new WeatherCondition("Ragged Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(230, new WeatherCondition("Light Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(231, new WeatherCondition("Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));
    put(232, new WeatherCondition("Heavy Storm", R.drawable.weather_storm, R.drawable.weather_storm_small));

    put(300, new WeatherCondition("Light Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(301, new WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(302, new WeatherCondition("Heavy Drizzle", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small));
    put(310, new WeatherCondition("Light Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small));
    put(311, new WeatherCondition("Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small));
    put(312, new WeatherCondition("Heavy Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));
    put(313, new WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(314, new WeatherCondition("Drizzle", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(321, new WeatherCondition("Drizzle", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));

    put(500, new WeatherCondition("Light Rain", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(501, new WeatherCondition("Rain", R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small));
    put(502, new WeatherCondition("Heavy Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));
    put(503, new WeatherCondition("Intense Rain", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));
    put(504, new WeatherCondition("Extreme Rain", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(511, new WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small));
    put(520, new WeatherCondition("Showers", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(521, new WeatherCondition("Showers", R.drawable.weather_lightrain, R.drawable.weather_lightrain_small));
    put(522, new WeatherCondition("Heavy Showers", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));
    put(531, new WeatherCondition("Ragged Showers", R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small));

    put(600, new WeatherCondition("Light Snow", R.drawable.weather_snow, R.drawable.weather_snow_small));
    put(601, new WeatherCondition("Snow", R.drawable.weather_snow, R.drawable.weather_snow_small));
    put(602, new WeatherCondition("Heavy Snow", R.drawable.weather_snow, R.drawable.weather_snow_small));
    put(611, new WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small));
    put(612, new WeatherCondition("Sleet", R.drawable.weather_sleet, R.drawable.weather_sleet_small));
    put(615, new WeatherCondition("Rainy Snow", R.drawable.weather_sleet, R.drawable.weather_sleet_small));
    put(616, new WeatherCondition("Rainy Snow", R.drawable.weather_sleet, R.drawable.weather_sleet_small));
    put(620, new WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small));
    put(621, new WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small));
    put(622, new WeatherCondition("Snow Showers", R.drawable.weather_snow, R.drawable.weather_snow_small));

    put(701, new WeatherCondition("Misty", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(711, new WeatherCondition("Smokey", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(721, new WeatherCondition("Hazy", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(731, new WeatherCondition("Dusty", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(741, new WeatherCondition("Foggy", R.drawable.weather_fog, R.drawable.weather_fog_small));
    put(751, new WeatherCondition("Sandy", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(761, new WeatherCondition("Dusty", R.drawable.weather_hazy, R.drawable.weather_hazy_small));
    put(762, new WeatherCondition("Volcanic Ash", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(771, new WeatherCondition("Squalls", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(781, new WeatherCondition("Tornado", R.drawable.weather_severe, R.drawable.weather_severe_small));

    put(800, new WeatherCondition("Clear", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(801, new WeatherCondition("Scattered Clouds", R.drawable.weather_partlycloudy, R.drawable.weather_partlycloudy_small, R.drawable.weather_nt_partlycloudy, R.drawable.weather_nt_partlycloudy_small));
    put(802, new WeatherCondition("Partly Cloudy", R.drawable.weather_partlycloudy, R.drawable.weather_partlycloudy_small, R.drawable.weather_nt_partlycloudy, R.drawable.weather_nt_partlycloudy_small));
    put(803, new WeatherCondition("Mostly Cloudy", R.drawable.weather_mostlycloudy, R.drawable.weather_mostlycloudy_small, R.drawable.weather_nt_mostlycloudy, R.drawable.weather_nt_mostlycloudy_small));
    put(804, new WeatherCondition("Overcast", R.drawable.weather_cloudy, R.drawable.weather_cloudy_small));

    put(900, new WeatherCondition("Tornado", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(901, new WeatherCondition("Tropical Storm", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(902, new WeatherCondition("Hurricane", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(903, new WeatherCondition("Cold", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(904, new WeatherCondition("Hot", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(905, new WeatherCondition("Windy", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(906, new WeatherCondition("Hail", R.drawable.weather_severe, R.drawable.weather_severe_small));

    put(950, new WeatherCondition("Setting", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(951, new WeatherCondition("Calm", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(952, new WeatherCondition("Light Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(953, new WeatherCondition("Gentle Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(954, new WeatherCondition("Breezy", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(955, new WeatherCondition("Fresh Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(956, new WeatherCondition("Strong Breeze", R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small));
    put(957, new WeatherCondition("High Winds", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(958, new WeatherCondition("Gale Winds", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(959, new WeatherCondition("Severe Gale", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(960, new WeatherCondition("Storm", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(961, new WeatherCondition("Violent Storm", R.drawable.weather_severe, R.drawable.weather_severe_small));
    put(962, new WeatherCondition("Hurrincae", R.drawable.weather_severe, R.drawable.weather_severe_small));

  }};
}
