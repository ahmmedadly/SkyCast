package com.adly.skycast.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.adly.skycast.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

    }
}