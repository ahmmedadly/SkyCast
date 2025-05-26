package com.adly.skycast.data.local_source

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.model.WeatherForecastEntity

@Database(entities = [WeatherForecastEntity::class, FavoriteLocationEntity::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weather_database"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            //will be removed after finshing the project just keepin it now for schema changes
            }
        }
    }
}
