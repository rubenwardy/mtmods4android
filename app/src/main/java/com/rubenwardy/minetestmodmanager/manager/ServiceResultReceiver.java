package com.rubenwardy.minetestmodmanager.manager;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Receive a result from the service
 */
public class ServiceResultReceiver extends ResultReceiver {
    public ServiceResultReceiver(Handler handler) {
        super(handler);
    }

    private void handleInstall(@NonNull Bundle b, @Nullable String modname, @Nullable String dest) {
        if (b.containsKey(ModInstallService.RET_ERROR)) {
            Log.w("SRR", "Install failed for " + modname + ": " +
                    b.getString(ModInstallService.RET_ERROR));
        } else if (!b.containsKey(ModInstallService.RET_PROGRESS)) {
            Log.w("SRR", "Got result " + dest);
            ModManager modman = new ModManager();
            ModList list = modman.get(dest);
            if (list != null) {
                Log.w("SRR", "Invalidating list");
                list.valid = false;
            }

            if (ModManager.mev != null) {
                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_INSTALL);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST_LIST, dest);
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
        } else {
            Integer progress = b.getInt(ModInstallService.RET_PROGRESS);
            Log.w("SRR", "Progress for " + modname + " at " + Integer.toString(progress) + "%");
        }
    }

    private void handleFetchModList(@NonNull Bundle b, @Nullable String url, @Nullable String dest) {
        if (dest == null || url == null) {
            Log.w("SRR", "Invalid modlist");
            return;
        }

        if (b.containsKey(ModInstallService.RET_ERROR)) {
            Log.w("SRR", "Fetch failed for " + url + ": " +
                    b.getString(ModInstallService.RET_ERROR));
        } else if (!b.containsKey(ModInstallService.RET_PROGRESS)) {
            Log.w("SRR", "Got result " + dest + " from " + url);
            ModManager modman = new ModManager();
            ModList list = new ModList(ModList.ModListType.EMLT_STORE, "Mod Store", "", url);
            list.valid = false;

            try {
                File file = new File(dest);
                if (!file.isFile()) {
                    Log.w("SRR", "No such json file!");
                    return;
                }
                JSONArray j = new JSONArray(Utils.readTextFile(file));
                if (!file.delete()) {
                    Log.w("SRR", "Failed to delete file!");
                    return;
                }

                for (int i = 0; i < j.length(); i++)
                {
                    try {
                        JSONObject item = j.getJSONObject(i);
                        // Pulling items from the array
                        String author = item.getString("author");
                        String modname = item.getString("name");
                        String title = item.getString("title");
                        String link = item.getString("link");
                        if (modname != null && title != null && link != null) {
                            Mod mod = new Mod(Mod.ModType.EMT_MOD, url, modname, title, "");
                            list.add(mod);
                            Log.w("SRR", "Added mod: " + modname);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return;
            }

            modman.addList(list);

            if (ModManager.mev != null) {
                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_FETCH_MODLIST);
                b2.putString(ModEventReceiver.PARAM_DEST_LIST, url);
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
        } else {
            Integer progress = b.getInt(ModInstallService.RET_PROGRESS);
            Log.w("SRR", "Progress for " + url + " at " + Integer.toString(progress) + "%");
        }
    }

    @Override
    protected void onReceiveResult(int resultCode, @NonNull Bundle b) {
        String modname = b.getString(ModInstallService.RET_NAME);
        String dest = b.getString(ModInstallService.RET_DEST);
        String action = b.getString(ModInstallService.RET_ACTION);
        if (action == null) {
            Log.w("SRR", "Invalid null action");
            return;
        }

        if (action.equals(ModInstallService.ACTION_INSTALL)) {
            handleInstall(b, modname, dest);
        } else if (action.equals(ModInstallService.ACTION_FETCH_MODLIST)) {
            handleFetchModList(b, modname, dest);
        } else {
            Log.w("SRR", "Unknown service action");
        }
    }

}