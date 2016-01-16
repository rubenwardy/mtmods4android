package com.rubenwardy.minetestmodmanager.manager;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Mod manager service.
 */
public class ModInstallService extends IntentService {
    private static final String ACTION_INSTALL = "com.rubenwardy.minetestmodmanager.action.INSTALL";

    private static final String EXTRA_MOD_NAME = "com.rubenwardy.minetestmodmanager.extra.MOD_NAME";
    private static final String EXTRA_URL = "com.rubenwardy.minetestmodmanager.extra.URL";
    private static final String EXTRA_DEST = "com.rubenwardy.minetestmodmanager.extra.DEST";
    public static final String RET_NAME = "name";
    public static final String RET_DEST = "dest";

    public ModInstallService() {
        super("ModInstallService");
    }

    public static void startActionInstall(Context context, ServiceResultReceiver srr,
            String modname, File zip, String dest) {
        Intent intent = new Intent(context, ModInstallService.class);
        intent.setAction(ACTION_INSTALL);
        intent.putExtra(EXTRA_MOD_NAME, modname);
        intent.putExtra(EXTRA_URL, zip.getAbsolutePath());
        intent.putExtra(EXTRA_DEST, dest);
        intent.putExtra("receiverTag", srr);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            ResultReceiver rec = intent.getParcelableExtra("receiverTag");
            if (ACTION_INSTALL.equals(action)) {
                final String modname = intent.getStringExtra(EXTRA_MOD_NAME);
                final String zippath = intent.getStringExtra(EXTRA_URL);
                final String dest = intent.getStringExtra(EXTRA_DEST);
                handleActionInstall(rec, modname, new File(zippath), new File(dest));
            } else {
                Log.w("ModService", "Invalid action request.");
            }
        }
    }

    /**
     * Handle action Install in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInstall(ResultReceiver rec, String modname, File zipfile, File dest) {
        Log.w("ModService", "Installing mod...");

        File dir = null;
        int i = 0;
        do {
            Log.w("ModService", "Checking tmp" + Integer.toString(i));
            dir = new File(getCacheDir(), "tmp" + Integer.toString(i));
            i++;
        } while (dir.exists());

        try {
            Utils.UnzipFile(zipfile, dir, null);

            Log.w("ModService", "Finding root dir:");
            File root = Utils.findRootDir(dir);
            if (root == null) {
                Log.w("ModService", "Unable to find root dir.");
            } else {
                Log.w("ModService", "Copying to ");
                Utils.copyFolder(root, new File(dest, modname));

                Bundle b = new Bundle();
                b.putString(RET_NAME, modname);
                b.putString(RET_DEST, dest.getAbsolutePath());
                rec.send(0, b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.w("ModService", "Finished installing mod.");
    }
}
