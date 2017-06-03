package com.rubenwardy.minetestmodmanager.presenters

import android.content.Context
import android.util.Log
import com.rubenwardy.minetestmodmanager.manager.ModManager
import com.rubenwardy.minetestmodmanager.models.ModSpec

class DisclaimerPresenter(val view: View) {

    fun onAcceptClicked(context: Context) {
        val modspec = view.getModInfo()
        if (modspec.listname != null && !modspec.name.isEmpty()) {
            val modman = ModManager.getInstance()
            val list = modman.getModList(modspec.listname)
            if (list != null) {
                val mod = list.get(modspec.name, modspec.author)
                if (mod != null && !mod.link.isEmpty()) {
                    modman.installUrlModAsync(context, mod,
                            mod.link,
                            modman.installDir)
                } else {
                    Log.e("DAct", "Unable to find an installable mod of that name! " + modspec.name)
                }
            } else {
                Log.e("DAct", "Unable to find a ModList of that id! " + modspec.listname)
            }
        }
    }

    interface View {
        fun setAgreedToDisclaimer()
        fun finishActivity()
        fun getModInfo() : ModSpec
    }
}