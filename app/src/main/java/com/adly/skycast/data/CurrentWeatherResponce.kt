package com.adly.skycast.data
import com.google.gson.annotations.SerializedName

data class CurrentWeatherResponce(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Item8>,
    val message: Int
)