package com.rubenwardy.minetestmodmanager

import android.app.Activity
import android.app.Application
import android.content.Context
import android.support.v7.app.AppCompatDelegate
import com.rubenwardy.minetestmodmanager.views.DisclaimerActivity

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val theme = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0).getInt("theme", 0)
        AppCompatDelegate.setDefaultNightMode(themeToDayNightMode(theme))
    }
}

fun themeToDayNightMode(themeId: Int): Int {
    if (themeId == 0) {
        return AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
    } else if (themeId == 1) {
        return AppCompatDelegate.MODE_NIGHT_NO
    } else if (themeId == 2) {
        return AppCompatDelegate.MODE_NIGHT_YES
    } else {
        return AppCompatDelegate.MODE_NIGHT_AUTO
    }
}