package com.rubenwardy.minetestmodmanager.manager;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides a collection of mods.
 */
public class ModManager {
    public static ModEventReceiver mev;
    private static Map<String, ModList> lists_map = new HashMap<String, ModList>();
    private static ServiceResultReceiver srr = new ServiceResultReceiver(new Handler());

    public void setEventReceiver(@NonNull ModEventReceiver mev) {
        Log.w("ModMan", "Set event receiver!");
        this.mev = mev;
    }

    public void unsetEventReceiver(@Nullable ModEventReceiver mev) {
        if (this.mev == mev) {
            this.mev = null;
            Log.w("ModMan", "Unset event receiver!");
        } else {
            Log.w("ModMan", "Ignored call to unset event receiver, already different.");
        }
    }

    public ModList get(String path) {
        return lists_map.get(path);
    }

    public ModList listFromMod(@NonNull Mod mod) {
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

    public void installModAsync(Context context, @NonNull Mod mod, @NonNull File zip, String path) {
        ModInstallService.startActionInstall(context, srr, mod.name, zip, path);
    }

    public void installUrlModAsync(Context context, @NonNull Mod mod, @NonNull String url, String path) {
        ModInstallService.startActionUrlInstall(context, srr, mod.name, url, path);
    }

    public boolean uninstallMod(@NonNull Mod mod) {
        if (mod.path.equals("")) {
            return false;
        } else {
            Utils.deleteRecursive(new File(mod.path));
            ModList list = listFromMod(mod);
            list.valid = false;
            return true;
        }
    }

    private boolean updatePathList(@NonNull ModList list) {
        Log.w("ModLib", "Collecting/updating ModList (type=dir).");

        File dirs = new File(list.uri);
        if (!dirs.exists()) {
            Log.w("ModLib", list.uri + " does not exist");
            return false;
        }

        list.mods.clear();
        list.mods_map.clear();

        File[] files = dirs.listFiles();
        for (File file:files) {
            if (file.isDirectory()) {
                Mod.ModType type = Utils.detectModType(file);
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
