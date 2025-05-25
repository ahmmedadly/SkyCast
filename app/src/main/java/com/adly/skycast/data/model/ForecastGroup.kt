package com.adly.skycast.data.model

data class ForecastGroup(
    val date: String, // e.g., "2025-05-25"
    val items: List<WeatherForecastEntity>
)
