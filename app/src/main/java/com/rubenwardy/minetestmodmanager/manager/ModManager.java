package com.rubenwardy.minetestmodmanager.manager;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.File;
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
    public ModList getAvailableMods() {
        for (ModList list : lists_map.values()) {
            if (list.type == ModList.ModListType.EMLT_ONLINE) {
                return list;
            }
        }
        return null;
    }

    @Nullable
    public String getInstallDir() {
        for (ModList list : lists_map.values()) {
            if (list.type == ModList.ModListType.EMLT_PATH) {
                return list.listname;
            }
        }
        return null;
    }

    @Nullable
    public ModList listFromMod(@NonNull Mod mod) {
        return get(mod.listname);
    }

    @MainThread
    public void installModAsync(Context context, @NonNull Mod mod, @NonNull File zip, String path) {
        ModInstallService.startActionInstall(context, srr, mod.name, mod.author, zip, path);
    }

    @MainThread
    public void installUrlModAsync(Context context, @NonNull Mod mod, @NonNull String url, String path) {
        if (url.equals("")) {
            Log.w("ModMan", "Failed to install blank url");
            return;
        }
        ModInstallService.startActionUrlInstall(context, srr, mod.name, mod.author, url, path);
    }

    @MainThread
    public void fetchModListAsync(Context context, String url) {
        ModInstallService.startActionFetchModList(context, srr, url);
    }

    @MainThread
    public void uninstallModAsync(Context context, @NonNull Mod mod) {
        if (mod.path != null && !mod.path.equals("")) {
            ModInstallService.startActionUninstall(context, srr, mod.name, mod.listname);
        }
    }

    @MainThread
    public void reportModAsync(Context context, @NonNull String modname, @Nullable String author,
                               @Nullable String list, @Nullable String link, @NonNull String info) {
        ModInstallService.startActionReport(context, srr, modname, author, list, link, info);
    }

    @MainThread
    public void cancelAsyncTask() {
        ModInstallService.cancelCurrentTask();
    }

    public boolean updatePathModList(@NonNull ModList list) {
        Log.w("ModLib", "Collecting/updating ModList (type=dir).");

        File dirs = new File(list.listname);
        if (!dirs.exists()) {
            Log.w("ModLib", list.listname + " does not exist");
            return false;
        }

        list.mods.clear();
        list.mods_map.clear();

        File[] files = dirs.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                Mod.ModType type = Utils.detectModType(file);
                if (type != Mod.ModType.EMT_INVALID) {
                    Log.w("ModLib", " - adding dir to list");

                    // Get Title
                    String title = file.getName();

                    // Get Description
                    String desc = "";
                    File descF = new File(file.getAbsolutePath(), "description.txt");
                    if (descF.exists()) {
                        Log.w("ModLib", " - found description.txt, reading...");
                        desc = Utils.readTextFile(descF);
                        if (desc == null) {
                            desc = "";
                        }
                    } else {
                        Log.w("ModLib", "No file at " + descF.getAbsolutePath());
                    }

                    // Create mod
                    Mod mod = new Mod(type, list.listname, file.getName(), title, desc);
                    mod.path = file.getAbsolutePath();

                    // Get author
                    File authF = new File(file.getAbsolutePath(), "author.txt");
                    if (authF.exists()) {
                        String author = Utils.readTextFile(authF);
                        if (author != null) {
                            author = author.trim();
                            if (!author.isEmpty()) {
                                mod.author = author;
                            }
                        }
                    }

                    // Get Screenshot
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
            return updatePathModList(list);
        } else {
            Log.w("ModLib", "Failed to update invalid ModList.");
            return false;
        }
    }

    public void addList(ModList list) {
        lists_map.put(list.listname, list);
    }

    @Nullable
    public ModList getModsFromDir(String title, String engine_root, String path) {
        if (lists_map.containsKey(path)) {
            Log.w("ModLib", "Returning existing ModList (type=dir).");
            return lists_map.get(path);
        }

        Log.w("ModLib", "Creating new ModList (type=dir).");
        Log.w("ModLib", " - engine_root: " + engine_root);
        Log.w("ModLib", " - path: " + path);
        ModList list = new ModList(ModList.ModListType.EMLT_PATH, title, engine_root, path);
        if (update(list)) {
            lists_map.put(path, list);
            return list;
        } else {
            return null;
        }
    }
}
