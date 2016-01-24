package au.com.codeka.weather;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

/**
 * Fragment which displays a map with some weather info overlayed over it.
 */
public class WeatherMapFragment extends Fragment {
  private ObservableScrollView scrollView;
  private SupportMapFragment mapFragment;
  private GoogleMap map;

  private Handler handler = new Handler();

  private GroundOverlay overlay;
  private LatLngBounds overlayLatLngBounds;
  private List<Bitmap> overlayBitmaps;
  private int overlayFrame;

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.weather_map_layout, container, false);
    scrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);

    mapFragment = new SupportMapFragment();
    FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
    fragmentTransaction.add(R.id.map, mapFragment);
    fragmentTransaction.commit();

    mapFragment.getMapAsync(new OnMapReadyCallback() {
      @Override
      public void onMapReady(GoogleMap googleMap) {
        // TODO: put the camera where we actually think you are
        LatLng latlng = new LatLng(40.7053111, -74.2581875);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 8.0f));

        overlayLatLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
        map = googleMap;

        new Thread(overlayUpdateRunnable).start();
      }
    });

    return rootView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    MaterialViewPagerHelper.registerScrollView(getActivity(), scrollView, null);
  }

  private final Runnable overlayUpdateRunnable = new Runnable() {
    @Override
    public void run() {
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

      getActivity().runOnUiThread(updateOverlayFrameRunnable);
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

      overlayFrame++;
      if (overlayFrame >= overlayBitmaps.size()) {
        overlayFrame = 0;
      }
      handler.postDelayed(updateOverlayFrameRunnable, 1000);
    }
  };
}
