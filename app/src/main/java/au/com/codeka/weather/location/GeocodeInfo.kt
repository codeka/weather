package au.com.codeka.weather.location

/** POKO object that represents the results of a Google Maps reverse-Geocode.  */
// a bunch of these, which we can ignore for now.
class GeocodeInfo {
  lateinit var results: Array<SingleResult>
  val status: String? = null

  fun findResult(type: String?): SingleResult? {
    for (i in results.indices) {
      if (results[i].isType(type)) {
        return results[i]
      }
    }
    return null
  }

  val longName: String?
    get() {
      var longResult = findResult("route")
      if (longResult == null) {
        longResult = findResult("street_address")
      }
      return longResult?.formattedAddress
    }

  val shortName: String?
    get() {
      val shortResult = findResult("locality")
      if (shortResult != null) {
        val addrComponents = shortResult.addressComponents
        var shortName = addrComponents[0].shortName
        if (addrComponents.size > 1) {
          shortName += ", " + addrComponents[1].shortName
        }
        return shortName
      }
      return null
    }

  override fun toString(): String {
    return longName!!
  }

  class SingleResult {
    lateinit var addressComponents: Array<AddressComponent>
    var formattedAddress: String? = null

    // geometry is ignored, we don't care
    lateinit var types: Array<String>

    fun isType(type: String?): Boolean {
      for (i in types.indices) {
        if (types[0].equals(type, ignoreCase = true)) {
          return true
        }
      }
      return false
    }
  }

  class AddressComponent {
    val longName: String? = null
    val shortName: String? = null
    lateinit var types: Array<String>

  }
}