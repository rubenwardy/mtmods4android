package com.rubenwardy.minetestmodmanager.manager;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import java.io.File;
import java.util.Arrays;
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
        ModManager.mev = mev;
    }

    public void unsetEventReceiver(@Nullable ModEventReceiver mev) {
        if (ModManager.mev == mev) {
            ModManager.mev = null;
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

    public int getNumberOfInstalledMods() {
        int count = 0;
        for (ModList list : lists_map.values()) {
            if (list.type == ModList.ModListType.EMLT_PATH) {
                count += list.mods.size();
            }
        }
        return count;
    }

    @MainThread
    public void installModAsync(Context context, @NonNull Mod mod, @NonNull File zip, String path) {
        ModInstallService.startActionInstall(context, srr, mod.name, mod.author, zip, path);
    }

    @MainThread
    public void installUrlModAsync(Context context, @NonNull Mod mod, @NonNull String url, String path) {
        if (url.equals("")) {
            Log.e("ModMan", "Failed to install blank url");
            return;
        }
        ModInstallService.startActionUrlInstall(context, srr, mod.name, mod.author, url, path);
    }

    @MainThread
    public void fetchModListAsync(Context context, String url) {
        ModInstallService.startActionFetchModList(context, srr, url);
    }

    @MainThread
    public void fetchScreenshot(Context context, String author, String name) {
        ModInstallService.startActionFetchScreenshot(context, srr, author, name);
    }

    @MainThread
    public void uninstallModAsync(Context context, @NonNull Mod mod) {
        if (mod.path != null && !mod.path.equals("")) {
            ModInstallService.startActionUninstall(context, srr, mod.name, mod.listname);
        }
    }

    @MainThread
    public void reportModAsync(Context context, @NonNull String modname, @Nullable String author,
                               @Nullable String list, @Nullable String link, @NonNull String reason,
                               @NonNull String info) {
        ModInstallService.startActionReport(context, srr, modname, author, list, link, reason, info);
    }

    @MainThread
    public void cancelAsyncTask() {
        ModInstallService.cancelCurrentTask();
    }

    public boolean updatePathModList(@NonNull ModList list) {
        File dirs = new File(list.listname);
        if (!dirs.exists()) {
            Log.e("ModLib", list.listname + " does not exist");
            return false;
        }

        list.mods.clear();
        list.mods_map.clear();

        File[] files = dirs.listFiles();
        Arrays.sort(files);
        for (File file : files) {
            if (file.isDirectory()) {
                Mod.ModType type = Utils.detectModType(file);
                if (type != Mod.ModType.EMT_INVALID) {
                    // Get Title
                    String title = file.getName();

                    // Get Description
                    String desc = "";
                    File descF = new File(file.getAbsolutePath(), "description.txt");
                    if (descF.exists()) {
                        desc = Utils.readTextFile(descF);
                        if (desc == null) {
                            desc = "";
                        }
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
                        mod.screenshot_uri = scsF.getAbsolutePath();
                    }

                    list.add(mod);
                }
            }
        }
        list.valid = true;
        return true;
    }

    public boolean update(@NonNull ModList list) {
        if (list.type == ModList.ModListType.EMLT_PATH) {
            return updatePathModList(list);
        } else {
            Log.e("ModLib", "Failed to update invalid ModList.");
            return false;
        }
    }

    public void addList(ModList list) {
        lists_map.put(list.listname, list);
    }

    @Nullable
    public ModList getModsFromDir(String title, String engine_root, String path) {
        if (lists_map.containsKey(path)) {
            return lists_map.get(path);
        }

        ModList list = new ModList(ModList.ModListType.EMLT_PATH, title, engine_root, path);
        if (update(list)) {
            lists_map.put(path, list);
            return list;
        } else {
            return null;
        }
    }
}
