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
  private static EntryBuilder mCurrentEntryBuilder;

  public static EntryBuilder current() {
    if (mCurrentEntryBuilder == null) {
      mCurrentEntryBuilder = new EntryBuilder();
    }
    return mCurrentEntryBuilder;
  }

  public static void saveCurrent(Context context) {
    Entry entry = current().build();
    mCurrentEntryBuilder = null;

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
    private long mTimestamp;
    private long mMillisToNextAlarm;
    private double mLat;
    private double mLng;
    private LogEntry[] mLogs;

    public long getTimestamp() {
      return mTimestamp;
    }

    public boolean hasLocation() {
      return mLat != 0.0f && mLng != 0.0f;
    }

    public String getMapLink() {
      return String.format(Locale.ENGLISH, "<a href=\"https://www.google.com.au/maps/preview/@%f,%f,16z\">%f,%f</a>",
          mLat, mLng, mLat, mLng);
    }

    public LogEntry[] getLogs() {
      return mLogs;
    }

    public long getMillisToNextAlarm() {
      return mMillisToNextAlarm;
    }
  }

  public static class LogEntry {
    private long mTimestamp;
    private String mMessage;

    public long getTimestamp() {
      return mTimestamp;
    }

    public String getMessage() {
      return mMessage;
    }
  }

  public static class EntryBuilder {
    private Entry mEntry;

    private EntryBuilder() {
      mEntry = new Entry();
      mEntry.mTimestamp = System.currentTimeMillis();
    }

    public EntryBuilder log(String msg) {
      if (mEntry.mLogs == null) {
        mEntry.mLogs = new LogEntry[1];
      } else {
        LogEntry[] newEntries = new LogEntry[mEntry.mLogs.length + 1];
        for (int i = 0; i < mEntry.mLogs.length; i++) {
          newEntries[i] = mEntry.mLogs[i];
        }
        mEntry.mLogs = newEntries;
      }
      LogEntry logEntry = new LogEntry();
      logEntry.mTimestamp = System.currentTimeMillis();
      logEntry.mMessage = msg;
      mEntry.mLogs[mEntry.mLogs.length - 1] = logEntry;
      return this;
    }

    public EntryBuilder setLocation(double lat, double lng) {
      mEntry.mLat = lat;
      mEntry.mLng = lng;
      return this;
    }

    public EntryBuilder setMillisToNextAlarm(long ms) {
      mEntry.mMillisToNextAlarm = ms;
      return this;
    }

    private Entry build() {
      return mEntry;
    }
  }
}
