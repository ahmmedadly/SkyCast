package com.adly.skycast.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.adly.skycast.data.local_source.WeatherDao
import com.adly.skycast.data.model.CurrentWeatherResponce
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.model.WeatherForecastEntity
import com.adly.skycast.data.remote_source.WeatherApiService
import java.util.Calendar

class WeatherRepository(
    private val api: WeatherApiService,
    private val dao: WeatherDao
) {

    fun getCachedForecast(lat: Double, lon: Double): LiveData<List<WeatherForecastEntity>> {
        return dao.getForecast(lat, lon)
    }

    suspend fun cacheForecastFromResponse(response: CurrentWeatherResponce, lat: Double, lon: Double) {
        try {
            val cityName = response.city.name
            val country = response.city.country

            val forecasts = response.list.map { item ->
                WeatherForecastEntity(
                    lat = lat,
                    lon = lon,
                    cityName = cityName,
                    country = country,
                    timestamp = System.currentTimeMillis(),
                    dateText = item.dtTxt,
                    temperature = item.main.temp,
                    description = item.weather[0].description,
                    icon = item.weather[0].icon,
                    pressure = item.main.pressure,
                    humidity = item.main.humidity,
                    windSpeed = item.wind.speed,
                    clouds = item.clouds.all
                )
            }


            // Replace old data
            dao.deleteOldForecast(lat, lon)
            dao.insertAll(forecasts)

            Log.d("WeatherRepo", "Cached ${forecasts.size} forecasts for [$lat, $lon]")
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Caching error: ${e.message}")
        }
    }
    fun getPastHourlyToday(): LiveData<List<WeatherForecastEntity>> {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = cal.timeInMillis

        return dao.getTodayPastForecast(startOfDay, now)
    }
    suspend fun cleanupOldForecasts() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfToday = cal.timeInMillis
        dao.deleteOldForecasts(startOfToday)
    }
    fun getFavorites(): LiveData<List<FavoriteLocationEntity>> = dao.getAllFavorites()

    suspend fun removeFavorite(location: FavoriteLocationEntity) = dao.deleteFavorite(location)
    suspend fun insertFavorite(fav: FavoriteLocationEntity) {
        dao.insertFavorite(fav)
    }


}
