package com.adly.skycast

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import com.adly.skycast.ui.home.HomeViewModel
import com.adly.skycast.utils.LocaleHelper

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.scheduleDailyCleanup(this)
        //viewModel.scheduleAlertWorker(this)

    }
    override fun attachBaseContext(newBase: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(newBase)
        val lang = prefs.getString("language", "en") ?: "en"
        val context = LocaleHelper.setAppLocale(newBase, lang)
        super.attachBaseContext(context)
    }

}