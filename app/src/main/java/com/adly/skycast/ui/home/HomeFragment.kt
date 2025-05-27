package com.adly.skycast.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.*
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.adly.skycast.R
import com.adly.skycast.databinding.FragmentHomeBinding
import com.adly.skycast.utils.LocaleHelper.translateDescription
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // Refresh
        binding.swipeRefresh.setOnRefreshListener { getCurrentLocationWeather() }

        // Hourly timeline
        val todayAdapter = TodayHourlyAdapter()
        binding.tvTodayTimeline.text = getString(R.string.today_timeline)
        binding.rvTodayTimeline.adapter = todayAdapter
        binding.rvTodayTimeline.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.todayHourly.observe(viewLifecycleOwner) { list ->
            todayAdapter.submitList(list)
        }

        // Settings menu popup
        binding.fabSetting.setOnClickListener { view ->
            val popupMenu = PopupMenu(requireContext(), view)
            popupMenu.menuInflater.inflate(R.menu.main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_settings -> {
                        findNavController().navigate(R.id.settingsFragment)
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show()
        }

        // Search
        binding.fabSearch.setOnClickListener {
            findNavController().navigate(R.id.searchFragment)
        }

        // 5-day grouped forecast
        val groupedAdapter = GroupedForecastAdapter()
        binding.tvForecastLabel.text = getString(R.string.forecast_5_day)
        binding.rvGroupedForecast.adapter = groupedAdapter
        binding.rvGroupedForecast.layoutManager = LinearLayoutManager(requireContext())

        // Main weather
        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            binding.swipeRefresh.isRefreshing = false
            weather?.let {
                val lang = Locale.getDefault().language
                val desc = translateDescription(it.list[0].weather[0].description, lang)


                val tempUnit = viewModel.getTemperatureUnit(requireContext())
                val windUnit = viewModel.getWindSpeedUnit(requireContext())

                val temp = it.list[0].main.temp

                binding.tvCity.text = it.city.name
                binding.tvTemp.text = formatTemp(temp, tempUnit)
                binding.tvDesc.text = desc
                binding.tvClouds.text = getString(R.string.clouds, weather.list[0].clouds.all)
                binding.tvHumidity.text = getString(R.string.humidity, weather.list[0].main.humidity)
                binding.tvPressure.text = getString(R.string.pressure, weather.list[0].main.pressure)
                binding.tvWind.text = getString(R.string.wind, formatWind(weather.list[0].wind.speed, windUnit))


                val now = SimpleDateFormat("EEE, dd MMM • hh:mm a", Locale.getDefault()).format(Date())
                binding.tvCurrentDateTime.text = now

                val iconUrl = "https://openweathermap.org/img/wn/${it.list[0].weather[0].icon}@2x.png"
                Glide.with(this).load(iconUrl).into(binding.ivIcon)
            }
        }

        // Grouped 5-day
        viewModel.groupedForecast.observe(viewLifecycleOwner) { groupedList ->
            groupedAdapter.submitList(groupedList)
        }

        // Offline message
        viewModel.cachedForecast.observe(viewLifecycleOwner) { forecast ->
            val first = forecast.firstOrNull()
            val weatherLoaded = viewModel.weather.value != null

            if (!weatherLoaded && first != null) {
                val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                val formatted = sdf.format(Date(first.timestamp))
                binding.tvStatusInfo.text = "⚠️ Offline • Last updated: $formatted"
                binding.tvStatusInfo.visibility = View.VISIBLE
            } else {
                binding.tvStatusInfo.visibility = View.GONE
            }
        }

        // Fetch weather
        getCurrentLocationWeather()

        return binding.root
    }

    private fun getCurrentLocationWeather() {
        val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())

        if (prefs.getBoolean("manual_location", false)) {
            val coord = prefs.getString("coordinates", "")
            val parts = coord?.split(",")?.map { it.trim().toDoubleOrNull() }
            if (parts?.size == 2 && parts[0] != null && parts[1] != null) {
                viewModel.fetchWeatherByCoordinates(parts[0]!!, parts[1]!!)
            } else {
                Toast.makeText(requireContext(), "Invalid manual coordinates", Toast.LENGTH_SHORT).show()
            }
            return
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                viewModel.fetchWeatherByCoordinates(it.latitude, it.longitude)
            } ?: Toast.makeText(requireContext(), "Could not get location", Toast.LENGTH_SHORT).show()
        }
    }

    private fun formatTemp(value: Double, unit: String): String {
        return when (unit) {
            "FAHRENHEIT" -> "${(value * 9 / 5 + 32).toInt()}°F"
            "KELVIN" -> "${(value + 273.15).toInt()}K"
            else -> "${value.toInt()}°C"
        }
    }

    private fun formatWind(value: Double, unit: String): String {
        return when (unit) {
            "MPH" -> "${(value * 2.237).toInt()} mi/h"
            else -> "${value.toInt()} km/h"
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocationWeather()
        }
    }
}
