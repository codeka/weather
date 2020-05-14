package au.com.codeka.weather

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * A more lenient [TypeAdapter] for double values, which returns 0 when there's an error
 * parsing the value. Some APIs are kinda dumb and put strings in their JSON to indicate "no value".
 */
class LenientDoubleTypeAdapter : TypeAdapter<Double?>() {
  override fun read(reader: JsonReader): Double? {
    if (reader.peek() == JsonToken.NULL) {
      reader.nextNull()
      return null
    }
    val stringValue = reader.nextString()
    return try {
      java.lang.Double.valueOf(stringValue)
    } catch (e: NumberFormatException) {
      null
    }
  }

  override fun write(writer: JsonWriter, value: Double?) {
    if (value == null) {
      writer.nullValue()
      return
    }
    writer.value(value)
  }
}