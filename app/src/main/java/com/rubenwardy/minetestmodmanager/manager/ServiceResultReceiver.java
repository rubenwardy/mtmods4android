package com.rubenwardy.minetestmodmanager.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.models.Mod;
import com.rubenwardy.minetestmodmanager.models.ModList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Receive a result from the service
 */
@SuppressLint("ParcelCreator")
class ServiceResultReceiver extends ResultReceiver {
    ServiceResultReceiver(Handler handler) {
        super(handler);
    }

    private void handleInstall(@NonNull Bundle b, @Nullable String modname, @Nullable String dest) {
        if (b.containsKey(ModInstallService.RET_ERROR)) {

            if (ModManager.mev != null) {
                final String error = b.getString(ModInstallService.RET_ERROR);

                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_INSTALL);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST_LIST, dest);
                b2.putString(ModEventReceiver.PARAM_ERROR, error);
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
        } else {
            ModManager modman = new ModManager();
            ModList list = modman.get(dest);
            if (list != null) {
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
        }
    }

    private void handleUninstall(@NonNull Bundle b, @Nullable String modname, @Nullable String dest) {
        if (b.containsKey(ModInstallService.RET_ERROR)) {
            if (ModManager.mev != null) {
                final String error = b.getString(ModInstallService.RET_ERROR);

                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_UNINSTALL);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST_LIST, dest);
                b2.putString(ModEventReceiver.PARAM_ERROR, error);
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
        } else {
            ModManager modman = new ModManager();
            ModList list = modman.get(dest);
            if (list != null) {
                list.valid = false;
            }

            if (ModManager.mev != null) {
                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_UNINSTALL);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST_LIST, dest);
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
        }
    }

    @Override
    protected void onReceiveResult(int resultCode, @NonNull Bundle b) {
        String modname = b.getString(ModInstallService.RET_NAME);
        String dest = b.getString(ModInstallService.RET_DEST);
        String action = b.getString(ModInstallService.RET_ACTION);
        if (action == null) {
            Log.e("SRR", "Invalid null action");
            return;
        }

        switch (action) {
        case ModInstallService.ACTION_INSTALL:
            handleInstall(b, modname, dest);
            break;
        case ModInstallService.ACTION_UNINSTALL:
            handleUninstall(b, modname, dest);
            break;
        case ModInstallService.ACTION_FETCH_SCREENSHOT:
            if (ModManager.mev != null) {
                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_FETCH_SCREENSHOT);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST, dest);
                if (b.containsKey(ModInstallService.RET_ERROR)) {
                    b2.putString(ModEventReceiver.PARAM_ERROR, b.getString(ModInstallService.RET_ERROR));
                }
                //noinspection ConstantConditions
                ModManager.mev.onModEvent(b2);
            }
            break;
        default:
            Log.e("SRR", "Unknown service action");
            break;
        }
    }

}