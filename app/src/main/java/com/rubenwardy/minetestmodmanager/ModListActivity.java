package com.rubenwardy.minetestmodmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

import java.io.File;
import java.util.List;

/**
 * An activity representing a list of Mods. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ModDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ModListActivity extends AppCompatActivity {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    private ModManager mModMan;
    private String current_list;

    @Override
    protected void onResume() {
        super.onResume();

        Log.w("MLAct", "Resuming!");
        ModList list = mModMan.get(current_list);
        if (list != null && !list.valid && mModMan.update(list)) {
            Log.w("MLAct", " - list has changed!");
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.mod_list);
            assert recyclerView != null;
            ModListRecyclerViewAdapter adapter = (ModListRecyclerViewAdapter) recyclerView.getAdapter();
            adapter.setMods(list.mods);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mod_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        File sdcard = Environment.getExternalStorageDirectory();
        current_list = sdcard.getAbsolutePath() + "/Minetest/mods/";
        mModMan = new ModManager();
        ModList list = mModMan.getModsFromDir(current_list);

        View recyclerView = findViewById(R.id.mod_list);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

        if (findViewById(R.id.mod_detail_container) != null) {
            mTwoPane = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_mod_list, menu);
        return true;
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        ModList list = mModMan.get(current_list);
        ModListRecyclerViewAdapter adapter = new ModListRecyclerViewAdapter(list.mods);
        //adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);
    }

    public class ModListRecyclerViewAdapter
            extends RecyclerView.Adapter<ModListRecyclerViewAdapter.ViewHolder> {

        private List<Mod> mMods;

        public ModListRecyclerViewAdapter(List<Mod> items) {
            mMods = items;
        }

        public void setMods(List<Mod> mods) {
            mMods = mods;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.mod_list_content, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.mItem = mMods.get(position);
            holder.mIdView.setText(holder.mItem.name);
            int len = holder.mItem.desc.indexOf('.') + 1;
            int slen = holder.mItem.desc.length();
            if (len > slen || len < 20) {
                len = slen;
            }
            if (len > 100) {
                len = 100;
            }
            holder.mContentView.setText(holder.mItem.desc.substring(0, len));

            holder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(ModDetailFragment.ARG_MOD_LIST, current_list);
                        arguments.putString(ModDetailFragment.ARG_MOD_NAME, holder.mItem.name);
                        ModDetailFragment fragment = new ModDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.mod_detail_container, fragment)
                                .commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, ModDetailActivity.class);
                        intent.putExtra(ModDetailFragment.ARG_MOD_LIST, current_list);
                        intent.putExtra(ModDetailFragment.ARG_MOD_NAME, holder.mItem.name);

                        context.startActivity(intent);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mMods.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final View mView;
            public final TextView mIdView;
            public final TextView mContentView;
            public Mod mItem;

            public ViewHolder(View view) {
                super(view);
                mView = view;
                mIdView = (TextView) view.findViewById(R.id.id);
                mContentView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public String toString() {
                return super.toString() + " '" + mContentView.getText() + "'";
            }
        }
    }
}
