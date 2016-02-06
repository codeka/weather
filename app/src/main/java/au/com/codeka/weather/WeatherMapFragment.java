package au.com.codeka.weather;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import au.com.codeka.weather.model.WeatherInfo;

/**
 * Fragment which displays a map with some weather info overlayed over it.
 */
public class WeatherMapFragment extends Fragment {
  private FrameLayout refreshButton;
  private ObservableScrollView scrollView;
  private SupportMapFragment mapFragment;
  private GoogleMap map;

  private Handler handler = new Handler();
  private boolean firstTimeVisible = true;
  private boolean needsRefresh;

  private boolean overlayLoading;
  private GroundOverlay overlay;
  private LatLngBounds overlayLatLngBounds;
  private boolean needsReposition;
  private List<Bitmap> overlayBitmaps;
  private int overlayFrame;

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.weather_map_layout, container, false);
    scrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);
    refreshButton = (FrameLayout) rootView.findViewById(R.id.refresh_button);

    mapFragment = new SupportMapFragment();
    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.replace(R.id.map, mapFragment);
    fragmentTransaction.commit();

    refreshButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        refreshOverlay();
      }
    });
    refreshButton.setClickable(false);

    mapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(GoogleMap googleMap) {
        // Start off at the weather location
        WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(getActivity());
        if (weatherInfo == null) {
          // TODO: refresh once it loads
          return;
        }
        LatLng latlng = new LatLng(weatherInfo.getLat(), weatherInfo.getLng());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8.0f));

        map = googleMap;

        if (needsRefresh) {
          refreshOverlay();
        }
        needsRefresh = false;
      }
    });

    return rootView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    MaterialViewPagerHelper.registerScrollView(getActivity(), scrollView, null);
  }

  /**
   * Called by the framework when we're visible/invisible to the user. The first time we're visible
   * is when we actually want to start loading the weather overlay.
   */
  @Override
  public void setUserVisibleHint(boolean isVisibleToUser) {
    super.setUserVisibleHint(isVisibleToUser);

    if (isVisibleToUser && firstTimeVisible) {
      refreshOverlay();
      firstTimeVisible = false;
    }
  }

  /**
   * Refreshes the overlay so that it's displaying on whatever the map is currently looking at.
   * Must run on the UI thread.
   */
  private void refreshOverlay() {
    if (map == null) {
      needsRefresh = true;
      return;
    }

    overlayLatLngBounds = map.getProjection().getVisibleRegion().latLngBounds;
    new Thread(overlayUpdateRunnable).start();
  }

  private final Runnable overlayUpdateRunnable = new Runnable() {
    @Override
    public void run() {
      overlayLoading = true;
      getActivity().runOnUiThread(updateRefreshButtonRunnable);

      InputStream ins = WeatherManager.i.fetchMapOverlay(
          overlayLatLngBounds,
          mapFragment.getView().getWidth() / 2,
          mapFragment.getView().getHeight() / 2);
      if (ins == null) {
        return;
      }

      GifDecoder gifDecoder = new GifDecoder();
      gifDecoder.read(ins);

      overlayBitmaps = new ArrayList<>();
      for (int frame = 0; frame < gifDecoder.getFrameCount(); frame++) {
        overlayBitmaps.add(gifDecoder.getFrame(frame));
      }

      overlayLoading = false;
      needsReposition = true;

      // could be null if we've finished by the time this runs.
      Activity activity = getActivity();
      if (activity != null) {
        activity.runOnUiThread(updateOverlayFrameRunnable);
      }
    }
  };

  private final Runnable updateOverlayFrameRunnable = new Runnable() {
    @Override
    public void run() {
      BitmapDescriptor bitmapDescriptor =
          BitmapDescriptorFactory.fromBitmap(overlayBitmaps.get(overlayFrame));
      if (overlay == null) {
        overlay = map.addGroundOverlay(new GroundOverlayOptions()
            .positionFromBounds(overlayLatLngBounds)
            .image(bitmapDescriptor));
      } else {
        overlay.setImage(bitmapDescriptor);
      }

      if (needsReposition) {
        overlay.setPositionFromBounds(overlayLatLngBounds);
        needsReposition = false;
      }

      overlayFrame++;
      if (overlayFrame >= overlayBitmaps.size()) {
        overlayFrame = 0;
      }

      // replace the existing callback (if there is one) with a new one.
      handler.removeCallbacks(updateOverlayFrameRunnable);
      handler.postDelayed(updateOverlayFrameRunnable, 1000);

      updateRefreshButtonRunnable.run();
    }
  };

  private final Runnable updateRefreshButtonRunnable = new Runnable() {
    @Override
    public void run() {
      if (overlayLoading) {
        refreshButton.setClickable(false);
        refreshButton.findViewById(R.id.refresh_progress).setVisibility(View.VISIBLE);
        refreshButton.findViewById(R.id.refresh_text).setVisibility(View.GONE);
      } else {
        refreshButton.setClickable(true);
        refreshButton.findViewById(R.id.refresh_progress).setVisibility(View.GONE);
        refreshButton.findViewById(R.id.refresh_text).setVisibility(View.VISIBLE);
      }
    }
  };
}
