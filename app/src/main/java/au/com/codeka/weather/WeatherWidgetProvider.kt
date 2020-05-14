package au.com.codeka.weather

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import au.com.codeka.weather.model.WeatherInfo
import java.util.*
import kotlin.math.roundToInt

/**
 * This is the widget provider which renders the actual widget.
 */
class WeatherWidgetProvider : AppWidgetProvider() {
  private var remoteViews: RemoteViews? = null
  private var componentName: ComponentName? = null

  /**
   * Called when we receive a notification, either from the widget subsystem directly, or from our
   * custom refresh code.
   */
  override fun onReceive(context: Context, intent: Intent) {
    WeatherAlarmReceiver.Companion.schedule(context)
    try {
      remoteViews = RemoteViews(context.packageName, R.layout.widget)
      componentName = ComponentName(context, WeatherWidgetProvider::class.java)
      if (intent.getIntExtra(CUSTOM_REFRESH_ACTION, 0) == 1) {
        refreshWidget(context)
      } else {
        super.onReceive(context, intent)
      }
      AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews)
    } catch (e: Exception) {
      Log.e(TAG, "Unhandled exception!", e)
    }
  }

  /**
   * This is called when the "options" of the widget change. We'll just refresh the widget.
   */
  override fun onAppWidgetOptionsChanged(context: Context, appWidgetManager: AppWidgetManager,
                                         appWidgetId: Int, newOptions: Bundle) {
    WeatherAlarmReceiver.Companion.schedule(context)
    refreshWidget(context)
  }

  /**
   * This is called when the widget is updated (usually when it starts up, but also gets called
   * ~every 30 minutes.
   */
  override fun onUpdate(context: Context, mgr: AppWidgetManager, appWidgetIds: IntArray) {
    super.onUpdate(context, mgr, appWidgetIds)
    WeatherAlarmReceiver.Companion.schedule(context)
    val intent = Intent(context, WeatherActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    remoteViews!!.setOnClickPendingIntent(R.id.weather_btn, pendingIntent)
    refreshWidget(context)
  }

  private fun refreshWidget(context: Context) {
    val prefs = context.getSharedPreferences("au.com.codeka.weather",
        Context.MODE_PRIVATE)
    val weatherInfo: WeatherInfo = WeatherInfo.Builder.Companion.load(prefs) ?: return
    val currentCondition = weatherInfo.currentCondition
        ?: // oops!
        return
    remoteViews!!.setTextViewText(R.id.weather_temp, String.format("%d °C",
        (currentCondition.temperature ?: 0.0).roundToInt().toInt()))
    remoteViews!!.setTextViewText(R.id.weather_text, currentCondition.description)
    val geocodeInfo = weatherInfo.geocodeInfo
    remoteViews!!.setTextViewText(R.id.geocode_location, geocodeInfo!!.shortName)

    // a bit hacky...
    val hour = GregorianCalendar()[Calendar.HOUR_OF_DAY]
    val isNight = hour < 6 || hour > 20
    val icon = currentCondition.icon
    remoteViews!!.setImageViewResource(R.id.current_icon, icon!!.getLargeIconId(isNight))
    val offset: Int
    if (hour < 12) {
      // still morning, show today's forecast
      offset = 0
      remoteViews!!.setTextViewText(R.id.tomorrow_text, "Today")
    } else {
      offset = 1
      remoteViews!!.setTextViewText(R.id.tomorrow_text, "Tomorrow")
    }
    val forecast = weatherInfo.forecasts[offset]
    remoteViews!!.setTextViewText(R.id.tomorrow_weather, String.format(
        "%d °C %s", Math.round(forecast.highTemperature), forecast.shortDescription))
    remoteViews!!.setImageViewResource(R.id.tomorrow_icon, forecast.icon!!.getSmallIconId(false))
  }

  companion object {
    private val TAG = WeatherWidgetProvider::class.java.simpleName
    const val CUSTOM_REFRESH_ACTION = "au.com.codeka.weather.UpdateAction"

    /**
     * You can call this to send a notification to the graph widget to update itself.
     */
    fun notifyRefresh(context: Context) {
      val i = Intent(context, WeatherWidgetProvider::class.java)
      i.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
      i.putExtra(CUSTOM_REFRESH_ACTION, 1)
      context.sendBroadcast(i)
    }
  }
}