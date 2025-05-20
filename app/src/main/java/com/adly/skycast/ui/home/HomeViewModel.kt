package com.adly.skycast.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adly.skycast.BuildConfig
import com.adly.skycast.data.model.CurrentWeatherResponce
import com.adly.skycast.data.model.GeoLocation
import com.adly.skycast.data.remote_source.RetrofitInstance
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    //A LiveData object to hold the current weather data.
    private val _weather = MutableLiveData<CurrentWeatherResponce>()
    val weather: LiveData<CurrentWeatherResponce> = _weather
    // A LiveData object to hold the list of suggestions for the city name.
    private val _suggestions = MutableLiveData<List<GeoLocation>>()
    val suggestions: LiveData<List<GeoLocation>> = _suggestions

    //Fetch weather data from the API using Retrofit and update the _weather LiveData object with the result.
    fun fetchWeather(city: String) {
        viewModelScope.launch {
            try {
                //Make a network request to fetch weather data for the given city
                val response = RetrofitInstance.api.getForecastByCity(city, BuildConfig.MY_API_KEY)
                _weather.value = response
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error: ${e.message}")
            }
        }
    }
    // Fetch coordinates for a city (autocomplete suggestions)
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

    // Fetch weather by coordinates
    fun fetchWeatherByCoordinates(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getForecastByCoords(lat, lon, BuildConfig.MY_API_KEY)
                _weather.value = response
            } catch (e: Exception) {
                Log.e("WeatherAPI", "Error: ${e.message}")
            }
        }
    }
}