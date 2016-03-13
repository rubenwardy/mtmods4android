package com.rubenwardy.minetestmodmanager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;

import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModEventReceiver;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

/**
 * An activity representing a single Mod detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ModListActivity}.
 */
public class ModDetailActivity
        extends AppCompatActivity
        implements ModEventReceiver {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ModManager modman = new ModManager();
        modman.setEventReceiver(this);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // savedInstanceState is non-null when there is mFragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the mFragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            String listname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_LIST);
            String modname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_NAME);
            String author = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_AUTHOR);
            CollapsingToolbarLayout ctoolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
            if (ctoolbar != null) {
                ModList list = modman.get(listname);
                if (list != null) {
                    Mod mod = list.get(modname, author);
                    if (mod.screenshot_uri != null) {
                        Drawable d = Drawable.createFromPath(mod.screenshot_uri);
                        if (d != null) {
                            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                                ctoolbar.setBackground(d);
                            } else {
                                ctoolbar.setBackgroundDrawable(d);
                            }

                            NestedScrollView scroll =
                                    (NestedScrollView) findViewById( R.id.mod_detail_container);
                            scroll.requestFocus();
                        }
                    }
                }
            }

            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(ModDetailFragment.ARG_MOD_LIST, listname);
            arguments.putString(ModDetailFragment.ARG_MOD_NAME, modname);
            arguments.putString(ModDetailFragment.ARG_MOD_AUTHOR, author);
            ModDetailFragment fragment = new ModDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.mod_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        ModManager modman = new ModManager();
        modman.setEventReceiver(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        ModManager modman = new ModManager();
        modman.unsetEventReceiver(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        ModManager modman = new ModManager();
        modman.unsetEventReceiver(this);
    }


    @Override
    public void onModEvent(@NonNull Bundle bundle) {
        String action = bundle.getString(PARAM_ACTION);
        if (action == null) {
            return;
        } if (action.equals(ACTION_UNINSTALL)) {
            finish();
        } else if (action.equals(ACTION_SEARCH)) {
            Intent k = new Intent(this, ModListActivity.class);
            Bundle extras = new Bundle();
            extras.putString(ModListActivity.PARAM_ACTION, ModListActivity.ACTION_SEARCH);
            extras.putString(ModListActivity.PARAM_ADDITIONAL, bundle.getString(PARAM_ADDITIONAL));
            k.putExtras(extras);
            startActivity(k);
            return;
        } else if (action.equals(ACTION_INSTALL)) {
            Resources res = getResources();
            if (bundle.containsKey(PARAM_ERROR)) {
                String text = String.format(res.getString(R.string.failed_install),
                        bundle.getString(PARAM_MODNAME), bundle.getString(PARAM_ERROR));
                Snackbar.make(findViewById(R.id.mod_detail_container), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            } else {
                String text = String.format(res.getString(R.string.installed_mod),
                        bundle.getString(PARAM_MODNAME));
                Snackbar.make(findViewById(R.id.mod_detail_container), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            NavUtils.navigateUpTo(this, new Intent(this, ModListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
