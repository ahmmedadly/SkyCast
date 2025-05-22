package com.adly.skycast.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.adly.skycast.data.local_source.WeatherDao
import com.adly.skycast.data.model.CurrentWeatherResponce
import com.adly.skycast.data.model.WeatherForecastEntity
import com.adly.skycast.data.remote_source.WeatherApiService

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
                    icon = item.weather[0].icon
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
}
