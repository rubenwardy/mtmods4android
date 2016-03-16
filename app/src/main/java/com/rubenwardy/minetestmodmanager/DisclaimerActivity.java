package com.rubenwardy.minetestmodmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

public class DisclaimerActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.rubenwardy.minetestmodmanager.PREFS";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        //toolbar.setTitle(getTitle());

        final String listname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_LIST);
        final String modname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_NAME);
        final String author = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_AUTHOR);

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
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("agreed_to_disclamer", true);
                editor.apply();

                if (listname != null && modname != null) {
                    ModManager modman = new ModManager();
                    ModList list = modman.get(listname);
                    if (list != null) {
                        Mod mod = list.get(modname, author);
                        if (mod != null && mod.link != null) {
                            modman.installUrlModAsync(getApplicationContext(), mod,
                                    mod.link,
                                    modman.getInstallDir());
                        } else {
                            Log.e("DAct", "Unable to find an installable mod of that name! " + modname);
                        }
                    } else {
                        Log.e("DAct", "Unable to find a ModList of that id! " + listname);
                    }
                }
                finish();
            }
        });
    }
}
