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
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        String dest = resultData.getString(ModInstallService.RET_DEST);
        Log.w("SRR", "Got result " + dest);
        ModManager modman = new ModManager();
        ModList list = modman.get(dest);
        if (list != null) {
            Log.w("SRR", "Invalidating list");
            list.valid = false;
        }
    }

}