/*
* Copyright 2017 Nokia Solutions and Networks
* Licensed under the Apache License, Version 2.0,
* see license.txt file for details.
*/
package org.robotframework.red.jface.dialogs;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

public class DetailedErrorDialog {

    public static void openErrorDialog(final String reason, final String detailedMessage) {
        final String pluginId = RedPlugin.PLUGIN_ID;
        Status status = new Status(IStatus.ERROR, pluginId, detailedMessage);
        MultiStatus ms = new MultiStatus(pluginId, IStatus.ERROR, new Status[] { status }, reason, null);
        StatusManager.getManager().handle(ms, StatusManager.SHOW);
    }
}
