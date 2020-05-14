package au.com.codeka.weather.location

import android.location.Address

class GeocodeInfo {
  private val addresses = ArrayList<GeocodeAddress>()

  constructor(result: List<Address>) {
    for (addr in result) {
      addresses.add(GeocodeAddress(addr))
    }
  }

  val longName: String?
    get() = "${addresses[0].featureName} ${addresses[0].thoroughfare}, ${addresses[0].locality}"

  val shortName: String?
    get() = addresses[0].locality

  override fun toString(): String {
    return longName ?: "??"
  }

  private class GeocodeAddress(addr: Address) {
    var adminArea = addr.adminArea
    var countryCode = addr.countryCode
    var countryName = addr.countryName
    var featureName = addr.featureName
    var locale = addr.locale
    var locality = addr.locality
    var phone = addr.phone
    var postalCode = addr.postalCode
    var premises = addr.premises
    var subAdminArea = addr.subAdminArea
    var subLocality = addr.subLocality
    var subThoroughfare = addr.subThoroughfare
    var thoroughfare = addr.thoroughfare
  }
}
