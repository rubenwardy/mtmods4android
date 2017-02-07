package com.rubenwardy.minetestmodmanager.manager;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.models.Events;
import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;
import com.rubenwardy.minetestmodmanager.restapi.StoreAPI;
import com.rubenwardy.minetestmodmanager.restapi.StoreAPIBuilder;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Provides a collection of mods.
 */
public class ModManager {
    @NonNull
    public static Map<String, ModList> lists_map = new HashMap<>();

    @NonNull
    private static ServiceResultReceiver srr = new ServiceResultReceiver(new Handler());

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
    public void fetchModListAsync(Context context) {
        StoreAPI api = StoreAPIBuilder.createService();
        api.getModList().enqueue(new Callback<List<StoreAPI.RestMod>>() {
            @Override
            public void onResponse(Call<List<StoreAPI.RestMod>> call, Response<List<StoreAPI.RestMod>> response) {
                Log.e("ModList", "Received modlist!");

                List<StoreAPI.RestMod> mods = response.body();
                if (mods != null) {
                    final String modstore_url = StoreAPIBuilder.API_BASE_URL;
                    ModList list = new ModList(ModList.ModListType.EMLT_ONLINE, "Available Mods", null, modstore_url);

                    for (StoreAPI.RestMod rmod : mods) {
                        String modname = rmod.basename;
                        String title = rmod.title;
                        String link = rmod.download_link;

                        if (modname != null && title != null && link != null) {
                            String author = rmod.author;
                            String type_s = rmod.type;

                            String desc = "";
                            if (rmod.description != null) {
                                desc = rmod.description;
                            }

                            String forum = null;
                            if (rmod.forum_url != null) {
                                forum = rmod.forum_url;
                            }

                            int size = rmod.download_size;

                            Mod.ModType type = Mod.ModType.EMT_MOD;
                            if (type_s != null) {
                                if (type_s.equals("1")) {
                                    type = Mod.ModType.EMT_MOD;
                                } else if (type_s.equals("2")) {
                                    type = Mod.ModType.EMT_MODPACK;
                                }
                            }

                            Mod mod = new Mod(type, modstore_url, modname, title, desc);
                            mod.link = link;
                            mod.author = author;
                            mod.forum_url = forum;
                            mod.size = size;
                            list.add(mod);
                        } else {
                            Log.e("ModMan", "Invalid object in list");
                        }
                    }

                    ModManager modman = new ModManager();
                    modman.addList(list);

                    EventBus.getDefault().post(new Events.FetchedListEvent(modstore_url));
                } else {
                    Log.e("ModMan", "Modlist fetch error!");
                    Log.e("ModMan", response.message());
                }
            }

            @Override
            public void onFailure(Call<List<StoreAPI.RestMod>> call, Throwable t) {
                Log.e("ModMan", "Modlist fetch error! 2");
                Log.e("ModMan", t.toString());
            }
        });
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
        StoreAPIBuilder.createService().sendReport(modname, info, reason, author, list, link).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
        });
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
