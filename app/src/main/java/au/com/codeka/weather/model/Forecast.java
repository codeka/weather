package au.com.codeka.weather.model;

/**
 * POJO that represents a "forecast".
 */
public class Forecast {
  private int offset;
  private String description;
  private String shortDescription;
  private double highTemperature;
  private double lowTemperature;
  private WeatherIcon icon;

  /**
   * Gets the "offset" of this forecast. 0 means it's todays, 1 means tomorrow, 2 the day after etc.
   */
  public int getOffset() {
    return offset;
  }

  public String getDescription() {
    return description;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public double getHighTemperature() {
    return highTemperature;
  }

  public double getLowTemperature() {
    return lowTemperature;
  }

  public WeatherIcon getIcon() {
    return icon;
  }

  public static class Builder {
    private Forecast forecast;

    public Builder() {
      forecast = new Forecast();
    }

    public Builder setOffset(int offset) {
      forecast.offset = offset;
      return this;
    }

    public Builder setDescription(String description) {
      forecast.description = description;
      return this;
    }

    public Builder setShortDescription(String shortDescription) {
      forecast.shortDescription = shortDescription;
      return this;
    }

    public Builder setHighTemperature(double temp) {
      forecast.highTemperature = temp;
      return this;
    }

    public Builder setLowTemperature(double temp) {
      forecast.lowTemperature = temp;
      return this;
    }

    public Builder setIcon(WeatherIcon icon) {
      forecast.icon = icon;
      return this;
    }

    public Forecast build() {
      return forecast;
    }
  }
}
