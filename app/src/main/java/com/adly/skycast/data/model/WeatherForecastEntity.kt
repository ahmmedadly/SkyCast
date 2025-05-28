package com.adly.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weather_forecast")
data class WeatherForecastEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val lat: Double,
    val lon: Double,
    val cityName: String,
    val country: String,
    val timestamp: Long,
    val dateText: String,
    val temperature: Double,
    val description: String,
    val icon: String,
    val pressure: Int,
    val humidity: Int,
    val windSpeed: Double,
    val clouds: Int

)
