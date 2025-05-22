package com.adly.skycast.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adly.skycast.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        getCurrentLocationWeather()

        // Autocomplete input
        binding.etCity.addTextChangedListener { editable ->
            val query = editable?.toString()
            if (!query.isNullOrBlank() && query.length >= 3) {
                viewModel.searchCity(query)
            }
        }

        // Button for city-based fetch
        binding.btnFetchWeather.setOnClickListener {
            val city = binding.etCity.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
            } else {
                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe live weather from API
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                binding.tvCity.text = it.city.name
                binding.tvTemp.text = "${it.list[0].main.temp}°C"
                binding.tvDesc.text = it.list[0].weather[0].description
            }
        }

        // Observe autocomplete results
        viewModel.suggestions.observe(viewLifecycleOwner) { locations ->
            val cityNames = locations.map { "${it.name}, ${it.country}" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cityNames)
            binding.etCity.setAdapter(adapter)

            binding.etCity.setOnItemClickListener { _, _, position, _ ->
                val selected = locations[position]
                viewModel.fetchWeatherByCoordinates(selected.lat, selected.lon)
            }
        }

        // Observe cached data
        viewModel.cachedForecast.observe(viewLifecycleOwner) { cached ->
            cached?.firstOrNull()?.let {
                binding.tvCity.text = "${it.cityName}, ${it.country}"
                binding.tvTemp.text = "${it.temperature}°C"
                binding.tvDesc.text = it.description
            }
        }

        return binding.root
    }

    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.fetchWeatherByCoordinates(it.latitude, it.longitude)
            } ?: Toast.makeText(requireContext(), "Could not get location", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationWeather()
        }
    }
}
