package au.com.codeka.weather.location;

/** POJO object that represents the results of a Google Maps reverse-Geocode. */
@SuppressWarnings("unused") // a bunch of these, which we can ignore for now.
public class GeocodeInfo {
  private SingleResult[] results;
  private String status;

  public SingleResult[] getResults() {
    return results;
  }

  public SingleResult findResult(String type) {
    for (int i = 0; i < results.length; i++) {
      if (results[i].isType(type)) {
        return results[i];
      }
    }
    return null;
  }

  public String getLongName() {
    GeocodeInfo.SingleResult longResult = findResult("route");
    if (longResult == null) {
      longResult = findResult("street_address");
    }
    if (longResult != null) {
      return longResult.getFormattedAddress();
    }

    return null;
  }

  public String getShortName() {
    GeocodeInfo.SingleResult shortResult = findResult("locality");
    if (shortResult != null) {
      GeocodeInfo.AddressComponent[] addrComponents = shortResult.getAddressComponents();
      String shortName = addrComponents[0].getShortName();
      if (addrComponents.length > 1) {
        shortName += ", " + addrComponents[1].getShortName();
      }
      return shortName;
    }

    return null;
  }

  public String getStatus() {
    return status;
  }

  @Override
  public String toString() {
    return getLongName();
  }

  public static class SingleResult {
    private AddressComponent[] addressComponents;
    private String formattedAddress;
    // geometry is ignored, we don't care
    private String[] types;

    public AddressComponent[] getAddressComponents() {
      return addressComponents;
    }

    public String getFormattedAddress() {
      return formattedAddress;
    }

    public String[] getTypes() {
      return types;
    }

    public boolean isType(String type) {
      for (int i = 0; i < types.length; i++) {
        if (types[0].equalsIgnoreCase(type)) {
          return true;
        }
      }
      return false;
    }
  }

  public static class AddressComponent {
    private String longName;
    private String shortName;
    private String[] types;

    public String getLongName() {
      return longName;
    }

    public String getShortName() {
      return shortName;
    }

    public String[] getTypes() {
      return types;
    }
  }
}
