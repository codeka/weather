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

  class WeatherWorker(private val context: Context, params: WorkerParameters)
    : Worker(context, params) {
    override fun doWork(): Result {
      // Make sure we wait for the foreground task to complete before actually returning (sometimes
      // WeatherManager.refreshWeather can return instantly).
      setForegroundAsync(createForegroundInfo(context)).get()

      WeatherManager.i.refreshWeather(context, false /* force */)

      // TODO: this is a crazy hack... sometimes the foreground icon hasn't finished being created
      // before we return (even when we get() the future) and there's a race where it never goes
      // away. Really we should only create the icon if we actually need to do something
      // long-running.
      Thread.sleep(1000)

      return Result.success()
    }

    private fun createForegroundInfo(context: Context): ForegroundInfo {
      val id = context.getString(R.string.notification_channel_id)
      val title = context.getString(R.string.notification_title)
      val cancel = context.getString(R.string.notification_cancel)
      // This PendingIntent can be used to cancel the worker
      val intent = WorkManager.getInstance(context).createCancelPendingIntent(getId())

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        ensureChannel(context);
      }

      val notification = NotificationCompat.Builder(context, id)
          .setContentTitle(title)
          .setTicker(title)
          .setSmallIcon(R.drawable.weather_mediumrain_small)
          .setOngoing(true)
          .setChannelId(NOTIFICATION_CHANNEL_ID)
          .setPriority(NotificationManager.IMPORTANCE_LOW)
          .addAction(android.R.drawable.ic_delete, cancel, intent)
          .build()

      return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun ensureChannel(context: Context) {
      val name: CharSequence = context.getString(R.string.notification_channel_name)
      val description: String = context.getString(R.string.notification_channel_desc)
      val importance: Int = NotificationManager.IMPORTANCE_LOW
      val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
      channel.description = description
      context.getSystemService(NotificationManager::class.java)!!.createNotificationChannel(channel)
    }
  }
}
