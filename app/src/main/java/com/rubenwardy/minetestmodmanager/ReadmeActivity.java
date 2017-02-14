package com.rubenwardy.minetestmodmanager;

import android.content.res.Resources;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.Utils;

import java.io.File;

public class ReadmeActivity extends AppCompatActivity {
    public static final String ARG_MOD_PATH = "path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_readme);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Resources res = getResources();
            actionBar.setTitle(res.getString(R.string.readme_title));
        }

        TextView text = (TextView) findViewById(R.id.text);
        assert text != null;

        String path = getIntent().getStringExtra(ARG_MOD_PATH);
        File folder = new File(path);
        if (!folder.isDirectory()) {
            text.setText("Unable to find dir " + path);
            return;
        }

        File readme = Utils.getReadmePath(folder);
        if (readme == null) {
            text.setText("Unable to find any valid readme files");
        } else {
            text.setText(Utils.readTextFile(readme));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case android.R.id.home:
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
