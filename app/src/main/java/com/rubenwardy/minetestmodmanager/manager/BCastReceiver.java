package com.rubenwardy.minetestmodmanager.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BCastReceiver extends BroadcastReceiver {
    public static final String ACTION_STOP_SERVICE = "stop_service";

    public BCastReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
        case ACTION_STOP_SERVICE:
            ModManager modman = new ModManager();
            modman.cancelAsyncTask();
            break;
        default:
            throw new UnsupportedOperationException("Not yet implemented");
        }
    }
}
