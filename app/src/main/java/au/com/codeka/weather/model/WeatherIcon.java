package au.com.codeka.weather.model;

import au.com.codeka.weather.R;

/**
 * Enumeration of all the weather icons we support.
 */
public enum WeatherIcon {
  STORM(R.drawable.weather_storm, R.drawable.weather_storm_small),
  LIGHT_RAIN(R.drawable.weather_lightrain, R.drawable.weather_lightrain_small),
  MEDIUM_RAIN(R.drawable.weather_mediumrain, R.drawable.weather_mediumrain_small),
  HEAVY_RAIN(R.drawable.weather_heavyrain, R.drawable.weather_heavyrain_small),
  SEVERE(R.drawable.weather_severe, R.drawable.weather_severe_small),
  SLEET(R.drawable.weather_sleet, R.drawable.weather_sleet_small),
  SNOW(R.drawable.weather_snow, R.drawable.weather_snow_small),
  HAZE(R.drawable.weather_hazy, R.drawable.weather_hazy_small),
  FOG(R.drawable.weather_fog, R.drawable.weather_fog_small),
  CLEAR(R.drawable.weather_clear, R.drawable.weather_clear_small, R.drawable.weather_nt_clear, R.drawable.weather_nt_clear_small),
  PARTLY_CLOUDY(R.drawable.weather_partlycloudy, R.drawable.weather_partlycloudy_small, R.drawable.weather_nt_partlycloudy, R.drawable.weather_nt_partlycloudy_small),
  MOSTLY_CLOUDY(R.drawable.weather_mostlycloudy, R.drawable.weather_mostlycloudy_small, R.drawable.weather_nt_mostlycloudy, R.drawable.weather_nt_mostlycloudy_small),
  CLOUDY(R.drawable.weather_cloudy, R.drawable.weather_cloudy_small);

  private int largeIconId;
  private int smallIconId;
  private int largeNightIconId;
  private int smallNightIconId;

  WeatherIcon(int largeIconId, int smallIconId) {
    this.largeIconId = largeIconId;
    this.smallIconId = smallIconId;
    this.largeNightIconId = largeIconId;
    this.smallNightIconId = smallIconId;
  }

  WeatherIcon(int largeIconId, int smallIconId, int largeNightIconId, int smallNightIconId) {
    this.largeIconId = largeIconId;
    this.smallIconId = smallIconId;
    this.largeNightIconId = largeNightIconId;
    this.smallNightIconId = smallNightIconId;
  }

  public int getLargeIconId(boolean isNight) {
    return isNight ? largeNightIconId : largeIconId;
  }

  public int getSmallIconId(boolean isNight) {
    return isNight ? smallNightIconId : smallIconId;
  }
}
