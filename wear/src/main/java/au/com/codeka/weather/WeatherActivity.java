package au.com.codeka.weather;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

public class WeatherActivity extends Activity {

  private TextView mTextView;

  private static final int PERMISSIONS_REQUEST_READ_CALENDAR = 36364;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_weather);
    final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
    stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
      @Override
      public void onLayoutInflated(WatchViewStub stub) {
        mTextView = (TextView) stub.findViewById(R.id.text);
      }
    });

    ActivityCompat.requestPermissions(this,
        new String[]{Manifest.permission.READ_CALENDAR},
        PERMISSIONS_REQUEST_READ_CALENDAR);

  }
}
