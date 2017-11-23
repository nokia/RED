/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.robotframework.ide.eclipse.main.plugin.RedImages;


/**
 * @author Michal Anglart
 *
 */
class RemoveTerminatedRedSessionAction extends Action {

    private RedSessionConsole console;

    RemoveTerminatedRedSessionAction(final RedSessionConsole console) {
        super("Remove session", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(RedImages.getCloseImage());
        setDisabledImageDescriptor(RedImages.getDisabledCloseImage());
        setEnabled(false);
        this.console = console;
    }

    @Override
    public void run() {
        if (console.isTerminated()) {
            ConsolePlugin.getDefault().getConsoleManager().removeConsoles(new IConsole[] { console });
        }
    }

    void dispose() {
        this.console = null;
    }
}
