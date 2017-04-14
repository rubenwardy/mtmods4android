package com.rubenwardy.minetestmodmanager.models;

import android.support.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Game {
    public final String name;
    private final File file;

    private final Map<String, ModList> lists = new HashMap<>();

    public Game(@NonNull String name, @NonNull File file) {
        this.name = name;

        this.file = file;
    }

    public String getPath() {
        return file.getAbsolutePath();
    }

    public ModList getList(String path) {
        return lists.get(new File(path).getAbsolutePath());
    }

    public Mod getMod(String name, String author) {
        for (ModList list : lists.values()) {
            Mod mod = list.get(name, author);
            if (mod != null) {
                return mod;
            }
        }

        return null;
    }

    public boolean isValid() {
        return file.isDirectory();
    }

    public boolean isLoaded() {
        return new File(file, "games").isDirectory();
    }

    public boolean hasWorld() {
        return new File(file, "worlds/world").isDirectory();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void forceCreate() {
        new File(file, "mods").mkdirs();
        new File(file, "worlds").mkdirs();
    }

    public List<String> getModPaths() {
        List<String> paths = new ArrayList<>();
        paths.add(new File(file, "mods").getAbsolutePath());
        return paths;
    }

    public List<ModList> getAllModLists() {
        List<ModList> ret = new ArrayList<>();
        ret.addAll(lists.values());
        return ret;
    }

    public List<Mod> getAllMods() {
        Map<String, Mod> map = new HashMap<>();

        for (ModList list : lists.values()) {
            for (Mod mod : list.mods) {
                map.put(mod.name, mod);
            }
        }

        List<Mod> ret = new ArrayList<>();
        ret.addAll(map.values());
        return ret;
    }

    public void addList(String path, ModList list) {
        lists.put(path, list);
    }
}
