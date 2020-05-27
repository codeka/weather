package au.com.codeka.weather.providers.rainviewer

import android.graphics.BitmapFactory
import android.util.Log
import au.com.codeka.weather.LenientDoubleTypeAdapter
import au.com.codeka.weather.model.MapOverlay
import au.com.codeka.weather.providers.MapOverlayProvider
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.BufferedInputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.math.ln
import kotlin.math.roundToInt


class RainViewerProvider : MapOverlayProvider {
  companion object {
    private const val TAG = "codeka.weather"

    const val GLOBE_WIDTH = 256

    // See: https://www.rainviewer.com/api.html#colorSchemes
    const val COLOR_CODE = 3

    // Whether to do smoothing of the images: 1 = yes, 0 = no
    const val SMOOTH = 0

    // Display snow in separate colors: 1 = yes, 0 = no
    const val SNOW = 1

    val METERS_PER_PIXEL_ZOOM = arrayOf(
        21282f, 16355f, 10064f, 5540f, 2909f, 1485f, 752f, 378f, 190f, 95f, 48f, 24f, 12f, 6f, 3f,
        1.48f, 0.74f, 0.37f, 0.19f)
  }

  val gson: Gson = GsonBuilder()
      .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
      .registerTypeAdapter(Double::class.java, LenientDoubleTypeAdapter())
      .create()

  override fun fetchMapOverlay(latLngBounds: LatLngBounds, width: Int, height: Int)
      : ArrayList<MapOverlay> {
    Log.i(TAG, "Fetching map overlay")

    val timestamps = fetchTimestamps()

    // calculate the approximate zoom level we need to cover the current view
    val west = latLngBounds.southwest.latitude;
    val east = latLngBounds.northeast.longitude;
    var angle = east - west;
    if (angle < 0) {
      angle += 360;
    }
    val zoom = (ln(width * 360 / angle / GLOBE_WIDTH) / ln(2.0)).roundToInt()
    Log.i(TAG, "Approximate zoom: $zoom")

    val overlays = arrayListOf<MapOverlay>()
    for (ts in timestamps) {
      val url = URL("https://tilecache.rainviewer.com/v2/radar/${ts}/512/$zoom/" +
          "${latLngBounds.center.latitude}/${latLngBounds.center.longitude}/" +
          "$COLOR_CODE/${SMOOTH}_$SNOW.png")
      Log.i(TAG, "Fetching: $url")
      val conn = url.openConnection()
      val ins = BufferedInputStream(conn.getInputStream())
      val bmp = BitmapFactory.decodeStream(ins)

      val overlay = MapOverlay(bmp, Date(ts * 1000L))
      overlay.latLng = latLngBounds.center

      val metersPerPixel = METERS_PER_PIXEL_ZOOM[zoom]
      overlay.widthMeters = 512 * metersPerPixel
      Log.i(TAG, "width of image in meters: ${overlay.widthMeters}")

      overlays.add(overlay)
    }

    return overlays
  }

  private fun fetchTimestamps(): ArrayList<Int> {
    val url = URL("https://api.rainviewer.com/public/maps.json")
    val conn = url.openConnection()
    val ins = BufferedInputStream(conn.getInputStream())
    val json = JsonReader(InputStreamReader(ins, "UTF-8"))

    val responseType = object : TypeToken<ArrayList<Int>>() {}.type
    return gson.fromJson(json, responseType)
  }
}
