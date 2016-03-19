package com.rubenwardy.minetestmodmanager.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads and writes Minetest style configurations
 **/
public class MinetestConf {
    private final Map<String, String> settings = new HashMap<>();

    public boolean read(File file) {
        if (!file.isFile()) {
            Log.e("Conf", "Configfile is not a file! " + file.getAbsolutePath());
            return false;
        }
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                int idx = line.indexOf("=");
                if (idx >= 0) {
                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1, line.length()).trim();
                    settings.put(key, value);
                }

            }
            bufferedReader.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save(File file) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for (Map.Entry<String, String> pair : settings.entrySet()) {
                writer.write(pair.getKey() + " = " + pair.getValue() + "\n");
            }
            writer.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isYes(String v) {
        v = v.trim();
        return (v.equals("true") || v.equals("1") || v.equals("yes"));
    }

    public String get(@NonNull String key) {
        return settings.get(key);
    }

    public boolean getBool(@NonNull String key) {
        String value = get(key);
        return (value != null) && isYes(value);
    }

    public void set(@NonNull String key, @Nullable String value) {
        if (value == null) {
            value = "";
        }
        settings.put(key, value);
    }

    public void setBool(@NonNull String key, boolean value) {
        set(key, value?"true":"false");
    }
}
