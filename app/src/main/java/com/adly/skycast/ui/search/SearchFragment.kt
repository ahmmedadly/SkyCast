package com.adly.skycast.ui.search

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.adly.skycast.R
import com.adly.skycast.databinding.FragmentSearchBinding
import com.adly.skycast.ui.home.HomeViewModel

class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    // Shared ViewModel between fragments
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)

        // Trigger city suggestions when typing
        binding.etCity.addTextChangedListener { query ->
            if (!query.isNullOrBlank() && query.length >= 3) {
                viewModel.searchCity(query.toString())
            }
        }
        binding.btnFavorites.setOnClickListener {
            findNavController().navigate(R.id.favoritesFragment)
        }
        // Manual search button click
        binding.btnFetchWeather.setOnClickListener {
            val city = binding.etCity.text.toString()
            if (city.isNotBlank()) {
                viewModel.fetchWeather(city)
                findNavController().navigate(R.id.weatherDetailsFragment)
            } else {
                Toast.makeText(requireContext(), "Please enter a city name", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe and show city autocomplete suggestions
        viewModel.suggestions.observe(viewLifecycleOwner) { locations ->
            val cityNames = locations.map { "${it.name}, ${it.country}" }
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cityNames)
            binding.etCity.setAdapter(adapter)

            // Click on suggestion → fetch coordinates + navigate
            binding.etCity.setOnItemClickListener { _, _, position, _ ->
                val selected = locations[position]
                viewModel.fetchWeatherByCoordinates(selected.lat, selected.lon)
                findNavController().navigate(R.id.weatherDetailsFragment)
            }
        }

        return binding.root
    }
}
