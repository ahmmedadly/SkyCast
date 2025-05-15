package com.adly.skycast

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Optional: Fullscreen splash
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        // Typewriter animation for welcome message
        val tvWelcomeMsg = findViewById<TextView>(R.id.tvWelcomeMsg)
        val welcomeText = "Rain or shine, I’ve got your forecast."
        val delay = 50L
        val index = intArrayOf(0)
        val handler = Handler(Looper.getMainLooper())

        handler.postDelayed(object : Runnable {
            override fun run() {
                if (index[0] <= welcomeText.length) {
                    tvWelcomeMsg.text = welcomeText.substring(0, index[0])
                    index[0]++
                    handler.postDelayed(this, delay)
                }
            }
        }, 1000)

        // App name fade-in animation
        val appNameText = findViewById<TextView>(R.id.appName).apply {
            alpha = 0f
        }

        ObjectAnimator.ofFloat(appNameText, View.ALPHA, 1f).apply {
            duration = 2000
            startDelay = 500
            start()
        }

        // Lottie animation listener to go to MainActivity
        val lottieAnimationView: LottieAnimationView = findViewById(R.id.splashAnimation)
        lottieAnimationView.addAnimatorListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
            }
        })
    }
}
