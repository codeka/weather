package au.com.codeka.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import au.com.codeka.weather.model.WeatherInfo
import com.github.florent37.materialviewpager.MaterialViewPagerHelper
import java.util.*
import kotlin.math.roundToInt

/**
 * Fragment which actually displays the details about the weather.
 */
class WeatherDetailsFragment : Fragment() {
  private var rootView: View? = null
  private var scrollView: NestedScrollView? = null
  override fun onCreateView(
      inflater: LayoutInflater,
      container: ViewGroup?,
      savedInstanceState: Bundle?): View? {
    rootView = inflater.inflate(
        R.layout.weather_details_fragment, container, false)
    scrollView = rootView!!.findViewById(R.id.scroll_view)
    return rootView
  }

  override fun onStart() {
    super.onStart()
    WeatherManager.Companion.i.addUpdateRunnable(weatherUpdatedRunnable)
    refresh()
  }

  override fun onStop() {
    super.onStop()
    WeatherManager.Companion.i.removeUpdateRunnable(weatherUpdatedRunnable)
  }

  private fun refresh() {
    val weatherInfo: WeatherInfo = WeatherManager.Companion.i.getCurrentWeather(activity) ?: return
    val currentCondition = weatherInfo.currentCondition ?: return

    // a bit hacky...
    val hour = GregorianCalendar()[Calendar.HOUR_OF_DAY]
    var isNight = hour < 6 || hour > 20
    val inflater = LayoutInflater.from(activity)
    (rootView!!.findViewById<View>(R.id.current_icon) as ImageView).setImageResource(
        currentCondition.icon!!.getLargeIconId(isNight))
    if (currentCondition.temperature == null) {
      (rootView!!.findViewById<View>(R.id.current_temp) as TextView).text = "??°C"
    } else {
      (rootView!!.findViewById<View>(R.id.current_temp) as TextView).text = String.format("%d°C", (currentCondition.temperature
          ?: 0.0).roundToInt())
    }
    (rootView!!.findViewById<View>(R.id.current_description) as TextView).text = currentCondition.description
    (rootView!!.findViewById<View>(R.id.observation_location) as TextView).text = String.format("at %s", currentCondition.observationLocation)
    val now = Date()
    val millis = now.time - currentCondition.observationTime!!.time
    (rootView!!.findViewById<View>(R.id.observation_time) as TextView).text = String.format("%d minutes ago", millis / 60000L)
    val feelsLike = if (currentCondition.feelsLike == null) "??" else (currentCondition.feelsLike ?: 0.0).roundToInt().toString()
    val relativeHumidity = if (currentCondition.relativeHumidity == null) "??" else (currentCondition.relativeHumidity ?: 0.0).roundToInt().toString()
    (rootView!!.findViewById<View>(R.id.extra_info_1) as TextView).text = String.format("Feels like %s°C, %s%% relative humidity", feelsLike, relativeHumidity)
    (rootView!!.findViewById<View>(R.id.extra_info_2) as TextView).text = String.format("%dmm last hour, %dmm today",
        currentCondition.precipitationLastHour.roundToInt(),
        currentCondition.precipitationToday.roundToInt())
    val hourlyParent = rootView!!.findViewById<View>(R.id.hourly_container) as LinearLayout
    hourlyParent.removeAllViews()
    for (hourlyForecast in weatherInfo.hourlyForecasts) {
      val hourlyForecastView = inflater.inflate(R.layout.weather_details_hourly_row, hourlyParent, false)
      isNight = hourlyForecast.hour < 6 || hourlyForecast.hour > 20
      var hourValue = hourlyForecast.hour.toString() + "am"
      if (hourlyForecast.hour == 0) {
        hourValue = "12am"
      } else if (hourlyForecast.hour == 12) {
        hourValue = "12pm"
      } else if (hourlyForecast.hour > 12) {
        hourValue = Integer.toString(hourlyForecast.hour - 12) + "pm"
      }
      (hourlyForecastView.findViewById<View>(R.id.forecast_hour) as TextView).text = hourValue
      (hourlyForecastView.findViewById<View>(R.id.forecast_icon) as ImageView).setImageResource(hourlyForecast.icon?.getSmallIconId(isNight) ?: 0)
      (hourlyForecastView.findViewById<View>(R.id.forecast_temp) as TextView).text = String.format("%d°C", hourlyForecast.temperature.roundToInt())
      if (hourlyForecast.qpfMillimeters < 0.5) {
        hourlyForecastView.findViewById<View>(R.id.forecast_precipitation).visibility = View.GONE
      } else {
        hourlyForecastView.findViewById<View>(R.id.forecast_precipitation).visibility = View.VISIBLE
        (hourlyForecastView.findViewById<View>(R.id.forecast_precipitation) as TextView).text = String.format("%dmm", hourlyForecast.qpfMillimeters.roundToInt())
      }
      hourlyParent.addView(hourlyForecastView)
    }
    val forecastParent: CardLinearLayout = rootView!!.findViewById(R.id.weather_cards)
    // Remove any views after the HorizontalScrollView, since they'll be old entries
    for (i in 0 until forecastParent.childCount) {
      if (forecastParent.getChildAt(i) is HorizontalScrollView) {
        val j = i + 1
        while (j < forecastParent.childCount) {
          forecastParent.removeViewAt(j)
        }
        break
      }
    }
    for (forecast in weatherInfo.forecasts) {
      val forecastView = inflater.inflate(
          R.layout.weather_details_forecast_row, forecastParent, false)
      var day = "Today"
      if (forecast.offset == 1) {
        day = "Tomorrow"
      } else if (forecast.offset > 1) {
        val cal: Calendar = GregorianCalendar()
        cal.add(Calendar.DAY_OF_YEAR, forecast.offset)
        day = cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.US)
      }
      (forecastView.findViewById<View>(R.id.forecast_day) as TextView).text = day
      (forecastView.findViewById<View>(R.id.forecast_description) as TextView).text = forecast.description
      (forecastView.findViewById<View>(R.id.forecast_temp_high) as TextView).text = String.format("%d°C", forecast.highTemperature.roundToInt())
      (forecastView.findViewById<View>(R.id.forecast_temp_low) as TextView).text = String.format("%d°C", forecast.lowTemperature.roundToInt())
      (forecastView.findViewById<View>(R.id.forecast_icon) as ImageView).setImageResource(
          forecast.icon!!.getLargeIconId(false))
      forecastParent.addView(forecastView)
    }
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    MaterialViewPagerHelper.registerScrollView(context, scrollView)
  }

  // Called when the weather updates.
  private val weatherUpdatedRunnable = Runnable { activity!!.runOnUiThread { refresh() } }
}