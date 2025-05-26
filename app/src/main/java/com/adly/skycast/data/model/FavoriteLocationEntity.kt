package com.adly.skycast.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_locations")
data class FavoriteLocationEntity(
    @PrimaryKey val name: String,
    val country: String,
    val lat: Double,
    val lon: Double
)
