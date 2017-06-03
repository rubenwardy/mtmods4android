package com.rubenwardy.minetestmodmanager.models;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ModSpec {
    @NonNull public final String name;
    @NonNull public String author;
    @Nullable public String listname;

    public ModSpec(@NonNull String name, @NonNull String author, @Nullable String listname) {
        this.name = name;
        this.author = author;
        this.listname = listname;
    }
}
