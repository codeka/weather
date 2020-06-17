package au.com.codeka.weather

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import android.util.Log
import au.com.codeka.weather.location.GeocodeProvider
import au.com.codeka.weather.location.LocationProvider
import au.com.codeka.weather.model.MapOverlay
import au.com.codeka.weather.model.WeatherInfo
import au.com.codeka.weather.providers.openweathermap.OpenWeatherMapProvider
import au.com.codeka.weather.providers.rainviewer.RainViewerProvider
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*
import java.util.concurrent.TimeUnit

class WeatherManager private constructor() {
  private var lastQueryTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(99)
  private val onUpdateRunnables = ArrayList<Runnable>()

  /** Adds a runnable to be called when the weather is updated (may not happen on UI thread)  */
  fun addUpdateRunnable(runnable: Runnable) {
    onUpdateRunnables.add(runnable)
  }

  fun removeUpdateRunnable(runnable: Runnable) {
    onUpdateRunnables.remove(runnable)
  }

  fun refreshWeather(context: Context, force: Boolean) {
    val now = System.currentTimeMillis()
    if (lastQueryTime > now - TimeUnit.MINUTES.toMillis(1) && !force) {
      return
    }
    lastQueryTime = now

    try {
      // We may be running on a background worker thread, so prepare a looper if that's the case.
      if (Looper.myLooper() == null) {
        Looper.prepare()
      }

      val prefs = context.getSharedPreferences("au.com.codeka.weather", Context.MODE_PRIVATE)
      Log.d(TAG, "Doing a location query.")
      val locationProvider = LocationProvider(context)
      locationProvider.getLocation(force, object : LocationProvider.LocationFetchedListener {
        override fun onLocationFetched(loc: Location) {
          Log.i(TAG, "Got location: " + loc.latitude + "," + loc.longitude)
          DebugLog.current().setLocation(loc.latitude, loc.longitude)
          DebugLog.current().log("Got location fix: " + loc.latitude + "," + loc.longitude)
          if (force) {
            DebugLog.current().log(" - doing a force-update")
          }
          val needWeatherQuery = force || checkNeedWeatherQuery(loc, prefs)
          if (needWeatherQuery) {
            queryWeather(context, loc, prefs)
          } else {
            DebugLog.current().log("No weather query required.")
          }
        }
      })
    } catch(e: Throwable) {
      Log.e(TAG, "Error in refreshWeather")
      DebugLog.current().log("Error: ${e.message}")
    }
  }

  fun getCurrentWeather(context: Context?): WeatherInfo? {
    val prefs = context!!.getSharedPreferences("au.com.codeka.weather",
        Context.MODE_PRIVATE)
    return WeatherInfo.Builder.load(prefs)
  }

  /** Fetches an image to overlay over a map. Must be called on a background thread.  */
  fun fetchMapOverlay(latLngBounds: LatLngBounds, width: Int, height: Int): ArrayList<MapOverlay> {
    return RainViewerProvider().fetchMapOverlay(latLngBounds, width, height)
  }

  /**
   * Checks whether we need to do a new weather query.
   *
   * We only do a weather query once every three hours by default, unless we've moved > 5km since
   * the last weather query, in which case we do it every 30 minutes.
   *
   * @param loc Current location, used to determine if we've moved since the last weather query.
   * @param prefs A SharedPreferences which holds our saved data.
   * @return A value which indicates whether we need to do a new weather query.
   */
  private fun checkNeedWeatherQuery(loc: Location?, prefs: SharedPreferences): Boolean {
    val timeOfLastWeatherQuery = prefs.getLong("TimeOfLastWeatherQuery", 0)
    if (timeOfLastWeatherQuery == 0L) {
      DebugLog.current().log("No previous weather queries, doing first one.")
      return true
    }
    val lastQueryLat = prefs.getFloat("LastQueryLat", 0.0f).toDouble()
    val lastQueryLng = prefs.getFloat("LastQueryLng", 0.0f).toDouble()
    var timeBetweenQueries = STATIONARY_QUERY_TIME_MS
    if (lastQueryLat != 0.0 && lastQueryLng != 0.0) {
      val results = FloatArray(1)
      Location.distanceBetween(loc!!.latitude, loc.longitude, lastQueryLat, lastQueryLng,
          results)
      DebugLog.current().log("We've moved " + results[0] + " metres since the last query.")
      if (results[0] > 5000.0f) {
        timeBetweenQueries = MOVING_QUERY_TIME_MS
      }
    }
    val timeSinceLastWeatherQuery = System.currentTimeMillis() - timeOfLastWeatherQuery
    if (timeSinceLastWeatherQuery > timeBetweenQueries) {
      DebugLog.current().log(timeSinceLastWeatherQuery
          .toString() + "ms has elapsed since last weather query. Performing new query now.")
      return true
    }
    return false
  }

  /** Fires off a thread to perform the actual weather query.  */
  private fun queryWeather(
      context: Context,
      loc: Location,
      prefs: SharedPreferences) {
    val t = Thread(Runnable {
      val geocodeInfo = GeocodeProvider().getGeocodeInfo(context, loc.latitude, loc.longitude)
      val builder = WeatherInfo.Builder(loc.latitude, loc.longitude)
      if (geocodeInfo != null) {
        builder.withGeocodeInfo(geocodeInfo)
      }
      OpenWeatherMapProvider().fetchWeather(builder)
      val weatherInfo = builder.build()
      DebugLog.current().log("Got weather: ${weatherInfo}")
      val editor = prefs.edit()
      weatherInfo.save(editor)
      editor.putLong("TimeOfLastWeatherQuery", System.currentTimeMillis())
      editor.putFloat("LastQueryLat", loc.latitude.toFloat())
      editor.putFloat("LastQueryLng", loc.longitude.toFloat())
      editor.apply()
      try {
        DebugLog.saveCurrent(context)
      } catch (e: Exception) {
        // ignore errors.
      }
      WeatherWidgetProvider.notifyRefresh(context)
      for (runnable in onUpdateRunnables) {
        runnable.run()
      }
    })
    t.start()
  }

  companion object {
    private val TAG = WeatherManager::class.java.simpleName
    var i = WeatherManager()
    private const val STATIONARY_QUERY_TIME_MS = 3 * 60 * 60 * 1000 // every three hours
        .toLong()
    private const val MOVING_QUERY_TIME_MS = 60 * 1000 // every minute
        .toLong()
  }
}