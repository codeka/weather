package au.com.codeka.weather;

import android.util.Log;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

/**
 * A more lenient {@link TypeAdapter} for double values, which returns 0 when there's an error
 * parsing the value. Some APIs are kinda dumb and put strings in their JSON to indicate "no value".
 */
public class LenientDoubleTypeAdapter extends TypeAdapter<Double> {
  @Override
  public Double read(JsonReader reader) throws IOException {
    if(reader.peek() == JsonToken.NULL){
      reader.nextNull();
      return null;
    }
    String stringValue = reader.nextString();
    try{
      return Double.valueOf(stringValue);
    }catch(NumberFormatException e){
      return null;
    }
  }

  @Override
  public void write(JsonWriter writer, Double value) throws IOException {
    if (value == null) {
      writer.nullValue();
      return;
    }
    writer.value(value);
  }
}