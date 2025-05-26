package com.adly.skycast.data.local_source

import androidx.lifecycle.LiveData
import androidx.room.*
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.model.WeatherForecastEntity

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather_forecast WHERE lat = :lat AND lon = :lon ORDER BY dateText ASC")
    fun getForecast(lat: Double, lon: Double): LiveData<List<WeatherForecastEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(forecast: List<WeatherForecastEntity>)

    @Query("DELETE FROM weather_forecast WHERE lat = :lat AND lon = :lon")
    suspend fun deleteOldForecast(lat: Double, lon: Double)

    // for saving the day hourly data
    @Query("""
    SELECT * FROM weather_forecast
    WHERE timestamp >= :startOfDay AND timestamp <= :now
    ORDER BY timestamp ASC
""")
    fun getTodayPastForecast(startOfDay: Long, now: Long): LiveData<List<WeatherForecastEntity>>

    @Query("DELETE FROM weather_forecast WHERE timestamp < :cutoff")
    suspend fun deleteOldForecasts(cutoff: Long)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(location: FavoriteLocationEntity)

    @Delete
    suspend fun deleteFavorite(location: FavoriteLocationEntity)

    @Query("SELECT * FROM favorite_locations")
    fun getAllFavorites(): LiveData<List<FavoriteLocationEntity>>

}
