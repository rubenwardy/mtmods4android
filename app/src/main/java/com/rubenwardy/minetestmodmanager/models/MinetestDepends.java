package com.rubenwardy.minetestmodmanager.models;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MinetestDepends {
    public final List<String> hard = new ArrayList<>();
    public final List<String> soft = new ArrayList<>();

    public boolean read(@NonNull File file) {
        if (!file.isFile()) {
            Log.e("Depends", "depfle is not a file! " + file.getAbsolutePath());
            return false;
        }

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    if (line.endsWith("?")) {
                        Log.e("Depends", "Soft: " + line.substring(0, line.length() - 1));
                        soft.add(line.substring(0, line.length() - 1));
                    } else {
                        Log.e("Depends", "Hard: " + line);
                        hard.add(line);
                    }
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
            for (String dep : hard) {
                writer.write(dep + "\n");
            }
            for (String dep : soft) {
                writer.write(dep + "?\n");
            }
            writer.close();

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
