package au.com.codeka.weather;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An activity for displaying the debug logs.
 */
public class DebugLogActivity extends AppCompatActivity {
  private ActivityLogEntryAdapter activityLogEntryAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.debug_log_activity);

    ListView activityLog = (ListView) findViewById(R.id.activity_log);
    activityLogEntryAdapter = new ActivityLogEntryAdapter();
    activityLog.setAdapter(activityLogEntryAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();

    List<DebugLog.Entry> logEntries = DebugLog.load(this);
    activityLogEntryAdapter.setEntries(logEntries);
  }

  private class ActivityLogEntryAdapter extends BaseAdapter {
    final List<DebugLog.Entry> entries = new ArrayList<>();

    private final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH);

    public ActivityLogEntryAdapter() {
    }

    public void setEntries(List<DebugLog.Entry> entries) {
      this.entries.clear();
      this.entries.addAll(entries);
      notifyDataSetChanged();
    }

    @Override
    public int getCount() {
      return entries.size();
    }

    @Override
    public boolean isEnabled(int position) {
      return false;
    }

    @Override
    public Object getItem(int position) {
      return entries.get(position);
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

      DebugLog.Entry entry = entries.get(position);

      StringBuilder logs = new StringBuilder();
      if (entry.getLogs() != null) {
        for (DebugLog.LogEntry log : entry.getLogs()) {
          logs.append(String.format("%8.2fs %s\n",
              (float) (log.getTimestamp() - entry.getTimestamp()) / 1000.0f,
              log.getMessage()));
        }
      }

      ((TextView) view.findViewById(R.id.timestamp)).setText(DATE_FORMAT.format(new Date(entry.getTimestamp())));
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

