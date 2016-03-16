package au.com.codeka.weather;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by dean on 17/03/2016.
 */
public class WatchFace extends CanvasWatchFaceService {
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
    return new Engine();
  }

  private static class EngineHandler extends Handler {
    private final WeakReference<Engine> weakReference;

    public EngineHandler(Engine reference) {
      weakReference = new WeakReference<>(reference);
    }

    @Override
    public void handleMessage(Message msg) {
      Engine engine = weakReference.get();
      if (engine != null) {
        switch (msg.what) {
          case MSG_UPDATE_TIME:
            engine.handleUpdateTimeMessage();
            break;
        }
      }
    }
  }

  private class Engine extends CanvasWatchFaceService.Engine {
    final Handler engineHandler = new EngineHandler(this);
    boolean registeredTimeZoneReceiver = false;
    Paint backgroundPaint;
    Paint textPaint;
    boolean ambient;
    Time time;
    final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        time.clear(intent.getStringExtra("time-zone"));
        time.setToNow();
      }
    };
    int tapCount;
    Typeface typeface;

    float xOffset;
    float yOffset;

    /**
     * Whether the display supports fewer bits for each color in ambient mode. When true, we
     * disable anti-aliasing in ambient mode.
     */
    boolean lowBitAmbient;

    @Override
    public void onCreate(SurfaceHolder holder) {
      super.onCreate(holder);

      typeface = Typeface.createFromAsset(getAssets(), "led-counter-7.regular.ttf");

      setWatchFaceStyle(new WatchFaceStyle.Builder(WatchFace.this)
          .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
          .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
          .setShowSystemUiTime(false)
          .setAcceptsTapEvents(true)
          .build());
      Resources resources = WatchFace.this.getResources();
      yOffset = resources.getDimension(R.dimen.digital_y_offset);

      backgroundPaint = new Paint();
      backgroundPaint.setColor(resources.getColor(R.color.background));

      textPaint = new Paint();
      textPaint = createTextPaint(Color.WHITE);

      time = new Time();
    }

    @Override
    public void onDestroy() {
      engineHandler.removeMessages(MSG_UPDATE_TIME);
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
        registerReceiver();

        // Update time zone in case it changed while we weren't visible.
        time.clear(TimeZone.getDefault().getID());
        time.setToNow();
      } else {
        unregisterReceiver();
      }

      // Whether the timer should be running depends on whether we're visible (as well as
      // whether we're in ambient mode), so we may need to start or stop the timer.
      updateTimer();
    }

    private void registerReceiver() {
      if (registeredTimeZoneReceiver) {
        return;
      }
      registeredTimeZoneReceiver = true;
      IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
      WatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
    }

    private void unregisterReceiver() {
      if (!registeredTimeZoneReceiver) {
        return;
      }
      registeredTimeZoneReceiver = false;
      WatchFace.this.unregisterReceiver(mTimeZoneReceiver);
    }

    @Override
    public void onApplyWindowInsets(WindowInsets insets) {
      super.onApplyWindowInsets(insets);

      // Load resources that have alternate values for round watches.
      Resources resources = WatchFace.this.getResources();
      boolean isRound = insets.isRound();
      xOffset = resources.getDimension(isRound
          ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
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
          backgroundPaint.setColor(resources.getColor(tapCount % 2 == 0 ?
              R.color.background : R.color.background2));
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
      float hhmmRealHeight = hhmmHeight * 0.75f;

      String ampm = morning ? "am" : "pm";
      textPaint.setTextSize(hhmmHeight * 0.5f);
      float ampmHeight = textPaint.measureText(ampm);

      textPaint.setTextSize(hhmmHeight * 0.5f);
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
        canvas.drawLine(
            0, hhmmHeight + (hhmmHeight * 0.6f) + 4.0f,
            canvas.getWidth(), hhmmHeight + (hhmmHeight * 0.6f) + 4.0f,
            textPaint);
        textPaint.setAlpha(255);
      }

      {
        setTextColor(R.color.date_text);
        textPaint.setTextSize(hhmmHeight * 0.6f);
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
            hhmmHeight + (hhmmHeight * 0.6f),
            textPaint);

        textPaint.setTextSize(hhmmHeight);
      }

      if (!ambient) {
        setTextColor(R.color.time_text);
        textPaint.setTextSize(hhmmHeight * 0.5f);
        canvas.drawText(
            ss,
            (canvas.getWidth() / 2.0f) - ((hhmmWidth + ssWidth) / 2.0f) + hhmmWidth,
            (canvas.getHeight() / 2.0f) + (hhmmRealHeight / 2.0f),
            textPaint);
        textPaint.setTextSize(hhmmHeight);
      }
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
  }
}
