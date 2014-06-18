package au.com.codeka.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.RemoteViews;

/**
 * This is the widget provider which renders the actual widget.
 */
public class WeatherWidgetProvider extends AppWidgetProvider {
  private static final String TAG = WeatherWidgetProvider.class.getSimpleName();
  private RemoteViews mRemoteViews;
  private ComponentName mComponentName;

  private static final SimpleDateFormat sTimeFormat = new SimpleDateFormat("h:mm", Locale.ENGLISH);
  private static final SimpleDateFormat sAmPmFormat = new SimpleDateFormat("a", Locale.ENGLISH);
  private static final SimpleDateFormat sDateFormat = new SimpleDateFormat("EEE, d MMMM, yyyy", Locale.ENGLISH);

  public static final String CUSTOM_REFRESH_ACTION = "au.com.codeka.weather.UpdateAction";

  /**
   * You can call this to send a notification to the graph widget to update
   * itself.
   */
  public static void notifyRefresh(Context context) {
    Intent i = new Intent(context, WeatherWidgetProvider.class);
    i.setAction(WeatherWidgetProvider.CUSTOM_REFRESH_ACTION);
    context.sendBroadcast(i);
  }

  /**
   * Called when we receive a notification, either from the widget subsystem directly, or from our
   * custom refresh code.
   */
  @Override
  public void onReceive(Context context, Intent intent) {
    try {
      mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
      mComponentName = new ComponentName(context, WeatherWidgetProvider.class);

      if (intent.getAction().equals(CUSTOM_REFRESH_ACTION)) {
        refreshWidget(context);
      } else {
        super.onReceive(context, intent);
      }

      AppWidgetManager.getInstance(context).updateAppWidget(mComponentName, mRemoteViews);
    } catch (Exception e) {
      Log.e(TAG, "Unhandled exception!", e);
    }
  }

  /**
   * This is called when the "options" of the widget change. In particular,
   * we're interested in when the dimensions change.
   * 
   * Unfortunately, the "width" and "height" we receive can, in reality, be
   * incredibly inaccurate, we need to provide a setting that the user can
   * override :\
   */
  @Override
  public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
      int appWidgetId, Bundle newOptions) {

    refreshWidget(context);
  }

  /**
   * This is called when the widget is updated (usually when it starts up, but also gets called
   * ~every 30 minutes.
   */
  @Override
  public void onUpdate(Context context, AppWidgetManager mgr, int[] appWidgetIds) {
    super.onUpdate(context, mgr, appWidgetIds);

    // make sure the alarm is running
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent intent = new Intent(context, WeatherAlarmReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

    long millisPerMinute = 1000 * 60;
    long nextMinute = System.currentTimeMillis();
    nextMinute = nextMinute - (nextMinute % millisPerMinute) + millisPerMinute;
    Log.d(TAG, "now="+System.currentTimeMillis()+" nextMinute="+nextMinute);

    alarmManager.setRepeating(AlarmManager.RTC, nextMinute,
        1000 * 60 * 1, // every minute (we have to at least update the clock)
        pendingIntent);

    intent = findClockIntent(context);
    if (intent != null) {
      pendingIntent = PendingIntent.getActivity(context, 0, intent,
         PendingIntent.FLAG_UPDATE_CURRENT);
      mRemoteViews.setOnClickPendingIntent(R.id.clock_btn, pendingIntent);
    }

    intent = new Intent(context, WeatherActivity.class);
    pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.weather_btn, pendingIntent);

    refreshWidget(context);
  }

  private void refreshWidget(Context context) {
    Date dt = new Date();
    mRemoteViews.setTextViewText(R.id.current_time, sTimeFormat.format(dt));
    mRemoteViews.setTextViewText(R.id.current_time_ampm, sAmPmFormat.format(dt));
    mRemoteViews.setTextViewText(R.id.current_date, sDateFormat.format(dt));

    String nextAlarm = Settings.System.getString(context.getContentResolver(),
        Settings.System.NEXT_ALARM_FORMATTED);
    if (nextAlarm != null && nextAlarm.length() > 0) {
      mRemoteViews.setTextViewText(R.id.next_alarm, "\u23F0 " + nextAlarm);
    } else {
      mRemoteViews.setTextViewText(R.id.next_alarm, "");
    }

    SharedPreferences prefs = context.getSharedPreferences("au.com.codeka.weather",
        Context.MODE_PRIVATE);
    WeatherInfo weatherInfo = new WeatherInfo.Builder().load(prefs);
    if (weatherInfo == null) {
      return;
    }

    OpenWeatherMapInfo.CurrentConditions currentConditions = weatherInfo.getWeather().getCurrentConditions();
    mRemoteViews.setTextViewText(R.id.weather_temp, String.format("%d \u00B0C", 
        (int) Math.round(currentConditions.getTemp())));
    mRemoteViews.setTextViewText(R.id.weather_text, currentConditions.getDescription());

    GeocodeInfo geocodeInfo = weatherInfo.getGeocodeInfo();
    mRemoteViews.setTextViewText(R.id.geocode_location, geocodeInfo.getShortName());

    mRemoteViews.setImageViewResource(R.id.current_icon, currentConditions.getLargeIcon());

    OpenWeatherMapInfo.ForecastEntry tomorrowWeather = weatherInfo.getWeather().getForecast(1);
    mRemoteViews.setTextViewText(R.id.tomorrow_weather, tomorrowWeather.getDescription());
    mRemoteViews.setImageViewResource(R.id.tomorrow_icon, tomorrowWeather.getSmallIcon());
  }

  private Intent findClockIntent(Context context) {
    PackageManager packageManager = context.getPackageManager();
    Intent intent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);

    try {
      ComponentName cn = new ComponentName("com.google.android.deskclock",
          "com.android.deskclock.DeskClock");
      packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);

      intent.setComponent(cn);
    } catch (PackageManager.NameNotFoundException e) {
      Log.w(TAG, "Could not find Clock activity!");
      // couldn't find clock action, shouldn't happen on stock Android.
      return null;
    }

    return intent;
  }
}
