package au.com.codeka.weather.location

import android.util.Log
import au.com.codeka.weather.DebugLog
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonReader
import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

/**
 * Fetches geocode information about our current location.
 */
class GeocodeProvider {
  /**
   * Fetches a [GeocodeInfo] for the given lat,lng. Should be called on a background thread.
   */
  fun getGeocodeInfo(lat: Double, lng: Double): GeocodeInfo? {
    val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create()
    return try {
      Log.d(TAG, "Querying Google for geocode info for: $lat,$lng")
      val latlng = "$lat,$lng"
      val url = URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=$latlng")
      val conn = url.openConnection()
      val ins: InputStream = BufferedInputStream(conn.getInputStream())
      val json = JsonReader(InputStreamReader(ins, "UTF-8"))
      val geocodeInfo = gson.fromJson<GeocodeInfo>(json, GeocodeInfo::class.java)
      DebugLog.current()!!.log("Geocoded location: $geocodeInfo")
      geocodeInfo
    } catch (e: IOException) {
      Log.e(TAG, "Error fetching geocode information.", e)
      DebugLog.current()!!.log("Error Fetching Geocode: " + e.message)
      null
    }
  }

  companion object {
    private const val TAG = "codeka.weather"
  }
}