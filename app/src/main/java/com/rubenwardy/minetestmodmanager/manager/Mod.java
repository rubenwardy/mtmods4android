package com.rubenwardy.minetestmodmanager.manager;

import android.support.annotation.NonNull;

/**
 * Represents a mod, installed or not.
 */
public class Mod {
    public enum ModType {
        EMT_INVALID,
        EMT_MOD,
        EMT_MODPACK
    }

    public final ModType type;
    public final String listname;
    public final String name;
    public final String title;
    public final String desc;
    public String screenshot_uri;
    public String path;

    public Mod(@NonNull ModType type, String listname, String name, String title, String desc) {
        this.type = type;
        this.listname = listname;
        this.name = name;
        this.title = title;
        this.desc = desc;
        this.path = "";
        this.screenshot_uri = "";
    }

    @Override
    public String toString() {
        return title;
    }
}
