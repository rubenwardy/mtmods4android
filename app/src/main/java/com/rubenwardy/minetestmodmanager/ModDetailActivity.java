package com.rubenwardy.minetestmodmanager;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
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

import com.rubenwardy.minetestmodmanager.models.Events;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.List;

/**
 * An activity representing a single Mod detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ModListActivity}.
 */
public class ModDetailActivity
        extends AppCompatActivity {

    String listname, modname, author;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_detail);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ModManager modman = ModManager.getInstance();

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Only setup fragment if there is no saved state
        if (savedInstanceState == null) {
            listname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_LIST);
            modname = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_NAME);
            author = getIntent().getStringExtra(ModDetailFragment.ARG_MOD_AUTHOR);
            setupFragment(listname, modname, author);
            setupToolBar(modman, listname, modname, author);
        } else {
//            onRestoreInstanceState(savedInstanceState);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.i("mda", "save");
        state.putString(ModDetailFragment.ARG_MOD_LIST, listname);
        state.putString(ModDetailFragment.ARG_MOD_NAME, modname);
        state.putString(ModDetailFragment.ARG_MOD_AUTHOR, author);
    }

    @Override
    public void onRestoreInstanceState(Bundle state) {
        Log.i("mda", "restore");
        listname = state.getString(ModDetailFragment.ARG_MOD_LIST);
        modname = state.getString(ModDetailFragment.ARG_MOD_NAME);
        author = state.getString(ModDetailFragment.ARG_MOD_AUTHOR);
        setupFragment(listname, modname, author);
    }

    private void setupToolBar(ModManager modman, String listname, String modname, String author) {
        CollapsingToolbarLayout ctoolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        if (ctoolbar == null) {
            return;
        }

        ModList list = modman.getModList(listname);
        if (list == null) {
            return;
        }

        Mod mod = list.get(modname, author);
        if (mod == null) {
            return;
        }

        // Set title
        ctoolbar.setTitle(mod.title);
        ctoolbar.setExpandedTitleColor(Color.TRANSPARENT);

        if (mod.screenshot_uri != null && !mod.screenshot_uri.equals("")) {
            Drawable d = Drawable.createFromPath(mod.screenshot_uri);
            if (d != null) {
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ctoolbar.setBackground(d);
                } else {
                    //noinspection deprecation
                    ctoolbar.setBackgroundDrawable(d);
                }

                NestedScrollView scroll =
                        (NestedScrollView) findViewById(R.id.mod_detail_container);
                scroll.requestFocus();
            }
        } else if (list.type == ModList.ModListType.EMLT_ONLINE) {
            modman.fetchScreenshot(getApplicationContext(), mod.name, mod.author);
        }

    }

    private void setupFragment(String listname, String modname, String author) {
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


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onModInstall(final Events.ModInstallEvent e) {
        Resources res = getResources();
        if (e.didError()) {
            String text = String.format(res.getString(R.string.event_failed_install),
                    e.modname, e.error);
            Snackbar.make(findViewById(R.id.mod_detail_container), text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            ModManager modman = ModManager.getInstance();
            List<String> uninstalled = modman.postInstallCheckDeps(modman.getModList(e.list).get(e.modname, null));
            for (String a : uninstalled) {
                Log.e("MDAct", "Mod not installed: " + a);
            }

            if (e.modname.equals(modname)) {
                Intent intent = new Intent(this, ModDetailActivity.class);
                intent.putExtra(ModDetailFragment.ARG_MOD_LIST, e.list);
                intent.putExtra(ModDetailFragment.ARG_MOD_AUTHOR, author);
                intent.putExtra(ModDetailFragment.ARG_MOD_NAME, modname);

                startActivity(intent);
            } else {
                String text = String.format(res.getString(R.string.event_installed_mod),
                        e.modname);
                Snackbar.make(findViewById(R.id.mod_detail_container), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        }
    }

    @Subscribe
    public void onModUninstall(final Events.ModUninstallEvent e) {
        if (e.didError()) {
            Resources res = getResources();
            String text = String.format(res.getString(R.string.event_failed_uninstall),
                    e.modname, e.error);
            Snackbar.make(findViewById(R.id.mod_detail_container), text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
            finish();
        }
    }

    @Subscribe
    public void onSearch(final Events.SearchEvent e) {
        Intent k = new Intent(this, ModListActivity.class);
        k.setAction(ModListActivity.ACTION_SEARCH);
        k.putExtra(ModListActivity.PARAM_QUERY, e.query);
        startActivity(k);
    }

    @Subscribe
    public void onFetchedScreenshot(final Events.FetchedScreenshotEvent e) {
        if (e.didError()) {
            Log.e("MDAct", e.error);
        } else {
            String dest = e.filepath;
            Drawable d = Drawable.createFromPath(dest);
            if (d != null) {
                CollapsingToolbarLayout ctoolbar = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    ctoolbar.setBackground(d);
                } else {
                    //noinspection deprecation
                    ctoolbar.setBackgroundDrawable(d);
                }
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
