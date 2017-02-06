package com.rubenwardy.minetestmodmanager.manager;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface ModEventReceiver {
    String ACTION_INSTALL = "install";
    String ACTION_UNINSTALL = "uninstall";
    String ACTION_FETCH_MODLIST = "fetch_modlist";
    String ACTION_FETCH_SCREENSHOT = "fetch_screenshot";
    String ACTION_SEARCH = "search";
    String PARAM_ERROR = "error";
    String PARAM_ACTION = "action";
    String PARAM_DEST_LIST = "destlist";
    String PARAM_DEST = "dest";
    String PARAM_MODNAME = "modname";
    String PARAM_ADDITIONAL = "additional";
    void onModEvent(@NonNull Bundle bundle);
}
