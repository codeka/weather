package au.com.codeka.weather;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.WebView;

/** Debug activity is used to display the raw JSON we have store, for debugging. */
public class DebugActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getActionBar().setDisplayHomeAsUpEnabled(true);
    setContentView(R.layout.debug_activity);

    WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(this);
    StringBuilder sb = new StringBuilder();
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    sb.append("<html><body><pre>");
    sb.append(String.format("Location: %f, %f\n", weatherInfo.getLat(), weatherInfo.getLng()));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getGeocodeInfo(), GeocodeInfo.class));
    sb.append("\n----------------------------------------\n\n");
    sb.append(gson.toJson(weatherInfo.getWeather(), OpenWeatherMapInfo.class));
    sb.append("</pre></body></html>");

    WebView debugInfo = (WebView) findViewById(R.id.debug_info);
    debugInfo.setVerticalScrollBarEnabled(true);
    debugInfo.setHorizontalScrollBarEnabled(true);

    String data = Base64.encodeToString(sb.toString().getBytes(), Base64.DEFAULT);
    debugInfo.loadData(data, "text/html", "base64");
  }
}
