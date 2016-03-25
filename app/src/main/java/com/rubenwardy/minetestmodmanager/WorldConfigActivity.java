package com.rubenwardy.minetestmodmanager;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.rubenwardy.minetestmodmanager.manager.MinetestConf;
import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WorldConfigActivity extends AppCompatActivity {
    @Nullable
    private String modpath = null;
    @Nullable
    private MinetestConf conf = null;
    @Nullable
    private File conf_file = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_world_config);

        File extern = Environment.getExternalStorageDirectory();
        File mt_root = new File(extern, "/Minetest");
        File mt_dir = new File(mt_root, "/mods");
        modpath = mt_dir.getAbsolutePath();

        conf = new MinetestConf();
        File world_dir = new File(mt_root, "/worlds/singleplayerworld");
        conf_file = new File(world_dir, "/world.mt");
        if (!world_dir.isDirectory()) {
            if (!world_dir.mkdirs()) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle(R.string.dialog_nowld_title);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setMessage(R.string.dialog_nowld_msg);
                alertDialogBuilder.setNegativeButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        WorldConfigActivity.this.finish();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return;
            }
        }
        if (!conf.read(conf_file)) {
            // Create configuration file
            conf.set("gameid", "minetest");
            conf.set("backend", "sqlite3");
        }

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
        if (conf == null) {
            return false;
        }

        switch (item.getItemId()) {
        case R.id.accept:
            conf.save(conf_file);
            Resources res = getResources();
            String text = res.getString(R.string.world_config_saved);
            Snackbar.make(findViewById(R.id.mod_list), text, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            return true;
        }

        return false;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        //Add your adapter to the sectionAdapter
        ModListRecyclerViewAdapter adapter = new ModListRecyclerViewAdapter();
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
            assert conf != null;
            boolean enabled = holder.mod.isEnabled(conf);
            if (holder.mod.type == Mod.ModType.EMT_MODPACK) {
                holder.view_modname.setText(holder.mod.name + " (Modpack)");
            }
            holder.view_modname.setChecked(enabled);

            holder.view_modname.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton comp_btn, boolean checked) {
                    holder.mod.setEnabled(conf, checked);
                }
            });
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
