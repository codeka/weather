package au.com.codeka.weather;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

    WeatherAlarmReceiver.schedule(context);

    Intent intent = new Intent(context, WeatherActivity.class);
    PendingIntent pendingIntent =
        PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    mRemoteViews.setOnClickPendingIntent(R.id.weather_btn, pendingIntent);

    refreshWidget(context);
  }

  private void refreshWidget(Context context) {
    SharedPreferences prefs = context.getSharedPreferences("au.com.codeka.weather",
        Context.MODE_PRIVATE);
    WeatherInfo weatherInfo = new WeatherInfo.Builder().load(prefs);
    if (weatherInfo == null) {
      return;
    }

    OpenWeatherMapInfo.CurrentConditions currentConditions =
        weatherInfo.getWeather().getCurrentConditions();
    mRemoteViews.setTextViewText(R.id.weather_temp, String.format("%d °C",
        (int) Math.round(currentConditions.getTemp())));
    mRemoteViews.setTextViewText(R.id.weather_text, currentConditions.getDescription());

    GeocodeInfo geocodeInfo = weatherInfo.getGeocodeInfo();
    mRemoteViews.setTextViewText(R.id.geocode_location, geocodeInfo.getShortName());

    mRemoteViews.setImageViewResource(R.id.current_icon, currentConditions.getLargeIcon());

    OpenWeatherMapInfo.ForecastEntry tomorrowWeather = weatherInfo.getWeather().getForecast(2);
    Log.i(TAG, String.format("Forecast for %s", tomorrowWeather.getTimeStamp()));
    mRemoteViews.setTextViewText(R.id.tomorrow_weather, String.format(
        "%d °C %s", Math.round(tomorrowWeather.getMaxTemp()), tomorrowWeather.getDescription()));
    mRemoteViews.setImageViewResource(R.id.tomorrow_icon, tomorrowWeather.getSmallIcon());
  }
}
