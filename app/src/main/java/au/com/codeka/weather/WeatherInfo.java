package au.com.codeka.weather;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import android.content.SharedPreferences;
import android.util.Log;

/**
 * Class which holds information about the current weather (including forecast for the future and
 * such).
 */
public class WeatherInfo {
  private static final String TAG = WeatherInfo.class.getSimpleName();

  private double lat;
  private double lng;

  private GeocodeInfo geocodeInfo;
  private OpenWeatherMapInfo weatherInfo;

  public double getLat() {
    return lat;
  }

  public double getLng() {
    return lng;
  }

  public GeocodeInfo getGeocodeInfo() {
    return geocodeInfo;
  }

  public OpenWeatherMapInfo getWeather() {
    return weatherInfo;
  }

  public void save(SharedPreferences.Editor editor) {
    editor.putBoolean("Weather.Exists", true);
    editor.putFloat("Weather.Lat", (float) lat);
    editor.putFloat("Weather.Lng", (float) lng);

    Gson gson = new GsonBuilder()
                      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                      .create();
    editor.putString("Weather.Geocode", gson.toJson(geocodeInfo));
    editor.putString("Weather.Weather", gson.toJson(weatherInfo));
  }

  public static class Builder {
    public WeatherInfo load(SharedPreferences prefs) {
      if (!prefs.getBoolean("Weather.Exists", false)) {
        return null;
      }

      WeatherInfo weatherInfo = new WeatherInfo();
      weatherInfo.lat = prefs.getFloat("Weather.Lat", 0.0f);
      weatherInfo.lng = prefs.getFloat("Weather.Lng", 0.0f);

      Gson gson = new GsonBuilder()
                        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                        .create();
      weatherInfo.geocodeInfo = gson.fromJson(prefs.getString("Weather.Geocode", ""), GeocodeInfo.class);
      weatherInfo.weatherInfo = gson.fromJson(prefs.getString("Weather.Weather", ""), OpenWeatherMapInfo.class);

      return weatherInfo;
    }

    public WeatherInfo fetch(double lat, double lng) {
      WeatherInfo weatherInfo = new WeatherInfo();
      weatherInfo.lat = lat;
      weatherInfo.lng = lng;

      Gson gson = new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .create();

      try {
        Log.d(TAG, "Querying google for geocode info.");
        String latlng = lat + "," + lng;
        URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latlng);
        URLConnection conn = url.openConnection();
        InputStream ins = new BufferedInputStream(conn.getInputStream());
        JsonReader json = new JsonReader(new InputStreamReader(ins, "UTF-8"));
        weatherInfo.geocodeInfo = gson.fromJson(json, GeocodeInfo.class);

        DebugLog.current().log("Geocoded location: " + weatherInfo.getGeocodeInfo());
      } catch (IOException e) {
        Log.e(TAG, "Error fetching geocode information.", e);
        DebugLog.current().log("Error Fetching Geocode: " + e.getMessage());
        return null;
      }

      OpenWeatherMapInfo.CurrentConditions currentConditions;
      try {
        Log.d(TAG, "Querying openweathermap for current conditions.");
        String query = weatherInfo.getGeocodeInfo().getShortName().replace(" ", "")
            .toLowerCase(Locale.ENGLISH);
        URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=" + query
            + "&APPID=296094169f5cd4b46d8cc05a97083b7f");
        URLConnection conn = url.openConnection();
        InputStream ins = new BufferedInputStream(conn.getInputStream());
        JsonReader json = new JsonReader(new InputStreamReader(ins, "UTF-8"));
        currentConditions = gson.fromJson(json, OpenWeatherMapInfo.CurrentConditions.class);

      } catch (IOException e) {
        Log.e(TAG, "Error fetching weather information.", e);
        DebugLog.current().log("Error Fetching Weather: " + e.getMessage());
        return null;
      }

      OpenWeatherMapInfo.Forecast forecast;
      try {
        Log.d(TAG, "Querying openweathermap for forecast.");
        String query = weatherInfo.getGeocodeInfo().getShortName().replace(" ", "")
            .toLowerCase(Locale.ENGLISH);
        URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=" + query +
            "&units=metric&cnt=7&APPID=296094169f5cd4b46d8cc05a97083b7f");
        URLConnection conn = url.openConnection();
        InputStream ins = new BufferedInputStream(conn.getInputStream());
        JsonReader json = new JsonReader(new InputStreamReader(ins, "UTF-8"));
        forecast = gson.fromJson(json, OpenWeatherMapInfo.Forecast.class);

      } catch (IOException e) {
        Log.e(TAG, "Error fetching weather information.", e);
        DebugLog.current().log("Error Fetching Weather: " + e.getMessage());
        return null;
      }

      weatherInfo.weatherInfo = new OpenWeatherMapInfo(currentConditions, forecast);
      DebugLog.current().log("Weather: " + weatherInfo.getWeather());
      Log.d(TAG, "Weather: "+weatherInfo.getWeather());
      return weatherInfo;
    }
  }
}
