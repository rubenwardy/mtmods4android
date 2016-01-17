package com.rubenwardy.minetestmodmanager.manager;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

/**
 * Receive a result from the service
 */
public class ServiceResultReceiver extends ResultReceiver {
    public ServiceResultReceiver(Handler handler) {
        super(handler);
    }

    @Override
    protected void onReceiveResult(int resultCode, Bundle b) {
        String modname = b.getString(ModInstallService.RET_NAME);
        String dest = b.getString(ModInstallService.RET_DEST);

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

            if (modman.mev != null) {
                Bundle b2 = new Bundle();
                b2.putString(ModEventReceiver.PARAM_ACTION, ModEventReceiver.ACTION_INSTALL);
                b2.putString(ModEventReceiver.PARAM_MODNAME, modname);
                b2.putString(ModEventReceiver.PARAM_DEST, dest);
                modman.mev.onModEvent(b2);
            }
        } else {
            Integer progress = b.getInt(ModInstallService.RET_PROGRESS);
            Log.w("SRR", "Progress for " + modname + " at " + Integer.toString(progress) + "%");
        }
    }

}