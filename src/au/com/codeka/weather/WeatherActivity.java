package au.com.codeka.weather;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WeatherActivity extends Activity {
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.weather_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.refresh:
      refreshWeather();
      return true;
    case R.id.debug:
      showDebugActivity();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  private void refreshWeather() {
    WeatherManager.i.refreshWeather(this, true);
  }

  private void showDebugActivity() {
    startActivity(new Intent(this, DebugActivity.class));
  }
}
