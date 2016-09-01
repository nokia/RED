/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.rf.ide.core.executor;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Splitter;

/**
 * @author Michal Anglart
 */
public class RedSystemProperties {

    public static final String RED_USE_DIRECT_INTERPRETER = "red.useDirectInterpreter";

    public static final String RED_SHOW_SESSION_CONSOLE = "red.showSessionConsole";

    public static final String RED_CONNECT_TO_SERVER_AT = "red.connectToServerAt";

    public static final String RED_USE_OLD_REPARSED_LINK_MODE = "red.useOldReparsedLinkMode";

    public static boolean shouldConnectToRunningServer() {
        return System.getProperty("red.connectToServerAt") != null;
    }

    public static int getPortOfRunningServer() {
        return Integer.valueOf(System.getProperty("red.connectToServerAt"));
    }

    public static boolean shouldUseDirectExecutor() {
        return Boolean.valueOf(System.getProperty("red.useDirectInterpreter")).booleanValue();
    }

    public static boolean shouldShowSessionConsole() {
        return Boolean.valueOf(System.getProperty("red.showSessionConsole")).booleanValue();
    }

    public static boolean shouldUseOldReparsedLinkMode() {
        return Boolean.valueOf(System.getProperty(RED_USE_OLD_REPARSED_LINK_MODE)).booleanValue();
    }

    public static boolean isWindowsPlatform() {
        return System.getProperty("os.name").toLowerCase().indexOf("win") >= 0;
    }

    public static String getPathsSeparator() {
        return isWindowsPlatform() ? ";" : ":";
    }

    public static List<String> getPythonPaths() {
        final String paths = System.getenv("PYTHONPATH");
        if (paths == null || paths.isEmpty()) {
            return new ArrayList<>();
        }
        return Splitter.on(getPathsSeparator()).splitToList(paths);
    }
}
