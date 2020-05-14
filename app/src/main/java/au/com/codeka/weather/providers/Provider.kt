package au.com.codeka.weather.providers

import au.com.codeka.weather.model.WeatherInfo
import com.google.android.gms.maps.model.LatLngBounds
import java.io.InputStream

/**
 * Base class for a provider of weather data.
 */
abstract class Provider {
  /** Populates the given [WeatherInfo.Builder] with weather information.  */
  abstract fun fetchWeather(builder: WeatherInfo.Builder)

  /** Fetches a map overlay of the given lat lng bounds.  */
  abstract fun fetchMapOverlay(latLngBounds: LatLngBounds?, width: Int, height: Int): InputStream?
}