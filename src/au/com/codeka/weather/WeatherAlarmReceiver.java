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
    Log.i(TAG, "alarm received.");

    try {
      // refresh the widget, because we'll have just switched over to a new minute
      WeatherWidgetProvider.notifyRefresh(context);
  
      // fetch weather, which may also update the widget as well...
      WeatherManager.i.refreshWeather(context, false);
    } catch (Exception e) {
      Log.e(TAG, "Exception caught processing alarm!", e);
    }

    // schedule the next alarm to run
    schedule(context);
  }

  /** Schedule the alarm to run just after the next minute ticks over. */
  public static void schedule(Context context) {
    // make sure the alarm is running
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, WeatherAlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    long millisPerMinute = 1000 * 60;
    long nextMinute = System.currentTimeMillis();
    nextMinute = nextMinute - (nextMinute % millisPerMinute) + millisPerMinute;
    Log.d(TAG, "now="+System.currentTimeMillis()+" nextMinute="+nextMinute);

    // we must remember to call schedule() after processing the first intent
    alarmManager.set(AlarmManager.RTC, nextMinute,  pendingIntent);
  }
}
