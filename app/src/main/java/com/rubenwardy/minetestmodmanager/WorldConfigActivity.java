package com.rubenwardy.minetestmodmanager;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.rubenwardy.minetestmodmanager.manager.MinetestConf;
import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldConfigActivity extends AppCompatActivity {
    String modpath = null;
    ModListRecyclerViewAdapter adapter = null;
    MinetestConf conf = null;
    File conf_file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_config);

        File extern = Environment.getExternalStorageDirectory();
        File mt_root = new File(extern, "/Minetest");
        File mt_dir = new File(mt_root, "/mods");
        modpath = mt_dir.getAbsolutePath();
        Log.w("WCAct", modpath);

        conf = new MinetestConf();
        conf_file = new File(mt_root, "/worlds/singleplayerworld/world.mt");
        conf.read(conf_file);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            Resources res = getResources();
            actionBar.setTitle(res.getString(R.string.world_config));
        }

        View recyclerView = findViewById(R.id.mod_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_world_config, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.accept:
                conf.save(conf_file);
                return true;
        }

        return false;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //Add your adapter to the sectionAdapter
        adapter = new ModListRecyclerViewAdapter();
        recyclerView.setAdapter(adapter);

        ModManager modman = new ModManager();
        ModList list = modman.get(modpath);
        if (list == null) {
            finish();
            return;
        }
        List<Mod> mods = new ArrayList<>(list.mods);
        adapter.setMods(mods);

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
                    .inflate(R.layout.mod_checklist_content, parent, false);
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

            //
            // Register callback
            //
            boolean enabled = false;
            if (holder.mod.type == Mod.ModType.EMT_MOD) {
                enabled = conf.getBool("load_mod_" + holder.mod.name);
            } else {
                // TODO: modpack support
                Log.w("WCMLA", "Modpack found but unable to enable modpacks yet!");
                holder.view_modname.setTextColor(Color.RED);
                holder.view_modname.setText(holder.mod.name + " (Modpack)");
            }
            holder.view_modname.setChecked(enabled);

            holder.view_modname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton comp_btn, boolean checked) {
                    if (holder.mod.type == Mod.ModType.EMT_MOD) {
                        if (checked) {
                            Log.w("WCMLA", "Enabling mod " + holder.mod.name);
                        } else {
                            Log.w("WCMLA", "Disabling mod " + holder.mod.name);
                        }
                        conf.setBool("load_mod_" + holder.mod.name, checked);
                    } else {
                        // TODO: modpack support
                    }
                }
            });
        }

        protected void enDisMod(Mod mod, boolean enable) {

        }

        @Override
        public int getItemCount() {
            return mods.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @NonNull
            public final View view;
            @NonNull
            public final CheckBox view_modname;
            @Nullable
            public Mod mod;

            public ViewHolder(@NonNull View view) {
                super(view);
                this.view = view;
                view_modname = (CheckBox) view.findViewById(R.id.modname);
            }

            @NonNull
            @Override
            public String toString() {
                return super.toString() + " '" + view_modname.getText() + "'";
            }
        }
    }
}
