package com.rubenwardy.minetestmodmanager.presenters

import android.os.Environment
import android.util.Log
import com.rubenwardy.minetestmodmanager.manager.ModManager
import com.rubenwardy.minetestmodmanager.models.Events
import com.rubenwardy.minetestmodmanager.models.Game
import org.greenrobot.eventbus.Subscribe
import java.io.File

class ModListPresenter(val view: View) {
    fun scanFileSystem() {
        val extern = Environment.getExternalStorageDirectory()
        if (!extern.exists()) {
            view.showNoExternalFSDialog()
            return
        }

        val minetest   = Game("Minetest", File(extern, "Minetest"))
        val multicraft = Game("Multicraft", File(extern, "MultiCraft"))

        if (!minetest.doesModDirExist && !multicraft.doesModDirExist) {
            view.getIsMinetestInstalled()

            view.showMinetestNotInstalledDialog()

            minetest.forceCreate()
        }

        if (!minetest.doesModDirExist && !multicraft.doesModDirExist) {
            view.showNoGameAvailable()
            return
        }

        val modman = ModManager.getInstance()
        modman.registerGame(minetest)
        modman.fetchModListAsync()
    }

    fun forceModListRefresh() {
        val modman = ModManager.getInstance()
        for (list in ModManager.getInstance().allModLists) {
            if (list.type.isLocal) {
                modman.updateLocalModList(list)
            }
        }
        modman.fetchModListAsync()
    }

    @Subscribe
    fun onModInstall(e: Events.ModInstallEvent) {
        if (e.didError()) {
            view.showModInstallErrorDialog(e.modname, e.error);
            return
        } else {
            val modman = ModManager.getInstance()
            val uninstalled = modman.getMissingDependsForMod(modman.getModList(e.list)!!.get(e.modname, null)!!)
            for (a in uninstalled) {
                Log.e("MDAct", "Mod not installed: " + a)
            }

            if (!uninstalled.isEmpty()) {
                view.showInstallsDependsDialog(uninstalled)
            }

            view.showModOnlyIfTwoPane(e.list, e.modname);
            view.showInstallMessage(e.modname)
        }

        view.updateModListAndRecyclerView(e.list)
    }


    interface View {
        fun showNoExternalFSDialog()
        fun showMinetestNotInstalledDialog()
        fun showNoGameAvailable()
        fun getIsMinetestInstalled(): Boolean
        fun showModInstallErrorDialog(modname: String, error: String)
        fun showInstallsDependsDialog(uninstalled: List<String>)
        fun updateModListAndRecyclerView(list: String)
        fun showInstallMessage(modname: String)
        fun  showModOnlyIfTwoPane(list: String, modname: String)
    }
}