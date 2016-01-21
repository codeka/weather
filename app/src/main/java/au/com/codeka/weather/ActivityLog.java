package au.com.codeka.weather;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/** For debugging purposes, we keep a log of everything we do. */
public class ActivityLog {
  private static final String TAG = ActivityLog.class.getSimpleName();
  private static EntryBuilder currentEntryBuilder;

  public static EntryBuilder current() {
    if (currentEntryBuilder == null) {
      currentEntryBuilder = new EntryBuilder();
    }
    return currentEntryBuilder;
  }

  public static void saveCurrent(Context context) {
    Entry entry = current().build();
    currentEntryBuilder = null;

    File cacheDir = new File(context.getCacheDir(), "logs");
    cacheDir.mkdirs();
    File cacheFileName = new File(cacheDir, entry.getTimestamp() + ".json");

    Gson gson = new GsonBuilder().create();
    try {
      FileWriter writer = new FileWriter(cacheFileName);
      gson.toJson(entry, writer);
      writer.close();
    } catch (IOException e) {
      Log.e(TAG, "Error writing entry.", e);
    }
  }

  /** Loads the activity log from disk. */
  public static List<Entry> load(Context context) {
    ArrayList<Entry> entries = new ArrayList<Entry>();
    Gson gson = new GsonBuilder().create();

    long minTimestamp = System.currentTimeMillis() - (1000 * 60 * 1440);
    File logDir = new File(context.getCacheDir(), "logs");
    if (logDir.listFiles() == null) {
      return entries;
    }
    for (File f : logDir.listFiles()) {
      long timestamp = Long.parseLong(f.getName().replaceFirst("[.][^.]+$", ""));
      if (timestamp < minTimestamp) {
        f.delete();
        continue;
      }

      try {
        Entry entry = gson.fromJson(new InputStreamReader(new FileInputStream(f)), Entry.class);
        if (entry == null) {
          throw new Exception("entry was null!");
        }
        if (entry.getLogs() == null) {
          Log.i(TAG, "Log " + f.getName() + " was empty, not loading.");
          continue;
        }
        entries.add(entry);
      } catch (Exception e) {
        Log.e(TAG, "Error loading entry.", e);
      }
    }

    Collections.reverse(entries);
    return entries;
  }

  /** An entry in the activity log is basically everything that happens in response to the alarm. */
  public static class Entry {
    private long timestamp;
    private long millisToNextAlarm;
    private double lat;
    private double lng;
    private LogEntry[] logs;

    public long getTimestamp() {
      return timestamp;
    }

    public boolean hasLocation() {
      return lat != 0.0f && lng != 0.0f;
    }

    public String getMapLink() {
      return String.format(Locale.ENGLISH,
          "<a href=\"https://www.google.com.au/maps/preview/@%f,%f,16z\">%f,%f</a>",
          lat, lng, lat, lng);
    }

    public LogEntry[] getLogs() {
      return logs;
    }

    public long getMillisToNextAlarm() {
      return millisToNextAlarm;
    }
  }

  public static class LogEntry {
    private long timestamp;
    private String message;

    public long getTimestamp() {
      return timestamp;
    }

    public String getMessage() {
      return message;
    }
  }

  public static class EntryBuilder {
    private Entry entry;

    private EntryBuilder() {
      entry = new Entry();
      entry.timestamp = System.currentTimeMillis();
    }

    public EntryBuilder log(String msg) {
      if (entry.logs == null) {
        entry.logs = new LogEntry[1];
      } else {
        LogEntry[] newEntries = new LogEntry[entry.logs.length + 1];
        for (int i = 0; i < entry.logs.length; i++) {
          newEntries[i] = entry.logs[i];
        }
        entry.logs = newEntries;
      }
      LogEntry logEntry = new LogEntry();
      logEntry.timestamp = System.currentTimeMillis();
      logEntry.message = msg;
      entry.logs[entry.logs.length - 1] = logEntry;
      return this;
    }

    public EntryBuilder setLocation(double lat, double lng) {
      entry.lat = lat;
      entry.lng = lng;
      return this;
    }

    public EntryBuilder setMillisToNextAlarm(long ms) {
      entry.millisToNextAlarm = ms;
      return this;
    }

    private Entry build() {
      return entry;
    }
  }
}
