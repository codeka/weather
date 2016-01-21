package au.com.codeka.weather.location;

import android.content.Context;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import au.com.codeka.weather.DebugLog;

/**
 * Fetches geocode information about our current location.
 */
public class GeocodeProvider {
  private static final String TAG = "codeka.weather";

  /**
   * Fetches a {@link GeocodeInfo} for the given lat,lng. Should be called on a background thread.
   */
  public GeocodeInfo getGeocodeInfo(double lat, double lng) {
    Gson gson = new GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();

    try {
      Log.d(TAG, "Querying Google for geocode info for: " + lat + "," + lng);
      String latlng = lat + "," + lng;
      URL url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + latlng);
      URLConnection conn = url.openConnection();
      InputStream ins = new BufferedInputStream(conn.getInputStream());
      JsonReader json = new JsonReader(new InputStreamReader(ins, "UTF-8"));
      GeocodeInfo geocodeInfo = gson.fromJson(json, GeocodeInfo.class);
      DebugLog.current().log("Geocoded location: " + geocodeInfo);
      return geocodeInfo;
    } catch (IOException e) {
      Log.e(TAG, "Error fetching geocode information.", e);
      DebugLog.current().log("Error Fetching Geocode: " + e.getMessage());
      return null;
    }
  }
}
