package au.com.codeka.weather.model;

import android.content.SharedPreferences;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import au.com.codeka.weather.location.GeocodeInfo;
import au.com.codeka.weather.providers.wunderground.WundergroundResponse;

/**
 * POJO that represents a combinations of the current weather conditions and a forecast of what's
 * coming up.
 */
public class WeatherInfo {
  private double lat;
  private double lng;
  private GeocodeInfo geocodeInfo;
  private List<Forecast> forecasts;
  private List<HourlyForecast> hourlyForecasts;
  private CurrentCondition currentCondition;

  public void save(SharedPreferences.Editor editor) {
    editor.putBoolean("Weather.Exists", true);
    editor.putFloat("Weather.Lat", (float) lat);
    editor.putFloat("Weather.Lng", (float) lng);

    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    editor.putString("Weather.Geocode", gson.toJson(geocodeInfo));
    editor.putString("Weather.Forecasts", gson.toJson(forecasts));
    editor.putString("Weather.HourlyForecasts", gson.toJson(hourlyForecasts));
    editor.putString("Weather.Current", gson.toJson(currentCondition));
  }

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public GeocodeInfo getGeocodeInfo() {
    return geocodeInfo;
  }

  public CurrentCondition getCurrentCondition() {
    return currentCondition;
  }

  public List<Forecast> getForecasts() {
    return forecasts;
  }

  public List<HourlyForecast> getHourlyForecasts() { return hourlyForecasts; }

  public static class Builder {
    public static WeatherInfo load(SharedPreferences prefs) {
      if (!prefs.getBoolean("Weather.Exists", false)) {
        return null;
      }

      WeatherInfo weatherInfo = new WeatherInfo();
      weatherInfo.lat = prefs.getFloat("Weather.Lat", 0.0f);
      weatherInfo.lng = prefs.getFloat("Weather.Lng", 0.0f);

      Gson gson = new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();
      weatherInfo.geocodeInfo = gson.fromJson(
          prefs.getString("Weather.Geocode", ""),
          GeocodeInfo.class);
      weatherInfo.forecasts = gson.fromJson(
          prefs.getString("Weather.Forecasts", ""),
          new TypeToken<ArrayList<Forecast>>() {}.getType());
      weatherInfo.hourlyForecasts = gson.fromJson(
          prefs.getString("Weather.HourlyForecasts", ""),
          new TypeToken<ArrayList<HourlyForecast>>() {}.getType());
      try {
        weatherInfo.currentCondition = gson.fromJson(
            prefs.getString("Weather.Current", ""),
            CurrentCondition.class);
      } catch (Exception e) {
        weatherInfo.currentCondition = null;
      }

      return weatherInfo;
    }

    private WeatherInfo weatherInfo;

    public Builder(double lat, double lng) {
      weatherInfo = new WeatherInfo();
      weatherInfo.lat = lat;
      weatherInfo.lng = lng;
      weatherInfo.forecasts = new ArrayList<>();
      weatherInfo.hourlyForecasts = new ArrayList<>();
    }

    public double getLat() {
      return weatherInfo.lat;
    }

    public double getLng() {
      return weatherInfo.lng;
    }

    public Builder setGeocodeInfo(GeocodeInfo geocodeInfo) {
      weatherInfo.geocodeInfo = geocodeInfo;
      return this;
    }

    public GeocodeInfo getGeocodeInfo() {
      return weatherInfo.geocodeInfo;
    }

    public Builder addForecast(Forecast forecast) {
      weatherInfo.forecasts.add(forecast);
      return this;
    }

    public Builder setForecasts(List<Forecast> forecasts) {
      weatherInfo.forecasts.addAll(forecasts);
      return this;
    }

    public Builder addHourlyForecast(HourlyForecast hourlyForecast) {
      weatherInfo.hourlyForecasts.add(hourlyForecast);
      return this;
    }

    public Builder setCurrentConditions(CurrentCondition currentConditions) {
      weatherInfo.currentCondition = currentConditions;
      return this;
    }

    public WeatherInfo build() {
      return weatherInfo;
    }
  }
}

