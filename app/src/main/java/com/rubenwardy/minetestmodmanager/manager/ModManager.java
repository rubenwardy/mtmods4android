package com.rubenwardy.minetestmodmanager.manager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides a collection of mods.
 */
public class ModManager {
    public static Map<String, ModList> lists_map = new HashMap<String, ModList>();

    public ModList get(String path) {
        return lists_map.get(path);
    }

    public ModList listFromMod(Mod mod) {
        // TODO: optimise this.
        for (ModList list : lists_map.values()) {
            for (Mod b : list.mods) {
                if (mod.path.equals(b.path)) {
                    return list;
                }
            }
        }
        return null;
    }

    public Mod.ModType detectModType(File file) {
        if (new File(file.getAbsolutePath(), "init.lua").exists()) {
            Log.w("ModLib", "Found mod at " + file.getName());
            return Mod.ModType.EMT_MOD;
        } else if (new File(file.getAbsolutePath(), "modpack.lua").exists()) {
            Log.w("ModLib", "Found modpack at " + file.getName());
            return Mod.ModType.EMT_MODPACK;
        } else {
            Log.w("ModLib", "Found invalid directory at " + file.getName());
            return Mod.ModType.EMT_INVALID;
        }
    }

    void deleteRecursive(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            for (File child : fileOrDir.listFiles()) {
                deleteRecursive(child);
            }
        }
        if (fileOrDir.delete())
            Log.w("utils", "Deleted path: " + fileOrDir.getAbsolutePath());
        else
            Log.w("utils", "Failed to delete path: " + fileOrDir.getAbsolutePath());
    }

    public boolean installMod(Mod mod, String zip, String path) {
        return true;
    }

    public boolean uninstallMod(Mod mod) {
        if (mod == null || mod.path.equals("")) {
            return false;
        } else {
            deleteRecursive(new File(mod.path));
            ModList list = listFromMod(mod);
            list.valid = false;
            return true;
        }
    }

    public boolean updatePathList(ModList list) {
        Log.w("ModLib", "Collecting/updating ModList (type=dir).");

        File dirs = new File(list.uri);
        if (!dirs.exists())
            return false;

        list.mods.clear();
        list.mods_map.clear();

        File[] files = dirs.listFiles();
        for (File file:files) {
            if (file.isDirectory()) {
                Mod.ModType type = detectModType(file);
                if (type != Mod.ModType.EMT_INVALID) {
                    Log.w("ModLib", " - adding dir to list");

                    String title = file.getName();
                    String desc = "Desc " + file.getAbsolutePath();
                    File descF = new File(file.getAbsolutePath(), "description.txt");
                    if (descF.exists()) {
                        Log.w("ModLib", " - found description.txt, reading...");
                        try {
                            StringBuilder text = new StringBuilder();
                            BufferedReader br = new BufferedReader(new FileReader(descF));
                            String line;
                            while ((line = br.readLine()) != null) {
                                text.append(line);
                                text.append('\n');
                            }
                            br.close();
                            desc = text.toString();
                        }catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.w("ModLib", "No file at " + descF.getAbsolutePath());
                    }

                    Mod mod = new Mod(type, file.getName(), title, desc);
                    mod.path = file.getAbsolutePath();
                    list.add(mod);
                }
            } else {
                Log.w("ModLib", "Found file at " + file.getName() + ", ignoring");
            }
        }
        list.valid = true;
        return true;
    }

    public boolean update(ModList list) {
        if (list.type == ModList.ModListType.EMLT_PATH) {
            return updatePathList(list);
        } else {
            Log.w("ModLib", "Failed to update invalid ModList.");
            return false;
        }
    }

    public ModList getModsFromDir(String path) {
        if (lists_map.containsKey(path)) {
            Log.w("ModLib", "Returning existing ModList (type=dir).");
            return lists_map.get(path);
        }

        Log.w("ModLib", "Creating new ModList (type=dir).");
        ModList list = new ModList(ModList.ModListType.EMLT_PATH, path);
        if (update(list)) {
            lists_map.put(path, list);
            return list;
        } else
            return null;
    }
}
