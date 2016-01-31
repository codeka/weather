package au.com.codeka.weather.model;

/**
 * Like {@link Forecast}, but for an hourly forecast, slightly simplified.
 */
public class HourlyForecast {
  private int hour;
  private String shortDescription;
  private double temperature;
  private WeatherIcon icon;
  private double qpfMillimeters;

  /**
   * Gets the "hour" of this forecast, in 24 hour time. We expect it to be the current day or
   * tomorrow if it passes midnight.
   */
  public int getHour() {
    return hour;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getQpfMillimeters() {
    return qpfMillimeters;
  }

  public WeatherIcon getIcon() {
    return icon;
  }

  public static class Builder {
    private HourlyForecast forecast;

    public Builder() {
      forecast = new HourlyForecast();
    }

    public Builder setHour(int hour) {
      forecast.hour = hour;
      return this;
    }

    public Builder setShortDescription(String shortDescription) {
      forecast.shortDescription = shortDescription;
      return this;
    }

    public Builder setQpfMillimeters(double qpf) {
      forecast.qpfMillimeters = qpf;
      return this;
    }

    public Builder setTemperature(double temp) {
      forecast.temperature = temp;
      return this;
    }

    public Builder setIcon(WeatherIcon icon) {
      forecast.icon = icon;
      return this;
    }

    public HourlyForecast build() {
      return forecast;
    }
  }
}
