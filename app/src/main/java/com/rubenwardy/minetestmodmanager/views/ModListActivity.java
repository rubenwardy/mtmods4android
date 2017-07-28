package com.rubenwardy.minetestmodmanager.views;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;
import com.rubenwardy.minetestmodmanager.BuildConfig;
import com.rubenwardy.minetestmodmanager.R;
import com.rubenwardy.minetestmodmanager.models.Events;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;
import com.rubenwardy.minetestmodmanager.presenters.ModListPresenter;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

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
        implements SearchView.OnQueryTextListener, ModListPresenter.View {

    ModListPresenter presenter = new ModListPresenter(this);

    public final static String PARAM_ACTION = "action";
    public final static String ACTION_SEARCH = "search";
    public final static String PARAM_QUERY = "query";

    // Layout related
    private boolean isTwoPane;
    private Menu menu;

    // Modman
    private ModManager modman;

    // Other
    private String search_filter = null;

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        EventBus.getDefault().register(presenter);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        EventBus.getDefault().unregister(presenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        modman = ModManager.getInstance();

        //
        // Read opening intent
        //
        Intent intent = getIntent();
        String action = intent.getAction();
        if (action != null && (action.equals("com.google.android.gms.actions.SEARCH_ACTION") ||
                intent.getAction().equals(ACTION_SEARCH))) {
            search_filter = intent.getExtras().getString(PARAM_QUERY);
        } else {
            Log.i("MAct", "Intent was " + intent.getAction());
        }


        setupLayout();
        setupRecyclerView((RecyclerView) findViewById(R.id.mod_list));

        presenter.scanFileSystem();
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
                presenter.forceModListRefresh();
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

    @Override
    protected void onResume() {
        super.onResume();

        for (ModList list : ModManager.getInstance().getAllModLists()) {
            updateModListAndRecyclerView(list);
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



    //
    // VIEW METHODS
    //

    @Override
    public void showNoExternalFSDialog() {
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
    }

    @Override
    public void showMinetestNotInstalledDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setTitle(R.string.dialog_no_minetest);
        alertDialogBuilder.setMessage(R.string.dialog_no_minetest_msg);
        alertDialogBuilder.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        alertDialogBuilder.setPositiveButton(R.string.mod_action_install, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Context context = getApplicationContext();
                Uri uri = Uri.parse("market://details?id=net.minetest.minetest");
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
                            Uri.parse("http://play.google.com/store/apps/details?id=net.minetest.minetest")));
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void showNoGameAvailable() {
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
    }

    @Override
    public boolean getIsMinetestInstalled() {
        try {
            getPackageManager().getPackageInfo("net.minetest.minetest", 0);
            return true;
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateModListAndRecyclerView(@Nullable String listname) {
        if (listname == null || listname.equals("")) {
            return;
        }

        ModList list = modman.getModList(listname);
        updateModListAndRecyclerView(list);
    }

    private void updateModListAndRecyclerView(@Nullable ModList list) {
        if (list != null) {
            modman.updateLocalModList(list);

            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
            assert recyclerView != null;
            fillRecyclerView(recyclerView, null);
        }
    }

    @Override
    public void showModInstallErrorDialog(@NotNull String modname, @NotNull String error) {
        Resources res = getResources();
        String text = String.format(res.getString(R.string.event_failed_install), modname, error);
        Log.e("MLAct", text);
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void showInstallsDependsDialog(@NotNull List<String> uninstalled) {
        InstallDependsDialogFragment dialog = new InstallDependsDialogFragment();
        Bundle b = new Bundle();
        b.putStringArrayList("mods", new ArrayList<>(uninstalled));
        dialog.setArguments(b);
        dialog.show(getSupportFragmentManager(), "InstallDependsDialogFragment");
    }

    @Override
    public void showInstallMessage(@NotNull String modname) {
        String text = String.format(getResources().getString(R.string.event_installed_mod), modname);
        Log.i("ModListActivity", text);
        Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void showModOnlyIfTwoPane(@NotNull String list, @NotNull String modname) {
        if (isTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(ModDetailFragment.ARG_MOD_LIST, list);
            // not setting the author probably doesn't matter here, as mod names are unique in installed locations
            arguments.putString(ModDetailFragment.ARG_MOD_NAME, modname);
            ModDetailFragment fragment = new ModDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mod_detail_container, fragment)
                    .commit();
        }
    }




    //
    // MISC CALLBACKS
    //

    @Subscribe
    public void onModUninstall(final Events.ModUninstallEvent e) {
        Resources res = getResources();
        if (e.didError()) {
            String text = String.format(res.getString(R.string.event_failed_uninstall), e.modname, e.error);
            Log.e("ModListActivity", text);
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

            return;
        } else if (isTwoPane) {
            FragmentManager fragman = getSupportFragmentManager();
            Fragment frag = fragman.findFragmentById(R.id.mod_detail_container);
            getSupportFragmentManager().beginTransaction()
                    .remove(frag)
                    .commit();
        }

        Log.i("ModListActivity", "Uninstalled mod");

        updateModListAndRecyclerView(e.list);
    }

    @Subscribe
    public void onSearch(final Events.SearchEvent e) {
        if (isTwoPane) {
            // Update SearchView
            final MenuItem item = menu.findItem(R.id.action_search);
            final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
            searchView.setIconified(false);
            searchView.setQuery(e.query, true);
            searchView.clearFocus();

            // Update RecyclerView
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
            assert recyclerView != null;
            fillRecyclerView(recyclerView, e.query);
        }
    }

    @Subscribe
    public void onFetchedModlist(final Events.FetchedListEvent e) {
        SwipeRefreshLayout srl = (SwipeRefreshLayout) findViewById(R.id.refresh);
        srl.setRefreshing(false);

        if (e.didError()) {
            Resources res = getResources();
            String text = String.format(res.getString(R.string.failed_list_download), e.error);
            Log.e("ModListActivity", text);
            Snackbar.make(findViewById(android.R.id.content), text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        } else {
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
            Intent k = new Intent(this, SettingsAndAboutActivity.class);
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
        Log.i("ModListActivity", "Setting up recycler view");

        //Add your adapter to the sectionAdapter
        ModListRecyclerViewAdapter adapter = new ModListRecyclerViewAdapter();
        SectionedRecyclerViewAdapter sectionedAdapter =
                new SectionedRecyclerViewAdapter(this, R.layout.section, R.id.section_text, adapter);
        recyclerView.setAdapter(sectionedAdapter);

        FastScroller fastScroller = (FastScroller) findViewById(R.id.fastscroll);
        fastScroller.setRecyclerView(recyclerView);

        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Fill view
        fillRecyclerView(recyclerView, null);
    }

    private void fillRecyclerView(@NonNull RecyclerView recyclerView, @Nullable String query) {
        Log.i("ModListActivity", "Filling recycler view");

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
        String title_mods = res.getString(R.string.modlist_mods);
        for (ModList list : ModManager.getInstance().getAllModLists()) {
            if (list.type == ModList.ModListType.EMLT_MODS) {
                sections.add(new SectionedRecyclerViewAdapter.Section(mods.size(), String.format(title_mods, list.game_name), list.getWorldsDir()));
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
        // Set sections and updateLocalModList.
        SectionedRecyclerViewAdapter.Section[] dummy =
                new SectionedRecyclerViewAdapter.Section[sections.size()];
        adapter.setSections(sections.toArray(dummy));
        adapter.notifyDataSetChanged();
    }

    class ModListRecyclerViewAdapter
            extends RecyclerView.Adapter<ModListRecyclerViewAdapter.ViewHolder>
            implements SectionTitleProvider {

        private List<Mod> mods;

        ModListRecyclerViewAdapter() {
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
            Mod mod = holder.mod;

            //
            // Fill out TextViews
            //

            holder.view_modname.setText(mod.name);
            holder.view_author.setText(mod.author);
            holder.view_description.setText(mod.getShortDesc());

            ModManager mm = ModManager.getInstance();

            holder.view_installed.setVisibility((mm.getModInstalledList(mod.name, mod.author) != null) ? View.VISIBLE : View.GONE);

            Callback callback = new Callback() {
                @Override
                public void onSuccess() {
                    Bitmap imageBitmap = ((BitmapDrawable) holder.view_preview.getDrawable()).getBitmap();
                    RoundedBitmapDrawable imageDrawable = RoundedBitmapDrawableFactory.create(getResources(), imageBitmap);
                    imageDrawable.setCircular(true);
                    imageDrawable.setCornerRadius(Math.max(imageBitmap.getWidth(), imageBitmap.getHeight()) / 2.0f);
                    holder.view_preview.setImageDrawable(imageDrawable);
                }
                @Override
                public void onError() {
                }
            };


            if (mod.screenshot_uri != null && !mod.screenshot_uri.equals("")) {
                Picasso.with(getApplicationContext())
                        .load(new File(mod.screenshot_uri))
                        .fit()
                        .error(R.drawable.mod_preview_circle)
                        .placeholder(R.drawable.mod_preview_circle)
                        .into(holder.view_preview, callback);
            } else if (!mod.isLocalMod()) {
                Picasso.with(getApplicationContext())
                        .load("https://minetest-mods.rubenwardy.com/screenshot/" + mod.author + "/" + mod.name + "/")
                        .fit()
                        .error(R.drawable.mod_preview_circle)
                        .placeholder(R.drawable.mod_preview_circle)
                        .into(holder.view_preview, callback);
            }

            //
            // Register callback
            //

            holder.view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View v) {
                    if (isTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ModDetailFragment.ARG_MOD_LIST, holder.mod.listname);
                        arguments.putString(ModDetailFragment.ARG_MOD_AUTHOR, holder.mod.author);
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

        @Override
        public String getSectionTitle(int position) {
            return mods.get(position).name.substring(0, 1);
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            @NonNull public final View view;
            @NonNull final TextView view_modname;
            @NonNull final TextView view_author;
            @NonNull final TextView view_description;
            @NonNull final View view_installed;
            @NonNull final ImageView view_preview;
            @Nullable public Mod mod;

            ViewHolder(@NonNull View view) {
                super(view);
                this.view = view;
                view_modname = (TextView) view.findViewById(R.id.modname);
                view_author = (TextView) view.findViewById(R.id.author);
                view_description = (TextView) view.findViewById(R.id.description);
                view_installed   = (View) view.findViewById(R.id.mod_installed);
                view_preview = (ImageView) view.findViewById(R.id.mod_preview);
            }

            @NonNull
            @Override
            public String toString() {
                return super.toString() + " '" + view_description.getText() + "'";
            }
        }
    }
}
