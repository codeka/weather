package au.com.codeka.weather

import android.content.Context
import android.util.Log
import com.google.gson.GsonBuilder
import java.io.*
import java.util.*

/** For debugging purposes, we keep a log of everything we do.  */
object DebugLog {
  private val TAG = DebugLog::class.java.simpleName
  private var currentEntryBuilder: EntryBuilder? = null

  fun current(): EntryBuilder {
    if (currentEntryBuilder == null) {
      currentEntryBuilder = EntryBuilder()
    }
    return currentEntryBuilder!!
  }

  /** Save the current entry and create a new one. */
  fun saveCurrent(context: Context) {
    val entry = current().build()
    currentEntryBuilder = null
    val cacheDir = File(context.cacheDir, "logs")
    cacheDir.mkdirs()
    val cacheFileName = File(cacheDir, entry.timestamp.toString() + ".json")
    val gson = GsonBuilder().create()
    try {
      val writer = FileWriter(cacheFileName)
      gson.toJson(entry, writer)
      writer.close()
    } catch (e: IOException) {
      Log.e(TAG, "Error writing entry.", e)
    }
  }

  /** Loads the activity log from disk.  */
  fun load(context: Context): ArrayList<Entry> {
    val entries = ArrayList<Entry>()
    val gson = GsonBuilder().create()
    val minTimestamp = System.currentTimeMillis() - 1000 * 60 * 1440
    val logDir = File(context.cacheDir, "logs")
    if (logDir.listFiles() == null) {
      return entries
    }
    for (f in logDir.listFiles()) {
      val timestamp = f.name.replaceFirst("[.][^.]+$".toRegex(), "").toLong()
      if (timestamp < minTimestamp) {
        f.delete()
        continue
      }
      try {
        val entry = gson.fromJson(InputStreamReader(FileInputStream(f)), Entry::class.java)
            ?: throw Exception("entry was null!")
        entries.add(entry)
      } catch (e: Exception) {
        Log.e(TAG, "Error loading entry.", e)
      }
    }
    entries.reverse()
    return entries
  }

  /** An entry in the activity log is basically everything that happens in response to the alarm. */
  class Entry {
    var timestamp: Long = 0
    var millisToNextAlarm: Long = 0
    var lat = 0.0
    var lng = 0.0
    var logs: ArrayList<LogEntry> = ArrayList()

    fun hasLocation(): Boolean {
      return lat != 0.0 && lng != 0.0
    }

    val mapLink: String
      get() = String.format(Locale.ENGLISH,
          "<a href=\"https://www.google.com.au/maps/preview/@%f,%f,16z\">%f,%f</a>",
          lat, lng, lat, lng)
  }

  data class LogEntry(val timestamp: Long, val message: String)

  class EntryBuilder {
    private val entry: Entry = Entry()

    fun log(msg: String): EntryBuilder {
      entry.logs.add(LogEntry(System.currentTimeMillis(), msg))
      return this
    }

    fun setLocation(lat: Double, lng: Double): EntryBuilder {
      entry.lat = lat
      entry.lng = lng
      return this
    }

    fun setMillisToNextAlarm(ms: Long): EntryBuilder {
      entry.millisToNextAlarm = ms
      return this
    }

    fun build(): Entry {
      return entry
    }

    init {
      entry.timestamp = System.currentTimeMillis()
    }
  }
}