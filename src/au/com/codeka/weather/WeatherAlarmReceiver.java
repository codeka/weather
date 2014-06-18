package au.com.codeka.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherAlarmReceiver extends BroadcastReceiver {
  private static final String TAG = WeatherAlarmReceiver.class.getSimpleName();

  /** This is called by the system when an alarm is received. */
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "alarm received.");

    // refresh the widget, because we'll have just switched over to a new minute
    WeatherWidgetProvider.notifyRefresh(context);

    // fetch weather, which may also update the widget as well...
    WeatherManager.i.refreshWeather(context, false);
  }
}
