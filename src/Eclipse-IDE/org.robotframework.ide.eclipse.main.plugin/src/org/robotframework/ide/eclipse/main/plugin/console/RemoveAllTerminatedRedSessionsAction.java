/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.robotframework.ide.eclipse.main.plugin.RedImages;


/**
 * @author Michal Anglart
 *
 */
class RemoveAllTerminatedRedSessionsAction extends Action {

    RemoveAllTerminatedRedSessionsAction() {
        super("Remove all terminated RED sessions", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(RedImages.getCloseAllImage());
        setDisabledImageDescriptor(RedImages.getDisabledCloseAllImage());
        setEnabled(false);
    }

    @Override
    public void run() {
        final IConsoleManager consolesManager = ConsolePlugin.getDefault().getConsoleManager();
        final List<IConsole> consolesToRemove = new ArrayList<>();
        for (final IConsole console : consolesManager.getConsoles()) {
            if (console instanceof RedSessionConsole && ((RedSessionConsole) console).isTerminated()) {
                consolesToRemove.add(console);
            }
        }
        consolesManager.removeConsoles(consolesToRemove.toArray(new IConsole[0]));
        setEnabled(false);
    }
}
