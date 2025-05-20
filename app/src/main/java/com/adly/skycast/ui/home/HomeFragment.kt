package com.adly.skycast.ui.home

import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.adly.skycast.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat


class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        binding.btnCurrentLocation.setOnClickListener {
            getCurrentLocationWeather()
        }


        //  Autocomplete when user types
        binding.etCity.addTextChangedListener { editable ->
            val query = editable?.toString()
            if (!query.isNullOrBlank() && query.length >= 3) {
                viewModel.searchCity(query)
            }
        }

        //  Button click to fetch weather by city name (fallback if not using autocomplete)
        binding.btnFetchWeather.setOnClickListener {
            val city = binding.etCity.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
            } else {
                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        //  Observe weather LiveData
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            if (weather != null) {
                binding.tvCity.text = weather.city.name
                binding.tvTemp.text = "${weather.list[0].main.temp}°C"
                binding.tvDesc.text = weather.list[0].weather[0].description
            }
        }

        //  Observe location suggestions
        viewModel.suggestions.observe(viewLifecycleOwner) { locations ->
            val cityNames = locations.map { "${it.name}, ${it.country}" }
            val adapter = ArrayAdapter(requireContext(), R.layout.simple_dropdown_item_1line, cityNames)
            binding.etCity.setAdapter(adapter)

            binding.etCity.setOnItemClickListener { _, _, position, _ ->
                val selected = locations[position]
                viewModel.fetchWeatherByCoordinates(selected.lat, selected.lon)
            }
        }

        return binding.root
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationWeather()
        }
    }
    private fun getCurrentLocationWeather() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                val lat = location.latitude
                val lon = location.longitude
                viewModel.fetchWeatherByCoordinates(lat, lon)
            } else {
                Toast.makeText(requireContext(), "Could not get location", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
