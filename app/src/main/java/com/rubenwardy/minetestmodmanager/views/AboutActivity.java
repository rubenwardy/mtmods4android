package com.rubenwardy.minetestmodmanager.views;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.rubenwardy.minetestmodmanager.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

}
