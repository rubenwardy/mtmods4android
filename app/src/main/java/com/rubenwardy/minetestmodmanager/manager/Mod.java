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

    @NonNull public String author;
    @Nullable public String link;
    @Nullable public String path;
    @Nullable public String screenshot_uri;
    public int verified;

    public Mod(@NonNull ModType type, @Nullable String listname, @NonNull String name,
               @Nullable String title, @NonNull String desc) {
        this.type = type;
        this.listname = listname;
        this.name = name;
        this.title = title;
        this.desc = desc;
        this.author = "";
        this.link = "";
        this.path = "";
        this.screenshot_uri = "";
        this.verified = 0;
    }

    public boolean isLocalMod() {
        return path != null && !path.equals("");
    }

    public String getShortDesc() {
        String cleaned_desc = desc.trim().replace("\n", " ");
        int len = cleaned_desc.indexOf(".", 20) + 1;
        int slen = cleaned_desc.length();
        len = Math.min(len, slen);
        if (len < 20) {
            len = slen;
        }

        if (len > 100) {
            String short_desc = cleaned_desc.substring(0, 99);
            char c = short_desc.charAt(short_desc.length() - 1);
            while (!(Character.isDigit(c) || Character.isLetter(c))) {
                short_desc = short_desc.substring(0, short_desc.length() - 1);
                c = short_desc.charAt(short_desc.length() - 1);
            }
            return short_desc + "…";
        } else {
            return cleaned_desc.substring(0, len);
        }
    }

    public String getShortLink() {
        String res = (link == null) ? "" : link.replace("https://", "").replace("http://", "");
        if (res.length() > 100) {
            res = res.substring(0, 99) + "…";
        }
        return res;
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
