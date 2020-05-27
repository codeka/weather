package au.com.codeka.weather.location

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import androidx.core.app.ActivityCompat
import au.com.codeka.weather.DebugLog
import java.util.concurrent.TimeUnit

/**
 * Provider for fetching our current location. We do some fancy tricks to ensure we don't query
 * the expensive sensor too often.
 */
class LocationProvider(private val context: Context) {
  interface LocationFetchedListener {
    fun onLocationFetched(loc: Location?)
  }

  private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
  private val prefs: SharedPreferences = context.getSharedPreferences("au.com.codeka.weather", Context.MODE_PRIVATE)

  /** Gets your current location as a lat/long  */
  fun getLocation(force: Boolean, locationFetcherListener: LocationFetchedListener) {
    val now = System.currentTimeMillis()

    // we'll use the PASSIVE_PROVIDER most of the time, but occasionally use GPS_PROVIDER for
    // more accurate updates.
    val timeSinceLastGpsRequest = prefs.getLong("TimeSinceLastGpsRequest", 0)
    var doGpsRequest = force
    if (now - timeSinceLastGpsRequest > LOCATION_UPDATE_TIME_MS) {
      doGpsRequest = true
    }
    if (doGpsRequest) {
      prefs.edit().putLong("TimeSinceLastGpsRequest", now).apply()
    }
    val criteria = Criteria()
    var provider: String? = LocationManager.PASSIVE_PROVIDER
    if (doGpsRequest) {
      provider = locationManager.getBestProvider(criteria, true)
      if (provider == null) {
        // fallback to network provider if we can't get GPS provider
        provider = LocationManager.NETWORK_PROVIDER
      }
    }
    val haveFineLocation = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val haveCoarseLocation = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (!haveCoarseLocation || !haveFineLocation) {
      // We'll only do this if we're called in the context of an activity. If it's the alarm
      // receiver calling us, then don't bother.
      if (context is Activity) {
        ActivityCompat.requestPermissions(context, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        ), 1)
      }
      Log.w(TAG, "No location permission, cannot get current location.")
      return
    }
    val myLocationListener = MyLocationListener(
        locationManager.getLastKnownLocation(provider))
    if (myLocationListener.isLocationGoodEnough) {
      // if lastKnownLocation is good enough, just use it.
      locationFetcherListener.onLocationFetched(myLocationListener.bestLocation)
      return
    }
    DebugLog.current().log("Using location provider: $provider")
    if (provider == LocationManager.GPS_PROVIDER) {
      // if we're getting GPS location, also listen for network locations in case we don't have
      // GPS lock and we're inside or whatever.
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0f,
          myLocationListener)
    }
    locationManager.requestLocationUpdates(provider, 0, 0f, myLocationListener)

    // check back in 10 seconds for the best location we've received in that time.
    DebugLog.current().log("Waiting for location fix.")
    Handler().postDelayed(Runnable {
      val loc = myLocationListener.bestLocation
      try {
        locationManager.removeUpdates(myLocationListener)
      } catch (e: SecurityException) {
        // Should never happen because we check for the permission above, but if we do get this,
        // just ignore it.
        return@Runnable
      }
      locationFetcherListener.onLocationFetched(loc)
    }, 10000)
  }

  private inner class MyLocationListener(var bestLocation: Location?) : LocationListener {
    val isLocationGoodEnough: Boolean
      get() {
        if (bestLocation == null) {
          return false
        }
        val nanos = SystemClock.elapsedRealtimeNanos() - bestLocation!!.elapsedRealtimeNanos
        return nanos < TimeUnit.MINUTES.toNanos(10)
      }

    override fun onLocationChanged(location: Location) {
      if (isBetterLocation(location, bestLocation)) {
        bestLocation = location
      }
    }

    override fun onProviderDisabled(provider: String) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
      if (currentBestLocation == null) {
        // A new location is always better than no location
        return true
      }

      // Check whether the new location fix is newer or older
      val timeDelta = location.time - currentBestLocation.time
      val isSignificantlyNewer = timeDelta > Companion.TWO_MINUTES
      val isSignificantlyOlder = timeDelta < -Companion.TWO_MINUTES
      val isNewer = timeDelta > 0

      // If it's been more than two minutes since the current location, use the new location
      // because the user has likely moved
      if (isSignificantlyNewer) {
        return true
        // If the new location is more than two minutes older, it must be worse
      } else if (isSignificantlyOlder) {
        return false
      }

      // Check whether the new location fix is more or less accurate
      val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
      val isLessAccurate = accuracyDelta > 0
      val isMoreAccurate = accuracyDelta < 0
      val isSignificantlyLessAccurate = accuracyDelta > 200

      // Check if the old and new location are from the same provider
      val isFromSameProvider = isSameProvider(location.provider,
          currentBestLocation.provider)

      // Determine location quality using a combination of timeliness and accuracy
      if (isMoreAccurate) {
        return true
      } else if (isNewer && !isLessAccurate) {
        return true
      } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
        return true
      }
      return false
    }

    /** Checks whether two providers are the same  */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
      return if (provider1 == null) {
        provider2 == null
      } else provider1 == provider2
    }
  }

  companion object {
    private const val TWO_MINUTES = 1000 * 60 * 2
    private const val TAG = "codeka.weather"
    private const val LOCATION_UPDATE_TIME_MS = 10 * 60 * 1000 // every 10 minutes
        .toLong()
  }

}