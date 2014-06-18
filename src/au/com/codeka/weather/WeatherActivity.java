package au.com.codeka.weather;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class WeatherActivity extends Activity {
  private ActivityLogEntryAdapter mActivityLogEntryAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.weather_activity);

    ListView activityLog = (ListView) findViewById(R.id.activity_log);
    mActivityLogEntryAdapter = new ActivityLogEntryAdapter();
    activityLog.setAdapter(mActivityLogEntryAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();

    List<ActivityLog.Entry> logEntries = ActivityLog.load(this);
    mActivityLogEntryAdapter.setEntries(logEntries);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.weather_activity_actions, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.refresh:
      refreshWeather();
      return true;
    case R.id.debug:
      showDebugActivity();
      return true;
    default:
      return super.onOptionsItemSelected(item);
    }
  }

  private void refreshWeather() {
    WeatherManager.i.refreshWeather(this, true);
  }

  private void showDebugActivity() {
    startActivity(new Intent(this, DebugActivity.class));
  }

  private class ActivityLogEntryAdapter extends BaseAdapter {
    List<ActivityLog.Entry> mEntries;

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS",
        Locale.ENGLISH);

    public ActivityLogEntryAdapter() {
      mEntries = new ArrayList<ActivityLog.Entry>();
    }

    public void setEntries(List<ActivityLog.Entry> entries) {
      mEntries = entries;
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return mEntries.size();
    }

    @Override
    public boolean isEnabled(int position) {
      return false;
    }

    @Override
    public Object getItem(int position) {
      return mEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View view = convertView;
      if (view == null) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.activity_log_row, parent, false);
      }

      ActivityLog.Entry entry = mEntries.get(position);

      StringBuilder logs = new StringBuilder();
      if (entry.getLogs() != null) {
        for (ActivityLog.LogEntry log : entry.getLogs()) {
          logs.append(String.format("%8.2fs %s\n",
              (float) (log.getTimestamp() - entry.getTimestamp()) / 1000.0f,
              log.getMessage()));
        }
      }

      ((TextView) view.findViewById(R.id.timestamp)).setText(mDateFormat.format(new Date(entry.getTimestamp())));
      if (entry.hasLocation()) {
        view.findViewById(R.id.location).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.location)).setText(Html.fromHtml(entry.getMapLink()));
        ((TextView) view.findViewById(R.id.location)).setMovementMethod(LinkMovementMethod.getInstance());
      } else {
        view.findViewById(R.id.location).setVisibility(View.GONE);
      }
      ((TextView) view.findViewById(R.id.logs)).setText(logs.toString());
      if (entry.getMillisToNextAlarm() == 0) {
        view.findViewById(R.id.next_timestamp).setVisibility(View.GONE);
      } else {
        view.findViewById(R.id.next_timestamp).setVisibility(View.VISIBLE);
        ((TextView) view.findViewById(R.id.next_timestamp)).setText(String.format("%.1fs",
            (float) entry.getMillisToNextAlarm() / 1000.0f));
      }

      return view;
    }
  }
}
