package au.com.codeka.weather.location

import android.content.Context
import android.location.Geocoder
import android.util.Log
import au.com.codeka.weather.DebugLog
import com.google.gson.GsonBuilder
import java.util.*

/**
 * Fetches geocode information about our current location.
 */
class GeocodeProvider {
  /**
   * Fetches a [GeocodeInfo] for the given lat,lng. Should be called on a background thread.
   */
  fun getGeocodeInfo(context: Context, lat: Double, lng: Double): GeocodeInfo? {
    return try {
      Log.d(TAG, "Querying Google for geocode info for: $lat,$lng")

      val geocoder = Geocoder(context, Locale.US)
      val results = GeocodeInfo(geocoder.getFromLocation(lat, lng, 3))

      val json = GsonBuilder().create().toJson(results)
      DebugLog.current().log("Geocode result: $json")
      Log.i(TAG, "Geocode result: $json")

      return results
    } catch (e: Throwable) {
      Log.e(TAG, "Error fetching geocode information.", e)
      DebugLog.current().log("Error Fetching Geocode: " + e.message)
      null
    }
  }

  companion object {
    private const val TAG = "codeka.weather"
  }
}

