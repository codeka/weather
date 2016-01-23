package au.com.codeka.weather.model;

import au.com.codeka.weather.R;

/**
 * Enumeration of all the weather icons we support.
 */
public enum WeatherIcon {
  STORM(
      R.drawable.weather_storm,
      R.drawable.weather_storm_small,
      "storm.jpg"),
  LIGHT_RAIN(
      R.drawable.weather_lightrain,
      R.drawable.weather_lightrain_small,
      "light-rain.jpg"),
  MEDIUM_RAIN(
      R.drawable.weather_mediumrain,
      R.drawable.weather_mediumrain_small,
      "heavy-rain.jpg"),
  HEAVY_RAIN(
      R.drawable.weather_heavyrain,
      R.drawable.weather_heavyrain_small,
      "heavy-rain.jpg"),
  SEVERE(
      R.drawable.weather_severe,
      R.drawable.weather_severe_small,
      "severe.jpg"),
  SLEET(
      R.drawable.weather_sleet,
      R.drawable.weather_sleet_small,
      "sleet.jpg"),
  SNOW(
      R.drawable.weather_snow,
      R.drawable.weather_snow_small,
      "snow.jpg"),
  HAZE(
      R.drawable.weather_hazy,
      R.drawable.weather_hazy_small,
      "haze.jpg"),
  FOG(
      R.drawable.weather_fog,
      R.drawable.weather_fog_small,
      "fog.jpg"),
  CLEAR(
      R.drawable.weather_clear,
      R.drawable.weather_clear_small,
      "clear.jpg",
      R.drawable.weather_nt_clear,
      R.drawable.weather_nt_clear_small,
      "clear-night.jpg"),
  PARTLY_CLOUDY(
      R.drawable.weather_partlycloudy,
      R.drawable.weather_partlycloudy_small,
      "partly-cloudy.jpg",
      R.drawable.weather_nt_partlycloudy,
      R.drawable.weather_nt_partlycloudy_small,
      "partly-cloudy-night.jpg"),
  MOSTLY_CLOUDY(
      R.drawable.weather_mostlycloudy,
      R.drawable.weather_mostlycloudy_small,
      "mostly-cloudy.jpg",
      R.drawable.weather_nt_mostlycloudy,
      R.drawable.weather_nt_mostlycloudy_small,
      "mostly-cloudy-night.jpg"),
  CLOUDY(
      R.drawable.weather_cloudy,
      R.drawable.weather_cloudy_small,
      "cloudy.jpg");

  private int largeIconId;
  private int smallIconId;
  private int largeNightIconId;
  private int smallNightIconId;
  private String headerAssetName;
  private String headerNightAssetName;

  WeatherIcon(int largeIconId, int smallIconId, String headerAssetName) {
    this.largeIconId = largeIconId;
    this.smallIconId = smallIconId;
    this.headerAssetName = headerAssetName;
    this.largeNightIconId = largeIconId;
    this.smallNightIconId = smallIconId;
    this.headerNightAssetName = headerAssetName;
  }

  WeatherIcon(
      int largeIconId,
      int smallIconId,
      String headerAssetName,
      int largeNightIconId,
      int smallNightIconId,
      String headerNightAssetName) {
    this.largeIconId = largeIconId;
    this.smallIconId = smallIconId;
    this.headerAssetName = headerAssetName;
    this.largeNightIconId = largeNightIconId;
    this.smallNightIconId = smallNightIconId;
    this.headerNightAssetName = headerNightAssetName;
  }

  public int getLargeIconId(boolean isNight) {
    return isNight ? largeNightIconId : largeIconId;
  }

  public int getSmallIconId(boolean isNight) {
    return isNight ? smallNightIconId : smallIconId;
  }

  public String getHeaderAssetName(boolean isNight) {
    return isNight ? headerNightAssetName : headerAssetName;
  }
}
