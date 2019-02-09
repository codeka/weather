package au.com.codeka.weather.model;

import androidx.annotation.Nullable;

import java.util.Date;

/**
 * POJO that represents the "current" conditions.
 */
public class CurrentCondition {
  private String observationLocation;
  private Date observationTime;
  @Nullable private Double temperature;
  @Nullable private Double feelsLike;
  private double precipitationLastHour;
  private double precipitationToday;
  @Nullable private Double relativeHumidity;
  private String description;
  private WeatherIcon icon;

  public String getObservationLocation() {
    return observationLocation;
  }

  public Date getObservationTime() {
    return observationTime;
  }

  @Nullable
  public Double getTemperature() {
    return temperature;
  }

  @Nullable
  public Double getFeelsLike() {
    return feelsLike;
  }

  public double getPrecipitationLastHour() {
    return precipitationLastHour;
  }

  public double getPrecipitationToday() {
    return precipitationToday;
  }

  @Nullable
  public Double getRelativeHumidity() {
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

    public Builder setTemperature(@Nullable Double temp) {
      currentCondition.temperature = temp;
      return this;
    }

    public Builder setFeelsLike(@Nullable Double temp) {
      currentCondition.feelsLike = temp;
      return this;
    }

    public Builder setPrecipitation(@Nullable Double lastHour, @Nullable Double today) {
      currentCondition.precipitationLastHour = (lastHour == null ? 0 : lastHour);
      currentCondition.precipitationToday = (today == null ? 0 : today);
      return this;
    }

    public Builder setRelativeHumidity(@Nullable Double percent) {
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
