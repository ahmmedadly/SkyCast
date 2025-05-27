package com.adly.skycast.utils

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.*

object LocaleHelper {

    fun setAppLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
            context
        }
    }
    fun translateDescription(desc: String, locale: String): String {
        if (locale == "ar") {
            return when (desc.lowercase(Locale.ROOT)) {
                "clear sky" -> "سماء صافية"
                "few clouds" -> "سحب قليلة"
                "scattered clouds" -> "سحب متناثرة"
                "broken clouds" -> "سحب متقطعة"
                "shower rain" -> "زخات مطر"
                "rain" -> "مطر"
                "thunderstorm" -> "عاصفة رعدية"
                "snow" -> "ثلج"
                "mist" -> "ضباب"
                else -> desc
            }
        }
        return desc
    }

}
