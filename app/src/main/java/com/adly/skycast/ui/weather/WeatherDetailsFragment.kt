package com.adly.skycast.ui.weather

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.adly.skycast.databinding.FragmentWeatherDetailsBinding
import com.adly.skycast.ui.home.HomeViewModel
import com.bumptech.glide.Glide
import androidx.recyclerview.widget.LinearLayoutManager
import com.adly.skycast.ui.home.ForecastAdapter


class WeatherDetailsFragment : Fragment() {
    private lateinit var binding: FragmentWeatherDetailsBinding
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentWeatherDetailsBinding.inflate(inflater, container, false)
        val forecastAdapter = ForecastAdapter()
        binding.rvForecastDetails.adapter = forecastAdapter
        binding.rvForecastDetails.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        viewModel.cachedForecast.observe(viewLifecycleOwner) { forecast ->
            forecastAdapter.submitList(forecast)
        }

        viewModel.weather.observe(viewLifecycleOwner) { weather ->
            weather?.let {
                val item = it.list[0]

                binding.tvCity.text = "${it.city.name}, ${it.city.country}"
                binding.tvTemp.text = "${item.main.temp.toInt()}°C"
                binding.tvDesc.text = item.weather[0].description.replaceFirstChar(Char::titlecase)

                binding.tvHumidity.text = "Humidity: ${item.main.humidity}%"
                binding.tvPressure.text = "Pressure: ${item.main.pressure} hPa"
                binding.tvWind.text = "Wind: ${item.wind.speed} m/s"

                val iconUrl = "https://openweathermap.org/img/wn/${item.weather[0].icon}@2x.png"
                Glide.with(this)
                    .load(iconUrl)
                    .into(binding.ivIcon)
            }
        }

        return binding.root
    }
}
