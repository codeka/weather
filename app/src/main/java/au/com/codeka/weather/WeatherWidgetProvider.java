package au.com.codeka.weather;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * This is the widget provider which renders the actual widget.
 */
public class WeatherWidgetProvider extends AppWidgetProvider {
  private static final String TAG = WeatherWidgetProvider.class.getSimpleName();
  private RemoteViews remoteViews;
  private ComponentName componentName;

  public static final String CUSTOM_REFRESH_ACTION = "au.com.codeka.weather.UpdateAction";

  /**
   * You can call this to send a notification to the graph widget to update itself.
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
      remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
      componentName = new ComponentName(context, WeatherWidgetProvider.class);

      if (intent.getAction().equals(CUSTOM_REFRESH_ACTION)) {
        refreshWidget(context);
      } else {
        super.onReceive(context, intent);
      }

      AppWidgetManager.getInstance(context).updateAppWidget(componentName, remoteViews);
    } catch (Exception e) {
      Log.e(TAG, "Unhandled exception!", e);
    }
  }

  /**
   * This is called when the "options" of the widget change. We'll just refresh the widget.
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
    remoteViews.setOnClickPendingIntent(R.id.weather_btn, pendingIntent);

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
    if (currentConditions == null) {
      // oops!
      return;
    }
    remoteViews.setTextViewText(R.id.weather_temp, String.format("%d °C",
        (int) Math.round(currentConditions.getTemp())));
    remoteViews.setTextViewText(R.id.weather_text, currentConditions.getDescription());

    GeocodeInfo geocodeInfo = weatherInfo.getGeocodeInfo();
    remoteViews.setTextViewText(R.id.geocode_location, geocodeInfo.getShortName());

    remoteViews.setImageViewResource(R.id.current_icon, currentConditions.getLargeIcon());

    int forecastDay = 1;
    int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
    if (hour < 12) {
      // still morning, show today's forecast
      forecastDay = 0;
      remoteViews.setTextViewText(R.id.tomorrow_text, "Today");
    } else {
      forecastDay = 1;
      remoteViews.setTextViewText(R.id.tomorrow_text, "Tomorrow");
    }
    OpenWeatherMapInfo.ForecastEntry forecastWeather =
        weatherInfo.getWeather().getForecast(forecastDay);
    remoteViews.setTextViewText(R.id.tomorrow_weather, String.format(
        "%d °C %s", Math.round(forecastWeather.getMaxTemp()), forecastWeather.getDescription()));
    remoteViews.setImageViewResource(R.id.tomorrow_icon, forecastWeather.getSmallIcon());

  }
}
