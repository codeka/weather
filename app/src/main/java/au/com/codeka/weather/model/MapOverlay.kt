package au.com.codeka.weather.model

import android.graphics.Bitmap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import java.util.*

class MapOverlay(var bitmap: Bitmap, var timestamp: Date) {

  // Either latLngBounds must be set, or latLng + widthMeters should be set.
  var latLngBounds: LatLngBounds? = null
  var latLng: LatLng? = null
  var widthMeters: Float? = null
}
