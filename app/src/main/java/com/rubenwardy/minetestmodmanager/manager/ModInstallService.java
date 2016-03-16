package com.rubenwardy.minetestmodmanager.manager;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.rubenwardy.minetestmodmanager.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Mod manager service.
 */
public class ModInstallService extends IntentService {
    public static final int UPDATE_PROGRESS = 8344;
    private static final String ACTION_URL_INSTALL = "com.rubenwardy.minetestmodmanager.action.URL_INSTALL";
    public static final String ACTION_INSTALL = "com.rubenwardy.minetestmodmanager.action.INSTALL";
    public static final String ACTION_FETCH_MODLIST = "com.rubenwardy.minetestmodmanager.action.FETCH_MODLIST";

    private static final String EXTRA_MOD_NAME = "com.rubenwardy.minetestmodmanager.extra.MOD_NAME";
    private static final String EXTRA_URL = "com.rubenwardy.minetestmodmanager.extra.URL";
    private static final String EXTRA_DEST = "com.rubenwardy.minetestmodmanager.extra.DEST";
    public static final String RET_ACTION = "action";
    public static final String RET_NAME = "name";
    public static final String RET_DEST = "dest";
    public static final String RET_ERROR = "error";
    public static final String RET_PROGRESS = "progress";

    public ModInstallService() {
        super("ModInstallService");
    }

    @MainThread
    public static void startActionInstall(@NonNull Context context, ServiceResultReceiver srr,
            String modname, @NonNull File zip, String dest) {
        Intent intent = new Intent(context, ModInstallService.class);
        intent.setAction(ACTION_INSTALL);
        intent.putExtra(EXTRA_MOD_NAME, modname);
        intent.putExtra(EXTRA_URL, zip.getAbsolutePath());
        intent.putExtra(EXTRA_DEST, dest);
        intent.putExtra("receiverTag", srr);
        context.startService(intent);
    }

    @MainThread
    public static void startActionUrlInstall(@NonNull Context context, ServiceResultReceiver srr,
                                          String modname, String url, String dest) {
        Intent intent = new Intent(context, ModInstallService.class);
        intent.setAction(ACTION_URL_INSTALL);
        intent.putExtra(EXTRA_MOD_NAME, modname);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_DEST, dest);
        intent.putExtra("receiverTag", srr);
        context.startService(intent);
    }

    @MainThread
    public static void startActionFetchModList(@NonNull Context context, ServiceResultReceiver srr,
                                               String url) {
        Intent intent = new Intent(context, ModInstallService.class);
        intent.setAction(ACTION_FETCH_MODLIST);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra("receiverTag", srr);
        context.startService(intent);
    }

    @Override
    @WorkerThread
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            ResultReceiver rec = intent.getParcelableExtra("receiverTag");

            if (ACTION_INSTALL.equals(action)) {
                final String modname = intent.getStringExtra(EXTRA_MOD_NAME);
                final String zippath = intent.getStringExtra(EXTRA_URL);
                final String dest = intent.getStringExtra(EXTRA_DEST);
                handleActionInstall(rec, modname, new File(zippath), new File(dest));
            } else if (ACTION_FETCH_MODLIST.equals(action)) {
                final String url = intent.getStringExtra(EXTRA_URL);
                handleActionFetchModList(rec, url);
            } else if (ACTION_URL_INSTALL.equals(action)) {
                final String modname = intent.getStringExtra(EXTRA_MOD_NAME);
                final String url = intent.getStringExtra(EXTRA_URL);
                final String dest = intent.getStringExtra(EXTRA_DEST);
                handleActionUrlInstall(rec, modname, url, new File(dest));
            } else {
                Log.w("ModService", "Invalid action request.");
            }
        }
    }

    @WorkerThread
    private void handleActionUrlInstall(@NonNull ResultReceiver rec, @NonNull String modname, @NonNull String url_str, @NonNull File dest) {
        Log.w("ModService", "Downloading " + url_str);
        try {
            // Resource
            String str_installing = "ERR! Installing $1...";
            String str_connecting = "ERR! Connecting...";
            String str_downloading = "ERR! Downloading...";
            String str_extracting = "ERR! Extracting...";
            Resources res = getApplicationContext().getResources();
            if (res != null) {
                str_installing = String.format(res.getString(R.string.installing), modname);
                str_connecting = res.getString(R.string.connecting);
                str_downloading = res.getString(R.string.downloading);
                str_extracting = res.getString(R.string.extracting);

            }

            // Notifcation
            NotificationManager notiman =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(str_installing)
                    .setContentText(str_connecting)
                    .setSmallIcon(R.drawable.notification_icon);
            mBuilder.setProgress(0, 0, true);
            notiman.notify(1337, mBuilder.build());


            URL url = new URL(url_str);
            {
                // Open HEAD http connection
                HttpURLConnection testcon = (HttpURLConnection)url.openConnection();
                testcon.setRequestMethod("HEAD");
                testcon.connect();

                // Check for existence
                int code = testcon.getResponseCode();
                if (code != HttpURLConnection.HTTP_OK){
                    Bundle b = new Bundle();
                    b.putString(RET_NAME, modname);
                    b.putString(RET_ACTION, ACTION_INSTALL);
                    b.putString(RET_DEST, dest.getAbsolutePath());
                    if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        b.putString(RET_ERROR, "Failed to download: 404 File not found.");
                    } else {
                        b.putString(RET_ERROR, "Failed to download: " + code + " Error");
                    }
                    rec.send(0, b);
                    return;
                }

                // Check type
                String contentType = testcon.getContentType();
                if (!contentType.equals("application/octet-stream") &&
                        !contentType.equals("application/zip") &&
                        !contentType.startsWith("application/x-zip")) {
                    Bundle b = new Bundle();
                    b.putString(RET_NAME, modname);
                    b.putString(RET_ACTION, ACTION_INSTALL);
                    b.putString(RET_DEST, dest.getAbsolutePath());
                    b.putString(RET_ERROR, "Failed to download: File is not a zip file");
                    rec.send(0, b);
                    return;
                }

                // TODO: Check size
                /*testcon.getInputStream();
                int size = testcon.getContentLength();
                testcon.disconnect();*/
            }
            {
                mBuilder.setContentText(str_downloading);
                notiman.notify(1337, mBuilder.build());

                // Start download
                URLConnection connection = url.openConnection();
                connection.connect();
                // this will be useful so that you can show a typical 0-100% progress bar
                int fileLength = connection.getContentLength();

                File file;
                int i = 0;
                do {
                    Log.w("ModService", "Checking file tmp" + Integer.toString(i) + ".zip");
                    file = new File(getCacheDir(), "tmp" + Integer.toString(i) + ".zip");
                    i++;
                } while (file.exists());

                // download the file
                InputStream input = new BufferedInputStream(connection.getInputStream());
                OutputStream output = new FileOutputStream(file);

                byte data[] = new byte[1024];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);

                    int prog = (int) (total * 100 / fileLength);
                    total += count;

                    // Report to ServiceResultReceiver
                    Bundle b = new Bundle();
                    b.putString(RET_NAME, modname);
                    b.putString(RET_ACTION, ACTION_INSTALL);
                    b.putString(RET_DEST, dest.getAbsolutePath());
                    b.putInt(RET_PROGRESS, prog);
                    rec.send(UPDATE_PROGRESS, b);

                    // Update Notification
                    mBuilder.setProgress(100, prog, false);
                    notiman.notify(1337, mBuilder.build());
                }

                mBuilder.setProgress(0, 0, true);
                mBuilder.setContentText(str_extracting);
                notiman.notify(1337, mBuilder.build());

                output.flush();
                output.close();
                input.close();

                handleActionInstall(rec, modname, file.getAbsoluteFile(), dest);
                try {
                    if (!file.delete())
                        Log.w("ModService", "Failed to delete tmp zip file");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            notiman.cancel(1337);
        } catch (IOException e) {
            Bundle b = new Bundle();
            b.putString(RET_NAME, modname);
            b.putString(RET_ACTION, ACTION_INSTALL);
            b.putString(RET_DEST, dest.getAbsolutePath());
            b.putString(RET_ERROR, "Failed to download: " + e.toString());
            rec.send(0, b);
            e.printStackTrace();
        }
    }


    /**
     * Handle action Install in the provided background thread with the provided
     * parameters.
     */
    @WorkerThread
    private void handleActionInstall(@NonNull ResultReceiver rec, @NonNull String modname, @NonNull File zipfile, @NonNull File dest) {
        Log.w("ModService", "Installing mod...");

        File dir;
        int i = 0;
        do {
            Log.w("ModService", "Checking tmp" + Integer.toString(i));
            dir = new File(getCacheDir(), "tmp" + Integer.toString(i));
            i++;
        } while (dir.exists());

        try {
            // UNZIP
            if (!Utils.UnzipFile(zipfile, dir, null)) {
                Log.w("ModService", "Unable to extract zip.");
                Bundle b = new Bundle();
                b.putString(RET_ACTION, ACTION_INSTALL);
                b.putString(RET_NAME, modname);
                b.putString(RET_DEST, dest.getAbsolutePath());
                b.putString(RET_ERROR, "Unable to extract zip. The file may be corrupted.");
                rec.send(0, b);
                return;
            }

            // Find mod_root
            Log.w("ModService", "Finding mod_root dir:");
            File mod_root = Utils.findRootDir(dir);
            if (mod_root == null) {
                Log.w("ModService", "Unable to find mod_root dir.");
                Bundle b = new Bundle();
                b.putString(RET_ACTION, ACTION_INSTALL);
                b.putString(RET_NAME, modname);
                b.putString(RET_DEST, dest.getAbsolutePath());
                b.putString(RET_ERROR, "Unable to find the mod in the download.");
                rec.send(0, b);
                return;
            }

            // Copy
            Log.w("ModService", "Copying to " + dest.getAbsolutePath());
            Utils.copyFolder(mod_root, new File(dest, modname));

            Bundle b = new Bundle();
            b.putString(RET_NAME, modname);
            b.putString(RET_ACTION, ACTION_INSTALL);
            b.putString(RET_DEST, dest.getAbsolutePath());
            rec.send(0, b);
        } catch (IOException e) {
            Bundle b = new Bundle();
            b.putString(RET_ACTION, ACTION_INSTALL);
            b.putString(RET_NAME, modname);
            b.putString(RET_DEST, dest.getAbsolutePath());
            b.putString(RET_ERROR, e.toString());
            rec.send(0, b);
            e.printStackTrace();
        }
        Log.w("ModService", "Finished installing mod.");

        if (dir.exists()) {
            Utils.deleteRecursive(dir);
        }
    }

    @WorkerThread
    private void handleActionFetchModList(@NonNull ResultReceiver rec, @NonNull String url_str) {
        Log.w("ModService", "Downloading modlist...");
        try {
            URL url = new URL(url_str);
            URLConnection connection = url.openConnection();
            connection.connect();
            // this will be useful so that you can show a typical 0-100% progress bar
            int fileLength = connection.getContentLength();

            File file;
            int i = 0;
            do {
                Log.w("ModService", "Checking file list" + Integer.toString(i) + ".json");
                file = new File(getCacheDir(), "list" + Integer.toString(i) + ".json");
                i++;
            } while (file.exists());

            // download the file
            InputStream input = new BufferedInputStream(connection.getInputStream());
            OutputStream output = new FileOutputStream(file);

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                Bundle b = new Bundle();
                b.putString(RET_ACTION, ACTION_FETCH_MODLIST);
                b.putString(RET_NAME, url_str);
                b.putString(RET_DEST, file.getAbsolutePath());
                b.putInt(RET_PROGRESS, (int) (total * 100 / fileLength));
                rec.send(UPDATE_PROGRESS, b);
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();

            Bundle b = new Bundle();
            b.putString(RET_ACTION, ACTION_FETCH_MODLIST);
            b.putString(RET_NAME, url_str);
            b.putString(RET_DEST, file.getAbsolutePath());
            rec.send(0, b);
        } catch (IOException e) {
            Bundle b = new Bundle();
            b.putString(RET_ACTION, ACTION_FETCH_MODLIST);
            b.putString(RET_NAME, url_str);
            b.putString(RET_ERROR, e.toString());
            rec.send(0, b);
            e.printStackTrace();
        }
    }
}
