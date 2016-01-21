package au.com.codeka.weather.providers;

import au.com.codeka.weather.model.WeatherInfo;

/**
 * Base class for a provider of weather data.
 */
public abstract class Provider {
  public abstract void fetchWeather(WeatherInfo.Builder builder);
}
