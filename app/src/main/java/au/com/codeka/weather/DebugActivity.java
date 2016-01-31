package au.com.codeka.weather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.webkit.WebView;

import java.util.ArrayList;

import au.com.codeka.weather.location.GeocodeInfo;
import au.com.codeka.weather.model.CurrentCondition;
import au.com.codeka.weather.model.Forecast;
import au.com.codeka.weather.model.HourlyForecast;
import au.com.codeka.weather.model.WeatherInfo;

/** Debug activity is used to display the raw JSON we have stored, for debugging. */
public class DebugActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.debug_activity);

    WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(this);
    StringBuilder sb = new StringBuilder();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    sb.append("<html><body><pre>");
    sb.append(String.format("Location: %f, %f\n", weatherInfo.getLat(), weatherInfo.getLng()));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getCurrentCondition(), CurrentCondition.class));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getForecasts(),
        new TypeToken<ArrayList<Forecast>>() {}.getType()));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getHourlyForecasts(),
        new TypeToken<ArrayList<HourlyForecast>>() {}.getType()));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getGeocodeInfo(), GeocodeInfo.class));
    sb.append("</pre></body></html>");

    WebView debugInfo = (WebView) findViewById(R.id.debug_info);
    debugInfo.setVerticalScrollBarEnabled(true);
    debugInfo.setHorizontalScrollBarEnabled(true);

    String data = Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
    debugInfo.loadData(data, "text/html", "base64");
  }
}
