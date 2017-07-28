package com.rubenwardy.minetestmodmanager.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.AppCompatDelegate
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

import com.rubenwardy.minetestmodmanager.R
import com.rubenwardy.minetestmodmanager.themeToDayNightMode

class SettingsAndAboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settingsandabout)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }

        val spinner = findViewById(R.id.settings_theme) as Spinner
        val adapter = ArrayAdapter.createFromResource(this, R.array.settings_theme_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.setSelection(getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0).getInt("theme", 0))
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val editor = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0).edit()
                editor.putInt("theme", position)
                editor.apply()

                delegate.setLocalNightMode(themeToDayNightMode(position))
            }
        }
    }

}
