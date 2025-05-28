package com.adly.skycast.ui.home

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.adly.skycast.BuildConfig
import com.adly.skycast.data.local_source.AppDatabase
import com.adly.skycast.data.model.CurrentWeatherResponce
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.data.model.ForecastGroup
import com.adly.skycast.data.model.GeoLocation
import com.adly.skycast.data.model.WeatherForecastEntity
import com.adly.skycast.data.remote_source.RetrofitInstance
import com.adly.skycast.data.repository.WeatherRepository
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit


class HomeViewModel(application: Application, weatherRepository: Any?) : AndroidViewModel(application) {

    private val repository: WeatherRepository

    // Live forecast from DB
    private val _cachedForecast = MutableLiveData<List<WeatherForecastEntity>>()
    val cachedForecast: LiveData<List<WeatherForecastEntity>> = _cachedForecast

    // Live weather from API (raw)
    private val _weather = MutableLiveData<CurrentWeatherResponce>()
    val weather: LiveData<CurrentWeatherResponce> = _weather

    // City autocomplete suggestions
    private val _suggestions = MutableLiveData<List<GeoLocation>>()
    val suggestions: LiveData<List<GeoLocation>> = _suggestions

    private val _groupedForecast = MediatorLiveData<List<ForecastGroup>>()
    val groupedForecast: LiveData<List<ForecastGroup>> = _groupedForecast
    // Today Hourly forecast for the day
    private val _todayHourly = MutableLiveData<List<WeatherForecastEntity>>()
    val todayHourly: LiveData<List<WeatherForecastEntity>> = _todayHourly


    init {
        val dao = AppDatabase.getDatabase(getApplication()).weatherDao()
        repository = WeatherRepository(RetrofitInstance.api, dao)

        // Load today's hourly forecast from 12 AM to now
        repository.getPastHourlyToday().observeForever { list ->
            val onePerHour = list.distinctBy { forecast ->
                // Round to nearest hour using timestamp
                val cal = Calendar.getInstance().apply { timeInMillis = forecast.timestamp }
                "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)} ${cal.get(Calendar.HOUR_OF_DAY)}"
            }
            _todayHourly.value = onePerHour
        }


        _groupedForecast.addSource(cachedForecast) { list ->
            val grouped = list.groupBy { forecast ->
                forecast.dateText.substring(0, 10) // group by YYYY-MM-DD
            }.map { (date, forecasts) ->
                ForecastGroup(date, forecasts)
            }
            _groupedForecast.value = grouped
        }
    }

    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getForecastByCity(city, BuildConfig.MY_API_KEY)
                _weather.value = response
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error: ${e.message}")
            }
        }
    }

    fun searchCity(query: String) {
        viewModelScope.launch {
            try {
                val result = RetrofitInstance.api.getCityCoordinates(query, 5, BuildConfig.MY_API_KEY)
                _suggestions.value = result
            } catch (e: Exception) {
                Log.e("CitySearch", "Error: ${e.message}")
            }
        }
    }

    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        // Then fetch from API and update cache
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getForecastByCoords(lat, lon, BuildConfig.MY_API_KEY)
                _weather.value = response
                repository.cacheForecastFromResponse(response, lat, lon)
                loadCachedForecast(lat, lon)
            } catch (e: Exception) {
                Log.e("WeatherAPI", "API Error: ${e.message}")
            }
        }
    }

    private fun loadCachedForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            repository.getCachedForecast(lat, lon).observeForever {
                _cachedForecast.value = it
            }
        }
    }
    fun scheduleDailyCleanup(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val request = PeriodicWorkRequestBuilder<ForecastCleanupWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(calculateMidnightDelay(), TimeUnit.MILLISECONDS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "DailyForecastCleanup",
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    fun calculateMidnightDelay(): Long {
        val now = Calendar.getInstance()
        val midnight = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.DAY_OF_YEAR, 1)
        }
        return midnight.timeInMillis - now.timeInMillis
    }
    val favorites: LiveData<List<FavoriteLocationEntity>> = repository.getFavorites()


    fun addFavorite(fav: FavoriteLocationEntity) {
        viewModelScope.launch {
            repository.insertFavorite(fav)
        }
    }
    fun removeFavorite(favorite: FavoriteLocationEntity) {
        viewModelScope.launch {
            repository.removeFavorite(favorite)
        }
    }
    fun getTemperatureUnit(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("temp_unit", "CELSIUS") ?: "CELSIUS"
    }

    fun getWindSpeedUnit(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString("wind_speed_unit", "KMH") ?: "KMH"
    }
    fun isFavoriteExists(favorite: FavoriteLocationEntity): Boolean {
        return favorites.value?.any {
            it.name == favorite.name && it.country == favorite.country
        } == true
    }
}