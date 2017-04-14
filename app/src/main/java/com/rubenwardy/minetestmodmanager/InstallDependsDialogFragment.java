package com.rubenwardy.minetestmodmanager;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.rubenwardy.minetestmodmanager.manager.ModManager;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
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
        for (String modname : listItems) {
            mods.add(availableMods.get(modname, null));
        }

        ArrayAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.simple_list_item_1, listItems);
        list.setAdapter(adapter);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.event_missing_dependencies)
                .setMessage(R.string.event_missing_dependencies_msg)
                .setView(root)
                .setPositiveButton(R.string.mod_action_install, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        for (Mod mod : mods) {
                            if (mod != null && mod.link != null) {
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
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // do nothing
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }


}
