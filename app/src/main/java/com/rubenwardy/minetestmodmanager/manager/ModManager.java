package com.rubenwardy.minetestmodmanager.manager;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    @Nullable
    public static ModEventReceiver mev;
    @NonNull
    public static Map<String, ModList> lists_map = new HashMap<>();
    @NonNull
    private static ServiceResultReceiver srr = new ServiceResultReceiver(new Handler());

    public void setEventReceiver(@NonNull ModEventReceiver mev) {
        Log.w("ModMan", "Set event receiver!");
        ModManager.mev = mev;
    }

    public void unsetEventReceiver(@Nullable ModEventReceiver mev) {
        if (ModManager.mev == mev) {
            ModManager.mev = null;
            Log.w("ModMan", "Unset event receiver!");
        } else {
            Log.w("ModMan", "Ignored call to unset event receiver, already different.");
        }
    }

    @Nullable
    public ModList get(String path) {
        return lists_map.get(path);
    }

    @Nullable
    public ModList listFromMod(@NonNull Mod mod) {
        return get(mod.listname);
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
            if (list != null) {
                list.valid = false;
            }
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

                    Mod mod = new Mod(type, list.uri, file.getName(), title, desc);
                    mod.path = file.getAbsolutePath();

                    File scsF = new File(file.getAbsolutePath(), "screenshot.png");
                    if (scsF.exists()) {
                        Log.w("ModLib", " - found screenshot.png");
                        mod.screenshot_uri = scsF.getAbsolutePath();
                    }

                    list.add(mod);
                }
            } else {
                Log.w("ModLib", "Found file at " + file.getName() + ", ignoring");
            }
        }
        list.valid = true;
        return true;
    }

    public boolean update(@NonNull ModList list) {
        if (list.type == ModList.ModListType.EMLT_PATH) {
            return updatePathList(list);
        } else {
            Log.w("ModLib", "Failed to update invalid ModList.");
            return false;
        }
    }

    @Nullable
    public ModList getModsFromDir(String title, String root, String path) {
        if (lists_map.containsKey(path)) {
            Log.w("ModLib", "Returning existing ModList (type=dir).");
            return lists_map.get(path);
        }

        Log.w("ModLib", "Creating new ModList (type=dir).");
        Log.w("ModLib", " - root: " + root);
        Log.w("ModLib", " - path: " + path);
        ModList list = new ModList(ModList.ModListType.EMLT_PATH, title, root, path);
        if (update(list)) {
            lists_map.put(path, list);
            return list;
        } else
            return null;
    }
}
