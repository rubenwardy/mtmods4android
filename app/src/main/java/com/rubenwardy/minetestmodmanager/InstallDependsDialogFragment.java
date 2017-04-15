package com.rubenwardy.minetestmodmanager;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.ModManager;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import java.util.ArrayList;
import java.util.List;


public class InstallDependsDialogFragment extends DialogFragment {
    public static final String ARG_MODS = "mods";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View root = inflater.inflate(R.layout.fragment_install_depends_dialog, null);

        final ListView list = (ListView) root.findViewById(R.id.dep_list);
        final List<String> listItems = getArguments().getStringArrayList(ARG_MODS);
        assert listItems != null;

        final ModManager modman = ModManager.getInstance();
        ModList availableMods = modman.getAvailableMods();
        assert availableMods != null;

        final List<Mod> mods = new ArrayList<>();
        int installable = 0;
        for (String modname : listItems) {
            Mod mod = availableMods.get(modname, null);

            if (mod == null) {
                mods.add(new Mod(Mod.ModType.EMT_MOD, null, modname, modname, ""));
            } else {
                mods.add(mod);
                installable++;
            }
        }

        list.setAdapter(new DependAdapter(mods));

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.event_missing_dependencies)
                .setView(root)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });

        if (installable == mods.size()) {
            builder.setMessage(getResources().getString(R.string.event_missing_dependencies_msg));
        } else if (installable > 0) {
            builder.setMessage(getResources().getString(R.string.event_missing_dependencies_msg_some_missing));
        } else {
            builder.setMessage(getResources().getString(R.string.event_missing_dependencies_msg_all_missing));
        }

        if (installable > 0) {
            builder.setPositiveButton(R.string.mod_action_install, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    for (Mod mod : mods) {
                        if (mod != null && !mod.link.isEmpty()) {
//                                Resources res = getResources();
//                                Snackbar.make(root, res.getString(R.string.event_installing_mod), Snackbar.LENGTH_LONG)
//                                        .setAction("Action", null).show();

                            modman.installUrlModAsync(getActivity().getApplicationContext(), mod,
                                    mod.link,
                                    modman.getInstallDir());

                            SharedPreferences settings = getActivity().getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
                            int installs = settings.getInt("installs_so_far", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("installs_so_far", installs + 1);
                            editor.apply();
                        }
                    }
                }
            });
        }

        // Create the AlertDialog object and return it
        return builder.create();
    }

    class DependAdapter extends BaseAdapter {
        private List<Mod> mods;

        DependAdapter(List<Mod> mods) {
            this.mods = mods;
        }

        @Override
        public int getCount() {
            return mods.size();
        }

        @Override
        public Object getItem(int position) {
            return mods.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mods.get(position).name.hashCode();
        }

        private class ViewHolderItem {
            TextView name;
            TextView author;

            ViewHolderItem(View view) {
                name   = (TextView) view.findViewById(R.id.modname);
                author = (TextView) view.findViewById(R.id.author);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolderItem viewHolder;

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.depends_list_item, parent, false);

                viewHolder = new ViewHolderItem(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolderItem) convertView.getTag();
            }

            // object item based on the position
            Mod mod = mods.get(position);

            // assign values if the object is not null
            if (mod != null) {
                viewHolder.name.setText(mod.name);
                if (mod.author.isEmpty()) {
                    viewHolder.name.setTextColor(Color.RED);
                } else {
                    viewHolder.name.setTextColor(Color.BLACK);
                    viewHolder.author.setText(mod.author);
                }
            }

            return convertView;

        }
    }
}
