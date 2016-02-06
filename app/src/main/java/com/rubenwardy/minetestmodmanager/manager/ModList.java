package com.rubenwardy.minetestmodmanager.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list of mods. May represent a folder in the file system or
 */
public class ModList {
    public enum ModListType {
        EMLT_STORE,
        EMLT_PATH
    }

    @Nullable public final String title;
    @NonNull  public final String listname;
    @NonNull public final String root;
    @NonNull public final ModListType type;
    public boolean valid;
    @NonNull
    public List<Mod> mods = new ArrayList<>();
    @NonNull
    public Map<String, Mod> mods_map = new HashMap<>();

    public ModList(@NonNull ModListType type, @Nullable String title, @NonNull String root,
            @NonNull String listname) {
        this.type = type;
        this.title = title;
        this.root = root;
        this.listname = listname;
        this.valid = true;
    }

    public void add(@NonNull Mod mod) {
        mods.add(mod);
        mods_map.put(mod.name, mod);
    }
}
