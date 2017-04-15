package com.rubenwardy.minetestmodmanager.models;

import android.support.annotation.NonNull;

import java.io.File;
import java.io.IOException;
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
        return file.isDirectory() && new File(file, "mods").isDirectory();
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

    public List<ModDir> getModPaths() {
        List<ModDir> paths = new ArrayList<>();
        paths.add(new ModDir(new File(file, "mods").getAbsolutePath(), ModList.ModListType.EMLT_MODS));
        paths.add(new ModDir(new File(file, "games/minetest_game/mods").getAbsolutePath(), ModList.ModListType.EMLT_GAME_MODS));
        return paths;
    }

    public List<ModList> getAllModLists() {
        List<ModList> ret = new ArrayList<>();
        ret.addAll(lists.values());
        return ret;
    }

    public Map<String, Mod> getAllMods() {
        Map<String, Mod> map = new HashMap<>();

        for (ModList list : lists.values()) {
            for (Mod mod : list.mods) {
                map.put(mod.name, mod);
            }
        }

        return map;
    }

    public void addList(String path, ModList list) {
        lists.put(path, list);
    }

    public boolean hasPath(String path) {
        String root;
        try {
            root = file.getCanonicalPath();
        } catch (IOException e) {
            root = file.getAbsolutePath();
        }

        File file2 = new File(path);
        try {
            path = file2.getCanonicalPath();
        } catch (IOException e) {
            path = file2.getAbsolutePath();
        }

        return path.startsWith(root);
    }

    public class ModDir {
        public String path;
        public ModList.ModListType type;

        ModDir(String absolutePath, ModList.ModListType emltGameMods) {
            this.path = absolutePath;
            this.type = emltGameMods;
        }
    }
}
