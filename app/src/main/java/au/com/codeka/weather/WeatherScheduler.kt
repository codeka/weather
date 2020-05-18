package au.com.codeka.weather

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import java.util.concurrent.TimeUnit


/**
 * WeatherScheduler works with the Android WorkManager API to schedule refreshes of weather info
 * at various times.
 */
class WeatherScheduler {
  companion object {
    private val TAG = WeatherScheduler::class.java.simpleName

    private const val NOTIFICATION_ID = 3871
    private const val NOTIFICATION_CHANNEL_ID = "weather_refresh"

    /** Schedule a job to run every now and then to update weather.  */
    fun schedule(context: Context) {
      val constraints = Constraints.Builder()
          .setRequiredNetworkType(NetworkType.CONNECTED)
          .build()

      val request = PeriodicWorkRequest.Builder(WeatherWorker::class.java, 15, TimeUnit.MINUTES)
          .setConstraints(constraints)
          .build()

      WorkManager.getInstance(context).enqueueUniquePeriodicWork(
          "weather_refresh", ExistingPeriodicWorkPolicy.REPLACE, request)
    }
  }

  private class WeatherWorker(private val context: Context, params: WorkerParameters)
    : Worker(context, params) {
    override fun doWork(): Result {
      setForegroundAsync(createForegroundInfo(context))
      WeatherManager.i.refreshWeather(context, false /* force */)
      return Result.success()
    }

    private fun createForegroundInfo(context: Context): ForegroundInfo {
      val id = context.getString(R.string.notification_channel_id)
      val title = context.getString(R.string.notification_title)
      val cancel = context.getString(R.string.notification_cancel)
      // This PendingIntent can be used to cancel the worker
      val intent = WorkManager.getInstance(context).createCancelPendingIntent(getId())

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createChannel(context);
      }

      val notification = NotificationCompat.Builder(context, id)
          .setContentTitle(title)
          .setTicker(title)
          .setSmallIcon(R.drawable.weather_clear)
          .setOngoing(true)
          .addAction(android.R.drawable.ic_delete, cancel, intent)
          .build()

      return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(context: Context) {
      val name: CharSequence = context.getString(R.string.notification_channel_name)
      val description: String = context.getString(R.string.notification_channel_desc)
      val importance: Int = NotificationManager.IMPORTANCE_DEFAULT
      val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
      channel.description = description
      context.getSystemService(NotificationManager::class.java)!!.createNotificationChannel(channel)
    }
  }
}
