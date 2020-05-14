package au.com.codeka.weather

import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import au.com.codeka.weather.model.MapOverlay
import au.com.codeka.weather.model.WeatherInfo
import com.github.florent37.materialviewpager.MaterialViewPagerHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import java.io.InputStream
import java.util.*

/**
 * Fragment which displays a map with some weather info overlayed over it.
 */
class WeatherMapFragment : Fragment() {
  companion object {
    private const val TAG = "codeka.weather"
  }

  private var refreshButton: FrameLayout? = null
  private var scrollView: NestedScrollView? = null
  private var mapFragment: SupportMapFragment? = null
  private var map: GoogleMap? = null
  private val handler = Handler()
  private var firstTimeVisible = true
  private var needsRefresh = false
  private var overlayLoading = false
  private var overlay: GroundOverlay? = null
  private var overlayLatLngBounds: LatLngBounds? = null
  private var needsReposition = false
  private var mapOverlays: ArrayList<MapOverlay> = arrayListOf()
  private var overlayFrame = 0

  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    val rootView = inflater.inflate(R.layout.weather_map_layout, container, false)
    scrollView = rootView.findViewById(R.id.scroll_view)
    refreshButton = rootView.findViewById(R.id.refresh_button)
    mapFragment = SupportMapFragment()
    val fragmentTransaction = childFragmentManager.beginTransaction()
    fragmentTransaction.replace(R.id.map, mapFragment!!)
    fragmentTransaction.commit()
    refreshButton!!.setOnClickListener { refreshOverlay() }
    refreshButton!!.isClickable = false
    mapFragment!!.getMapAsync(OnMapReadyCallback { googleMap -> // Start off at the weather location
      val weatherInfo: WeatherInfo = WeatherManager.i.getCurrentWeather(activity)
          ?: // TODO: refresh once it loads
          return@OnMapReadyCallback
      val latlng = LatLng(weatherInfo.lat, weatherInfo.lng)
      googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8.0f))
      map = googleMap
      if (needsRefresh) {
        refreshOverlay()
      }
      needsRefresh = false
    })
    return rootView
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    MaterialViewPagerHelper.registerScrollView(context, scrollView)
  }

  /**
   * Called by the framework when we're visible/invisible to the user. The first time we're visible
   * is when we actually want to start loading the weather overlay.
   */
  override fun setUserVisibleHint(isVisibleToUser: Boolean) {
    super.setUserVisibleHint(isVisibleToUser)
    if (isVisibleToUser && firstTimeVisible) {
      refreshOverlay()
      firstTimeVisible = false
    }
  }

  /**
   * Refreshes the overlay so that it's displaying on whatever the map is currently looking at.
   * Must run on the UI thread.
   */
  private fun refreshOverlay() {
    if (map == null) {
      needsRefresh = true
      return
    }

    overlayLatLngBounds = map!!.projection.visibleRegion.latLngBounds
    Thread(overlayUpdateRunnable).start()
  }

  private val overlayUpdateRunnable = Runnable {
    overlayLoading = true
    activity!!.runOnUiThread(updateRefreshButtonRunnable)
    mapOverlays = WeatherManager.i.fetchMapOverlay(
        overlayLatLngBounds!!,
        mapFragment!!.view!!.width / 2,
        mapFragment!!.view!!.height / 2)
        ?: return@Runnable
    overlayLoading = false
    needsReposition = true

    // could be null if we've finished by the time this runs.
    val activity: Activity? = activity
    activity?.runOnUiThread(updateOverlayFrameRunnable)
  }

  private val updateOverlayFrameRunnable: Runnable = object : Runnable {
    override fun run() {
      if (mapOverlays.size == 0) {
        return
      }
      val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(mapOverlays[overlayFrame].bitmap)
      if (overlay == null) {
        val overlayOptions = GroundOverlayOptions()
            .image(bitmapDescriptor)
        if (mapOverlays[overlayFrame].latLngBounds != null) {
          overlayOptions.positionFromBounds(overlayLatLngBounds)
        } else {
          Log.i(TAG, "fitting to latlng + widthMeters")
          overlayOptions.position(
              mapOverlays[overlayFrame].latLng, mapOverlays[overlayFrame].widthMeters!!)
        }
        overlay = map!!.addGroundOverlay(overlayOptions)
      } else {
        overlay!!.setImage(bitmapDescriptor)
      }
      if (needsReposition) {
        overlay!!.setPositionFromBounds(overlayLatLngBounds)
        needsReposition = false
      }
      overlayFrame++
      if (overlayFrame >= mapOverlays.size) {
        overlayFrame = 0
      }

      // replace the existing callback (if there is one) with a new one.
      handler.removeCallbacks(this)
      handler.postDelayed(this, 1000)
      updateRefreshButtonRunnable.run()
    }
  }

  private val updateRefreshButtonRunnable = Runnable {
    if (overlayLoading) {
      refreshButton!!.isClickable = false
      refreshButton!!.findViewById<View>(R.id.refresh_progress).visibility = View.VISIBLE
      refreshButton!!.findViewById<View>(R.id.refresh_text).visibility = View.GONE
    } else {
      refreshButton!!.isClickable = true
      refreshButton!!.findViewById<View>(R.id.refresh_progress).visibility = View.GONE
      refreshButton!!.findViewById<View>(R.id.refresh_text).visibility = View.VISIBLE
    }
  }
}