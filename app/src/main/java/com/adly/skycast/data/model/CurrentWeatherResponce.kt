package com.adly.skycast.data.model

data class CurrentWeatherResponce(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Item8>,
    val message: Int
)