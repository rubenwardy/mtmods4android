package com.rubenwardy.minetestmodmanager.manager;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface ModEventReceiver {
    public static final String ACTION_INSTALL = "install";
    public static final String ACTION_UNINSTALL = "uninstall";
    public static final String ACTION_ENABLE = "enable";
    public static final String ACTION_DISABLE = "disable";
    public static final String PARAM_ERROR = "error";
    public static final String PARAM_ACTION = "action";
    public static final String PARAM_DEST = "dest";
    public static final String PARAM_MODNAME = "modname";
    public void onModEvent(@NonNull Bundle bundle);
}
