package com.adly.skycast.ui.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.adly.skycast.BuildConfig
import com.adly.skycast.data.local_source.AppDatabase
import com.adly.skycast.data.model.CurrentWeatherResponce
import com.adly.skycast.data.model.ForecastGroup
import com.adly.skycast.data.model.GeoLocation
import com.adly.skycast.data.model.WeatherForecastEntity
import com.adly.skycast.data.remote_source.RetrofitInstance
import com.adly.skycast.repository.WeatherRepository
import kotlinx.coroutines.launch


class HomeViewModel(application: Application) : AndroidViewModel(application) {

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

    init {
        _groupedForecast.addSource(cachedForecast) { list ->
            val grouped = list.groupBy { forecast ->
                forecast.dateText.substring(0, 10) // group by YYYY-MM-DD
            }.map { (date, forecasts) ->
                ForecastGroup(date, forecasts)
            }
            _groupedForecast.value = grouped
        }
    }


    init {
        val dao = AppDatabase.getDatabase(application).weatherDao()
        repository = WeatherRepository(RetrofitInstance.api, dao)
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
        // Load from DB first (offline support)
        loadCachedForecast(lat, lon)

        // Then fetch from API and update cache
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getForecastByCoords(lat, lon, BuildConfig.MY_API_KEY)
                _weather.value = response
                repository.cacheForecastFromResponse(response, lat, lon)
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
}
