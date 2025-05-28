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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TodayHourlyAdapter : RecyclerView.Adapter<TodayHourlyAdapter.HourViewHolder>() {

    private var items: List<WeatherForecastEntity> = emptyList()

    fun submitList(data: List<WeatherForecastEntity>) {
        items = data
        notifyDataSetChanged()
    }

    class HourViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvHour: TextView = itemView.findViewById(R.id.tvHour)
        val tvTemp: TextView = itemView.findViewById(R.id.tvTemperature)
        val ivIcon: ImageView = itemView.findViewById(R.id.ivWeatherIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_hourly_forecast, parent, false)
        return HourViewHolder(view)
    }

    override fun onBindViewHolder(holder: HourViewHolder, position: Int) {
        val item = items[position]
        val time = SimpleDateFormat("hh a", Locale.getDefault()).format(Date(item.timestamp))
        holder.tvHour.text = time
        holder.tvTemp.text = "${item.temperature.toInt()}°C"
        val iconUrl = "https://openweathermap.org/img/wn/${item.icon}@2x.png"
        Glide.with(holder.itemView.context).load(iconUrl).into(holder.ivIcon)
    }

    override fun getItemCount(): Int = items.size
}
