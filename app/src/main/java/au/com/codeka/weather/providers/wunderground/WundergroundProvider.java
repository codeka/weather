package au.com.codeka.weather.providers.wunderground;

import android.util.Log;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import au.com.codeka.weather.DebugLog;
import au.com.codeka.weather.LenientDoubleTypeAdapter;
import au.com.codeka.weather.model.CurrentCondition;
import au.com.codeka.weather.model.Forecast;
import au.com.codeka.weather.model.HourlyForecast;
import au.com.codeka.weather.model.WeatherIcon;
import au.com.codeka.weather.model.WeatherInfo;
import au.com.codeka.weather.providers.Provider;

/**
 * Provider for fetching weather details from weather underground.
 */
public class WundergroundProvider extends Provider {
  private static final String TAG = "codeka.weather";
  private static final String API_KEY = "094ceebb0893481d";

  /** Fetches weather info from Weather Underground. */
  @Override
  public void fetchWeather(WeatherInfo.Builder builder) {
    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Double.class, new LenientDoubleTypeAdapter())
        .create();

    try {
      String latlng = builder.getLat() + "," + builder.getLng();
      Log.d(TAG, "Querying Wunderground for weather info for: " + latlng);
      URL url = new URL(String.format(
          "http://api.wunderground.com/api/%s/conditions/forecast10day/hourly/q/%s.json",
          API_KEY,
          latlng));
      Log.d(TAG, "Connecting to: " + url);
      URLConnection conn = url.openConnection();
      InputStream ins = new BufferedInputStream(conn.getInputStream());

      JsonReader json = new JsonReader(new InputStreamReader(ins, "UTF-8"));
      WundergroundResponse response = gson.fromJson(json, WundergroundResponse.class);
      Log.d(TAG, "Response parsed successfully.");

      long seconds = Long.parseLong(response.currentObservation.observationTime);
      Date dt = new Date(seconds * 1000);

      builder.setCurrentConditions(new CurrentCondition.Builder()
          .setObservationLocation(response.currentObservation.observationLocation.full)
          .setObservationTime(dt)
          .setTemperature(response.currentObservation.temp)
          .setFeelsLike(response.currentObservation.feelsLike)
          .setDescription(response.currentObservation.weather)
          .setPrecipitation(
              response.currentObservation.precipitationLastHour,
              response.currentObservation.precipitationToday)
          .setRelativeHumidity(Double.parseDouble(
              response.currentObservation.relativeHumidity.substring(
                  0, response.currentObservation.relativeHumidity.length() - 1)))
          .setIcon(getWeatherIcon(response.currentObservation.icon))
          .build());

      for (int i = 0; i < response.forecast.txtForecast.days.size() / 2; i++) {
        builder.addForecast(new Forecast.Builder()
            .setOffset(i)
            .setDescription(response.forecast.txtForecast.days.get(i * 2).forecastText)
            .setShortDescription(response.forecast.simpleForecast.days.get(i).conditions)
            .setIcon(getWeatherIcon(response.forecast.txtForecast.days.get(i * 2).icon))
            .setHighTemperature(
                Double.parseDouble(response.forecast.simpleForecast.days.get(i).high.celsius))
            .setLowTemperature(
                Double.parseDouble(response.forecast.simpleForecast.days.get(i).low.celsius))
            .build());
      }

      for (int i = 0; i < response.hourlyForecast.size(); i++) {
        builder.addHourlyForecast(new HourlyForecast.Builder()
            .setHour(Integer.parseInt(response.hourlyForecast.get(i).time.hour))
            .setTemperature(Double.parseDouble(response.hourlyForecast.get(i).temp.metric))
            .setShortDescription(response.hourlyForecast.get(i).shortDescription)
            .setQpfMillimeters(Double.parseDouble(response.hourlyForecast.get(i).qpf.metric))
            .setIcon(getWeatherIcon(response.hourlyForecast.get(i).icon))
            .build());
      }
    } catch (IOException e) {
      Log.e(TAG, "Error fetching weather information.", e);
      DebugLog.current().log("Error fetching weather: " + e.getMessage());
    }
  }

  @Override
  public InputStream fetchMapOverlay(LatLngBounds latLngBounds, int width, int height) {
    try {
      URL url = new URL(String.format("http://api.wunderground.com/api/%s/animatedradar/image.gif?"
              + "minlat=%f&minlon=%f&maxlat=%f&maxlon=%f&width=%d&height=%d&timelabel=1&"
              + "timelabel.x=%d&timelabel.y=40&reproj.automerc=1&num=10&delay=50",
          API_KEY, latLngBounds.southwest.latitude, latLngBounds.southwest.longitude,
          latLngBounds.northeast.latitude, latLngBounds.northeast.longitude,
          width, height, width - 120));
      Log.d(TAG, "Connecting to: " + url);
      URLConnection conn = url.openConnection();
      return new BufferedInputStream(conn.getInputStream());
    } catch (IOException e) {
      Log.e(TAG, "Error fetching weather information.", e);
      DebugLog.current().log("Error fetching weather: " + e.getMessage());
      return null;
    }
  }

  private static WeatherIcon getWeatherIcon(String name) {
    // ignore the "chance" ones, they're just the same as non-chance ones.
    if (name.startsWith("chance")) {
      name = name.substring(6);
    }
    // ignore the night ones, too, we do our own night calculations
    if (name.startsWith("nt_")) {
      name = name.substring(3);
    }

    switch (name) {
      case "clear":
        return WeatherIcon.CLEAR;
      case "cloudy":
        return WeatherIcon.CLOUDY;
      case "flurries":
        return WeatherIcon.LIGHT_RAIN;
      case "fog":
        return WeatherIcon.FOG;
      case "hazy":
        return WeatherIcon.HAZE;
      case "mostlycloudy":
        return WeatherIcon.MOSTLY_CLOUDY;
      case "mostlysunny":
        return WeatherIcon.PARTLY_CLOUDY;
      case "partlysunny":
        return WeatherIcon.MOSTLY_CLOUDY;
      case "partlycloudy":
        return WeatherIcon.PARTLY_CLOUDY;
      case "sleet":
        return WeatherIcon.SLEET;
      case "rain":
        return WeatherIcon.MEDIUM_RAIN;
      case "snow":
        return WeatherIcon.SNOW;
      case "sunny":
        return WeatherIcon.CLEAR;
      case "tstorms":
        return WeatherIcon.STORM;
      default:
        Log.w(TAG, "Unknown icon: " + name);
        return WeatherIcon.SEVERE;
    }
  }
}
