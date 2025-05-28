    package com.adly.skycast.ui.home

    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.TextView
    import androidx.recyclerview.widget.LinearLayoutManager
    import androidx.recyclerview.widget.RecyclerView
    import com.adly.skycast.R
    import com.adly.skycast.data.model.ForecastGroup
    import java.text.SimpleDateFormat
    import java.util.*

    class GroupedForecastAdapter : RecyclerView.Adapter<GroupedForecastAdapter.GroupViewHolder>() {

        private var groups: List<ForecastGroup> = emptyList()

        fun submitList(data: List<ForecastGroup>) {
            groups = data
            notifyDataSetChanged()
        }


        inner class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val tvDayTitle: TextView = itemView.findViewById(R.id.tvDayName)
            val rvDayItems: RecyclerView = itemView.findViewById(R.id.rvHourlyForecast)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_forecast_group, parent, false)
            return GroupViewHolder(view)
        }

        override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
            val group = groups[position]

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
            val dateObj = inputFormat.parse(group.date)
            val dayTitle = outputFormat.format(dateObj ?: Date())

            holder.tvDayTitle.text = dayTitle

            val adapter = ForecastAdapter()
            holder.rvDayItems.layoutManager = LinearLayoutManager(holder.itemView.context, LinearLayoutManager.HORIZONTAL, false)
            holder.rvDayItems.adapter = adapter
            adapter.submitList(group.items)
        }

        override fun getItemCount(): Int = groups.size
    }