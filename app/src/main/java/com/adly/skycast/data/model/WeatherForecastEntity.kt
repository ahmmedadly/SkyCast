package com.adly.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lat: Double,
    val lon: Double,
    val timestamp: Long,        // when data was fetched
    val dateText: String,       // from API: dt_txt
    val cityName: String,
    val country: String,
    val temperature: Double,
    val description: String,
    val icon: String            // from API: weather[0].icon
)
