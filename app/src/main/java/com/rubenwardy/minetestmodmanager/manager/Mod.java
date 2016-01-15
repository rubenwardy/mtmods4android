package com.rubenwardy.minetestmodmanager.manager;

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
    public final String name;
    public final String title;
    public final String desc;

    public Mod(ModType type, String name, String title, String desc) {
        this.type = type;
        this.name = name;
        this.title = title;
        this.desc = desc;
    }

    @Override
    public String toString() {
        return title;
    }
}
