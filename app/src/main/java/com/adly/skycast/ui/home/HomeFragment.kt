package com.adly.skycast.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adly.skycast.R
import com.adly.skycast.databinding.FragmentHomeBinding
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Swipe to refresh
        binding.swipeRefresh.setOnRefreshListener {
            getCurrentLocationWeather()
        }

        // FAB to search
        binding.fabSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        // Forecast grouped adapter setup
        val groupedAdapter = GroupedForecastAdapter()
        binding.rvGroupedForecast.adapter = groupedAdapter
        binding.rvGroupedForecast.layoutManager = LinearLayoutManager(requireContext())

        getCurrentLocationWeather()

        // Observe live weather from API
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            binding.swipeRefresh.isRefreshing = false
            weather?.let {
                binding.tvCity.text = it.city.name
                binding.tvTemp.text = "${it.list[0].main.temp}°C"
                binding.tvDesc.text = it.list[0].weather[0].description
                binding.tvClouds.text = "Clouds: ${it.list[0].clouds.all}%"
                binding.tvHumidity.text = "Humidity: ${it.list[0].main.humidity}%"
                binding.tvPressure.text = "Pressure: ${it.list[0].main.pressure} hPa"
                binding.tvWind.text = "Wind: ${it.list[0].wind.speed} m/s"
                val now = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date())
                binding.tvCurrentDateTime.text = now
                val iconCode = it.list[0].weather[0].icon
                val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
                Glide.with(this).load(iconUrl).into(binding.ivIcon)

            }
        }

        // Observe grouped forecast
        viewModel.groupedForecast.observe(viewLifecycleOwner) { groupedList ->
            groupedAdapter.submitList(groupedList)
        }

        // Observe cached forecast for offline state
        viewModel.cachedForecast.observe(viewLifecycleOwner) { forecast ->
            val first = forecast.firstOrNull()
            val weatherLoaded = viewModel.weather.value != null

            if (!weatherLoaded && first != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val formatted = sdf.format(Date(first.timestamp))
                val message = "⚠️ Offline • Last updated: $formatted"

                binding.tvStatusInfo.text = message
                binding.tvStatusInfo.visibility = View.VISIBLE
            } else {
                binding.tvStatusInfo.visibility = View.GONE
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
