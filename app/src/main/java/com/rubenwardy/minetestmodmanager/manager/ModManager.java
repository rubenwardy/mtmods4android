package com.rubenwardy.minetestmodmanager.manager;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides a collection of mods.
 */
public class ModManager {
    public class ModList {
        public List<Mod> mods = new ArrayList<Mod>();
        public Map<String, Mod> mods_map = new HashMap<String, Mod>();
        public void add(Mod mod) {
            mods.add(mod);
            mods_map.put(mod.name, mod);
        }
    }

    public static Map<String, ModList> lists_map = new HashMap<String, ModList>();

    public ModList get(String path) {
        return lists_map.get(path);
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

    public ModList getModsFromDir(String path) {
        if (lists_map.containsKey(path)) {
            Log.w("ModLib", "Returning existing ModList (type=dir).");
            return lists_map.get(path);
        }

        Log.w("ModLib", "Creating new ModList (type=dir).");
        File dirs = new File(path);
        if (!dirs.exists())
            return null;

        ModList list = new ModList();
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
                    list.add(mod);
                }
            } else {
                Log.w("ModLib", "Found file at " + file.getName() + ", ignoring");
            }
        }
        lists_map.put(path, list);
        return list;
    }
}
