package com.rubenwardy.minetestmodmanager;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModEventReceiver;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * An activity representing a list of Mods. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ModDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ModListActivity
        extends AppCompatActivity
        implements ModEventReceiver, SearchView.OnQueryTextListener {

    // Layout related
    private boolean isTwoPane;
    private Menu menu;

    // Modman
    private ModManager modman;
    private final String mod_list_url = "http://app-mtmm.rubenwardy.com/v2/list/";

    // Other
    private String search_filter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        modman = new ModManager();

        //
        // Read opening intent
        //
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(PARAM_ACTION)) {
            String action = extras.getString(PARAM_ACTION);
            if (action != null && action.equals(ACTION_SEARCH)) {
                search_filter = extras.getString(PARAM_ADDITIONAL);
            }
        }

        setupLayout();
        scanFileSystem();
    }

    private void setupLayout() {
        setContentView(R.layout.activity_mod_list);

        // Setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Pull down to refresh
        SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.refresh);
        assert srl != null;
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                for (ModList list : ModManager.lists_map.values()) {
                    if (list.type == ModList.ModListType.EMLT_PATH) {
                        modman.update(list);
                    }
                }
                modman.fetchModListAsync(getApplicationContext(), mod_list_url);
            }
        });

        // Set rate callback
        TextView rate_yes = (TextView) findViewById(R.id.rate_yes);
        assert rate_yes != null;
        rate_yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                SharedPreferences settings = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("shown_rate_me", true);
                editor.apply();
                findViewById(R.id.rate_me).setVisibility(View.GONE);

                Context context = getApplicationContext();
                Uri uri = Uri.parse("market://details?id=" + context.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + context.getPackageName())));
                }
            }
        });

        // Set no callback
        TextView rate_no = (TextView) findViewById(R.id.rate_no);
        assert rate_no != null;
        rate_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                SharedPreferences settings = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("shown_rate_me", true);
                editor.apply();
                findViewById(R.id.rate_me).setVisibility(View.GONE);
            }
        });

        // Set no callback
        TextView help_query_close = (TextView) findViewById(R.id.help_config_close);
        assert help_query_close != null;
        help_query_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(@NonNull View v) {
                SharedPreferences settings = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean("shown_help_config", true);
                editor.apply();
                findViewById(R.id.help_config).setVisibility(View.GONE);
            }
        });

        if (findViewById(R.id.mod_detail_container) != null) {
            isTwoPane = true;
        }
    }

    private void scanFileSystem() {
        File extern = Environment.getExternalStorageDirectory();
        if (!extern.exists()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle(R.string.dialog_noext_title);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage(R.string.dialog_noext_msg);
            alertDialogBuilder.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ModListActivity.this.finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        // Add Minetest mod location
        File mt_root = new File(extern, "/Minetest");
        File mt_dir = new File(mt_root, "/mods");
        File mul_root = new File(extern, "/MultiCraft");
        File mul_dir = new File(mul_root, "/mods");

        // Check there is at least one mod dir
        if (mt_root.exists() && !mt_dir.exists() && mt_dir.mkdirs()) {
            mt_dir = new File(mt_root, "/mods");
        }
        if (mul_root.exists() && !mul_dir.exists() && mul_dir.mkdirs()) {
            mul_dir = new File(mul_root, "/mods");
        }
        if (!mt_dir.exists() && !mul_dir.exists() && !mt_dir.mkdirs()) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setTitle(R.string.dialog_nomt_title);
            alertDialogBuilder.setMessage(R.string.dialog_nomt_msg);
            alertDialogBuilder.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ModListActivity.this.finish();
                }
            });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
            return;
        }

        // Bind event receiver
        modman.setEventReceiver(this);

        // Add lists
        Resources res = getResources();
        modman.getModsFromDir(res.getString(R.string.modlist_minetest), mt_root.getAbsolutePath(), mt_dir.getAbsolutePath());
        modman.getModsFromDir(res.getString(R.string.modlist_multicraft), mul_root.getAbsolutePath(), mul_dir.getAbsolutePath());

        View recyclerView = findViewById(R.id.mod_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        modman.fetchModListAsync(getApplicationContext(), mod_list_url);
    }

    @Override
    protected void onResume() {
        super.onResume();

        modman.setEventReceiver(this);

        for (ModList list : ModManager.lists_map.values()) {
            checkChanges(list);
        }

        SharedPreferences settings = getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
        assert settings != null;

        // Check whether to show rate me
        boolean enabled = BuildConfig.ENABLE_RATE_ME;
        if (enabled) {
            boolean dismissed = settings.getBoolean("shown_rate_me", false);
            int installs = settings.getInt("installs_so_far", 0);
            if (!dismissed && installs > 5) {
                findViewById(R.id.rate_me).setVisibility(View.VISIBLE);
            }
        }

        // Check whether to show hint
        boolean shown_help_config = settings.getBoolean("shown_help_config", false);
        if (!shown_help_config && modman.getNumberOfInstalledMods() > 0) {
            findViewById(R.id.help_config).setVisibility(View.VISIBLE);
        }
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
        Resources res = getResources();
        String action = bundle.getString(PARAM_ACTION);
        if (action == null) {
            return;
        } else if (action.equals(ACTION_INSTALL)) {
            final String modname = bundle.getString(PARAM_MODNAME);
            if (bundle.containsKey(PARAM_ERROR)) {
                String error = bundle.getString(PARAM_ERROR);
                String text = String.format(res.getString(R.string.failed_install), modname, error);
                Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            } else {
                String text = String.format(res.getString(R.string.installed_mod), modname);
                Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        } else if (isTwoPane && action.equals(ACTION_UNINSTALL)) {
            final String modname = bundle.getString(PARAM_MODNAME);
            if (bundle.containsKey(PARAM_ERROR)) {
                final String error = bundle.getString(PARAM_ERROR);
                final String text = String.format(res.getString(R.string.failed_uninstall), modname, error);
                Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                return;
            } else {
                FragmentManager fragman = getSupportFragmentManager();
                Fragment frag = fragman.findFragmentById(R.id.mod_detail_container);
                getSupportFragmentManager().beginTransaction()
                        .remove(frag)
                        .commit();
            }
        } else if (isTwoPane && action.equals(ACTION_SEARCH)) {
            String query = bundle.getString(PARAM_ADDITIONAL);

            // Update SearchView
            final MenuItem item = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setIconified(false);
            searchView.setQuery(query, true);
            searchView.clearFocus();

            // Update RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
            assert recyclerView != null;
            fillRecyclerView(recyclerView, query);
        } else if (action.equals(ACTION_FETCH_MODLIST)) {
            SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.refresh);
            srl.setRefreshing(false);
        }

        checkChanges(bundle.getString(ModEventReceiver.PARAM_DEST_LIST));
    }

    private void checkChanges(@Nullable String listname) {
        if (listname == null || listname.equals("")) {
            return;
        }

        ModList list = modman.get(listname);
        checkChanges(list);
    }

    private void checkChanges(@Nullable ModList list) {
        if (list != null && !list.valid &&
                (list.type == ModList.ModListType.EMLT_ONLINE || modman.update(list))) {
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
            assert recyclerView != null;
            fillRecyclerView(recyclerView, null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_mod_list, menu);

        final MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(this);

        if (search_filter != null) {
            searchView.setIconified(false);
            searchView.setQuery(search_filter, true);
            searchView.clearFocus();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_world: {
            Intent k = new Intent(this, WorldConfigActivity.class);
            startActivity(k);
            return true;
        }
        case R.id.action_about: {
            Intent k = new Intent(this, AboutActivity.class);
            startActivity(k);
            return true;
        } }

        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
        assert recyclerView != null;
        fillRecyclerView(recyclerView, query);

        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return onQueryTextChange(query);
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //Add your adapter to the sectionAdapter
        ModListRecyclerViewAdapter adapter = new ModListRecyclerViewAdapter();
        SectionedRecyclerViewAdapter sectionedAdapter =
                new SectionedRecyclerViewAdapter(this, R.layout.section, R.id.section_text, adapter);
        recyclerView.setAdapter(sectionedAdapter);

        // Fill view
        fillRecyclerView(recyclerView, null);
    }

    private void fillRecyclerView(@NonNull RecyclerView recyclerView, @Nullable String query) {
        if (query != null) {
            query = query.trim().toLowerCase();
            if (query.isEmpty()) {
                search_filter = null;
            } else {
                search_filter = query;
            }
        }

        query = search_filter;
        String match_name = null;
        String match_author = null;
        if (query != null) {
            boolean changed;
            do {
                changed = false;
                if (query.startsWith("name:")) {
                    query = query.substring(5, query.length()).trim();
                    int idx = query.indexOf(' ');
                    if (idx >= 0) {
                        match_name = query.substring(0, idx);
                        query = query.substring(idx + 1, query.length()).trim();
                    } else {
                        match_name = query;
                        query = "";
                    }
                    changed = true;
                } else if (query.startsWith("author:")) {
                    query = query.substring(7, query.length()).trim();
                    int idx = query.indexOf(' ');
                    if (idx >= 0) {
                        match_author = query.substring(0, idx);
                        query = query.substring(idx + 1, query.length()).trim();
                    } else {
                        match_author = query;
                        query = "";
                    }
                    changed = true;
                }
            } while(changed);
        }

        Resources res = getResources();
        SectionedRecyclerViewAdapter adapter =
                (SectionedRecyclerViewAdapter)recyclerView.getAdapter();

        // Add path lists
        List<Mod> mods = new ArrayList<>();
        List<SectionedRecyclerViewAdapter.Section> sections =
                new ArrayList<>();
        for (ModList list : ModManager.lists_map.values()) {
            if (list.type == ModList.ModListType.EMLT_PATH) {
                sections.add(new SectionedRecyclerViewAdapter.Section(mods.size(), list.title, list.getWorldsDir()));
                if (query != null) {
                    for (Mod mod : list.mods) {
                        if (
                                (match_name == null || mod.name.toLowerCase().equals(match_name)) &&
                                (match_author == null || mod.author.toLowerCase().equals(match_author)) &&
                                (query.equals("") || mod.name.toLowerCase().contains(query)
                                        || mod.desc.toLowerCase().contains(query)
                                        || mod.author.toLowerCase().contains(query))
                        ) {
                            mods.add(mod);
                        }
                    }
                } else {
                    mods.addAll(list.mods);
                }

            }
        }
        sections.add(new SectionedRecyclerViewAdapter.Section(mods.size(),
                res.getString(R.string.modlist_available_mods), null));

        ModList list = modman.getAvailableMods();
        if (list != null) {
            if (query != null) {
                for (Mod mod : list.mods) {
                    if (
                            (match_name == null || mod.name.toLowerCase().equals(match_name)) &&
                            (match_author == null || mod.author.toLowerCase().equals(match_author)) &&
                            (query.equals("") || mod.name.toLowerCase().contains(query)
                                    || mod.desc.toLowerCase().contains(query)
                                    || mod.author.toLowerCase().contains(query))
                    ) {
                        mods.add(mod);
                    }
                }
            } else {
                mods.addAll(list.mods);
            }
        }

        adapter.setMods(mods);
        // Set sections and update.
        SectionedRecyclerViewAdapter.Section[] dummy =
                new SectionedRecyclerViewAdapter.Section[sections.size()];
        adapter.setSections(sections.toArray(dummy));
        adapter.notifyDataSetChanged();
    }

    public class ModListRecyclerViewAdapter
            extends RecyclerView.Adapter<ModListRecyclerViewAdapter.ViewHolder> {

        private List<Mod> mods;

        public ModListRecyclerViewAdapter() {
            mods = new ArrayList<>();
        }

        public void setMods(List<Mod> mods) {
            this.mods = mods;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mod_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
            // Get Mod
            holder.mod = mods.get(position);

            //
            // Fill out TextViews
            //

            holder.view_modname.setText(holder.mod.name);
            holder.view_author.setText(holder.mod.author);
            holder.view_description.setText(holder.mod.getShortDesc());

            //
            // Register callback
            //

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
                    if (isTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ModDetailFragment.ARG_MOD_LIST, holder.mod.listname);
                        arguments.putString(ModDetailFragment.ARG_MOD_NAME, holder.mod.name);
                        ModDetailFragment fragment = new ModDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mod_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ModDetailActivity.class);
                        intent.putExtra(ModDetailFragment.ARG_MOD_LIST, holder.mod.listname);
                        intent.putExtra(ModDetailFragment.ARG_MOD_AUTHOR, holder.mod.author);
                        intent.putExtra(ModDetailFragment.ARG_MOD_NAME, holder.mod.name);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mods.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @NonNull public final View view;
            @NonNull public final TextView view_modname;
            @NonNull public final TextView view_author;
            @NonNull public final TextView view_description;
            @Nullable public Mod mod;

            public ViewHolder(@NonNull View view) {
                super(view);
                this.view = view;
                view_modname = (TextView) view.findViewById(R.id.modname);
                view_author = (TextView) view.findViewById(R.id.author);
                view_description = (TextView) view.findViewById(R.id.description);
            }

            @NonNull
            @Override
            public String toString() {
                return super.toString() + " '" + view_description.getText() + "'";
            }
        }
    }
}
