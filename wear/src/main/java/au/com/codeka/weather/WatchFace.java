package au.com.codeka.weather;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.CalendarContract;
import androidx.core.app.ActivityCompat;
import android.support.wearable.provider.WearableCalendarContract;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Watch face for my watch.
 */
public class WatchFace extends CanvasWatchFaceService {
  private static final String TAG = "WatchFace";

  /**
   * Update rate in milliseconds for interactive mode. We update once a second since seconds are
   * displayed in interactive mode.
   */
  private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

  /**
   * Handler message id for updating the time periodically in interactive mode.
   */
  private static final int MSG_UPDATE_TIME = 0;

  @Override
  public Engine onCreateEngine() {
    return new WatchFaceEngine();
  }

  private static class EngineHandler extends Handler {
    private final WeakReference<WatchFaceEngine> weakReference;

    public EngineHandler(WatchFaceEngine reference) {
      weakReference = new WeakReference<>(reference);
    }

    @Override
    public void handleMessage(Message msg) {
      WatchFaceEngine engine = weakReference.get();
      if (engine != null) {
        switch (msg.what) {
          case MSG_UPDATE_TIME:
            engine.handleUpdateTimeMessage();
            break;
        }
      }
    }
  }

  private class WatchFaceEngine extends CanvasWatchFaceService.Engine {
    static final int MSG_LOAD_MEETINGS = 0;

    final Handler engineHandler = new EngineHandler(this);
    boolean registeredTimeZoneReceiver = false;
    Paint backgroundPaint;
    Paint textPaint;
    boolean ambient;
    Time time;
    final BroadcastReceiver timeZoneReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        time.clear(intent.getStringExtra("time-zone"));
        time.setToNow();
      }
    };
    int tapCount;
    Typeface typeface;

    /**
     * Whether the display supports fewer bits for each color in ambient mode. When true, we
     * disable anti-aliasing in ambient mode.
     */
    boolean lowBitAmbient;

    private boolean calendarChangedReceiverRegistered;
    private boolean calendarPermissionApproved;
    private String calendarNotApprovedMessage;
    private AsyncTask<Void, Void, ArrayList<EventDetails>> loadMeetingsTask;
    ArrayList<EventDetails> events = null;

    /** Handler to load the meetings once a minute in interactive mode. */
    final Handler loadMeetingsHandler = new Handler() {
      @Override
      public void handleMessage(Message message) {
        switch (message.what) {
          case MSG_LOAD_MEETINGS:
            cancelLoadMeetingTask();

            // Loads meetings.
            if (calendarPermissionApproved) {
              loadMeetingsTask = new LoadMeetingsTask();
              loadMeetingsTask.execute();
            }
            break;
        }
      }
    };

    @Override
    public void onCreate(SurfaceHolder holder) {
      super.onCreate(holder);

      typeface = Typeface.createFromAsset(getAssets(), "led-counter-7.regular.ttf");

      calendarPermissionApproved = ActivityCompat.checkSelfPermission(
          getApplicationContext(),
          Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;
      if (calendarPermissionApproved) {
        Log.w(TAG, "We've got the calendar permission.");
        loadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
      } else {
        Log.w(TAG, "We DON'T have the calendar permission.");
      }

      setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
          .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
          .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
          .setShowSystemUiTime(false)
          .setAcceptsTapEvents(true)
          .build());
      Resources resources = WatchFace.this.getResources();

      backgroundPaint = new Paint();
      backgroundPaint.setColor(resources.getColor(R.color.background));

      textPaint = new Paint();
      textPaint = createTextPaint(Color.WHITE);

      time = new Time();
    }

    private BroadcastReceiver calendarChangedReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_PROVIDER_CHANGED.equals(intent.getAction())
            && WearableCalendarContract.CONTENT_URI.equals(intent.getData())) {
          Log.i(TAG, "Calendar has changed, refreshing meeting list.");
          loadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
        }
      }
    };

    @Override
    public void onDestroy() {
      engineHandler.removeMessages(MSG_UPDATE_TIME);
      loadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
      super.onDestroy();
    }

    private Paint createTextPaint(int textColor) {
      Paint paint = new Paint();
      paint.setColor(textColor);
      paint.setAntiAlias(true);
      paint.setTypeface(typeface);
      return paint;
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
      super.onVisibilityChanged(visible);

      if (visible) {
        // Update time zone in case it changed while we weren't visible.
        time.clear(TimeZone.getDefault().getID());
        time.setToNow();

        if (!registeredTimeZoneReceiver) {
          IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
          registerReceiver(timeZoneReceiver, filter);
          registeredTimeZoneReceiver = true;
        }

        // Enables app to handle 23+ (M+) style permissions.
        calendarPermissionApproved = ActivityCompat.checkSelfPermission(
            getApplicationContext(),
            Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED;

        if (calendarPermissionApproved && !calendarChangedReceiverRegistered) {
          IntentFilter filter = new IntentFilter(Intent.ACTION_PROVIDER_CHANGED);
          filter.addDataScheme("content");
          filter.addDataAuthority(WearableCalendarContract.AUTHORITY, null);
          registerReceiver(calendarChangedReceiver, filter);
          calendarChangedReceiverRegistered = true;
        }

        loadMeetingsHandler.sendEmptyMessage(MSG_LOAD_MEETINGS);
      } else {
        if (registeredTimeZoneReceiver) {
          unregisterReceiver(timeZoneReceiver);
          registeredTimeZoneReceiver = false;
        }

        if (calendarChangedReceiverRegistered) {
          unregisterReceiver(calendarChangedReceiver);
          calendarChangedReceiverRegistered = false;
        }
        loadMeetingsHandler.removeMessages(MSG_LOAD_MEETINGS);
      }

      // Whether the timer should be running depends on whether we're visible (as well as
      // whether we're in ambient mode), so we may need to start or stop the timer.
      updateTimer();
    }

    @Override
    public void onApplyWindowInsets(WindowInsets insets) {
      super.onApplyWindowInsets(insets);

      // Load resources that have alternate values for round watches.
      Resources resources = WatchFace.this.getResources();
      boolean isRound = insets.isRound();
      float textSize = resources.getDimension(isRound
          ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

      textPaint.setTextSize(textSize);
    }

    @Override
    public void onPropertiesChanged(Bundle properties) {
      super.onPropertiesChanged(properties);
      lowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
    }

    @Override
    public void onTimeTick() {
      super.onTimeTick();
      invalidate();
    }

    @Override
    public void onAmbientModeChanged(boolean inAmbientMode) {
      super.onAmbientModeChanged(inAmbientMode);
      if (ambient != inAmbientMode) {
        ambient = inAmbientMode;
        if (lowBitAmbient) {
          textPaint.setAntiAlias(!inAmbientMode);
        }
        invalidate();
      }

      // Whether the timer should be running depends on whether we're visible (as well as
      // whether we're in ambient mode), so we may need to start or stop the timer.
      updateTimer();
    }

    /**
     * Captures tap event (and tap type) and toggles the background color if the user finishes
     * a tap.
     */
    @Override
    public void onTapCommand(int tapType, int x, int y, long eventTime) {
      Resources resources = WatchFace.this.getResources();
      switch (tapType) {
        case TAP_TYPE_TOUCH:
          // The user has started touching the screen.
          break;
        case TAP_TYPE_TOUCH_CANCEL:
          // The user has started a different gesture or otherwise cancelled the tap.
          break;
        case TAP_TYPE_TAP:
          // The user has completed the tap gesture.
          tapCount++;
          if (!calendarPermissionApproved) {
            Intent permissionIntent = new Intent(
                getApplicationContext(),
                WeatherActivity.class);
            permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(permissionIntent);
          } else {
            backgroundPaint.setColor(resources.getColor(tapCount % 2 == 0 ?
                R.color.background : R.color.background2));
          }
          break;
      }

      invalidate();
    }

    private void setTextColor(int colorRes) {
      if (isInAmbientMode()) {
        textPaint.setColor(Color.WHITE);
      } else {
        textPaint.setColor(getResources().getColor(colorRes));
      }
    }

    @Override
    public void onDraw(Canvas canvas, Rect bounds) {
      // Draw the background.
      if (isInAmbientMode()) {
        canvas.drawColor(Color.BLACK);
      } else {
        canvas.drawRect(0, 0, bounds.width(), bounds.height(), backgroundPaint);
      }

      // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
      time.setToNow();
      int hour = time.hour;
      boolean morning = true;
      if (hour == 0) {
        hour = 12;
      } else if (hour > 12) {
        hour -= 12;
        morning = false;
      } else if (hour == 12) {
        morning = false;
      }
      String hhmm = String.format("%d:%02d ", hour, time.minute);
      float hhmmWidth = textPaint.measureText(hhmm);
      float hhmmHeight = textPaint.getTextSize();
      float hhmmRealHeight = (int)(hhmmHeight * 0.75f);

      String ampm = morning ? "am" : "pm";
      textPaint.setTextSize(hhmmHeight * 0.5f);
      float ampmHeight = textPaint.measureText(ampm);

      textPaint.setTextSize((int)(hhmmHeight * 0.5f));
      String ss = String.format("%02d", time.second);
      float ssWidth = textPaint.measureText(ss);

      setTextColor(R.color.time_text);
      textPaint.setTextSize(hhmmHeight);
      canvas.drawText(
          hhmm,
          (canvas.getWidth() / 2.0f) - ((hhmmWidth + ssWidth) / 2.0f),
          (canvas.getHeight() / 2.0f) + (hhmmRealHeight / 2.0f),
          textPaint);

      textPaint.setTextSize(ampmHeight * 0.5f);
      canvas.drawText(
          ampm,
          (canvas.getWidth() / 2.0f) - ((hhmmWidth + ssWidth) / 2.0f) + hhmmWidth,
          (canvas.getHeight() / 2.0f) - (hhmmRealHeight ) + ampmHeight,
          textPaint);

      if (!isInAmbientMode()) {
        textPaint.setAlpha(120);
        textPaint.setStrokeWidth(2.0f);
        float y = (((canvas.getHeight() / 2.0f) - (hhmmRealHeight ) + ampmHeight) +
            (hhmmHeight + (int)(hhmmHeight * 0.6f))) / 2.0f;
        canvas.drawLine(0, y, canvas.getWidth(), y, textPaint);
        textPaint.setAlpha(255);
      }

      {
        setTextColor(R.color.date_text);
        textPaint.setTextSize((int)(hhmmHeight * 0.6f));
        String day = getDayOfWeekName(time.weekDay);
        float dayWidth = textPaint.measureText(day);
        canvas.drawText(
            day,
            (canvas.getWidth() / 2.0f) - (dayWidth / 2.0f),
            hhmmHeight,
            textPaint);

        String ddmmyy = String.format("%02d-%s-%04d",
            time.monthDay, getMonthName(time.month), time.year);
        float ddmmyyWidth = textPaint.measureText(ddmmyy);
        canvas.drawText(
            ddmmyy,
            (canvas.getWidth() / 2.0f) - (ddmmyyWidth / 2.0f),
            hhmmHeight + (int)(hhmmHeight * 0.6f),
            textPaint);

        textPaint.setTextSize(hhmmHeight);
      }

      if (!ambient) {
        setTextColor(R.color.time_text);
        textPaint.setTextSize((int)((hhmmHeight * 0.5f)));
        canvas.drawText(
            ss,
            (canvas.getWidth() / 2.0f) - ((hhmmWidth + ssWidth) / 2.0f) + hhmmWidth,
            (canvas.getHeight() / 2.0f) + (hhmmRealHeight / 2.0f),
            textPaint);

        EventDetails event = getNextEvent();
        if (event != null) {
          textPaint.setTextSize((int)(hhmmHeight * 0.4f));
          textPaint.setColor(Color.YELLOW);

          Calendar cal = Calendar.getInstance();
          cal.setTimeInMillis(event.startTime);
          String title = String.format(Locale.US, "%d:%02d %s",
              cal.get(Calendar.HOUR) == 0 ? 12 : cal.get(Calendar.HOUR),
              cal.get(Calendar.MINUTE),
              event.title);
          title = ensureNoWiderThan(textPaint, title, canvas.getWidth() * 0.9f);
          float titleWidth = textPaint.measureText(title);
          canvas.drawText(
              title,
              (canvas.getWidth() / 2.0f) - (titleWidth / 2.0f),
              (canvas.getHeight() / 2.0f) + hhmmHeight,
              textPaint);

          String room = event.room;
          // strip MTV-RLS1-2-, then MTV-RLS1- (if it's on a different floor), then MTV- (if it's
          // in a different building).
          if (room.startsWith("MTV-RLS1-2-")) {
            room = room.substring(11);
          } else if (room.startsWith("MTV-RLS1-")) {
            room = room.substring(9);
          } else if (room.startsWith("MTV-")) {
            room = room.substring(4);
          }

          room = ensureNoWiderThan(textPaint, room, canvas.getWidth() * 0.8f);
          float roomWidth = textPaint.measureText(room);
          canvas.drawText(
              room,
              (canvas.getWidth() / 2.0f) - (roomWidth / 2.0f),
              (canvas.getHeight() / 2.0f) + hhmmHeight + (int) (hhmmHeight * 0.4f),
              textPaint);
        }
      }

      textPaint.setTextSize(hhmmHeight);
    }

    /** Gets the next event in your calendar, or null if there are no upcoming events. */
    private EventDetails getNextEvent() {
      if (events == null) {
        return null;
      }

      Date maxStartTime = new Date();
      maxStartTime.setTime(maxStartTime.getTime() - 15 * 60 * 1000); // don't return events which started > 15 minutes ago
      for (EventDetails event : events) {
        if (new Date(event.startTime).before(maxStartTime)) {
          continue;
        }
        return event;
      }

      // No events found.
      return null;
    }

    private String ensureNoWiderThan(Paint textPaint, String str, float maxWidth) {
      float width = textPaint.measureText(str);
      while (width > maxWidth) {
        str = str.substring(0, str.length() - 4) + "...";
        width = textPaint.measureText(str);
      }
      return str;
    }

    private String getMonthName(int month) {
      String[] monthNames = {
          "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
      };
      return monthNames[month];
    }

    private String getDayOfWeekName(int dayOfWeek) {
      String[] dayNames = {
          "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
      };
      return dayNames[dayOfWeek];
    }

    private void cancelLoadMeetingTask() {
      if (loadMeetingsTask != null) {
        loadMeetingsTask.cancel(true);
      }
    }

    private void onMeetingsLoaded(ArrayList<EventDetails> result) {
      if (result != null) {
        events = new ArrayList<>(result);
        invalidate();
      }
    }

    /**
     * Starts the {@link #engineHandler} timer if it should be running and isn't currently
     * or stops it if it shouldn't be running but currently is.
     */
    private void updateTimer() {
      engineHandler.removeMessages(MSG_UPDATE_TIME);
      if (shouldTimerBeRunning()) {
        engineHandler.sendEmptyMessage(MSG_UPDATE_TIME);
      }
    }

    /**
     * Returns whether the {@link #engineHandler} timer should be running. The timer should
     * only run when we're visible and in interactive mode.
     */
    private boolean shouldTimerBeRunning() {
      return isVisible() && !isInAmbientMode();
    }

    /**
     * Handle updating the time periodically in interactive mode.
     */
    private void handleUpdateTimeMessage() {
      invalidate();
      if (shouldTimerBeRunning()) {
        long timeMs = System.currentTimeMillis();
        long delayMs = INTERACTIVE_UPDATE_RATE_MS
            - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
        engineHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
      }
    }

    /**
     * Asynchronous task to load the meetings from the content provider and report the number of
     * meetings back via {@link #onMeetingsLoaded}.
     */
    private class LoadMeetingsTask extends AsyncTask<Void, Void, ArrayList<EventDetails>> {
      private PowerManager.WakeLock wakeLock;

      @Override
      protected ArrayList<EventDetails> doInBackground(Void... voids) {
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
          wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "weather:WatchFaceWakeLock");
          wakeLock.acquire(10000);
        }

        // include meetings that started 15 minutes ago
        long begin = System.currentTimeMillis() - (15 * 60 * 1000);
        Uri.Builder builder = WearableCalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, begin);
        ContentUris.appendId(builder, begin + DateUtils.DAY_IN_MILLIS);
        Cursor cursor = getContentResolver().query(builder.build(),
            new String[] {
                CalendarContract.Instances.EVENT_ID,
                CalendarContract.Instances.TITLE,
                CalendarContract.Instances.ALL_DAY,
                CalendarContract.Instances.SELF_ATTENDEE_STATUS,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.BEGIN,
            },
            null, null, null);
        if (cursor == null) {
          return null;
        }

        ArrayList<EventDetails> newEvents = new ArrayList<>();
        while (cursor.moveToNext()) {
          // These must match the order in the query above.
          String eventId = cursor.getString(0);
          String title = cursor.getString(1);
          boolean allDay = cursor.getInt(2) != 0;
          int selfAttendeeStatus = cursor.getInt(3);
          String location = cursor.getString(4);
          long startTime = cursor.getLong(5);

          if (allDay) {
            // Skip all-day events.
            continue;
          }
          if (selfAttendeeStatus != CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED &&
              selfAttendeeStatus != CalendarContract.Attendees.ATTENDEE_STATUS_NONE) {
            // You've declined (or not accepted at least), skip.
            continue;
          }
          EventDetails eventDetails = new EventDetails(eventId, title, location, startTime);
          newEvents.add(eventDetails);
        }
        cursor.close();

        cursor = getContentResolver().query(
            WearableCalendarContract.Attendees.CONTENT_URI,
            new String[] {
                CalendarContract.Attendees.ATTENDEE_NAME,
                CalendarContract.Attendees.ATTENDEE_STATUS,
                CalendarContract.Attendees.EVENT_ID },
            null, null, null);
        if (cursor == null) {
          return newEvents;
        }

        // Override the 'location' with the 'real' room, if we have one.
        while (cursor.moveToNext()) {
          String attendeeName = cursor.getString(0);
          int attendeeStatus = cursor.getInt(1);
          String eventId = cursor.getString(2);
          if (attendeeStatus != CalendarContract.Attendees.ATTENDEE_STATUS_ACCEPTED) {
            // Skip if they're not explicitly accepted.
            continue;
          }
          if (attendeeName.startsWith("MTV-")) {
            for (EventDetails event : newEvents) {
              if (event.id.equals(eventId)) {
                event.room = attendeeName;
              }
            }
          }
        }
        cursor.close();

        Collections.sort(newEvents, new Comparator<EventDetails>() {
          @Override
          public int compare(EventDetails lhs, EventDetails rhs) {
            return (int)(lhs.startTime - rhs.startTime);
          }
        });

        Log.d(TAG, "Meetings refreshed:");
        for (EventDetails event : newEvents) {
          Log.d(TAG, "[" + event.title + "] in " + event.room + " at "
              + new Date(event.startTime).toString());
        }

        return newEvents;
      }

      @Override
      protected void onPostExecute(ArrayList<EventDetails> result) {
        releaseWakeLock();
        onMeetingsLoaded(result);
      }

      @Override
      protected void onCancelled() {
        releaseWakeLock();
      }

      private void releaseWakeLock() {
        if (wakeLock != null) {
          wakeLock.release();
          wakeLock = null;
        }
      }
    }

    private class EventDetails {
      public String id;
      public String title;
      public String room;
      public long startTime;

      public EventDetails(String id, String title, String room, long startTime) {
        this.id = id;
        this.title = title;
        this.room = room;
        this.startTime = startTime;
      }
    }
  }
}
