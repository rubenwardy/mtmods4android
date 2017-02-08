package com.rubenwardy.minetestmodmanager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.rubenwardy.minetestmodmanager.models.Events;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;
import com.rubenwardy.minetestmodmanager.manager.ModManager;
import com.rubenwardy.minetestmodmanager.manager.Utils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

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
        Log.e("mdf", "new");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {Log.e("mdf", "create");

        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
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
                            listname + ": " + res.getString(R.string.invalid_modlist_desc));
                } else {
                    mod = list.get(name, author);
                    if (mod == null) {
                        Resources res = getResources();
                        list.valid = false;
                        mod = new Mod(Mod.ModType.EMT_INVALID,
                                "", "invalid",
                                res.getString(R.string.invalid_mod),
                                author + "/" + name  + ": " + res.getString(R.string.invalid_mod_desc));
                    }
                }
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        Log.e("mdf", "save");
        state.putString(ModDetailFragment.ARG_MOD_LIST, mod.listname);
        state.putString(ModDetailFragment.ARG_MOD_NAME, mod.name);
        state.putString(ModDetailFragment.ARG_MOD_AUTHOR, mod.author);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.mod_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mod != null) {
            Resources res = getResources();

            //
            // HEADER
            //

            // Title
            TextView txt_title = (TextView) rootView.findViewById(R.id.mod_header_title);
            txt_title.setText(mod.title);

            // Author
            TextView txt_author = (TextView) rootView.findViewById(R.id.mod_header_author);
            txt_author.setText(mod.author);
            txt_author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    if (!mod.author.equals("")) {
                        EventBus.getDefault().post(new Events.SearchEvent("author:" + mod.author));
                    }
                }
            });


            Button btn_main = (Button) rootView.findViewById(R.id.mod_header_uninstall);
            if (mod.isLocalMod()) {
                btn_main.getBackground().setColorFilter(ContextCompat.getColor(getContext(), R.color.colorRemove),
                        PorterDuff.Mode.MULTIPLY);
                btn_main.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        ModManager modman = new ModManager();
                        modman.uninstallModAsync(getContext(), mod);
                    }
                });
            } else {
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

                            int installs = settings.getInt("installs_so_far", 0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putInt("installs_so_far", installs + 1);
                            editor.apply();
                        }
                    }
                });
            }

            //
            // Description
            //

            ((TextView) rootView.findViewById(R.id.mod_desc)).setText(mod.desc);


            //
            // ACTION BUTTONS
            //

            // Report
            Button btn_report = (Button) rootView.findViewById(R.id.action_report);
            btn_report.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(@NonNull View view) {
                    Context context = getContext();
                    Intent intent = new Intent(context, ReportActivity.class);
                    intent.putExtra(ReportActivity.EXTRA_LIST, mod.listname);
                    intent.putExtra(ReportActivity.EXTRA_AUTHOR, mod.author);
                    intent.putExtra(ReportActivity.EXTRA_MOD_NAME, mod.name);
                    intent.putExtra(ReportActivity.EXTRA_LINK, mod.link);
                    context.startActivity(intent);
                }
            });

            // Forum
            Button btn_forum = (Button) rootView.findViewById(R.id.forum_topic);
            if (mod.forum_url == null || mod.forum_url.isEmpty()) {
                btn_forum.setVisibility(View.GONE);
            } else {
                btn_forum.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        if (mod.forum_url != null) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mod.forum_url));
                            startActivity(browserIntent);
                        }
                    }
                });
            }

            // Readme
            Button btn_readme = (Button) rootView.findViewById(R.id.readme);
            if (mod.isLocalMod() && Utils.getReadmePath(new File(mod.path)) != null) {
                btn_readme.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(@NonNull View view) {
                        Context context = view.getContext();
                        Intent intent = new Intent(context, ReadmeActivity.class);
                        intent.putExtra(ReadmeActivity.ARG_MOD_PATH, mod.path);
                        context.startActivity(intent);
                    }
                });
            } else {
                btn_readme.setVisibility(View.GONE);
            }

            // Infomation
            rootView.findViewById(R.id.action_info).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment dialog = new ModInfoDialogFragment();
                    Bundle args = new Bundle();
                    args.putString(ARG_MOD_AUTHOR, mod.author);
                    args.putString(ARG_MOD_LIST, mod.listname);
                    args.putString(ARG_MOD_NAME, mod.name);
                    dialog.setArguments(args);
                    dialog.show(getActivity().getSupportFragmentManager(), "ModInfoDialogFragment");
                }
            });
        }

        return rootView;
    }
}
