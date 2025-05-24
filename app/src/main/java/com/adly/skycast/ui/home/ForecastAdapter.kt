package com.adly.skycast.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adly.skycast.R
import com.adly.skycast.data.model.WeatherForecastEntity
import com.bumptech.glide.Glide

class ForecastAdapter : RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder>() {

    private var forecastList: List<WeatherForecastEntity> = emptyList()

    fun submitList(data: List<WeatherForecastEntity>) {
        forecastList = data
        notifyDataSetChanged()
    }

    inner class ForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val imgIcon: ImageView = view.findViewById(R.id.imgWeatherIcon)
        val tvTemp: TextView = view.findViewById(R.id.tvTempForecast)
        val tvDesc: TextView = view.findViewById(R.id.tvDescForecast)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForecastViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.forecast_item, parent, false)
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(holder: ForecastViewHolder, position: Int) {
        val item = forecastList[position]
        holder.tvDate.text = item.dateText.substring(5, 16)  // e.g., "04-24 15:00"
        holder.tvTemp.text = "${item.temperature.toInt()}°C"
        holder.tvDesc.text = item.description
        val iconUrl = "https://openweathermap.org/img/wn/${item.icon}@2x.png"
        Glide.with(holder.itemView.context).load(iconUrl).into(holder.imgIcon)
    }

    override fun getItemCount(): Int = forecastList.size
}
