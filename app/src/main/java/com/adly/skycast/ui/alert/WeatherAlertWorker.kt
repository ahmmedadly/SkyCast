package com.adly.skycast.ui.alert

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adly.skycast.BuildConfig
import com.adly.skycast.R
import com.adly.skycast.data.local_source.AppDatabase
import com.adly.skycast.data.remote_source.RetrofitInstance
import com.adly.skycast.data.repository.WeatherRepository
import java.util.Random

class WeatherAlertWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    private val repo = WeatherRepository(
        RetrofitInstance.api,
        AppDatabase.getDatabase(context).weatherDao()
    )

    override suspend fun doWork(): Result {
        val prefs = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        if (!prefs.getBoolean("alerts_enabled", true)) return Result.success()

        val (lat, lon) = getSavedLocation(prefs) ?: return Result.failure()

        try {
            val response = RetrofitInstance.api.getForecastByCoords(lat, lon, BuildConfig.MY_API_KEY)
            val severe = response.list.any {
                val main = it.weather[0].main.lowercase()
                main.contains("storm") || main.contains("extreme") || main.contains("snow")
            }
            if (severe) sendAlertNotification(response.city.name)
        } catch (e: Exception) {
            return Result.retry()
        }

        return Result.success()
    }

    private fun getSavedLocation(prefs: SharedPreferences): Pair<Double, Double>? {
        val manual = prefs.getBoolean("manual_location", false)
        return if (manual) {
            val coord = prefs.getString("coordinates", "") ?: ""
            val parts = coord.split(",")
            if (parts.size == 2) Pair(parts[0].toDoubleOrNull() ?: 0.0, parts[1].toDoubleOrNull() ?: 0.0) else null
        } else null
    }

    private fun sendAlertNotification(city: String) {
        val channelId = "weather_alerts"
        val notifManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Weather Alerts", NotificationManager.IMPORTANCE_HIGH)
            notifManager.createNotificationChannel(channel)
        }

        // Build and send notification
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.thermostat_24px)
            .setContentTitle("⚠️ Severe Weather Alert")
            .setContentText("Severe conditions expected in $city.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notifManager.notify(Random().nextInt(), builder.build())
    }
}
