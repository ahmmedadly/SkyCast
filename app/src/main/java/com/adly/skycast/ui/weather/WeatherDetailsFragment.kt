package com.adly.skycast.ui.weather

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.adly.skycast.databinding.FragmentWeatherDetailsBinding
import com.adly.skycast.ui.home.HomeViewModel
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.LinearLayoutManager
import com.adly.skycast.R
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.ui.home.GroupedForecastAdapter

class WeatherDetailsFragment : Fragment() {
    private lateinit var binding: FragmentWeatherDetailsBinding
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)
        val groupedAdapter = GroupedForecastAdapter()
        binding.rvGroupedForecastDetails.adapter = groupedAdapter
        binding.rvGroupedForecastDetails.layoutManager = LinearLayoutManager(requireContext())

        viewModel.groupedForecast.observe(viewLifecycleOwner) { groupedList ->
            groupedAdapter.submitList(groupedList)
        }

        binding.btnAddToFavorites.setOnClickListener {
            viewModel.weather.value?.let { weather ->
                val city = weather.city
                val fav = FavoriteLocationEntity(
                    name = city.name,
                    country = city.country,
                    lat = city.coord.lat,
                    lon = city.coord.lon
                )

                // Check if favorite already exists
                if (viewModel.isFavoriteExists(fav)) {
                    Toast.makeText(
                        requireContext(),
                        R.string.already_in_favorites,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    viewModel.addFavorite(fav)
                    Toast.makeText(
                        requireContext(),
                        R.string.added_to_favorites,
                        Toast.LENGTH_SHORT
                    ).show()
                    findNavController().popBackStack()
                }
            }
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                val item = it.list[0]

                binding.tvCity.text = "${it.city.name}, ${it.city.country}"
                binding.tvTemp.text = "${item.main.temp.toInt()}°C"
                binding.tvDesc.text = item.weather[0].description.replaceFirstChar(Char::titlecase)
                binding.tvClouds.text = getString(R.string.clouds , item.clouds.all)
                binding.tvCurrentDateTime.text = item.dtTxt
                binding.tvHumidity.text = getString(R.string.humidity , item.main.humidity)
                binding.tvPressure.text = getString(R.string.pressure, item.main.pressure)
                binding.tvWind.text = getString(R.string.wind, item.wind)

                val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
                Glide.with(this)
                    .load(iconUrl)
                    .into(binding.ivIcon)

            }
        }
        viewModel.groupedForecast.observe(viewLifecycleOwner) { groupedList ->
            groupedAdapter.submitList(groupedList)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }
}