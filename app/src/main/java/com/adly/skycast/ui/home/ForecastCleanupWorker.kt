package com.adly.skycast.ui.home

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.adly.skycast.data.local_source.AppDatabase
import com.adly.skycast.data.remote_source.RetrofitInstance
import com.adly.skycast.data.repository.WeatherRepository

class ForecastCleanupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val dao = AppDatabase.getDatabase(applicationContext).weatherDao()
        val repository = WeatherRepository(RetrofitInstance.api, dao)

        repository.cleanupOldForecasts()
        return Result.success()
    }
}