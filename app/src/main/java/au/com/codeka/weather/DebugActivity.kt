package au.com.codeka.weather

import android.os.Bundle
import android.util.Base64
import android.view.View
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import au.com.codeka.weather.location.GeocodeInfo
import au.com.codeka.weather.model.CurrentCondition
import au.com.codeka.weather.model.Forecast
import au.com.codeka.weather.model.HourlyForecast
import au.com.codeka.weather.model.WeatherInfo
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.util.*

/** Debug activity is used to display the raw JSON we have stored, for debugging.  */
class DebugActivity : AppCompatActivity() {
  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.debug_activity)
    val weatherInfo = WeatherManager.i.getCurrentWeather(this)
    val sb = StringBuilder()

    if (weatherInfo == null) {
      sb.append("No info")
    } else {
      val gson = GsonBuilder().setPrettyPrinting().create()
      sb.append("<html><body><pre>")
      sb.append(String.format("Location: %f, %f\n", weatherInfo.lat, weatherInfo.lng))
      sb.append("\n----------------------------------------\n\n")
      sb.append(gson.toJson(weatherInfo.currentCondition, CurrentCondition::class.java))
      sb.append("\n----------------------------------------\n\n")
      sb.append(gson.toJson(weatherInfo.forecasts,
          object : TypeToken<ArrayList<Forecast?>?>() {}.type))
      sb.append("\n----------------------------------------\n\n")
      sb.append(gson.toJson(weatherInfo.hourlyForecasts,
          object : TypeToken<ArrayList<HourlyForecast?>?>() {}.type))
      sb.append("\n----------------------------------------\n\n")
      sb.append(gson.toJson(weatherInfo.geocodeInfo, GeocodeInfo::class.java))
      sb.append("</pre></body></html>")
    }
    val debugInfo = findViewById<View>(R.id.debug_info) as WebView
    debugInfo.isVerticalScrollBarEnabled = true
    debugInfo.isHorizontalScrollBarEnabled = true
    val data = Base64.encodeToString(sb.toString().toByteArray(), Base64.DEFAULT)
    debugInfo.loadData(data, "text/html", "base64")
  }
}