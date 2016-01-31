package au.com.codeka.weather.providers.wunderground;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * POJO which we use Gson to decode the weather underground response into.
 */
@SuppressWarnings("unused")
public class WundergroundResponse {
  @SerializedName("current_observation")
  CurrentObservation currentObservation;
  Forecast forecast;

  public static class CurrentObservation {
    // image -- ignored
    // display_location -- ignored
    ObservationLocation observationLocation;
    // estinated -- ignored
    String stationId;
    @SerializedName("observation_epoch")
    String observationTime; // seconds since epoch
    String weather; // e.g. "Light Rain", "Sunny" etc.
    @SerializedName("temp_c")
    @Nullable
    Double temp; // temperature in degrees celsius
    String relativeHumidity;
    String windDir; // e.g. N, NE, etc.
    @Nullable
    Double windKph;
    @Nullable
    Double windGustKph;
    @SerializedName("feelslike_c")
    @Nullable
    Double feelsLike; // "feels like" temperaure in degrees celcius
    @SerializedName("precip_1hr_metric")
    @Nullable
    Double precipitationLastHour; // in mm
    @SerializedName("precip_today_metric")
    @Nullable
    Double precipitationToday; // in mm
    String icon;
  }

  public static class ObservationLocation {
    String full;
    String city;
    String state;
    String country;
    String lat;
    String lng;
    String elevation;
  }

  public static class Forecast {
    TextForecast txtForecast;
    @SerializedName("simpleforecast")
    SimpleForecast simpleForecast;
  }

  public static class TextForecast {
    @SerializedName("forecastday")
    ArrayList<TextForecastDay> days;
  }

  public static class TextForecastDay {
    int period;
    String icon;
    String title;
    @SerializedName("fcttext_metric")
    String forecastText;
  }

  public static class SimpleForecast {
    @SerializedName("forecastday")
    ArrayList<SimpleForecastDay> days;
  }

  public static class SimpleForecastDay {
    int period;
    SimpleForecastTemp high;
    SimpleForecastTemp low;
    String conditions;
    String icon;
  }

  public static class SimpleForecastTemp {
    String fahrenheit;
    String celsius;
  }
}
