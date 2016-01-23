package au.com.codeka.weather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPagerHelper;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import au.com.codeka.weather.model.CurrentCondition;
import au.com.codeka.weather.model.Forecast;
import au.com.codeka.weather.model.WeatherInfo;

/**
 * Fragment which actually displays the details about the weather.
 */
public class WeatherDetailsFragment extends Fragment {
  private ObservableScrollView scrollView;

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    ViewGroup rootView = (ViewGroup) inflater.inflate(
        R.layout.weather_details_fragment, container, false);
    scrollView = (ObservableScrollView) rootView.findViewById(R.id.scroll_view);

    WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(getActivity());
    // a bit hacky...
    int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
    boolean isNight = hour < 6 || hour > 20;

    CurrentCondition currentCondition = weatherInfo.getCurrentCondition();
    ((ImageView) rootView.findViewById(R.id.current_icon)).setImageResource(
        currentCondition.getIcon().getLargeIconId(isNight));
    ((TextView) rootView.findViewById(R.id.current_temp)).setText(
        String.format("%d°C", Math.round(currentCondition.getTemperature())));
    ((TextView) rootView.findViewById(R.id.current_description)).setText(
        currentCondition.getDescription());
    ((TextView) rootView.findViewById(R.id.observation_location)).setText(
        String.format("at %s", currentCondition.getObservationLocation()));
    long nanos = new Date().getTime() - currentCondition.getObservationTime().getTime();
    ((TextView) rootView.findViewById(R.id.observation_time)).setText(
        String.format("%d minutes ago", nanos / 60000000000L));

    ((TextView) rootView.findViewById(R.id.extra_info_1)).setText(
        String.format("Feels like %d°C, %d%% relative humidity",
            Math.round(currentCondition.getFeelsLike()),
            Math.round(currentCondition.getRelativeHumidity())));
    ((TextView) rootView.findViewById(R.id.extra_info_2)).setText(
        String.format("%dmm last hour, %dmm today",
            Math.round(currentCondition.getPrecipitationLastHour()),
            Math.round(currentCondition.getPrecipitationToday())));

    for (Forecast forecast : weatherInfo.getForecasts()) {
      CardLinearLayout forecastParent =
          (CardLinearLayout) rootView.findViewById(R.id.weather_cards);
      View forecastView = inflater.inflate(R.layout.weather_details_forecast_row, forecastParent, false);

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

    return rootView;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    MaterialViewPagerHelper.registerScrollView(getActivity(), scrollView, null);
  }
}
