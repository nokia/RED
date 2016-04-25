/*
 * Copyright 2016 Nokia Solutions and Networks
 * Licensed under the Apache License, Version 2.0,
 * see license.txt file for details.
 */
package org.robotframework.ide.eclipse.main.plugin.console;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.rf.ide.core.jvmutils.process.OSProcessHelper;
import org.rf.ide.core.jvmutils.process.OSProcessHelper.ProcessHelperException;
import org.robotframework.ide.eclipse.main.plugin.RedImages;
import org.robotframework.ide.eclipse.main.plugin.RedPlugin;

/**
 * @author Michal Anglart
 */
class TerminateRedSessionAction extends Action {

    private RedSessionConsole console;

    TerminateRedSessionAction(final RedSessionConsole console) {
        super("Terminate", IAction.AS_PUSH_BUTTON);
        setImageDescriptor(RedImages.getStopImage());

        this.console = console;
    }

    @Override
    public void run() {
        final Process process = console.getProcess();
        if (process != null) {
            try {
                new OSProcessHelper().destroyProcessTree(process);
            } catch (final ProcessHelperException e1) {
                RedPlugin.logError("Problem during RED server shutdown.", e1);
            }
            if (process != null) {
                process.destroyForcibly();
            }
            try {
                if (process != null) {
                    process.waitFor();
                }
            } catch (final InterruptedException e) {
                // nothing to do
            }
        }
    }

    void dispose() {
        this.console = null;
    }
}
