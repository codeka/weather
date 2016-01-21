package au.com.codeka.weather.model;

import java.util.Date;

/**
 * POJO that represents the "current" conditions.
 */
public class CurrentCondition {
  private String observationLocation;
  private Date observationTime;
  private double temperature;
  private double feelsLike;
  private double precipitationLastHour;
  private double precipitationToday;
  private double relativeHumidity;
  private String description;
  private WeatherIcon icon;

  public String getObservationLocation() {
    return observationLocation;
  }

  public Date getObservationTime() {
    return observationTime;
  }

  public double getTemperature() {
    return temperature;
  }

  public double getFeelsLike() {
    return feelsLike;
  }

  public double getPrecipitationLastHour() {
    return precipitationLastHour;
  }

  public double getPrecipitationToday() {
    return precipitationToday;
  }

  public double getRelativeHumidity() {
    return relativeHumidity;
  }

  public String getDescription() {
    return description;
  }

  public WeatherIcon getIcon() {
    return icon;
  }

  public static class Builder {
    private CurrentCondition currentCondition;

    public Builder() {
      currentCondition = new CurrentCondition();
    }

    public Builder setObservationLocation(String location) {
      currentCondition.observationLocation = location;
      return this;
    }

    public Builder setObservationTime(Date dt) {
      currentCondition.observationTime = dt;
      return this;
    }

    public Builder setTemperature(double temp) {
      currentCondition.temperature = temp;
      return this;
    }

    public Builder setFeelsLike(double temp) {
      currentCondition.feelsLike = temp;
      return this;
    }

    public Builder setPrecipitation(double lastHour, double today) {
      currentCondition.precipitationLastHour = lastHour;
      currentCondition.precipitationToday = today;
      return this;
    }

    public Builder setRelativeHumidity(double percent) {
      currentCondition.relativeHumidity = percent;
      return this;
    }

    public Builder setDescription(String description) {
      currentCondition.description = description;
      return this;
    }

    public Builder setIcon(WeatherIcon icon) {
      currentCondition.icon = icon;
      return this;
    }

    public CurrentCondition build() {
      return currentCondition;
    }
  }
}
