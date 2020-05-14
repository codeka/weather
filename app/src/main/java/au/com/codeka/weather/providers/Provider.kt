package au.com.codeka.weather.providers

import au.com.codeka.weather.model.MapOverlay
import au.com.codeka.weather.model.WeatherInfo
import com.google.android.gms.maps.model.LatLngBounds
import java.io.InputStream

/**
 * Base class for a provider of weather data.
 */
interface WeatherProvider {
  /** Populates the given [WeatherInfo.Builder] with weather information.  */
  fun fetchWeather(builder: WeatherInfo.Builder)
}

interface MapOverlayProvider {
  /** Fetches a map overlay of the given lat lng bounds.  */
  fun fetchMapOverlay(latLngBounds: LatLngBounds, width: Int, height: Int): ArrayList<MapOverlay>
}
