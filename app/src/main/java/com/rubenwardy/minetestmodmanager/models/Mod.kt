package com.rubenwardy.minetestmodmanager.models

import com.rubenwardy.minetestmodmanager.manager.ModManager

/**
 * Represents a mod, installed or not.
 */
class Mod(val type: Mod.ModType, listname: String?, name: String,
          val title: String?, val desc: String) : ModSpec(name, "", listname) {

    enum class ModType {
        EMT_INVALID,
        EMT_MOD,
        EMT_MODPACK,
        EMT_SUBGAME
    }

    var link: String = ""
    var path: String? = ""
    var screenshot_uri: String? = ""
    var forum_url: String? = null
    var verified: Int = 0
    var size: Int = -1

    val isLocalMod: Boolean
        get() = path != null && path != ""

    val shortDesc: String
        get() {
            val cleaned_desc = desc.trim { it <= ' ' }.replace("\n", " ")
            var len = cleaned_desc.indexOf(".", 20) + 1
            val slen = cleaned_desc.length
            len = Math.min(len, slen)
            if (len < 20) {
                len = slen
            }

            if (len > 100) {
                var short_desc = cleaned_desc.substring(0, 99)
                var c = short_desc[short_desc.length - 1]
                while (!(Character.isDigit(c) || Character.isLetter(c))) {
                    short_desc = short_desc.substring(0, short_desc.length - 1)
                    c = short_desc[short_desc.length - 1]
                }
                return short_desc + "…"
            } else {
                return cleaned_desc.substring(0, len)
            }
        }

    val shortLink: String
        get() {
            var res = link.replace("https://", "").replace("http://", "")
            if (res.length > 100) {
                res = res.substring(0, 99) + "…"
            }
            return res
        }

    val shortForumLink: String
        get() {
            var res = (forum_url ?: "").replace("https://", "").replace("http://", "")
            if (res.length > 100) {
                res = res.substring(0, 99) + "…"
            }
            return res
        }

    val downloadSize: String?
        get() {
            if (size > 1000000) {
                val size2 = Math.round(size / 100000.0) / 10.0
                return size2.toString() + " MB"
            } else if (size > 500) {
                val size2 = Math.round(size / 100.0) / 10.0
                return size2.toString() + " KB"
            } else if (size > 0) {
                return size.toString() + " B"
            } else {
                return null
            }
        }

    override fun toString(): String {
        return this.name
    }

    fun isEnabledInConfig(conf: MinetestConf): Boolean {
        if (type == ModType.EMT_MOD) {
            return conf.getBool("load_mod_" + name)
        } else if (type == ModType.EMT_MODPACK) {
            assert(path != null)
            val sublist = ModList(ModList.ModListType.EMLT_MODS, "", null, path!!)
            val modman = ModManager.getInstance()
            modman.updatePathModList(sublist)
            for (submod in sublist.mods) {
                if (!submod.isEnabledInConfig(conf)) {
                    return false
                }
            }
            return true
        } else {
            return false
        }
    }

    fun setEnabled(conf: MinetestConf, enable: Boolean) {
        if (type == ModType.EMT_MOD) {
            conf.setBool("load_mod_" + name, enable)
        } else if (type == ModType.EMT_MODPACK) {
            assert(path != null)
            val sublist = ModList(ModList.ModListType.EMLT_MODS, "", "", path!!)
            val modman = ModManager.getInstance()
            modman.updatePathModList(sublist)
            for (submod in sublist.mods) {
                submod.setEnabled(conf, enable)
            }
        }
    }
}
