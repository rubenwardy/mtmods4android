package com.rubenwardy.minetestmodmanager;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.ModManager;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import static com.rubenwardy.minetestmodmanager.ModDetailFragment.ARG_MOD_AUTHOR;
import static com.rubenwardy.minetestmodmanager.ModDetailFragment.ARG_MOD_LIST;
import static com.rubenwardy.minetestmodmanager.ModDetailFragment.ARG_MOD_NAME;

public class ModInfoDialogFragment extends DialogFragment {
    private Mod mod;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (getArguments().containsKey(ARG_MOD_NAME) &&
                    getArguments().containsKey(ARG_MOD_LIST)) {
                String name = getArguments().getString(ARG_MOD_NAME);
                String author = getArguments().getString(ARG_MOD_AUTHOR);
                String listname = getArguments().getString(ARG_MOD_LIST);
                ModManager modman = ModManager.getInstance();

                ModList list = modman.getModList(listname);
                if (list == null) {
                    Resources res = getResources();
                    mod = new Mod(Mod.ModType.EMT_INVALID,
                            "", "invalid",
                            res.getString(R.string.mod_invalid_modlist_title),
                            listname + ": " + res.getString(R.string.mod_invalid_modlist_desc));
                } else {
                    mod = list.get(name, author);
                    if (mod == null) {
                        Resources res = getResources();
                        list.valid = false;
                        mod = new Mod(Mod.ModType.EMT_INVALID,
                                "", "invalid",
                                res.getString(R.string.mod_invalid_mod_title),
                                author + "/" + name  + ": " + res.getString(R.string.mod_invalid_mod_desc));
                    }
                }
            }
        }


        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_mod_information, null);

        setupView(rootView);

        builder.setMessage(R.string.modinfo_title)
                .setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                })
                .setView(rootView);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public void setupView(View rootView) {
        Resources res = getResources();

        // Mod name
        ((TextView) rootView.findViewById(R.id.mod_detail_name)).setText(mod.name);

        // Type
        String type;
        if (mod.type == Mod.ModType.EMT_MOD) {
            type = res.getString(R.string.modinfo_details_type_mod);
        } else if (mod.type == Mod.ModType.EMT_MODPACK) {
            type = res.getString(R.string.modinfo_details_type_modpack);
        } else if (mod.type == Mod.ModType.EMT_SUBGAME) {
            type = res.getString(R.string.modinfo_details_type_subgame);
        } else {
            type = "Invalid";
        }
        ((TextView) rootView.findViewById(R.id.mod_detail_type)).setText(type);

        // Location
        if (mod.isLocalMod()) {
            ((TextView) rootView.findViewById(R.id.mod_detail_location)).setText(mod.path);
        } else {
            rootView.findViewById(R.id.mod_detail_loc_row).setVisibility(View.GONE);
        }

        // Download link and size
        if (mod.isLocalMod()) {
            rootView.findViewById(R.id.mod_detail_link_row).setVisibility(View.GONE);
            rootView.findViewById(R.id.mod_detail_size_row).setVisibility(View.GONE);
        } else {
            ((TextView) rootView.findViewById(R.id.mod_detail_link)).setText(mod.getShortLink());
            String dlsize = mod.getDownloadSize();
            if (dlsize == null) {
                dlsize = res.getString(R.string.modinfo_details_size_unknown);
            }
            ((TextView) rootView.findViewById(R.id.mod_detail_size)).setText(dlsize);
        }
    }
}
