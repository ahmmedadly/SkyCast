package com.adly.skycast.ui.favorites

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.adly.skycast.data.model.FavoriteLocationEntity
import com.adly.skycast.databinding.ItemFavoriteLocationBinding

class FavoriteLocationAdapter(
    private val onClick: (FavoriteLocationEntity) -> Unit, // Fixed: Added comma
    private val onDelete: (FavoriteLocationEntity) -> Unit
) : ListAdapter<FavoriteLocationEntity, FavoriteLocationAdapter.FavViewHolder>(DIFF_CALLBACK) {

    class FavViewHolder(private val binding: ItemFavoriteLocationBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: FavoriteLocationEntity,
            onClick: (FavoriteLocationEntity) -> Unit,
            onDelete: (FavoriteLocationEntity) -> Unit // Added parameter
        ) {
            binding.tvCityName.text = "${item.name}, ${item.country}"
            binding.root.setOnClickListener { onClick(item) }
            binding.btnDelete.setOnClickListener { onDelete(item) } // Fixed: Use passed onDelete
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemFavoriteLocationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavViewHolder(binding)
        // Removed erroneous holder.bind() call
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(getItem(position), onClick, onDelete) // Fixed: Added onDelete parameter
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FavoriteLocationEntity>() {
            override fun areItemsTheSame(oldItem: FavoriteLocationEntity, newItem: FavoriteLocationEntity) =
                oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: FavoriteLocationEntity, newItem: FavoriteLocationEntity) =
                oldItem == newItem
        }
    }
}
