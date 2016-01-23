package au.com.codeka.weather.model;

import au.com.codeka.weather.R;

/**
 * Enumeration of all the weather icons we support.
 */
public enum WeatherIcon {
  STORM(
      R.drawable.weather_storm,
      R.drawable.weather_storm_small,
      "partly-cloudy.jpg"),
  LIGHT_RAIN(
      R.drawable.weather_lightrain,
      R.drawable.weather_lightrain_small,
      "partly-cloudy.jpg"),
  MEDIUM_RAIN(
      R.drawable.weather_mediumrain,
      R.drawable.weather_mediumrain_small,
      "partly-cloudy.jpg"),
  HEAVY_RAIN(
      R.drawable.weather_heavyrain,
      R.drawable.weather_heavyrain_small,
      "partly-cloudy.jpg"),
  SEVERE(
      R.drawable.weather_severe,
      R.drawable.weather_severe_small,
      "partly-cloudy.jpg"),
  SLEET(
      R.drawable.weather_sleet,
      R.drawable.weather_sleet_small,
      "partly-cloudy.jpg"),
  SNOW(
      R.drawable.weather_snow,
      R.drawable.weather_snow_small,
      "partly-cloudy.jpg"),
  HAZE(
      R.drawable.weather_hazy,
      R.drawable.weather_hazy_small,
      "partly-cloudy.jpg"),
  FOG(
      R.drawable.weather_fog,
      R.drawable.weather_fog_small,
      "partly-cloudy.jpg"),
  CLEAR(
      R.drawable.weather_clear,
      R.drawable.weather_clear_small,
      "partly-cloudy.jpg",
      R.drawable.weather_nt_clear,
      R.drawable.weather_nt_clear_small,
      "partly-cloudy.jpg"),
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
      "partly-cloudy.jpg",
      R.drawable.weather_nt_mostlycloudy,
      R.drawable.weather_nt_mostlycloudy_small,
      "partly-cloudy.jpg"),
  CLOUDY(
      R.drawable.weather_cloudy,
      R.drawable.weather_cloudy_small,
      "partly-cloudy.jpg");

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
