package au.com.codeka.weather;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.github.florent37.materialviewpager.MaterialViewPager;
import com.github.florent37.materialviewpager.header.HeaderDesign;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import au.com.codeka.weather.model.CurrentCondition;
import au.com.codeka.weather.model.WeatherInfo;

public class WeatherActivity extends AppCompatActivity {
  private static final String TAG = "codeka.weather";

  private DrawerLayout drawer;
  private ActionBarDrawerToggle drawerToggle;
  private Toolbar toolbar;
  private MaterialViewPager viewPager;
  private WeatherPagerAdapter pagerAdapter;
  private WeatherPagerListener pagerListener;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.weather_activity);
    setTitle("");

    // Instantiate a ViewPager and a PagerAdapter.
    viewPager = (MaterialViewPager) findViewById(R.id.view_pager);
    drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

    toolbar = viewPager.getToolbar();
    drawerToggle = new ActionBarDrawerToggle(this, drawer, 0, 0);

    setSupportActionBar(toolbar);
    final ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setDisplayShowHomeEnabled(true);
      actionBar.setDisplayShowTitleEnabled(true);
      actionBar.setDisplayUseLogoEnabled(false);
      actionBar.setHomeButtonEnabled(true);
    }

    drawer.setDrawerListener(drawerToggle);

    pagerAdapter = new WeatherPagerAdapter(getSupportFragmentManager());
    pagerListener = new WeatherPagerListener();
    viewPager.getViewPager().setAdapter(pagerAdapter);
    viewPager.setMaterialViewPagerListener(pagerListener);
    viewPager.getPagerTitleStrip().setViewPager(viewPager.getViewPager());

  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    drawerToggle.syncState();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.weather_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.refresh:
      refreshWeather();
      return true;
    case R.id.debug:
      showDebugActivity();
      return true;
    case R.id.activity_log:
      showDebugLogActivity();
      return true;
    default:
      return drawerToggle.onOptionsItemSelected(item) ||
          super.onOptionsItemSelected(item);
    }
  }

  private void refreshWeather() {
    WeatherManager.i.refreshWeather(this, true);
  }

  private void showDebugActivity() {
    startActivity(new Intent(this, DebugActivity.class));
  }

  private void showDebugLogActivity() {
    startActivity(new Intent(this, DebugLogActivity.class));
  }

  /**
   * A simple pager adapter that represents 5 WeatherDetailsFragment objects, in
   * sequence.
   */
  private class WeatherPagerAdapter extends FragmentStatePagerAdapter {
    public WeatherPagerAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      switch (position) {
        case 0:
          return new WeatherDetailsFragment();
        case 1:
          return new WeatherMapFragment();
        default:
          throw new RuntimeException("Unexpected position: " + position);
      }
    }

    @Override
    public int getCount() {
      return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(WeatherActivity.this);

      switch (position) {
        case 0:
          if (weatherInfo == null || weatherInfo.getGeocodeInfo() == null) {
            return "Your location";
          }
          return weatherInfo.getGeocodeInfo().getShortName();
        case 1:
          return "Map";
      }
      return "";
    }
  }

  private class WeatherPagerListener implements MaterialViewPager.Listener {
    @Override
    public HeaderDesign getHeaderDesign(int page) {
      WeatherInfo weatherInfo = WeatherManager.i.getCurrentWeather(WeatherActivity.this);
      if (weatherInfo == null) {
        // TODO: refresh once it's loaded.
        return null;
      }

      // a bit hacky...
      int hour = new GregorianCalendar().get(Calendar.HOUR_OF_DAY);
      boolean isNight = hour < 6 || hour > 20;

      CurrentCondition currentCondition = weatherInfo.getCurrentCondition();

      String assetName = currentCondition.getIcon().getHeaderAssetName(isNight);
      Bitmap bitmap;
      try {
        bitmap = BitmapFactory.decodeStream(getAssets().open(assetName));
      } catch (IOException e) {
        Log.e(TAG, "Should never happen!", e);
        return null;
      }

      // TODO: don't do this on a UI thread
      int color = Color.BLACK;
      Palette.Swatch swatch = Palette.from(bitmap).generate().getVibrantSwatch();
      if (swatch != null) {
        color = swatch.getRgb();
      }

      switch (page) {
        case 0:
          ((TextView) viewPager.findViewById(R.id.header)).setText(String.format(
              "%d °C %s",
              Math.round(currentCondition.getTemperature()),
              currentCondition.getDescription()));

          return HeaderDesign.fromColorAndDrawable(
              color, new BitmapDrawable(getResources(), bitmap));
        case 1:
          ((TextView) viewPager.findViewById(R.id.header)).setText("Weather Map");

          return HeaderDesign.fromColorAndDrawable(
              color, new BitmapDrawable(getResources(), bitmap));
      }

      return null;
    }
  }
}
