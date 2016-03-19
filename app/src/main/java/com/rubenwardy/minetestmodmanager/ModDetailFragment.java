package com.rubenwardy.minetestmodmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.manager.Mod;
import com.rubenwardy.minetestmodmanager.manager.ModEventReceiver;
import com.rubenwardy.minetestmodmanager.manager.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;

/**
 * A mFragment representing a single Mod detail screen.
 * This mFragment is either contained in a {@link ModListActivity}
 * in two-pane mode (on tablets) or a {@link ModDetailActivity}
 * on handsets.
 */
public class ModDetailFragment extends Fragment {
    /**
     * The mFragment argument representing the item ID that this mFragment
     * represents.
     */
    public static final String ARG_MOD_LIST = "mod_list";
    public static final String ARG_MOD_NAME = "mod_name";
    public static final String ARG_MOD_AUTHOR = "mod_author";


    private Mod mod;

    /**
     * Mandatory empty constructor for the mFragment manager to instantiate the
     * mFragment (e.g. upon screen orientation changes).
     */
    public ModDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_MOD_NAME) &&
                getArguments().containsKey(ARG_MOD_LIST)) {
            String name = getArguments().getString(ARG_MOD_NAME);
            String author = getArguments().getString(ARG_MOD_AUTHOR);
            String listname = getArguments().getString(ARG_MOD_LIST);
            ModManager modman = new ModManager();
            ModList list = modman.get(listname);
            if (list == null) {
                Resources res = getResources();
                mod = new Mod(Mod.ModType.EMT_INVALID,
                        "", "invalid",
                        res.getString(R.string.invalid_modlist),
                        res.getString(R.string.invalid_modlist_desc));
            } else {
                mod = list.get(name, author);
                if (mod == null) {
                    Resources res = getResources();
                    list.valid = false;
                    mod = new Mod(Mod.ModType.EMT_INVALID,
                            "", "invalid",
                            res.getString(R.string.invalid_mod),
                            res.getString(R.string.invalid_mod_desc));
                }
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mod_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mod != null) {
            Resources res = getResources();

            // Set text
            ((TextView) rootView.findViewById(R.id.mod_desc)).setText(mod.desc);
            ((TextView) rootView.findViewById(R.id.mod_detail_name)).setText(mod.name);
            TextView txt_author = (TextView) rootView.findViewById(R.id.mod_detail_author);
            txt_author.setText(mod.author);
            ((TextView) rootView.findViewById(R.id.mod_detail_link)).setText(mod.getShortLink());
            TextView txt_forum = (TextView) rootView.findViewById(R.id.mod_detail_forum);
            txt_forum.setText(mod.getShortForumLink());

            String ver;
            if (mod.verified == 1) {
                ver = res.getString(R.string.mod_verified_yes);
            } else if (mod.isLocalMod()) {
                ver = res.getString(R.string.mod_verified_uk);
            } else {
                ver = res.getString(R.string.mod_verified_no);
            }
            ((TextView) rootView.findViewById(R.id.mod_detail_ver)).setText(ver);

            String dlsize = mod.getDownloadSize();
            if (dlsize == null) {
                dlsize = res.getString(R.string.size_unknown);
            }
            ((TextView) rootView.findViewById(R.id.mod_detail_size)).setText(dlsize);

            // Find Elsewhere
            Button btn_find = (Button) rootView.findViewById(R.id.find);
            btn_find.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    Bundle bundle = new Bundle();
                    bundle.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_SEARCH);
                    bundle.putString(ModEventReceiver.PARAM_MODNAME, mod.name);
                    bundle.putString(ModEventReceiver.PARAM_ADDITIONAL, "name:" + mod.name);
                    ((ModEventReceiver) getActivity()).onModEvent(bundle);
                }
            });

            // Author
            txt_author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    if (!mod.author.equals("")) {
                        Bundle bundle = new Bundle();
                        bundle.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_SEARCH);
                        bundle.putString(ModEventReceiver.PARAM_MODNAME, mod.name);
                        bundle.putString(ModEventReceiver.PARAM_ADDITIONAL, "author:" + mod.author);
                        ((ModEventReceiver) getActivity()).onModEvent(bundle);
                    }
                }
            });

            txt_forum.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    if (mod.forum_url != null) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mod.forum_url));
                        startActivity(browserIntent);
                    }
                }
            });

            // Type
            String type;
            if (mod.type == Mod.ModType.EMT_MOD) {
                type = res.getString(R.string.type_mod);
            } else if (mod.type == Mod.ModType.EMT_MODPACK) {
                type = res.getString(R.string.type_modpack);
            } else if (mod.type == Mod.ModType.EMT_SUBGAME) {
                type = res.getString(R.string.type_subgame);
            } else {
                type = "Invalid";
            }
            ((TextView) rootView.findViewById(R.id.mod_detail_type)).setText(type);

            Button btn_main = (Button) rootView.findViewById(R.id.uninstall);
            if (mod.isLocalMod()) {
                ((TextView) rootView.findViewById(R.id.mod_detail_location)).setText(mod.path);
                btn_main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        ModManager modman = new ModManager();
                        modman.uninstallModAsync(getContext(), mod);
                    }
                });
            } else {
                rootView.findViewById(R.id.mod_detail_loc_row).setVisibility(View.GONE);
                btn_main.setText(res.getString(R.string.action_install));

                btn_main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        SharedPreferences settings = getActivity().getSharedPreferences(DisclaimerActivity.PREFS_NAME, 0);
                        boolean agreed = settings.getBoolean("agreed_to_disclaimer", false);
                        if (!agreed) {
                            Context context = view.getContext();
                            Intent intent = new Intent(context, DisclaimerActivity.class);
                            intent.putExtra(ModDetailFragment.ARG_MOD_LIST, mod.listname);
                            intent.putExtra(ModDetailFragment.ARG_MOD_AUTHOR, mod.author);
                            intent.putExtra(ModDetailFragment.ARG_MOD_NAME, mod.name);
                            context.startActivity(intent);
                        } else {
                            ModManager modman = new ModManager();
                            Resources res = getResources();
                            Snackbar.make(view, res.getString(R.string.installing_mod), Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();

                            if (mod.link != null) {
                                modman.installUrlModAsync(getActivity().getApplicationContext(), mod,
                                        mod.link,
                                        modman.getInstallDir());
                            }
                        }
                    }
                });
            }
        }

        return rootView;
    }
}
