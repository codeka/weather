package au.com.codeka.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class WeatherAlarmReceiver extends BroadcastReceiver {
  private static final String TAG = WeatherAlarmReceiver.class.getSimpleName();

  /** This is called by the system when an alarm is received. */
  @Override
  public void onReceive(Context context, Intent intent) {
    Log.i(TAG, "Alarm received.");

    try {
      // Fetch weather, which may schedule an update of the widget.
      WeatherManager.i.refreshWeather(context, false);
    } catch (Exception e) {
      Log.e(TAG, "Exception caught processing alarm!", e);
    }

    // schedule the next alarm to run
    schedule(context);
  }

  /** Schedule the alarm to run every five minutes, the maximum frequency we'll update at. */
  public static void schedule(Context context) {
    // make sure the alarm is running
    Intent intent = new Intent(context, WeatherAlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    long nextAlarm = System.currentTimeMillis();
    if (WeatherManager.i.getCurrentWeather(context) != null) {
      long millisPerFiveMinutes = 5 * 1000 * 60;
      nextAlarm = nextAlarm - (nextAlarm % millisPerFiveMinutes) + millisPerFiveMinutes;
    }

    long millis = nextAlarm - System.currentTimeMillis();
    Log.d(TAG, "Next alarm in " + millis + "ms");
    DebugLog.current().setMillisToNextAlarm(millis);

    // we must remember to call schedule() after processing the first intent
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarmManager.set(AlarmManager.RTC, nextAlarm,  pendingIntent);
  }
}
