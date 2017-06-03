package com.rubenwardy.minetestmodmanager.views;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.rubenwardy.minetestmodmanager.R;
import com.rubenwardy.minetestmodmanager.models.ModSpec;
import com.rubenwardy.minetestmodmanager.presenters.DisclaimerPresenter;

import org.jetbrains.annotations.NotNull;

public class DisclaimerActivity extends AppCompatActivity implements DisclaimerPresenter.View {
    public static final String PREFS_NAME = "com.rubenwardy.minetestmodmanager.PREFS";

    DisclaimerPresenter presenter = new DisclaimerPresenter(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Resources res = getResources();
            actionBar.setTitle(res.getString(R.string.disclaimer_title));
        }

        Button button = (Button) findViewById(R.id.accept);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                presenter.onAcceptClicked(getApplicationContext());
            }
        });
    }

    @Override
    public void setAgreedToDisclaimer() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean("agreed_to_disclaimer", true);
        editor.apply();
    }

    @Override
    public void finishActivity() {
        finish();
    }

    @NotNull
    @Override
    public ModSpec getModInfo() {
        final String listname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_LIST);
        final String modname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_NAME);
        final String author = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_AUTHOR);
        return new ModSpec(modname, author, listname);
    }
}
