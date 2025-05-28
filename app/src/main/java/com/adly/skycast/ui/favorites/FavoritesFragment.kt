package com.adly.skycast.ui.favorites

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.adly.skycast.R
import com.adly.skycast.databinding.FragmentFavoritesBinding
import com.adly.skycast.ui.home.HomeViewModel
import com.google.android.material.snackbar.Snackbar

class FavoritesFragment : Fragment() {
    private lateinit var binding: FragmentFavoritesBinding
    private val viewModel: HomeViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        val adapter = FavoriteLocationAdapter(
            onClick = { favorite ->
                viewModel.fetchWeatherByCoordinates(favorite.lat, favorite.lon)
                findNavController().navigate(R.id.weatherDetailsFragment)
            },
            onDelete = { favorite ->
                // Delete the favorite
                viewModel.removeFavorite(favorite)
                // Show undo Snackbar
                Snackbar.make(
                    binding.root,
                    getString(R.string.removed_favorite, favorite.name),
                    Snackbar.LENGTH_LONG
                )                    .setAction("UNDO") {
                    viewModel.addFavorite(favorite) // Re-add if undone
                }
                    .show()
            }
        )

        binding.rvFavorites.adapter = adapter
        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())

        viewModel.favorites.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Handle back press to go back to previous screen
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().popBackStack()
        }
    }
}