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
        EMT_MODPACK,
        EMT_SUBGAME
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
    @Nullable public String forum_url;
    public int verified;
    public int size;

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
        this.size = -1;
    }

    public boolean isLocalMod() {
        return path != null && !path.equals("");
    }

    @NonNull
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

    @NonNull
    public String getShortLink() {
        String res = (link == null) ? "" : link.replace("https://", "").replace("http://", "");
        if (res.length() > 100) {
            res = res.substring(0, 99) + "…";
        }
        return res;
    }

    @NonNull
    public String getShortForumLink() {
        String res = (forum_url == null) ? "" : forum_url.replace("https://", "").replace("http://", "");
        if (res.length() > 100) {
            res = res.substring(0, 99) + "…";
        }
        return res;
    }

    @Nullable
    public String getDownloadSize() {
        if (size > 1000000) {
            double size2 = Math.round(size / 100000.0) / 10.0;
            return size2 + " MB";
        } else if (size > 500) {
            double size2 = Math.round(size / 100.0) / 10.0;
            return size2 + " KB";
        } else if (size > 0) {
            return size + " B";
        } else {
            return null;
        }
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }

    public boolean isEnabled(@NonNull MinetestConf conf) {
        if (type == ModType.EMT_MOD) {
            return conf.getBool("load_mod_" + name);
        } else if (type == ModType.EMT_MODPACK) {
            assert (path != null);
            ModList sublist = new ModList(ModList.ModListType.EMLT_PATH, "", "", path);
            ModManager modman = new ModManager();
            modman.updatePathModList(sublist);
            for (Mod submod : sublist.mods) {
                if (!submod.isEnabled(conf)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void setEnabled(@NonNull MinetestConf conf, boolean enable) {
        if (type == ModType.EMT_MOD) {
            conf.setBool("load_mod_" + name, enable);
        } else if (type == ModType.EMT_MODPACK) {
            assert (path != null);
            ModList sublist = new ModList(ModList.ModListType.EMLT_PATH, "", "", path);
            ModManager modman = new ModManager();
            modman.updatePathModList(sublist);
            for (Mod submod : sublist.mods) {
                submod.setEnabled(conf, enable);
            }
        }
    }
}
