package com.rubenwardy.minetestmodmanager.manager;

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

    public final String title;
    public final String uri;
    public final String root;
    public final ModListType type;
    public boolean valid;
    public List<Mod> mods = new ArrayList<>();
    public Map<String, Mod> mods_map = new HashMap<>();

    public ModList(ModListType type, String title, String root, String uri) {
        this.type = type;
        this.title = title;
        this.root = root;
        this.uri = uri;
        this.valid = true;
    }

    public void add(Mod mod) {
        mods.add(mod);
        mods_map.put(mod.name, mod);
    }
}
