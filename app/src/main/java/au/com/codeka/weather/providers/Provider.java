package au.com.codeka.weather.providers;

import com.google.android.gms.maps.model.LatLngBounds;

import java.io.InputStream;

import au.com.codeka.weather.model.WeatherInfo;

/**
 * Base class for a provider of weather data.
 */
public abstract class Provider {
  /** Populates the given {@link WeatherInfo.Builder} with weather information. */
  public abstract void fetchWeather(WeatherInfo.Builder builder);

  /** Fetches a map overlay of the given lat lng bounds. */
  public abstract InputStream fetchMapOverlay(LatLngBounds latLngBounds, int width, int height);
}
