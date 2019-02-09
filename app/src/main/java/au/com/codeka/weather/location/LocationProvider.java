package au.com.codeka.weather.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import androidx.core.app.ActivityCompat;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import au.com.codeka.weather.DebugLog;

/**
 * Provider for fetching our current location. We do some fancy tricks to ensure we don't query
 * the expensive sensor too often.
 */
public class LocationProvider {
  public interface LocationFetchedListener {
    void onLocationFetched(Location loc);
  }

  private static final String TAG = "codeka.weather";
  private static final long LOCATION_UPDATE_TIME_MS = 10 * 60 * 1000; // every 10 minutes

  private final Context context;
  private final LocationManager locationManager;
  private final SharedPreferences prefs;

  public LocationProvider(Context context) {
    this.context = context;
    this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    this.prefs = context.getSharedPreferences("au.com.codeka.weather", Context.MODE_PRIVATE);
  }

  /** Gets your current location as a lat/long */
  public void getLocation(boolean force, final LocationFetchedListener locationFetcherListener) {
    long now = System.currentTimeMillis();

    // we'll use the PASSIVE_PROVIDER most of the time, but occasionally use GPS_PROVIDER for
    // more accurate updates.
    long timeSinceLastGpsRequest = prefs.getLong("TimeSinceLastGpsRequest", 0);
    boolean doGpsRequest = force;
    if (now - timeSinceLastGpsRequest > LOCATION_UPDATE_TIME_MS) {
      doGpsRequest = true;
    }
    if (doGpsRequest) {
      prefs.edit().putLong("TimeSinceLastGpsRequest", now).apply();
    }

    Criteria criteria = new Criteria();
    String provider = LocationManager.PASSIVE_PROVIDER;
    if (doGpsRequest) {
      provider = locationManager.getBestProvider(criteria, true);
      if (provider == null) {
        // fallback to network provider if we can't get GPS provider
        provider = LocationManager.NETWORK_PROVIDER;
      }
    }

    boolean haveFineLocation = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    boolean haveCoarseLocation = ActivityCompat.checkSelfPermission(context,
        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    if (!haveCoarseLocation || !haveFineLocation) {
      // We'll only do this if we're called in the context of an activity. If it's the alarm
      // receiver calling us, then don't bother.
      if (context instanceof Activity) {
        ActivityCompat.requestPermissions((Activity) context, new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
        }, 1);
      }
      Log.w(TAG, "No location permission, cannot get current location.");
      return;
    }

    final MyLocationListener myLocationListener = new MyLocationListener(
        locationManager.getLastKnownLocation(provider));
    if (myLocationListener.isLocationGoodEnough()) {
      // if lastKnownLocation is good enough, just use it.
      locationFetcherListener.onLocationFetched(myLocationListener.getBestLocation());
      return;
    }

    DebugLog.current().log("Using location provider: " + provider);
    if (provider.equals(LocationManager.GPS_PROVIDER)) {
      // if we're getting GPS location, also listen for network locations in case we don't have
      // GPS lock and we're inside or whatever.
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0,
          myLocationListener);
    }
    locationManager.requestLocationUpdates(provider, 0, 0, myLocationListener);

    // check back in 10 seconds for the best location we've received in that time.
    DebugLog.current().log("Waiting for location fix.");
    new Handler().postDelayed(new Runnable() {
      @Override
      public void run() {
        Location loc = myLocationListener.getBestLocation();
        try {
          locationManager.removeUpdates(myLocationListener);
        } catch (SecurityException e) {
          // Should never happen because we check for the permission above, but if we do get this,
          // just ignore it.
          return;
        }

        locationFetcherListener.onLocationFetched(loc);
      }
    }, 10000);
  }


  private class MyLocationListener implements LocationListener {
    private static final int TWO_MINUTES = 1000 * 60 * 2;
    private Location lastKnownLocation;

    public MyLocationListener(Location lastKnownLocation) {
      this.lastKnownLocation = lastKnownLocation;
    }

    public boolean isLocationGoodEnough() {
      if (lastKnownLocation == null) {
        return false;
      }
      long nanos = SystemClock.elapsedRealtimeNanos() - lastKnownLocation.getElapsedRealtimeNanos();
      return nanos < TimeUnit.MINUTES.toNanos(10);
    }

    public Location getBestLocation() {
      return lastKnownLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
      if (isBetterLocation(location, lastKnownLocation)) {
        lastKnownLocation = location;
      }
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
      if (currentBestLocation == null) {
        // A new location is always better than no location
        return true;
      }

      // Check whether the new location fix is newer or older
      long timeDelta = location.getTime() - currentBestLocation.getTime();
      boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
      boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
      boolean isNewer = timeDelta > 0;

      // If it's been more than two minutes since the current location, use the new location
      // because the user has likely moved
      if (isSignificantlyNewer) {
        return true;
        // If the new location is more than two minutes older, it must be worse
      } else if (isSignificantlyOlder) {
        return false;
      }

      // Check whether the new location fix is more or less accurate
      int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
      boolean isLessAccurate = accuracyDelta > 0;
      boolean isMoreAccurate = accuracyDelta < 0;
      boolean isSignificantlyLessAccurate = accuracyDelta > 200;

      // Check if the old and new location are from the same provider
      boolean isFromSameProvider = isSameProvider(location.getProvider(),
          currentBestLocation.getProvider());

      // Determine location quality using a combination of timeliness and accuracy
      if (isMoreAccurate) {
        return true;
      } else if (isNewer && !isLessAccurate) {
        return true;
      } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
        return true;
      }
      return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
      if (provider1 == null) {
        return provider2 == null;
      }
      return provider1.equals(provider2);
    }
  }
}
