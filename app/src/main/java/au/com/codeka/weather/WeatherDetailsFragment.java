package au.com.codeka.weather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import au.com.codeka.weather.model.CurrentCondition;
import au.com.codeka.weather.model.Forecast;
import au.com.codeka.weather.model.HourlyForecast;
import au.com.codeka.weather.model.WeatherInfo;

/**
 * Fragment which actually displays the details about the weather.
 */
public class WeatherDetailsFragment extends Fragment {
  private View rootView;
  private ObservableScrollView scrollView;

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    rootView = inflater.inflate(
        R.layout.weather_details_fragment, container, false);
    scrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);

    return rootView;
  }

  @Override
  public void onStart() {
    super.onStart();
    WeatherManager.i.addUpdateRunnable(weatherUpdatedRunnable);
    refresh();
  }

  public void onStop() {
    super.onStop();
    WeatherManager.i.removeUpdateRunnable(weatherUpdatedRunnable);
  }

  private void refresh() {
    WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(getActivity());
    if (weatherInfo == null) {
      return;
    }

    CurrentCondition currentCondition = weatherInfo.getCurrentCondition();
    if (currentCondition == null) {
      return;
    }

    // a bit hacky...
    int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
    boolean isNight = hour < 6 || hour > 20;

    LayoutInflater inflater = LayoutInflater.from(getActivity());

    ((ImageView) rootView.findViewById(R.id.current_icon)).setImageResource(
        currentCondition.getIcon().getLargeIconId(isNight));
    if (currentCondition.getTemperature() == null) {
      ((TextView) rootView.findViewById(R.id.current_temp)).setText("??°C");
    } else {
      ((TextView) rootView.findViewById(R.id.current_temp)).setText(
          String.format("%d°C", Math.round(currentCondition.getTemperature())));
    }
    ((TextView) rootView.findViewById(R.id.current_description)).setText(
        currentCondition.getDescription());
    ((TextView) rootView.findViewById(R.id.observation_location)).setText(
        String.format("at %s", currentCondition.getObservationLocation()));
    Date now = new Date();
    long millis = now.getTime() - currentCondition.getObservationTime().getTime();
    ((TextView) rootView.findViewById(R.id.observation_time)).setText(
        String.format("%d minutes ago", millis / 60000L));

    String feelsLike = currentCondition.getFeelsLike() == null
        ? "??"
        : Long.toString(Math.round(currentCondition.getFeelsLike()));
    String relativeHumidity = currentCondition.getRelativeHumidity() == null
        ? "??"
        : Long.toString(Math.round(currentCondition.getRelativeHumidity()));
    ((TextView) rootView.findViewById(R.id.extra_info_1)).setText(
        String.format("Feels like %s°C, %s%% relative humidity", feelsLike, relativeHumidity));
    ((TextView) rootView.findViewById(R.id.extra_info_2)).setText(
        String.format("%dmm last hour, %dmm today",
            Math.round(currentCondition.getPrecipitationLastHour()),
            Math.round(currentCondition.getPrecipitationToday())));

    LinearLayout hourlyParent = (LinearLayout) rootView.findViewById(R.id.hourly_container);
    hourlyParent.removeAllViews();
    for (HourlyForecast hourlyForecast : weatherInfo.getHourlyForecasts()) {
      View hourlyForecastView =
          inflater.inflate(R.layout.weather_details_hourly_row, hourlyParent, false);

      isNight = hourlyForecast.getHour() < 6 || hourlyForecast.getHour() > 20;
      String hourValue = Integer.toString(hourlyForecast.getHour()) + "am";
      if (hourlyForecast.getHour() == 0) {
        hourValue = "12am";
      } else if (hourlyForecast.getHour() == 12) {
        hourValue = "12pm";
      } else if (hourlyForecast.getHour() > 12) {
        hourValue = Integer.toString(hourlyForecast.getHour() - 12) + "pm";
      }
      ((TextView) hourlyForecastView.findViewById(R.id.forecast_hour)).setText(hourValue);
      ((ImageView) hourlyForecastView.findViewById(R.id.forecast_icon)).setImageResource(
          hourlyForecast.getIcon().getSmallIconId(isNight));
      ((TextView) hourlyForecastView.findViewById(R.id.forecast_temp)).setText(
          String.format("%d°C", Math.round(hourlyForecast.getTemperature())));
      if (hourlyForecast.getQpfMillimeters() < 0.5) {
        hourlyForecastView.findViewById(R.id.forecast_precipitation).setVisibility(View.GONE);
      } else {
        hourlyForecastView.findViewById(R.id.forecast_precipitation).setVisibility(View.VISIBLE);
        ((TextView) hourlyForecastView.findViewById(R.id.forecast_precipitation)).setText(
            String.format("%dmm", Math.round(hourlyForecast.getQpfMillimeters())));
      }

      hourlyParent.addView(hourlyForecastView);
    }

    CardLinearLayout forecastParent =
        (CardLinearLayout) rootView.findViewById(R.id.weather_cards);
    forecastParent.removeAllViews();
    for (Forecast forecast : weatherInfo.getForecasts()) {
      View forecastView = inflater.inflate(
          R.layout.weather_details_forecast_row, forecastParent, false);

      String day = "Today";
      if (forecast.getOffset() == 1) {
        day = "Tomorrow";
      } else if (forecast.getOffset() > 1) {
        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, forecast.getOffset());
        day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US);
      }
      ((TextView) forecastView.findViewById(R.id.forecast_day)).setText(day);
      ((TextView) forecastView.findViewById(R.id.forecast_description)).setText(
          forecast.getDescription());
      ((TextView) forecastView.findViewById(R.id.forecast_temp_high)).setText(
          String.format("%d°C", Math.round(forecast.getHighTemperature())));
      ((TextView) forecastView.findViewById(R.id.forecast_temp_low)).setText(
          String.format("%d°C", Math.round(forecast.getLowTemperature())));
      ((ImageView) forecastView.findViewById(R.id.forecast_icon)).setImageResource(
          forecast.getIcon().getLargeIconId(false));

      forecastParent.addView(forecastView);
    }
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    MaterialViewPagerHelper.registerScrollView(getActivity(), scrollView, null);
  }

  // Called when the weather updates.
  private final Runnable weatherUpdatedRunnable = new Runnable() {
    @Override
    public void run() {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          refresh();
        }
      });
    }
  };
}
