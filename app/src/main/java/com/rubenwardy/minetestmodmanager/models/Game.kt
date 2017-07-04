package com.rubenwardy.minetestmodmanager.models

import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashMap

import io.realm.RealmObject

open class Game(val name: String, private val file: File) {

    private val lists = HashMap<String, ModList>()

    val path: String
        get() = file.absolutePath

    val doesModDirExist: Boolean
        get() = file.isDirectory && File(file, "mods").isDirectory

    val gamesDirExists: Boolean
        get() = File(file, "games").isDirectory

    val hasWorld: Boolean
        get() = File(file, "worlds/world").isDirectory

    fun getListFromPath(path: String): ModList? {
        return lists[File(path).absolutePath]
    }

    fun getModFromNameAuthor(name: String, author: String): Mod? {
        for (list in lists.values) {
            val mod = list.get(name, author)
            if (mod != null) {
                return mod
            }
        }

        return null
    }

    fun forceCreate() {
        File(file, "mods").mkdirs()
        File(file, "worlds").mkdirs()
    }

    val modPaths: List<ModDir>
        get() {
            val paths = ArrayList<ModDir>()
            paths.add(ModDir(File(file, "mods").absolutePath, ModList.ModListType.EMLT_MODS))
            paths.add(ModDir(File(file, "games/minetest_game/mods").absolutePath, ModList.ModListType.EMLT_GAME_MODS))
            return paths
        }

    val allModLists: List<ModList>
        get() {
            val ret = ArrayList<ModList>()
            ret.addAll(lists.values)
            return ret
        }

    val allMods: Map<String, Mod>
        get() {
            val map = HashMap<String, Mod>()

            for (list in lists.values) {
                for (mod in list.mods) {
                    map.put(mod.name, mod)
                }
            }

            return map
        }

    fun addList(path: String, list: ModList) {
        lists.put(path, list)
    }

    fun hasPath(path: String): Boolean {
        var testPath = path
        var root: String
        try {
            root = file.canonicalPath
        } catch (e: IOException) {
            root = file.absolutePath
        }

        val file2 = File(testPath)
        try {
            testPath = file2.canonicalPath
        } catch (e: IOException) {
            testPath = file2.absolutePath
        }

        return testPath.startsWith(root)
    }

    inner class ModDir internal constructor(var path: String, var type: ModList.ModListType)

    companion object {
        fun isMTGMod(mod_name: String): Boolean {
            val mods = arrayOf("beds", "boats", "bones", "bucket", "carts", "creative", "default", "doors", "dye", "farming", "fire", "flowers", "give_initial_stuff", "killme", "screwdriver", "sethome", "sfinv", "stairs", "tnt", "vessels", "walls", "wool", "xpanes")

            for (mod in mods) {
                if (mod == mod_name) {
                    return true
                }
            }

            return false
        }
    }
}
