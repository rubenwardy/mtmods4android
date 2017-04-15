package com.rubenwardy.minetestmodmanager.manager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.models.Events;
import com.rubenwardy.minetestmodmanager.models.ModList;

import org.greenrobot.eventbus.EventBus;

/**
 * Receive a result from the service
 */
@SuppressLint("ParcelCreator")
class ServiceResultReceiver extends ResultReceiver {
    ServiceResultReceiver(Handler handler) {
        super(handler);
    }

    private void handleInstall(@NonNull Bundle b, @Nullable String modname, @Nullable String listname) {
        if (b.containsKey(ModInstallService.RET_ERROR)) {
            final String error = b.getString(ModInstallService.RET_ERROR);
            EventBus.getDefault().post(new Events.ModInstallEvent(modname, null, listname, error));
        } else {
            ModManager modman = ModManager.getInstance();
            ModList list = modman.getModList(listname);
            if (list != null) {
                list.valid = false;
            }

            modman.updateLocalModList(list);

            EventBus.getDefault().post(
                    new Events.ModInstallEvent(modname, listname + "/" + modname, listname, ""));
        }
    }

    private void handleUninstall(@NonNull Bundle b, @Nullable String modname, @Nullable String listname) {
        if (b.containsKey(ModInstallService.RET_ERROR)) {
            final String error = b.getString(ModInstallService.RET_ERROR);
            EventBus.getDefault().post(new Events.ModUninstallEvent(modname, null, listname, error));
        } else {
            ModManager modman = ModManager.getInstance();
            ModList list = modman.getModList(listname);
            if (list != null) {
                list.valid = false;
            }

            EventBus.getDefault().post(
                    new Events.ModUninstallEvent(modname, listname + "/" + modname, listname, ""));
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
            String error = "";
            if (b.containsKey(ModInstallService.RET_ERROR)) {
                error = b.getString(ModInstallService.RET_ERROR);
            }
            EventBus.getDefault().post(
                    new Events.FetchedScreenshotEvent(modname, dest, error));
            break;
        default:
            Log.e("SRR", "Unknown service action");
            break;
        }
    }

}