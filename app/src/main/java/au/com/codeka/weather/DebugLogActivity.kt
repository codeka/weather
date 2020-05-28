package au.com.codeka.weather

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

/**
 * An activity for displaying the debug logs.
 */
class DebugLogActivity : AppCompatActivity() {
  private lateinit var activityLogEntryAdapter: ActivityLogEntryAdapter

  public override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.debug_log_activity)
    val activityLog = findViewById<View>(R.id.activity_log) as ListView
    activityLogEntryAdapter = ActivityLogEntryAdapter()
    activityLog.adapter = activityLogEntryAdapter
  }

  public override fun onResume() {
    super.onResume()
    val logEntries = arrayListOf(DebugLog.current().build())
    logEntries.addAll(DebugLog.load(this))
    activityLogEntryAdapter.setEntries(logEntries)
  }

  private inner class ActivityLogEntryAdapter : BaseAdapter() {
    val entries: MutableList<DebugLog.Entry> = ArrayList()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)

    fun setEntries(entries: ArrayList<DebugLog.Entry>) {
      this.entries.clear()
      this.entries.addAll(entries)
      notifyDataSetChanged()
    }

    override fun getCount(): Int {
      return entries.size
    }

    override fun isEnabled(position: Int): Boolean {
      return false
    }

    override fun getItem(position: Int): Any {
      return entries[position]
    }

    override fun getItemId(position: Int): Long {
      return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
      val view = convertView ?: run {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.activity_log_row, parent, false)
      }
      val entry = entries[position]
      val logs = StringBuilder()
      for (log in entry.logs) {
        logs.append(String.format("%8.2fs %s\n", log.timestamp.toFloat() / 1000.0f, log.message))
      }
      (view.findViewById<View>(R.id.timestamp) as TextView).text =
          dateFormat.format(Date(entry.timestamp))
      if (entry.hasLocation()) {
        view.findViewById<View>(R.id.location).visibility = View.VISIBLE
        (view.findViewById<View>(R.id.location) as TextView).text = Html.fromHtml(entry.mapLink)
        (view.findViewById<View>(R.id.location) as TextView).movementMethod =
            LinkMovementMethod.getInstance()
      } else {
        view.findViewById<View>(R.id.location).visibility = View.GONE
      }
      (view.findViewById<View>(R.id.logs) as TextView).text = logs.toString()
      if (entry.millisToNextAlarm == 0L) {
        view.findViewById<View>(R.id.next_timestamp).visibility = View.GONE
      } else {
        view.findViewById<View>(R.id.next_timestamp).visibility = View.VISIBLE
        (view.findViewById<View>(R.id.next_timestamp) as TextView).text =
            String.format("%.1fs", entry.millisToNextAlarm.toFloat() / 1000.0f)
      }
      return view
    }
  }
}