package com.adly.skycast

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.adly.skycast.ui.home.HomeViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        viewModel.scheduleDailyCleanup(this)
    }

}