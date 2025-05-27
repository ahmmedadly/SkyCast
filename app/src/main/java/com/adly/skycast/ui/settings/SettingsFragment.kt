package com.adly.skycast.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.adly.skycast.R
import com.adly.skycast.utils.LocaleHelper

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

            override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
                setPreferencesFromResource(R.xml.preferences, rootKey)
            }

            override fun onResume() {
                super.onResume()
                preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(this)
            }

            override fun onPause() {
                super.onPause()
                preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(this)
            }
            override fun onStop() {
                super.onStop()
                requireActivity().recreate()
            }

            override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences?, key: String?) {
                when (key) {
                    "language" -> {
                        // Apply language immediately or prompt restart
                    }
                    "temp_unit", "wind_speed_unit" -> {
                        // Optionally update displayed units in UI
                    }
                    "manual_location", "coordinates" -> {
                        // Manually fetch weather using coordinates if enabled
                    }
                }
            }
        }

    }
    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val languagePref = findPreference<ListPreference>("language")
            languagePref?.setOnPreferenceChangeListener { _, newValue ->
                val newLang = newValue.toString()
                LocaleHelper.setAppLocale(requireContext(), newLang)

                // Refresh UI
                activity?.recreate()
                true
            }
        }
    }

}