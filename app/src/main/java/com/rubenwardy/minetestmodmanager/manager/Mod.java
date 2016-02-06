package com.rubenwardy.minetestmodmanager.manager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Represents a mod, installed or not.
 */
public class Mod {
    public enum ModType {
        EMT_INVALID,
        EMT_MOD,
        EMT_MODPACK
    }

    @NonNull  public final ModType type;
    @Nullable public final String listname;

    @NonNull  public final String name;
    @Nullable public final String title;
    @NonNull public final String desc;
    @Nullable public String link;
    @Nullable public String path;
    @Nullable public String screenshot_uri;

    public Mod(@NonNull ModType type, @Nullable String listname, @NonNull String name,
               @Nullable String title, @NonNull String desc) {
        this.type = type;
        this.listname = listname;
        this.name = name;
        this.title = title;
        this.desc = desc;
        this.link = "";
        this.path = "";
        this.screenshot_uri = "";
    }

    public boolean isLocalMod() {
        return path != null && !path.equals("");
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
