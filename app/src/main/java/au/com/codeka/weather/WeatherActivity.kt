package au.com.codeka.weather

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.palette.graphics.Palette
import com.github.florent37.materialviewpager.MaterialViewPager
import com.github.florent37.materialviewpager.header.HeaderDesign
import java.io.IOException
import java.util.*
import kotlin.math.roundToInt

class WeatherActivity : AppCompatActivity() {
  private lateinit var drawer: DrawerLayout
  private lateinit var drawerToggle: ActionBarDrawerToggle
  private lateinit var toolbar: Toolbar
  private lateinit var viewPager: MaterialViewPager
  private lateinit var pagerAdapter: WeatherPagerAdapter
  private lateinit var pagerListener: WeatherPagerListener

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.weather_activity)
    title = ""

    // Instantiate a ViewPager and a PagerAdapter.
    viewPager = findViewById<View>(R.id.view_pager) as MaterialViewPager
    drawer = findViewById<View>(R.id.drawer_layout) as DrawerLayout
    toolbar = viewPager.toolbar
    drawerToggle = ActionBarDrawerToggle(this, drawer, 0, 0)
    setSupportActionBar(toolbar)
    val actionBar = supportActionBar
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true)
      actionBar.setDisplayShowHomeEnabled(true)
      actionBar.setDisplayShowTitleEnabled(true)
      actionBar.setDisplayUseLogoEnabled(false)
      actionBar.setHomeButtonEnabled(true)
    }
    drawer.addDrawerListener(drawerToggle)
    pagerAdapter = WeatherPagerAdapter(supportFragmentManager)
    pagerListener = WeatherPagerListener()
    viewPager.viewPager.adapter = pagerAdapter
    viewPager.setMaterialViewPagerListener(pagerListener)
    viewPager.pagerTitleStrip.setViewPager(viewPager.viewPager)
    val weatherInfo = WeatherManager.i.getCurrentWeather(this@WeatherActivity)
    if (weatherInfo?.currentCondition == null) {
      WeatherManager.i.refreshWeather(this, true)
    }
  }

  public override fun onResume() {
    super.onResume()
  }

  override fun onPostCreate(savedInstanceState: Bundle?) {
    super.onPostCreate(savedInstanceState)
    drawerToggle.syncState()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    val inflater = menuInflater
    inflater.inflate(R.menu.weather_activity_actions, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    return when (item.itemId) {
      R.id.refresh -> {
        refreshWeather()
        true
      }
      R.id.activity_log -> {
        showDebugLogActivity()
        true
      }
      else -> drawerToggle.onOptionsItemSelected(item) ||
          super.onOptionsItemSelected(item)
    }
  }

  private fun refreshWeather() {
//     WeatherScheduler.scheduleNow(this)
    WeatherManager.i.refreshWeather(this, true)
  }

  private fun showDebugLogActivity() {
    startActivity(Intent(this, DebugLogActivity::class.java))
  }

  /**
   * A simple pager adapter that represents 5 WeatherDetailsFragment objects, in
   * sequence.
   */
  private inner class WeatherPagerAdapter(fm: FragmentManager?) : FragmentStatePagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
      return when (position) {
        0 -> WeatherDetailsFragment()
        1 -> WeatherMapFragment()
        else -> throw RuntimeException("Unexpected position: $position")
      }
    }

    override fun getCount(): Int {
      return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
      val weatherInfo = WeatherManager.i.getCurrentWeather(this@WeatherActivity)
      when (position) {
        0 -> {
          return if (weatherInfo?.geocodeInfo == null) {
            "Your location"
          } else weatherInfo.geocodeInfo?.shortName
        }
        1 -> return "Map"
      }
      return ""
    }
  }

  private inner class WeatherPagerListener : MaterialViewPager.Listener {
    override fun getHeaderDesign(page: Int): HeaderDesign? {
      val weatherInfo = WeatherManager.i.getCurrentWeather(this@WeatherActivity)
          ?: // TODO: refresh once it's loaded.
          return null
      val currentCondition = weatherInfo.currentCondition
          ?: // TODO: refresh once it's loaded.
          return null

      // a bit hacky...
      val hour = GregorianCalendar()[Calendar.HOUR_OF_DAY]
      val isNight = hour < 6 || hour > 20
      val assetName = currentCondition.icon?.getHeaderAssetName(isNight) ?: return null
      val bitmap: Bitmap
      bitmap = try {
        BitmapFactory.decodeStream(assets.open(assetName))
      } catch (e: IOException) {
        Log.e(TAG, "Should never happen!", e)
        return null
      }

      // TODO: don't do this on a UI thread
      var color = Color.BLACK
      val swatch = Palette.from(bitmap).generate().vibrantSwatch
      if (swatch != null) {
        color = swatch.rgb
      }
      when (page) {
        0 -> {
          (viewPager.findViewById<View>(R.id.header) as TextView).text = String.format(
              Locale.ENGLISH,
              "%d °C %s",
              (currentCondition.temperature ?: 0.0).roundToInt(),
              currentCondition.description)
          return HeaderDesign.fromColorAndDrawable(
              color, BitmapDrawable(resources, bitmap))
        }
        1 -> {
          (viewPager.findViewById<View>(R.id.header) as TextView).text = "Weather Map"
          return HeaderDesign.fromColorAndDrawable(
              color, BitmapDrawable(resources, bitmap))
        }
      }
      return null
    }
  }

  companion object {
    private const val TAG = "codeka.weather"
  }
}