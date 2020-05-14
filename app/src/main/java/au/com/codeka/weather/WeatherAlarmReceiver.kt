package au.com.codeka.weather

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class WeatherAlarmReceiver : BroadcastReceiver() {
  /** This is called by the system when an alarm is received.  */
  override fun onReceive(context: Context, intent: Intent) {
    Log.i(TAG, "Alarm received.")
    try {
      // Fetch weather, which may schedule an update of the widget.
      WeatherManager.i.refreshWeather(context, false)
    } catch (e: Exception) {
      Log.e(TAG, "Exception caught processing alarm!", e)
    }

    // schedule the next alarm to run
    schedule(context)
  }

  companion object {
    private val TAG = WeatherAlarmReceiver::class.java.simpleName

    /** Schedule the alarm to run every five minutes, the maximum frequency we'll update at.  */
    fun schedule(context: Context) {
      // make sure the alarm is running
      val intent = Intent(context, WeatherAlarmReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0)
      var nextAlarm = System.currentTimeMillis()
      if (WeatherManager.i.getCurrentWeather(context) != null) {
        val millisPerFiveMinutes = 5 * 1000 * 60.toLong()
        nextAlarm = nextAlarm - nextAlarm % millisPerFiveMinutes + millisPerFiveMinutes
      }
      val millis = nextAlarm - System.currentTimeMillis()
      Log.d(TAG, "Next alarm in " + millis + "ms")
      DebugLog.current()!!.setMillisToNextAlarm(millis)

      // we must remember to call schedule() after processing the first intent
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
      alarmManager[AlarmManager.RTC, nextAlarm] = pendingIntent
    }
  }
}